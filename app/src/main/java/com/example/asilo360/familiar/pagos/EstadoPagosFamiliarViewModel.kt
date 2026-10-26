package com.example.asilo360.familiar.pagos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.asilo360.data.model.Pago
import com.example.asilo360.data.model.Residente
import com.example.asilo360.data.repository.FamiliarRepository
import com.example.asilo360.data.repository.PagosRepository
import com.example.asilo360.data.session.UserSessionManager
import com.example.asilo360.familiar.pagos.components.ResumenPagosKpi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

enum class FiltroEstadoPagoTab {
    TODOS, PAGADOS, PENDIENTES, VENCIDOS, CANCELADOS
}

sealed interface EstadoPagosFamiliarUiState {
    object Loading : EstadoPagosFamiliarUiState
    data class Success(
        val listaResidentes: List<Residente>,
        val residenteSeleccionado: Residente?,
        val listaCompleta: List<Pago>,
        val listaFiltrada: List<Pago>,
        val kpi: ResumenPagosKpi = ResumenPagosKpi(),
        val filtroTab: FiltroEstadoPagoTab = FiltroEstadoPagoTab.TODOS,
        val searchQuery: String = ""
    ) : EstadoPagosFamiliarUiState
    data class Error(val message: String) : EstadoPagosFamiliarUiState
}

sealed interface DetallePagoFamiliarUiState {
    object Idle : DetallePagoFamiliarUiState
    object Loading : DetallePagoFamiliarUiState
    data class Success(val pago: Pago) : DetallePagoFamiliarUiState
    data class Error(val message: String) : DetallePagoFamiliarUiState
}

