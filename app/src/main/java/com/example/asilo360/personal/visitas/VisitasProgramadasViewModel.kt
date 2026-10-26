package com.example.asilo360.personal.visitas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.asilo360.data.model.Visita
import com.example.asilo360.data.repository.VisitasRepository
import com.example.asilo360.personal.visitas.components.ResumenVisitasKpi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class FiltroVisitaTab {
    TODAS, HOY, PROXIMAS, REALIZADAS, CANCELADAS
}

sealed interface VisitasProgramadasUiState {
    object Loading : VisitasProgramadasUiState
    data class Success(
        val listaCompleta: List<Visita>,
        val listaHoy: List<Visita>,
        val listaFiltrada: List<Visita>,
        val resumenKpi: ResumenVisitasKpi = ResumenVisitasKpi(),
        val searchQuery: String = "",
        val filtroTab: FiltroVisitaTab = FiltroVisitaTab.TODAS,
        val filtroEstado: String = "Todas"
    ) : VisitasProgramadasUiState
    data class Error(val message: String) : VisitasProgramadasUiState
}

sealed interface VisitaDetailUiState {
    object Idle : VisitaDetailUiState
    object Loading : VisitaDetailUiState
    data class Success(val visita: Visita) : VisitaDetailUiState
    data class Error(val message: String) : VisitaDetailUiState
}

sealed interface VisitaOperacionState {
    object Idle : VisitaOperacionState
    data class Cargando(val message: String = "Actualizando visita...") : VisitaOperacionState
    data class Exito(val message: String) : VisitaOperacionState
    data class Error(val message: String) : VisitaOperacionState
}

