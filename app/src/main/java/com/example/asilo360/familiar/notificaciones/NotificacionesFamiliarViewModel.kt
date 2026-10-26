package com.example.asilo360.familiar.notificaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.asilo360.data.model.Notificacion
import com.example.asilo360.data.repository.NotificacionesRepository
import com.example.asilo360.data.session.UserSessionManager
import com.example.asilo360.familiar.notificaciones.components.ResumenNotificacionesFamiliarKpi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

enum class FiltroEstadoFamiliarLectura {
    TODAS, NO_LEIDAS, LEIDAS
}

sealed interface NotificacionesFamiliarUiState {
    object Loading : NotificacionesFamiliarUiState
    data class Success(
        val listaCompleta: List<Notificacion>,
        val listaFiltrada: List<Notificacion>,
        val resumenKpi: ResumenNotificacionesFamiliarKpi = ResumenNotificacionesFamiliarKpi(),
        val filtroEstado: FiltroEstadoFamiliarLectura = FiltroEstadoFamiliarLectura.TODAS,
        val filtroCategoria: String = "Todas"
    ) : NotificacionesFamiliarUiState
    data class Error(val message: String) : NotificacionesFamiliarUiState
}

sealed interface NotificacionFamiliarDetailUiState {
    object Idle : NotificacionFamiliarDetailUiState
    object Loading : NotificacionFamiliarDetailUiState
    data class Success(val notificacion: Notificacion) : NotificacionFamiliarDetailUiState
    data class Error(val message: String) : NotificacionFamiliarDetailUiState
}

class NotificacionesFamiliarViewModel(
    private val repository: NotificacionesRepository = NotificacionesRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificacionesFamiliarUiState>(NotificacionesFamiliarUiState.Loading)
    val uiState: StateFlow<NotificacionesFamiliarUiState> = _uiState.asStateFlow()

    private val _detailState = MutableStateFlow<NotificacionFamiliarDetailUiState>(NotificacionFamiliarDetailUiState.Idle)
    val detailState: StateFlow<NotificacionFamiliarDetailUiState> = _detailState.asStateFlow()

    private var todasLasNotificaciones: List<Notificacion> = emptyList()
    private var filtroEstadoActual: FiltroEstadoFamiliarLectura = FiltroEstadoFamiliarLectura.TODAS
    private var filtroCategoriaActual: String = "Todas"

    init {
        escucharNotificacionesEnTiempoReal()
    }

    fun cargarNotificaciones() {
        viewModelScope.launch {
            _uiState.value = NotificacionesFamiliarUiState.Loading
            val currentUserId = UserSessionManager.getUserId()
            try {
                todasLasNotificaciones = repository.obtenerNotificaciones(currentUserId)
                aplicarFiltros()
            } catch (e: Exception) {
                _uiState.value = NotificacionesFamiliarUiState.Error("No fue posible consultar las notificaciones.")
            }
        }
    }

    private fun escucharNotificacionesEnTiempoReal() {
        viewModelScope.launch {
            val currentUserId = UserSessionManager.getUserId()
            repository.obtenerNotificacionesFamiliarEnTiempoReal(currentUserId)
                .catch {
                    cargarNotificaciones()
                }
                .collect { lista ->
                    todasLasNotificaciones = lista
                    aplicarFiltros()
                }
        }
    }

    fun onFiltroEstadoChange(filtro: FiltroEstadoFamiliarLectura) {
        filtroEstadoActual = filtro
        aplicarFiltros()
    }

    fun onFiltroCategoriaChange(categoria: String) {
        filtroCategoriaActual = categoria
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        var resultado = todasLasNotificaciones

        resultado = when (filtroEstadoActual) {
            FiltroEstadoFamiliarLectura.TODAS -> resultado
            FiltroEstadoFamiliarLectura.NO_LEIDAS -> resultado.filter { !it.leida }
            FiltroEstadoFamiliarLectura.LEIDAS -> resultado.filter { it.leida }
        }

        if (filtroCategoriaActual.isNotBlank() && filtroCategoriaActual != "Todas") {
            resultado = resultado.filter {
                it.tipo.contains(filtroCategoriaActual, ignoreCase = true)
            }
        }

        resultado = resultado.sortedWith(
            compareBy<Notificacion> { it.leida }.thenByDescending { it.fecha.orEmpty() }
        )

        val total = todasLasNotificaciones.size
        val sinLeer = todasLasNotificaciones.count { !it.leida }
        val leidas = total - sinLeer

        val resumen = ResumenNotificacionesFamiliarKpi(
            total = total,
            sinLeer = sinLeer,
            leidas = leidas
        )

        _uiState.value = NotificacionesFamiliarUiState.Success(
            listaCompleta = todasLasNotificaciones,
            listaFiltrada = resultado,
            resumenKpi = resumen,
            filtroEstado = filtroEstadoActual,
            filtroCategoria = filtroCategoriaActual
        )
    }

    fun obtenerNotificacion(idNotificacion: Int) {
        viewModelScope.launch {
            _detailState.value = NotificacionFamiliarDetailUiState.Loading
            val currentUserId = UserSessionManager.getUserId()
            try {
                var notificacion = repository.obtenerNotificacionPorId(idNotificacion, currentUserId)
                if (notificacion == null) {
                    notificacion = todasLasNotificaciones.find { it.id_notificacion == idNotificacion }
                }

                if (notificacion != null) {
                    _detailState.value = NotificacionFamiliarDetailUiState.Success(notificacion)
                    if (!notificacion.leida) {
                        marcarComoLeida(idNotificacion)
                    }
                } else {
                    _detailState.value = NotificacionFamiliarDetailUiState.Error("Aviso no encontrado.")
                }
            } catch (e: Exception) {
                val local = todasLasNotificaciones.find { it.id_notificacion == idNotificacion }
                if (local != null) {
                    _detailState.value = NotificacionFamiliarDetailUiState.Success(local)
                } else {
                    _detailState.value = NotificacionFamiliarDetailUiState.Error("Error al cargar el detalle.")
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
