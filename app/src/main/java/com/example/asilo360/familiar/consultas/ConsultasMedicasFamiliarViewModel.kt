package com.example.asilo360.familiar.consultas

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.asilo360.data.model.Consulta
import com.example.asilo360.data.model.ResidenteVinculado
import com.example.asilo360.data.repository.FamiliarConsultasRepository
import com.example.asilo360.data.session.UserSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class FiltroConsultaEstado {
    TODAS, PROXIMAS, REALIZADAS, CANCELADAS
}

data class ConsultasFamiliarUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val residentesAutorizados: List<ResidenteVinculado> = emptyList(),
    val selectedResidenteId: Int = 0,
    val consultas: List<Consulta> = emptyList(),
    val consultasFiltradas: List<Consulta> = emptyList(),
    val filtroEstado: FiltroConsultaEstado = FiltroConsultaEstado.TODAS,
    val searchQuery: String = "",
    val proximasCount: Int = 0,
    val realizadasCount: Int = 0,
    val canceladasCount: Int = 0
) {
    val estaVacio: Boolean
        get() = !isLoading && errorMessage == null && consultas.isEmpty()

    val sinResultadosDeBusqueda: Boolean
        get() = !isLoading && errorMessage == null && consultas.isNotEmpty() && consultasFiltradas.isEmpty()
}

data class ConsultaFamiliarDetailUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val consulta: Consulta? = null
)

/**
 * ViewModel que gestiona la carga, filtrado y detalle de consultas médicas
 * exclusivamente para los residentes autorizados del usuario familiar.
 */
