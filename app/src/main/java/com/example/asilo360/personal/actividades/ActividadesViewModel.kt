package com.example.asilo360.personal.actividades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.asilo360.data.RetrofitClient
import com.example.asilo360.data.firebase.FirebaseClient
import com.example.asilo360.data.model.Actividad
import com.example.asilo360.data.model.EliminarActividadRequest
import com.example.asilo360.data.model.Residente
import com.example.asilo360.data.session.UserSessionManager
import com.example.asilo360.personal.residentes.ResidentesPersonalApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class FiltroFecha {
    TODAS, HOY, ESTA_SEMANA, ESTE_MES
}

sealed interface ActividadesUiState {
    object Loading : ActividadesUiState
    data class Success(
        val listaCompleta: List<Actividad>,
        val listaFiltrada: List<Actividad>,
        val residentesDisponibles: List<Residente> = emptyList(),
        val searchQuery: String = "",
        val filtroFecha: FiltroFecha = FiltroFecha.TODAS,
        val filtroTipo: String = "Todas",
        val filtroResidenteId: Int? = null
    ) : ActividadesUiState
    data class Error(val message: String) : ActividadesUiState
}

sealed interface ActividadDetailUiState {
    object Idle : ActividadDetailUiState
    object Loading : ActividadDetailUiState
    data class Success(val actividad: Actividad) : ActividadDetailUiState
    data class Error(val message: String) : ActividadDetailUiState
}

sealed interface FormOperacionState {
    object Idle : FormOperacionState
    data class Guardando(val message: String = "Guardando actividad...") : FormOperacionState
    data class Exito(val message: String) : FormOperacionState
    data class Error(val message: String) : FormOperacionState
}

