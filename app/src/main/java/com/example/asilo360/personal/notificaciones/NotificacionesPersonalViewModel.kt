package com.example.asilo360.personal.notificaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.asilo360.data.model.Notificacion
import com.example.asilo360.data.repository.NotificacionesRepository
import com.example.asilo360.data.session.UserSessionManager
import com.example.asilo360.personal.notificaciones.components.ResumenNotificacionesKpi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

enum class FiltroEstadoLectura {
    TODAS, SIN_LEER, LEIDAS
}

sealed interface NotificacionesPersonalUiState {
    object Loading : NotificacionesPersonalUiState
    data class Success(
        val listaCompleta: List<Notificacion>,
        val listaFiltrada: List<Notificacion>,
        val resumenKpi: ResumenNotificacionesKpi = ResumenNotificacionesKpi(),
        val filtroEstado: FiltroEstadoLectura = FiltroEstadoLectura.TODAS,
        val filtroCategoria: String = "Todas"
    ) : NotificacionesPersonalUiState
    data class Error(val message: String) : NotificacionesPersonalUiState
}

sealed interface NotificacionPersonalDetailUiState {
    object Idle : NotificacionPersonalDetailUiState
    object Loading : NotificacionPersonalDetailUiState
    data class Success(val notificacion: Notificacion) : NotificacionPersonalDetailUiState
    data class Error(val message: String) : NotificacionPersonalDetailUiState
}

class NotificacionesPersonalViewModel(
    private val repository: NotificacionesRepository = NotificacionesRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificacionesPersonalUiState>(NotificacionesPersonalUiState.Loading)
    val uiState: StateFlow<NotificacionesPersonalUiState> = _uiState.asStateFlow()

    private val _detailState = MutableStateFlow<NotificacionPersonalDetailUiState>(NotificacionPersonalDetailUiState.Idle)
    val detailState: StateFlow<NotificacionPersonalDetailUiState> = _detailState.asStateFlow()

    private var todasLasNotificaciones: List<Notificacion> = emptyList()
    private var filtroEstadoActual: FiltroEstadoLectura = FiltroEstadoLectura.TODAS
    private var filtroCategoriaActual: String = "Todas"

    init {
        escucharNotificacionesEnTiempoReal()
    }

    fun cargarNotificaciones() {
        viewModelScope.launch {
            _uiState.value = NotificacionesPersonalUiState.Loading
            val currentUserId = UserSessionManager.getUserId()
            try {
                todasLasNotificaciones = repository.obtenerNotificaciones(currentUserId)
                aplicarFiltros()
            } catch (e: Exception) {
                _uiState.value = NotificacionesPersonalUiState.Error("No fue posible cargar las notificaciones.")
            }
        }
    }

    private fun escucharNotificacionesEnTiempoReal() {
        viewModelScope.launch {
            val currentUserId = UserSessionManager.getUserId()
            repository.obtenerNotificacionesEnTiempoReal(currentUserId)
                .catch {
                    cargarNotificaciones()
                }
                .collect { lista ->
                    todasLasNotificaciones = lista
                    aplicarFiltros()
                }
        }
    }

    fun onFiltroEstadoChange(filtro: FiltroEstadoLectura) {
        filtroEstadoActual = filtro
        aplicarFiltros()
    }

    fun onFiltroCategoriaChange(categoria: String) {
        filtroCategoriaActual = categoria
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        var resultado = todasLasNotificaciones

        // 1. Filtro por Estado de Lectura
        resultado = when (filtroEstadoActual) {
            FiltroEstadoLectura.TODAS -> resultado
            FiltroEstadoLectura.SIN_LEER -> resultado.filter { !it.leida }
            FiltroEstadoLectura.LEIDAS -> resultado.filter { it.leida }
        }

        // 2. Filtro por Categoría
        if (filtroCategoriaActual.isNotBlank() && filtroCategoriaActual != "Todas") {
            resultado = resultado.filter {
                it.tipo.contains(filtroCategoriaActual, ignoreCase = true)
            }
        }

        // Ordenar primero las no leídas, luego por fecha descendente
        resultado = resultado.sortedWith(
            compareBy<Notificacion> { it.leida }.thenByDescending { it.fecha.orEmpty() }
        )

        val total = todasLasNotificaciones.size
        val sinLeer = todasLasNotificaciones.count { !it.leida }
        val leidas = total - sinLeer

        val resumen = ResumenNotificacionesKpi(
            total = total,
            sinLeer = sinLeer,
            leidas = leidas
        )

        _uiState.value = NotificacionesPersonalUiState.Success(
            listaCompleta = todasLasNotificaciones,
            listaFiltrada = resultado,
            resumenKpi = resumen,
            filtroEstado = filtroEstadoActual,
            filtroCategoria = filtroCategoriaActual
        )
    }

    fun obtenerNotificacion(idNotificacion: Int) {
        viewModelScope.launch {
            _detailState.value = NotificacionPersonalDetailUiState.Loading
            val currentUserId = UserSessionManager.getUserId()
            try {
                var notificacion = repository.obtenerNotificacionPorId(idNotificacion, currentUserId)
                if (notificacion == null) {
                    notificacion = todasLasNotificaciones.find { it.id_notificacion == idNotificacion }
                }

                if (notificacion != null) {
                    _detailState.value = NotificacionPersonalDetailUiState.Success(notificacion)
                    // Auto-marcar como leída al abrir detalle si no lo estaba
                    if (!notificacion.leida) {
                        marcarComoLeida(idNotificacion)
                    }
                } else {
                    _detailState.value = NotificacionPersonalDetailUiState.Error("Notificación no encontrada.")
                }
            } catch (e: Exception) {
                val local = todasLasNotificaciones.find { it.id_notificacion == idNotificacion }
                if (local != null) {
                    _detailState.value = NotificacionPersonalDetailUiState.Success(local)
                } else {
                    _detailState.value = NotificacionPersonalDetailUiState.Error("No fue posible cargar el detalle.")
                }
            }
        }
    }

    fun marcarComoLeida(idNotificacion: Int) {
        viewModelScope.launch {
            try {
                repository.marcarComoLeida(idNotificacion)
            } catch (_: Exception) { }
        }
    }

    fun marcarTodasComoLeidas() {
        viewModelScope.launch {
            val currentUserId = UserSessionManager.getUserId()
            try {
                repository.marcarTodasComoLeidas(currentUserId)
            } catch (_: Exception) { }
        }
    }
}