class ConsultasMedicasFamiliarViewModel(
    private val repository: FamiliarConsultasRepository = FamiliarConsultasRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConsultasFamiliarUiState())
    val uiState: StateFlow<ConsultasFamiliarUiState> = _uiState.asStateFlow()

    private val _detailState = MutableStateFlow(ConsultaFamiliarDetailUiState())
    val detailState: StateFlow<ConsultaFamiliarDetailUiState> = _detailState.asStateFlow()

    init {
        cargarDatos()
    }

    /**
     * Carga los residentes autorizados y sus citas médicas asociadas.
     */
    fun cargarDatos(isRefresh: Boolean = false) {
        val currentUserId = UserSessionManager.getUserId()

        _uiState.update {
            it.copy(
                isLoading = !isRefresh && it.consultas.isEmpty(),
                isRefreshing = isRefresh,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val residentes = repository.obtenerResidentesAutorizados(currentUserId)
                val selResId = _uiState.value.selectedResidenteId

                val consultasCargadas = repository.obtenerConsultasAutorizadas(
                    idUsuario = currentUserId,
                    idResidenteFiltro = selResId
                )

                val ordenadas = ordenarConsultas(consultasCargadas)

                val (prox, real, canc) = calcularContadores(ordenadas)

                _uiState.update { state ->
                    val filtradas = aplicarFiltrosYBusqueda(
                        lista = ordenadas,
                        filtro = state.filtroEstado,
                        query = state.searchQuery
                    )
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = null,
                        residentesAutorizados = residentes,
                        consultas = ordenadas,
                        consultasFiltradas = filtradas,
                        proximasCount = prox,
                        realizadasCount = real,
                        canceladasCount = canc
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar las consultas del familiar", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = "No fue posible obtener la información de las consultas médicas."
                    )
                }
            }
        }
    }

    /**
     * Cambia el filtro de estado (Todas, Próximas, Realizadas, Canceladas).
     */
    fun onFiltroEstadoChange(nuevoFiltro: FiltroConsultaEstado) {
        _uiState.update { state ->
            val filtradas = aplicarFiltrosYBusqueda(
                lista = state.consultas,
                filtro = nuevoFiltro,
                query = state.searchQuery
            )
            state.copy(
                filtroEstado = nuevoFiltro,
                consultasFiltradas = filtradas
            )
        }
    }

    /**
     * Cambia la consulta de texto para buscar por motivo, fecha o personal responsable.
     */
    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            val filtradas = aplicarFiltrosYBusqueda(
                lista = state.consultas,
                filtro = state.filtroEstado,
                query = query
            )
            state.copy(
                searchQuery = query,
                consultasFiltradas = filtradas
            )
        }
    }

    /**
     * Selecciona un residente específico para filtrar sus consultas o 0 para mostrar todos.
     */
    fun onSeleccionarResidente(idResidente: Int) {
        if (_uiState.value.selectedResidenteId != idResidente) {
            _uiState.update { it.copy(selectedResidenteId = idResidente) }
            cargarDatos(isRefresh = false)
        }
    }

    /**
     * Limpia la búsqueda y resetea el filtro de estado.
     */
    fun limpiarFiltros() {
        _uiState.update { state ->
            val filtradas = aplicarFiltrosYBusqueda(
                lista = state.consultas,
                filtro = FiltroConsultaEstado.TODAS,
                query = ""
            )
            state.copy(
                searchQuery = "",
                filtroEstado = FiltroConsultaEstado.TODAS,
                consultasFiltradas = filtradas
            )
        }
    }

    /**
     * Carga la información en detalle de una consulta específica.
     */
    fun cargarDetalleConsulta(idConsulta: Int) {
        _detailState.value = ConsultaFamiliarDetailUiState(isLoading = true)

        viewModelScope.launch {
            try {
                val currentUserId = UserSessionManager.getUserId()
                val detalle = repository.obtenerDetalleConsultaAutorizada(currentUserId, idConsulta)

                if (detalle != null) {
                    _detailState.value = ConsultaFamiliarDetailUiState(
                        isLoading = false,
                        consulta = detalle
                    )
                } else {
                    _detailState.value = ConsultaFamiliarDetailUiState(
                        isLoading = false,
                        errorMessage = "No se encontró la consulta médica especificada o no tienes autorización para verla."
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar detalle de consulta $idConsulta", e)
                _detailState.value = ConsultaFamiliarDetailUiState(
                    isLoading = false,
                    errorMessage = "Error de conexión al cargar la consulta."
                )
            }
        }
    }

    private fun aplicarFiltrosYBusqueda(
        lista: List<Consulta>,
        filtro: FiltroConsultaEstado,
        query: String
    ): List<Consulta> {
        val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())

        var resultado = when (filtro) {
            FiltroConsultaEstado.TODAS -> lista
            FiltroConsultaEstado.PROXIMAS -> lista.filter {
                it.estado.contains("Programada", ignoreCase = true) ||
                        it.estado.contains("Pendiente", ignoreCase = true) ||
                        (it.fecha >= hoy && !it.estado.contains("Cancelada", ignoreCase = true))
            }
            FiltroConsultaEstado.REALIZADAS -> lista.filter {
                it.estado.contains("Realizada", ignoreCase = true) ||
                        it.estado.contains("Completada", ignoreCase = true)
            }
            FiltroConsultaEstado.CANCELADAS -> lista.filter {
                it.estado.contains("Cancelada", ignoreCase = true)
            }
        }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            resultado = resultado.filter { c ->
                c.motivo.lowercase().contains(q) ||
                        c.fecha.contains(q) ||
                        c.obtenerNombrePersonal().lowercase().contains(q) ||
                        c.obtenerNombreResidente().lowercase().contains(q)
            }
        }

        return resultado
    }

    private fun ordenarConsultas(lista: List<Consulta>): List<Consulta> {
        val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())

        val proximas = lista.filter {
            it.estado.contains("Programada", ignoreCase = true) || (it.fecha >= hoy && !it.estado.contains("Cancelada", ignoreCase = true))
        }.sortedWith(compareBy({ it.fecha }, { it.hora }))

        val pasadas = lista.filterNot {
            it.estado.contains("Programada", ignoreCase = true) || (it.fecha >= hoy && !it.estado.contains("Cancelada", ignoreCase = true))
        }.sortedWith(compareByDescending<Consulta> { it.fecha }.thenByDescending { it.hora })

        return proximas + pasadas
    }

    private fun calcularContadores(lista: List<Consulta>): Triple<Int, Int, Int> {
        val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())

        val prox = lista.count {
            it.estado.contains("Programada", ignoreCase = true) ||
                    it.estado.contains("Pendiente", ignoreCase = true) ||
                    (it.fecha >= hoy && !it.estado.contains("Cancelada", ignoreCase = true))
        }
        val real = lista.count {
            it.estado.contains("Realizada", ignoreCase = true) ||
                    it.estado.contains("Completada", ignoreCase = true)
        }
        val canc = lista.count {
            it.estado.contains("Cancelada", ignoreCase = true)
        }

        return Triple(prox, real, canc)
    }

    companion object {
        private const val TAG = "ConsultasFamiliarVM"
    }
}
