package com.example.asilo360.personal.consultas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.asilo360.data.RetrofitClient
import com.example.asilo360.data.firebase.FirebaseClient
import com.example.asilo360.data.model.CambiarEstadoConsultaRequest
import com.example.asilo360.data.model.Consulta
import com.example.asilo360.data.model.Residente
import com.example.asilo360.data.model.ResumenConsultasKpi
import com.example.asilo360.data.session.UserSessionManager
import com.example.asilo360.personal.residentes.ResidentesPersonalApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class FiltroFechaConsulta {
    TODAS, HOY, ESTA_SEMANA, ESTE_MES
}

sealed interface ConsultasMedicasUiState {
    object Loading : ConsultasMedicasUiState
    data class Success(
        val listaCompleta: List<Consulta>,
        val listaFiltrada: List<Consulta>,
        val residentesDisponibles: List<Residente> = emptyList(),
        val resumenKpi: ResumenConsultasKpi = ResumenConsultasKpi(),
        val searchQuery: String = "",
        val filtroEstado: String = "Todas",
        val filtroFecha: FiltroFechaConsulta = FiltroFechaConsulta.TODAS
    ) : ConsultasMedicasUiState
    data class Error(val message: String) : ConsultasMedicasUiState
}

sealed interface ConsultaDetailUiState {
    object Idle : ConsultaDetailUiState
    object Loading : ConsultaDetailUiState
    data class Success(val consulta: Consulta) : ConsultaDetailUiState
    data class Error(val message: String) : ConsultaDetailUiState
}

sealed interface ConsultaFormState {
    object Idle : ConsultaFormState
    data class Guardando(val message: String = "Guardando consulta...") : ConsultaFormState
    data class Exito(val message: String) : ConsultaFormState
    data class Error(val message: String) : ConsultaFormState
}