class EstadoPagosFamiliarViewModel(
    private val repository: PagosRepository = PagosRepository(),
    private val familiarRepository: FamiliarRepository = FamiliarRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<EstadoPagosFamiliarUiState>(EstadoPagosFamiliarUiState.Loading)
    val uiState: StateFlow<EstadoPagosFamiliarUiState> = _uiState.asStateFlow()

    private val _detailState = MutableStateFlow<DetallePagoFamiliarUiState>(DetallePagoFamiliarUiState.Idle)
    val detailState: StateFlow<DetallePagoFamiliarUiState> = _detailState.asStateFlow()

    private var todosLosPagos: List<Pago> = emptyList()
    private var listaResidentesVinculados: List<Residente> = emptyList()
    private var residenteActual: Residente? = null
    private var idFamiliarActual: Int = 0

    private var filtroTabActual = FiltroEstadoPagoTab.TODOS
    private var searchQueryActual = ""

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.value = EstadoPagosFamiliarUiState.Loading
            try {
                val currentUserId = UserSessionManager.getUserId()

                val vinculados = familiarRepository.obtenerResidentesVinculados(currentUserId)
                listaResidentesVinculados = vinculados.map { it.residente }
                idFamiliarActual = vinculados.firstOrNull()?.idFamiliar ?: currentUserId

                if (residenteActual == null && listaResidentesVinculados.isNotEmpty()) {
                    residenteActual = listaResidentesVinculados.first()
                }

                if (listaResidentesVinculados.isNotEmpty()) escucharPagos() else aplicarFiltros()

            } catch (e: Exception) {
                listaResidentesVinculados = emptyList()
                residenteActual = null
                idFamiliarActual = 0
                todosLosPagos = emptyList()
                _uiState.value = EstadoPagosFamiliarUiState.Error("No fue posible cargar tus residentes vinculados.")
            }
        }
    }

    private fun escucharPagos() {
        viewModelScope.launch {
            repository.obtenerPagosEnTiempoReal()
                .catch {
                    _uiState.value = EstadoPagosFamiliarUiState.Error("No fue posible consultar el estado de pagos.")
                }
                .collect { listaDirecta ->
                    val idsResidentesAutorizados = listaResidentesVinculados.map { it.id_residente }.toSet()
                    todosLosPagos = listaDirecta.filter { it.id_residente in idsResidentesAutorizados }
                    aplicarFiltros()
                }
        }
    }

    fun onSeleccionarResidente(residente: Residente) {
        residenteActual = residente
        aplicarFiltros()
    }

    fun onFiltroTabChange(tab: FiltroEstadoPagoTab) {
        filtroTabActual = tab
        aplicarFiltros()
    }

    fun onSearchQueryChange(query: String) {
        searchQueryActual = query
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        var resultado = todosLosPagos

        if (residenteActual != null) {
            resultado = resultado.filter { it.id_residente == residenteActual!!.id_residente }
        } else {
            resultado = emptyList()
        }

        if (searchQueryActual.isNotBlank()) {
            val q = searchQueryActual.trim().lowercase()
            resultado = resultado.filter { p ->
                p.concepto.lowercase().contains(q) || p.fecha_pago.lowercase().contains(q) || p.metodo_pago.lowercase().contains(q)
            }
        }

        // Filtro por Tab
        resultado = when (filtroTabActual) {
            FiltroEstadoPagoTab.TODOS -> resultado
            FiltroEstadoPagoTab.PAGADOS -> resultado.filter { it.estado.equals("Pagado", true) || it.estado.equals("Completado", true) }
            FiltroEstadoPagoTab.PENDIENTES -> resultado.filter { it.estado.equals("Pendiente", true) }
            FiltroEstadoPagoTab.VENCIDOS -> resultado.filter { it.estado.equals("Vencido", true) }
            FiltroEstadoPagoTab.CANCELADOS -> resultado.filter { it.estado.equals("Cancelado", true) }
        }

        // Ordenar del más reciente al más antiguo
        resultado = resultado.sortedByDescending { it.fecha_pago }

        val paraResidente = if (residenteActual != null) {
            todosLosPagos.filter { it.id_residente == residenteActual!!.id_residente }
        } else {
            emptyList()
        }

        val totalReg = paraResidente.size
        val totalPag = paraResidente.filter { it.estado.equals("Pagado", true) || it.estado.equals("Completado", true) }.sumOf { it.monto }
        val totalPen = paraResidente.filter { it.estado.equals("Pendiente", true) || it.estado.equals("Vencido", true) }.sumOf { it.monto }
        val ultimo = paraResidente.filter { it.estado.equals("Pagado", true) }.maxByOrNull { it.fecha_pago }?.monto ?: 0.0

        val kpi = ResumenPagosKpi(
            totalRegistros = totalReg,
            totalPagado = totalPag,
            totalPendiente = totalPen,
            ultimoPagoMonto = ultimo
        )

        _uiState.value = EstadoPagosFamiliarUiState.Success(
            listaResidentes = listaResidentesVinculados,
            residenteSeleccionado = residenteActual,
            listaCompleta = paraResidente,
            listaFiltrada = resultado,
            kpi = kpi,
            filtroTab = filtroTabActual,
            searchQuery = searchQueryActual
        )
    }

    fun obtenerPago(idPago: Int) {
        viewModelScope.launch {
            _detailState.value = DetallePagoFamiliarUiState.Loading
            try {
                val pago = repository.obtenerPagoPorId(idPago)
                    ?: todosLosPagos.find { it.id_pago == idPago }

                if (pago != null) {
                    _detailState.value = DetallePagoFamiliarUiState.Success(pago)
                } else {
                    _detailState.value = DetallePagoFamiliarUiState.Error("Registro de pago no encontrado.")
                }
            } catch (e: Exception) {
                val local = todosLosPagos.find { it.id_pago == idPago }
                if (local != null) {
                    _detailState.value = DetallePagoFamiliarUiState.Success(local)
                } else {
                    _detailState.value = DetallePagoFamiliarUiState.Error("Error al cargar los detalles del pago.")
                }
            }
        }
    }
}