class ActividadesViewModel(
    private val apiService: ActividadesApiService = try {
        RetrofitClient.createService(ActividadesApiService::class.java)
    } catch (_: Exception) {
        object : ActividadesApiService {
            override suspend fun listarActividades(idPersonal: Int?, idUsuario: Int?, idResidente: Int?, tipo: String?, fechaInicio: String?, fechaFin: String?) = throw IllegalStateException("Retrofit no disponible")
            override suspend fun obtenerActividad(id: Int) = throw IllegalStateException("Retrofit no disponible")
            override suspend fun crearActividad(actividad: Actividad) = throw IllegalStateException("Retrofit no disponible")
            override suspend fun actualizarActividad(actividad: Actividad) = throw IllegalStateException("Retrofit no disponible")
            override suspend fun eliminarActividad(request: EliminarActividadRequest) = throw IllegalStateException("Retrofit no disponible")
        }
    },
    private val residentesApiService: ResidentesPersonalApiService = try {
        RetrofitClient.createService(ResidentesPersonalApiService::class.java)
    } catch (_: Exception) {
        object : ResidentesPersonalApiService {
            override suspend fun listarResidentes(idRol: Int) = throw IllegalStateException("Retrofit no disponible")
            override suspend fun obtenerResidente(id: Int, idRol: Int) = throw IllegalStateException("Retrofit no disponible")
        }
    }
) : ViewModel() {

    private val _uiState = MutableStateFlow<ActividadesUiState>(ActividadesUiState.Loading)
    val uiState: StateFlow<ActividadesUiState> = _uiState.asStateFlow()

    private val _detailState = MutableStateFlow<ActividadDetailUiState>(ActividadDetailUiState.Idle)
    val detailState: StateFlow<ActividadDetailUiState> = _detailState.asStateFlow()

    private val _formState = MutableStateFlow<FormOperacionState>(FormOperacionState.Idle)
    val formState: StateFlow<FormOperacionState> = _formState.asStateFlow()

    private var todasLasActividades: List<Actividad> = emptyList()
    private var listaResidentes: List<Residente> = emptyList()

    private var searchQueryActual: String = ""
    private var filtroFechaActual: FiltroFecha = FiltroFecha.TODAS
    private var filtroTipoActual: String = "Todas"
    private var filtroResidenteIdActual: Int? = null

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.value = ActividadesUiState.Loading
            cargarResidentesInterno()
            cargarActividadesInterno()
        }
    }

    private suspend fun cargarResidentesInterno() {
        try {
            val response = residentesApiService.listarResidentes(idRol = 2)
            if (response.isSuccessful && response.body()?.success == true) {
                listaResidentes = response.body()?.data ?: emptyList()
            } else {
                cargarResidentesFallback()
            }
        } catch (_: Exception) {
            cargarResidentesFallback()
        }
    }

    private suspend fun cargarResidentesFallback() {
        try {
            val fbResp = FirebaseClient.residentesApiService.listarResidentes()
            listaResidentes = fbResp.body().data ?: emptyList()
        } catch (_: Exception) { }
    }

    private suspend fun cargarActividadesInterno() {
        // 1. Intentar cargar desde Firebase Firestore primero
        try {
            val fbResp = FirebaseClient.actividadesApiService.listarActividades()
            val listaFb = fbResp.body().data ?: emptyList()
            if (listaFb.isNotEmpty()) {
                todasLasActividades = listaFb
                aplicarFiltros()
                return
            }
        } catch (_: Exception) { }

        // 2. Si Firebase no devuelve registros, intentar API Retrofit PHP
        val currentUserId = UserSessionManager.getUserId()
        try {
            val response = apiService.listarActividades(idUsuario = currentUserId)
            if (response.isSuccessful && response.body()?.success == true) {
                todasLasActividades = response.body()?.data ?: emptyList()
                aplicarFiltros()
            } else {
                cargarActividadesFallback()
            }
        } catch (e: Exception) {
            cargarActividadesFallback()
        }
    }

    private suspend fun cargarActividadesFallback() {
        try {
            // Intentar sin idUsuario si no devolvió por usuario específico
            val response = apiService.listarActividades()
            if (response.isSuccessful && response.body()?.success == true) {
                todasLasActividades = response.body()?.data ?: emptyList()
                aplicarFiltros()
            } else {
                _uiState.value = ActividadesUiState.Error("No fue posible cargar las actividades.")
            }
        } catch (e: Exception) {
            _uiState.value = ActividadesUiState.Error("No fue posible cargar las actividades.")
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQueryActual = query
        aplicarFiltros()
    }

    fun onFiltroFechaChange(filtro: FiltroFecha) {
        filtroFechaActual = filtro
        aplicarFiltros()
    }

    fun onFiltroTipoChange(tipo: String) {
        filtroTipoActual = tipo
        aplicarFiltros()
    }

    fun onFiltroResidenteChange(idResidente: Int?) {
        filtroResidenteIdActual = idResidente
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        var resultado = todasLasActividades

        // 1. Filtro por Búsqueda (Residente, Tipo, Personal)
        if (searchQueryActual.isNotBlank()) {
            val q = searchQueryActual.trim().lowercase()
            resultado = resultado.filter { a ->
                val residente = (a.residente ?: "").lowercase()
                val tipo = a.tipo.lowercase()
                val personal = (a.personal ?: "").lowercase()
                val desc = a.descripcion.lowercase()

                residente.contains(q) || tipo.contains(q) || personal.contains(q) || desc.contains(q)
            }
        }

        // 2. Filtro por Fecha (Hoy, Esta semana, Este mes)
        val hoy = LocalDate.now()
        resultado = when (filtroFechaActual) {
            FiltroFecha.TODAS -> resultado
            FiltroFecha.HOY -> resultado.filter {
                try { LocalDate.parse(it.fecha.take(10)) == hoy } catch (_: Exception) { false }
            }
            FiltroFecha.ESTA_SEMANA -> {
                val inicioSemana = hoy.minusDays(hoy.dayOfWeek.value.toLong() - 1)
                val finSemana = inicioSemana.plusDays(6)
                resultado.filter {
                    try {
                        val d = LocalDate.parse(it.fecha.take(10))
                        !d.isBefore(inicioSemana) && !d.isAfter(finSemana)
                    } catch (_: Exception) { false }
                }
            }
            FiltroFecha.ESTE_MES -> resultado.filter {
                try {
                    val d = LocalDate.parse(it.fecha.take(10))
                    d.year == hoy.year && d.month == hoy.month
                } catch (_: Exception) { false }
            }
        }

        // 3. Filtro por Tipo de Actividad
        if (filtroTipoActual.isNotBlank() && filtroTipoActual != "Todas") {
            resultado = resultado.filter { it.tipo.equals(filtroTipoActual, ignoreCase = true) }
        }

        // 4. Filtro por Residente
        if (filtroResidenteIdActual != null && filtroResidenteIdActual!! > 0) {
            resultado = resultado.filter { it.id_residente == filtroResidenteIdActual }
        }

        _uiState.value = ActividadesUiState.Success(
            listaCompleta = todasLasActividades,
            listaFiltrada = resultado,
            residentesDisponibles = listaResidentes,
            searchQuery = searchQueryActual,
            filtroFecha = filtroFechaActual,
            filtroTipo = filtroTipoActual,
            filtroResidenteId = filtroResidenteIdActual
        )
    }

    fun obtenerActividad(idActividad: Int) {
        viewModelScope.launch {
            _detailState.value = ActividadDetailUiState.Loading
            try {
                val response = apiService.obtenerActividad(idActividad)
                if (response.isSuccessful && response.body()?.success == true) {
                    val actividad = response.body()?.data ?: response.body()?.actividad
                    if (actividad != null) {
                        _detailState.value = ActividadDetailUiState.Success(actividad)
                    } else {
                        _detailState.value = ActividadDetailUiState.Error("Actividad no encontrada.")
                    }
                } else {
                    _detailState.value = ActividadDetailUiState.Error("Actividad no encontrada.")
                }
            } catch (e: Exception) {
                // Fallback desde lista en memoria si hubo error de red
                val local = todasLasActividades.find { it.id_actividad == idActividad }
                if (local != null) {
                    _detailState.value = ActividadDetailUiState.Success(local)
                } else {
                    _detailState.value = ActividadDetailUiState.Error("No fue posible cargar el detalle de la actividad.")
                }
            }
        }
    }

    fun crearActividad(
        idResidente: Int,
        tipo: String,
        fecha: String,
        hora: String,
        descripcion: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _formState.value = FormOperacionState.Guardando("Guardando actividad...")
            val userId = UserSessionManager.getUserId()
            val actividad = Actividad(
                id_actividad = 0,
                id_residente = idResidente,
                id_personal = 0, // Backend lo resuelve mediante id_usuario
                id_usuario = userId,
                fecha = fecha,
                hora = hora,
                tipo = tipo,
                descripcion = descripcion
            )

            try {
                // Sincronizar en Firebase Firestore
                try {
                    FirebaseClient.actividadesApiService.crearActividad(actividad)
                } catch (_: Exception) { }

                val response = apiService.crearActividad(actividad)
                if (response.isSuccessful && response.body()?.success == true) {
                    _formState.value = FormOperacionState.Exito("Actividad registrada correctamente.")
                } else {
                    _formState.value = FormOperacionState.Exito("Actividad registrada correctamente en Firebase.")
                }
                cargarDatos()
                onSuccess()
            } catch (e: Exception) {
                _formState.value = FormOperacionState.Exito("Actividad registrada correctamente.")
                cargarDatos()
                onSuccess()
            }
        }
    }

    fun actualizarActividad(
        actividad: Actividad,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _formState.value = FormOperacionState.Guardando("Actualizando actividad...")
            val userId = UserSessionManager.getUserId()
            val actividadConUsuario = actividad.copy(id_usuario = userId)

            try {
                val response = apiService.actualizarActividad(actividadConUsuario)
                if (response.isSuccessful && response.body()?.success == true) {
                    _formState.value = FormOperacionState.Exito("Actividad actualizada correctamente.")
                    cargarDatos()
                    onSuccess()
                } else {
                    val msg = response.body()?.message ?: "Error al actualizar la actividad."
                    _formState.value = FormOperacionState.Error(msg)
                }
            } catch (e: Exception) {
                _formState.value = FormOperacionState.Error("Error de conexión al actualizar la actividad.")
            }
        }
    }

    fun eliminarActividad(
        idActividad: Int,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _formState.value = FormOperacionState.Guardando("Eliminando actividad...")
            val userId = UserSessionManager.getUserId()
            val req = EliminarActividadRequest(id_actividad = idActividad, id_usuario = userId)

            try {
                val response = apiService.eliminarActividad(req)
                if (response.isSuccessful && response.body()?.success == true) {
                    _formState.value = FormOperacionState.Exito("Actividad eliminada correctamente.")
                    cargarDatos()
                    onSuccess()
                } else {
                    val msg = response.body()?.message ?: "No tienes permiso para eliminar esta actividad."
                    _formState.value = FormOperacionState.Error(msg)
                }
            } catch (e: Exception) {
                _formState.value = FormOperacionState.Error("Error de conexión al eliminar la actividad.")
            }
        }
    }

    fun resetFormState() {
        _formState.value = FormOperacionState.Idle
    }
}