class VisitasProgramadasViewModel(
    private val repository: VisitasRepository = VisitasRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<VisitasProgramadasUiState>(VisitasProgramadasUiState.Loading)
    val uiState: StateFlow<VisitasProgramadasUiState> = _uiState.asStateFlow()

    private val _detailState = MutableStateFlow<VisitaDetailUiState>(VisitaDetailUiState.Idle)
    val detailState: StateFlow<VisitaDetailUiState> = _detailState.asStateFlow()

    private val _operacionState = MutableStateFlow<VisitaOperacionState>(VisitaOperacionState.Idle)
    val operacionState: StateFlow<VisitaOperacionState> = _operacionState.asStateFlow()

    private var todasLasVisitas: List<Visita> = emptyList()
    private var searchQueryActual: String = ""
    private var filtroTabActual: FiltroVisitaTab = FiltroVisitaTab.TODAS
    private var filtroEstadoActual: String = "Todas"

    init {
        escucharVisitasTiempoReal()
    }

    fun cargarVisitas() {
        viewModelScope.launch {
            _uiState.value = VisitasProgramadasUiState.Loading
            try {
                todasLasVisitas = repository.obtenerVisitas()
                aplicarFiltros()
            } catch (e: Exception) {
                _uiState.value = VisitasProgramadasUiState.Error("No fue posible cargar las visitas.")
            }
        }
    }

    private fun escucharVisitasTiempoReal() {
        viewModelScope.launch {
            repository.obtenerVisitasEnTiempoReal()
                .catch {
                    // Fallback a consulta tradicional
                    cargarVisitas()
                }
                .collect { listaDirecta ->
                    if (listaDirecta.isNotEmpty()) {
                        // Enriquecer nombres de residentes y familiares si faltan
                        val visitasEnriquecidas = try {
                            val completas = repository.obtenerVisitas().associateBy { it.id_visita }
                            listaDirecta.map { v ->
                                val comp = completas[v.id_visita]
                                v.copy(
                                    nombre_residente = v.nombre_residente ?: comp?.nombre_residente,
                                    habitacion = v.habitacion ?: comp?.habitacion,
                                    nombre_familiar = v.nombre_familiar ?: comp?.nombre_familiar,
                                    parentesco = v.parentesco ?: comp?.parentesco
                                )
                            }
                        } catch (_: Exception) {
                            listaDirecta
                        }
                        todasLasVisitas = visitasEnriquecidas
                        aplicarFiltros()
                    } else {
                        cargarVisitas()
                    }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQueryActual = query
        aplicarFiltros()
    }

    fun onFiltroTabChange(tab: FiltroVisitaTab) {
        filtroTabActual = tab
        aplicarFiltros()
    }

    fun onFiltroEstadoChange(estado: String) {
        filtroEstadoActual = estado
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        val hoy = LocalDate.now()

        // 1. Visitas de Hoy
        val listaHoy = todasLasVisitas.filter { v ->
            try {
                LocalDate.parse(v.fecha.take(10)) == hoy
            } catch (_: Exception) {
                false
            }
        }.sortedBy { it.hora }

        var resultado = todasLasVisitas

        // 2. Filtro por Búsqueda (Residente, Familiar, Parentesco, Habitación)
        if (searchQueryActual.isNotBlank()) {
            val q = searchQueryActual.trim().lowercase()
            resultado = resultado.filter { v ->
                val residente = (v.nombre_residente ?: "").lowercase()
                val habitacion = (v.habitacion ?: "").lowercase()
                val familiar = (v.nombre_familiar ?: "").lowercase()
                val parentesco = (v.parentesco ?: "").lowercase()

                residente.contains(q) || habitacion.contains(q) || familiar.contains(q) || parentesco.contains(q)
            }
        }

        // 3. Filtro por Tab
        resultado = when (filtroTabActual) {
            FiltroVisitaTab.TODAS -> resultado
            FiltroVisitaTab.HOY -> resultado.filter {
                try { LocalDate.parse(it.fecha.take(10)) == hoy } catch (_: Exception) { false }
            }
            FiltroVisitaTab.PROXIMAS -> resultado.filter {
                try {
                    val d = LocalDate.parse(it.fecha.take(10))
                    !d.isBefore(hoy) && !it.estado.equals("Realizada", true) && !it.estado.equals("Cancelada", true)
                } catch (_: Exception) { false }
            }
            FiltroVisitaTab.REALIZADAS -> resultado.filter { it.estado.equals("Realizada", true) }
            FiltroVisitaTab.CANCELADAS -> resultado.filter { it.estado.equals("Cancelada", true) }
        }

        // 4. Filtro por Estado
        if (filtroEstadoActual.isNotBlank() && filtroEstadoActual != "Todas") {
            resultado = resultado.filter { it.estado.equals(filtroEstadoActual, ignoreCase = true) }
        }

        // Ordenar con prioridad a la fecha y hora
        resultado = resultado.sortedWith(compareBy({ it.fecha }, { it.hora }))

        // KPIs
        val hoyCount = todasLasVisitas.count {
            try { LocalDate.parse(it.fecha.take(10)) == hoy } catch (_: Exception) { false }
        }
        val prog = todasLasVisitas.count { it.estado.equals("Programada", true) }
        val conf = todasLasVisitas.count { it.estado.equals("Confirmada", true) }
        val real = todasLasVisitas.count { it.estado.equals("Realizada", true) }
        val canc = todasLasVisitas.count { it.estado.equals("Cancelada", true) }

        val resumen = ResumenVisitasKpi(
            hoy = hoyCount,
            programadas = prog,
            confirmadas = conf,
            realizadas = real,
            canceladas = canc
        )

        _uiState.value = VisitasProgramadasUiState.Success(
            listaCompleta = todasLasVisitas,
            listaHoy = listaHoy,
            listaFiltrada = resultado,
            resumenKpi = resumen,
            searchQuery = searchQueryActual,
            filtroTab = filtroTabActual,
            filtroEstado = filtroEstadoActual
        )
    }

    fun cargarVisita(idVisita: Int) {
        viewModelScope.launch {
            _detailState.value = VisitaDetailUiState.Loading
            try {
                val visita = repository.obtenerVisitaPorId(idVisita)
                if (visita != null) {
                    _detailState.value = VisitaDetailUiState.Success(visita)
                } else {
                    _detailState.value = VisitaDetailUiState.Error("Visita no encontrada.")
                }
            } catch (e: Exception) {
                _detailState.value = VisitaDetailUiState.Error("Error al cargar la visita.")
            }
        }
    }

    fun confirmarVisita(idVisita: Int, onSuccess: () -> Unit) {
        actualizarEstadoVisita(idVisita, "Confirmada", "Visita confirmada correctamente.", onSuccess)
    }

    fun marcarRealizada(idVisita: Int, onSuccess: () -> Unit) {
        actualizarEstadoVisita(idVisita, "Realizada", "Visita marcada como realizada.", onSuccess)
    }

    fun cancelarVisita(idVisita: Int, onSuccess: () -> Unit) {
        actualizarEstadoVisita(idVisita, "Cancelada", "Visita cancelada correctamente.", onSuccess)
    }

    private fun actualizarEstadoVisita(idVisita: Int, nuevoEstado: String, mensajeExito: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _operacionState.value = VisitaOperacionState.Cargando("Actualizando estado a $nuevoEstado...")
            try {
                repository.actualizarEstado(idVisita, nuevoEstado)
                _operacionState.value = VisitaOperacionState.Exito(mensajeExito)
                cargarVisita(idVisita)
                onSuccess()
            } catch (e: Exception) {
                _operacionState.value = VisitaOperacionState.Error("Error al actualizar el estado de la visita.")
            }
        }
    }

    fun guardarObservaciones(idVisita: Int, observaciones: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _operacionState.value = VisitaOperacionState.Cargando("Guardando observaciones...")
            try {
                repository.actualizarObservaciones(idVisita, observaciones.trim())
                _operacionState.value = VisitaOperacionState.Exito("Observaciones guardadas correctamente.")
                cargarVisita(idVisita)
                onSuccess()
            } catch (e: Exception) {
                _operacionState.value = VisitaOperacionState.Error("Error al guardar las observaciones.")
            }
        }
    }

    fun resetOperacionState() {
        _operacionState.value = VisitaOperacionState.Idle
    }
}