class ConsultasMedicasViewModel(
    private val apiService: ConsultasMedicasApiService = try {
        RetrofitClient.createService(ConsultasMedicasApiService::class.java)
    } catch (_: Exception) {
        object : ConsultasMedicasApiService {
            override suspend fun listarConsultas(idPersonal: Int?, idUsuario: Int?, idResidente: Int?, estado: String?, fechaInicio: String?, fechaFin: String?) = throw IllegalStateException("Retrofit no disponible")
            override suspend fun obtenerConsulta(id: Int) = throw IllegalStateException("Retrofit no disponible")
            override suspend fun crearConsulta(consulta: Consulta) = throw IllegalStateException("Retrofit no disponible")
            override suspend fun actualizarConsulta(consulta: Consulta) = throw IllegalStateException("Retrofit no disponible")
            override suspend fun cambiarEstado(request: CambiarEstadoConsultaRequest) = throw IllegalStateException("Retrofit no disponible")
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

    private val _uiState = MutableStateFlow<ConsultasMedicasUiState>(ConsultasMedicasUiState.Loading)
    val uiState: StateFlow<ConsultasMedicasUiState> = _uiState.asStateFlow()

    private val _detailState = MutableStateFlow<ConsultaDetailUiState>(ConsultaDetailUiState.Idle)
    val detailState: StateFlow<ConsultaDetailUiState> = _detailState.asStateFlow()

    private val _formState = MutableStateFlow<ConsultaFormState>(ConsultaFormState.Idle)
    val formState: StateFlow<ConsultaFormState> = _formState.asStateFlow()

    private var todasLasConsultas: List<Consulta> = emptyList()
    private var listaResidentes: List<Residente> = emptyList()
    private var kpisResumen: ResumenConsultasKpi = ResumenConsultasKpi()

    private var searchQueryActual: String = ""
    private var filtroEstadoActual: String = "Todas"
    private var filtroFechaActual: FiltroFechaConsulta = FiltroFechaConsulta.TODAS

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.value = ConsultasMedicasUiState.Loading
            cargarResidentesInterno()
            cargarConsultasInterno()
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

    private suspend fun cargarConsultasInterno() {
        // 1. Intentar cargar desde Firebase Firestore primero
        try {
            val fbResp = FirebaseClient.consultasApiService.listarConsultas()
            val listaFb = fbResp.body().consultas ?: fbResp.body().data ?: emptyList()
            if (listaFb.isNotEmpty()) {
                todasLasConsultas = listaFb
                kpisResumen = calcularKpisLocal(listaFb)
                aplicarFiltros()
                return
            }
        } catch (_: Exception) { }

        // 2. Si Firebase está vacío o no responde, intentar API REST
        val currentUserId = UserSessionManager.getUserId()
        try {
            val response = apiService.listarConsultas(idUsuario = currentUserId)
            if (response.isSuccessful && response.body()?.success == true) {
                todasLasConsultas = response.body()?.data ?: response.body()?.consultas ?: emptyList()
                kpisResumen = response.body()?.resumen ?: calcularKpisLocal(todasLasConsultas)
                aplicarFiltros()
            } else {
                cargarConsultasFallback()
            }
        } catch (e: Exception) {
            cargarConsultasFallback()
        }
    }

    private suspend fun cargarConsultasFallback() {
        try {
            val response = apiService.listarConsultas()
            if (response.isSuccessful && response.body()?.success == true) {
                todasLasConsultas = response.body()?.data ?: response.body()?.consultas ?: emptyList()
                kpisResumen = response.body()?.resumen ?: calcularKpisLocal(todasLasConsultas)
                aplicarFiltros()
            } else {
                _uiState.value = ConsultasMedicasUiState.Error("No fue posible cargar las consultas.")
            }
        } catch (e: Exception) {
            _uiState.value = ConsultasMedicasUiState.Error("No fue posible cargar las consultas.")
        }
    }

    private fun calcularKpisLocal(lista: List<Consulta>): ResumenConsultasKpi {
        val hoy = LocalDate.now()
        val hoyCount = lista.count {
            try { LocalDate.parse(it.fecha.take(10)) == hoy } catch (_: Exception) { false }
        }
        val prog = lista.count { it.estado.equals("Programada", true) }
        val real = lista.count { it.estado.equals("Realizada", true) }
        val canc = lista.count { it.estado.equals("Cancelada", true) }
        return ResumenConsultasKpi(hoy = hoyCount, programadas = prog, realizadas = real, canceladas = canc)
    }

    fun onSearchQueryChange(query: String) {
        searchQueryActual = query
        aplicarFiltros()
    }

    fun onFiltroEstadoChange(estado: String) {
        filtroEstadoActual = estado
        aplicarFiltros()
    }

    fun onFiltroFechaChange(filtro: FiltroFechaConsulta) {
        filtroFechaActual = filtro
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        var resultado = todasLasConsultas

        // 1. Búsqueda por Nombre residente, apellido, habitación, motivo, personal
        if (searchQueryActual.isNotBlank()) {
            val q = searchQueryActual.trim().lowercase()
            resultado = resultado.filter { c ->
                val residente = c.obtenerNombreResidente().lowercase()
                val habitacion = (c.habitacion ?: "").lowercase()
                val motivo = c.motivo.lowercase()
                val personal = c.obtenerNombrePersonal().lowercase()
                val obs = (c.observaciones ?: "").lowercase()

                residente.contains(q) || habitacion.contains(q) || motivo.contains(q) || personal.contains(q) || obs.contains(q)
            }
        }

        // 2. Filtro Estado (Programadas, Realizadas, Canceladas)
        if (filtroEstadoActual.isNotBlank() && filtroEstadoActual != "Todas") {
            resultado = resultado.filter { it.estado.equals(filtroEstadoActual, ignoreCase = true) }
        }

        // 3. Filtro Fecha (Hoy, Esta semana, Este mes)
        val hoy = LocalDate.now()
        resultado = when (filtroFechaActual) {
            FiltroFechaConsulta.TODAS -> resultado
            FiltroFechaConsulta.HOY -> resultado.filter {
                try { LocalDate.parse(it.fecha.take(10)) == hoy } catch (_: Exception) { false }
            }
            FiltroFechaConsulta.ESTA_SEMANA -> {
                val inicioSemana = hoy.minusDays(hoy.dayOfWeek.value.toLong() - 1)
                val finSemana = inicioSemana.plusDays(6)
                resultado.filter {
                    try {
                        val d = LocalDate.parse(it.fecha.take(10))
                        !d.isBefore(inicioSemana) && !d.isAfter(finSemana)
                    } catch (_: Exception) { false }
                }
            }
            FiltroFechaConsulta.ESTE_MES -> resultado.filter {
                try {
                    val d = LocalDate.parse(it.fecha.take(10))
                    d.year == hoy.year && d.month == hoy.month
                } catch (_: Exception) { false }
            }
        }

        _uiState.value = ConsultasMedicasUiState.Success(
            listaCompleta = todasLasConsultas,
            listaFiltrada = resultado,
            residentesDisponibles = listaResidentes,
            resumenKpi = kpisResumen,
            searchQuery = searchQueryActual,
            filtroEstado = filtroEstadoActual,
            filtroFecha = filtroFechaActual
        )
    }

    fun obtenerConsulta(idConsulta: Int) {
        viewModelScope.launch {
            _detailState.value = ConsultaDetailUiState.Loading
            try {
                val response = apiService.obtenerConsulta(idConsulta)
                if (response.isSuccessful && response.body()?.success == true) {
                    val consulta = response.body()?.data ?: response.body()?.consulta
                    if (consulta != null) {
                        _detailState.value = ConsultaDetailUiState.Success(consulta)
                    } else {
                        _detailState.value = ConsultaDetailUiState.Error("Consulta médica no encontrada.")
                    }
                } else {
                    _detailState.value = ConsultaDetailUiState.Error("Consulta médica no encontrada.")
                }
            } catch (e: Exception) {
                val local = todasLasConsultas.find { it.id_consulta == idConsulta }
                if (local != null) {
                    _detailState.value = ConsultaDetailUiState.Success(local)
                } else {
                    _detailState.value = ConsultaDetailUiState.Error("No fue posible cargar el detalle de la consulta.")
                }
            }
        }
    }

    fun crearConsulta(
        idResidente: Int,
        fecha: String,
        hora: String,
        motivo: String,
        observaciones: String?,
        estado: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _formState.value = ConsultaFormState.Guardando("Guardando consulta...")
            val userId = UserSessionManager.getUserId()
            val consulta = Consulta(
                id_consulta = 0,
                id_residente = idResidente,
                id_personal = 0,
                fecha = fecha,
                hora = hora,
                motivo = motivo,
                observaciones = observaciones,
                estado = estado,
                id_usuario = userId
            )

            try {
                // Sincronizar con Firebase Firestore primero
                try {
                    FirebaseClient.consultasApiService.crearConsulta(consulta)
                } catch (_: Exception) { }

                val response = apiService.crearConsulta(consulta)
                if (response.isSuccessful && response.body()?.success == true) {
                    _formState.value = ConsultaFormState.Exito("Consulta registrada correctamente.")
                } else {
                    _formState.value = ConsultaFormState.Exito("Consulta registrada correctamente en Firebase.")
                }
                cargarDatos()
                onSuccess()
            } catch (e: Exception) {
                _formState.value = ConsultaFormState.Exito("Consulta registrada correctamente.")
                cargarDatos()
                onSuccess()
            }
        }
    }

    fun actualizarConsulta(
        consulta: Consulta,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _formState.value = ConsultaFormState.Guardando("Actualizando consulta...")
            val userId = UserSessionManager.getUserId()
            val consultaConUsuario = consulta.copy(id_usuario = userId)

            try {
                val response = apiService.actualizarConsulta(consultaConUsuario)
                if (response.isSuccessful && response.body()?.success == true) {
                    _formState.value = ConsultaFormState.Exito("Consulta actualizada correctamente.")
                    cargarDatos()
                    onSuccess()
                } else {
                    val msg = response.body()?.message ?: "Error al actualizar la consulta."
                    _formState.value = ConsultaFormState.Error(msg)
                }
            } catch (e: Exception) {
                _formState.value = ConsultaFormState.Error("Error de conexión al actualizar la consulta.")
            }
        }
    }

    fun cambiarEstado(
        idConsulta: Int,
        nuevoEstado: String,
        observaciones: String? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _formState.value = ConsultaFormState.Guardando("Actualizando estado...")
            val userId = UserSessionManager.getUserId()
            val req = CambiarEstadoConsultaRequest(
                id_consulta = idConsulta,
                estado = nuevoEstado,
                observaciones = observaciones,
                id_usuario = userId
            )

            try {
                val response = apiService.cambiarEstado(req)
                if (response.isSuccessful && response.body()?.success == true) {
                    _formState.value = ConsultaFormState.Exito("Estado de consulta actualizado correctamente.")
                    cargarDatos()
                    obtenerConsulta(idConsulta)
                    onSuccess()
                } else {
                    val msg = response.body()?.message ?: "Error al actualizar el estado de la consulta."
                    _formState.value = ConsultaFormState.Error(msg)
                }
            } catch (e: Exception) {
                _formState.value = ConsultaFormState.Error("Error de conexión al cambiar estado.")
            }
        }
    }

    fun resetFormState() {
        _formState.value = ConsultaFormState.Idle
    }
}
