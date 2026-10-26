package com.example.asilo360.personal.residentes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.asilo360.data.RetrofitClient
import com.example.asilo360.data.firebase.FirebaseClient
import com.example.asilo360.data.model.Residente
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class EstadoFiltro {
    TODOS, ACTIVOS, INACTIVOS
}

enum class CriterioOrden {
    NOMBRE, HABITACION, FECHA_INGRESO
}

sealed interface ResidentesPersonalUiState {
    object Loading : ResidentesPersonalUiState
    data class Success(
        val listaCompleta: List<Residente>,
        val listaFiltrada: List<Residente>,
        val searchQuery: String = "",
        val filtroEstado: EstadoFiltro = EstadoFiltro.TODOS,
        val criterioOrden: CriterioOrden = CriterioOrden.NOMBRE
    ) : ResidentesPersonalUiState
    data class Error(val message: String) : ResidentesPersonalUiState
}

sealed interface ResidenteDetailUiState {
    object Idle : ResidenteDetailUiState
    object Loading : ResidenteDetailUiState
    data class Success(val residente: Residente) : ResidenteDetailUiState
    data class Error(val message: String) : ResidenteDetailUiState
}

class ResidentesPersonalViewModel(
    private val apiService: ResidentesPersonalApiService = try {
        RetrofitClient.createService(ResidentesPersonalApiService::class.java)
    } catch (_: Exception) {
        object : ResidentesPersonalApiService {
            override suspend fun listarResidentes(idRol: Int) = throw IllegalStateException("Retrofit no disponible")
            override suspend fun obtenerResidente(id: Int, idRol: Int) = throw IllegalStateException("Retrofit no disponible")
        }
    }
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResidentesPersonalUiState>(ResidentesPersonalUiState.Loading)
    val uiState: StateFlow<ResidentesPersonalUiState> = _uiState.asStateFlow()

    private val _detailState = MutableStateFlow<ResidenteDetailUiState>(ResidenteDetailUiState.Idle)
    val detailState: StateFlow<ResidenteDetailUiState> = _detailState.asStateFlow()

    private var todosLosResidentes: List<Residente> = emptyList()
    private var searchQueryActual: String = ""
    private var filtroEstadoActual: EstadoFiltro = EstadoFiltro.TODOS
    private var criterioOrdenActual: CriterioOrden = CriterioOrden.NOMBRE

    init {
        cargarResidentes()
    }

    fun cargarResidentes() {
        viewModelScope.launch {
            _uiState.value = ResidentesPersonalUiState.Loading
            try {
                val response = apiService.listarResidentes(idRol = 2)
                if (response.isSuccessful && response.body()?.success == true) {
                    val lista = response.body()?.data ?: emptyList()
                    todosLosResidentes = lista
                    aplicarFiltrosYOrden()
                } else {
                    // Intento de fallback si Retrofit o API PHP no está activa
                    cargarDesdeFallback()
                }
            } catch (e: Exception) {
                cargarDesdeFallback()
            }
        }
    }

    private suspend fun cargarDesdeFallback() {
        try {
            val fbResponse = FirebaseClient.residentesApiService.listarResidentes()
            val lista = fbResponse.body().data ?: emptyList()
            todosLosResidentes = lista
            aplicarFiltrosYOrden()
        } catch (e: Exception) {
            _uiState.value = ResidentesPersonalUiState.Error("No fue posible cargar los residentes.")
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQueryActual = query
        aplicarFiltrosYOrden()
    }

    fun onFiltroEstadoChange(filtro: EstadoFiltro) {
        filtroEstadoActual = filtro
        aplicarFiltrosYOrden()
    }

    fun onCriterioOrdenChange(criterio: CriterioOrden) {
        criterioOrdenActual = criterio
        aplicarFiltrosYOrden()
    }

    private fun aplicarFiltrosYOrden() {
        var resultado = todosLosResidentes

        // 1. Filtrar por búsqueda (Nombre, Apellido paterno, Apellido materno, Habitación)
        if (searchQueryActual.isNotBlank()) {
            val queryClean = searchQueryActual.trim().lowercase()
            resultado = resultado.filter { r ->
                val nombre = r.nombre.lowercase()
                val paterno = r.apellido_paterno.lowercase()
                val materno = (r.apellido_materno ?: "").lowercase()
                val habitacion = (r.habitacion ?: "").lowercase()

                nombre.contains(queryClean) ||
                        paterno.contains(queryClean) ||
                        materno.contains(queryClean) ||
                        habitacion.contains(queryClean)
            }
        }

        // 2. Filtrar por estado (Activos / Inactivos)
        resultado = when (filtroEstadoActual) {
            EstadoFiltro.TODOS -> resultado
            EstadoFiltro.ACTIVOS -> resultado.filter { it.estado.equals("Activo", ignoreCase = true) }
            EstadoFiltro.INACTIVOS -> resultado.filter { !it.estado.equals("Activo", ignoreCase = true) }
        }

        // 3. Ordenar
        resultado = when (criterioOrdenActual) {
            CriterioOrden.NOMBRE -> resultado.sortedBy { it.nombre.lowercase() }
            CriterioOrden.HABITACION -> resultado.sortedBy { it.habitacion.orEmpty().lowercase() }
            CriterioOrden.FECHA_INGRESO -> resultado.sortedByDescending { it.fecha_ingreso.orEmpty() }
        }

        _uiState.value = ResidentesPersonalUiState.Success(
            listaCompleta = todosLosResidentes,
            listaFiltrada = resultado,
            searchQuery = searchQueryActual,
            filtroEstado = filtroEstadoActual,
            criterioOrden = criterioOrdenActual
        )
    }

    fun cargarDetalleResidente(idResidente: Int) {
        viewModelScope.launch {
            _detailState.value = ResidenteDetailUiState.Loading
            try {
                val response = apiService.obtenerResidente(id = idResidente, idRol = 2)
                if (response.isSuccessful && response.body()?.success == true) {
                    val residente = response.body()?.data ?: response.body()?.residente
                    if (residente != null) {
                        _detailState.value = ResidenteDetailUiState.Success(residente)
                    } else {
                        _detailState.value = ResidenteDetailUiState.Error("Residente no encontrado.")
                    }
                } else {
                    cargarDetalleFallback(idResidente)
                }
            } catch (e: Exception) {
                cargarDetalleFallback(idResidente)
            }
        }
    }

    private suspend fun cargarDetalleFallback(idResidente: Int) {
        try {
            val fbResponse = FirebaseClient.residentesApiService.listarResidentes()
            val residente = fbResponse.body().data?.find { it.id_residente == idResidente }
            if (residente != null) {
                _detailState.value = ResidenteDetailUiState.Success(residente)
            } else {
                _detailState.value = ResidenteDetailUiState.Error("Residente no encontrado.")
            }
        } catch (e: Exception) {
            _detailState.value = ResidenteDetailUiState.Error("No fue posible cargar el detalle del residente.")
        }
    }
}
