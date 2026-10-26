package com.example.asilo360.familiar.visitas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.asilo360.data.model.Residente
import com.example.asilo360.data.model.Visita
import com.example.asilo360.data.repository.FamiliarRepository
import com.example.asilo360.data.repository.VisitasRepository
import com.example.asilo360.data.session.UserSessionManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

enum class FiltroFamiliarVisitaTab {
    TODAS, PROXIMAS, CONFIRMADAS, REALIZADAS, CANCELADAS
}

data class ResumenVisitasFamiliarKpi(
    val proximas: Int = 0,
    val confirmadas: Int = 0,
    val realizadas: Int = 0
)

sealed interface VisitasFamiliarUiState {
    object Loading : VisitasFamiliarUiState
    data class Success(
        val listaResidentes: List<Residente>,
        val residenteSeleccionado: Residente?,
        val proximaVisita: Visita?,
        val listaCompleta: List<Visita>,
        val listaFiltrada: List<Visita>,
        val kpi: ResumenVisitasFamiliarKpi = ResumenVisitasFamiliarKpi(),
        val filtroTab: FiltroFamiliarVisitaTab = FiltroFamiliarVisitaTab.TODAS,
        val searchQuery: String = ""
    ) : VisitasFamiliarUiState
    data class Error(val message: String) : VisitasFamiliarUiState
}

sealed interface VisitaFamiliarDetailUiState {
    object Idle : VisitaFamiliarDetailUiState
    object Loading : VisitaFamiliarDetailUiState
    data class Success(val visita: Visita) : VisitaFamiliarDetailUiState
    data class Error(val message: String) : VisitaFamiliarDetailUiState
}

class VisitasFamiliarViewModel(
    private val repository: VisitasRepository = VisitasRepository(),
    private val familiarRepository: FamiliarRepository = FamiliarRepository(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow<VisitasFamiliarUiState>(VisitasFamiliarUiState.Loading)
    val uiState: StateFlow<VisitasFamiliarUiState> = _uiState.asStateFlow()

    private val _detailState = MutableStateFlow<VisitaFamiliarDetailUiState>(VisitaFamiliarDetailUiState.Idle)
    val detailState: StateFlow<VisitaFamiliarDetailUiState> = _detailState.asStateFlow()

    private var todasLasVisitas: List<Visita> = emptyList()
    private var listaResidentesVinculados: List<Residente> = emptyList()
    private var residenteActual: Residente? = null
    private var idFamiliarActual: Int = 0

    private var filtroTabActual = FiltroFamiliarVisitaTab.TODAS
    private var searchQueryActual = ""

    init {
        cargarDatosYEscuchar()
    }

    fun cargarDatosYEscuchar() {
        viewModelScope.launch {
            _uiState.value = VisitasFamiliarUiState.Loading
            try {
                val currentUserId = UserSessionManager.getUserId()

                // Resolver residentes y parentesco solo desde vinculos del id_usuario autenticado.
                val vinculados = familiarRepository.obtenerResidentesVinculados(currentUserId)
                listaResidentesVinculados = vinculados.map { it.residente }
                idFamiliarActual = vinculados.firstOrNull()?.idFamiliar ?: currentUserId

                if (residenteActual == null && listaResidentesVinculados.isNotEmpty()) {
                    residenteActual = listaResidentesVinculados.first()
                }

                // No consultar visitas si la cuenta no tiene residentes autorizados.
                if (listaResidentesVinculados.isNotEmpty()) escucharVisitas() else aplicarFiltros()

            } catch (e: Exception) {
                listaResidentesVinculados = emptyList()
                residenteActual = null
                idFamiliarActual = 0
                todasLasVisitas = emptyList()
                _uiState.value = VisitasFamiliarUiState.Error("No fue posible cargar tus residentes vinculados.")
            }
        }
    }

    private fun escucharVisitas() {
        viewModelScope.launch {
            repository.obtenerVisitasEnTiempoReal()
                .catch {
                    _uiState.value = VisitasFamiliarUiState.Error("No fue posible cargar tus visitas.")
                }
                .collect { listaDirecta ->
                    val idsResidentesAutorizados = listaResidentesVinculados.map { it.id_residente }.toSet()
                    val filtradasPorFamiliar = listaDirecta.filter { it.id_residente in idsResidentesAutorizados }

                    todasLasVisitas = filtradasPorFamiliar
                    aplicarFiltros()
                }
        }
    }

    fun onSeleccionarResidente(residente: Residente) {
        residenteActual = residente
        aplicarFiltros()
    }

    fun onFiltroTabChange(tab: FiltroFamiliarVisitaTab) {
        filtroTabActual = tab
        aplicarFiltros()
    }

    fun onSearchQueryChange(query: String) {
        searchQueryActual = query
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        val hoy = LocalDate.now()

        var resultado = todasLasVisitas

        if (residenteActual != null) {
            resultado = resultado.filter { it.id_residente == residenteActual!!.id_residente }
        } else {
            resultado = emptyList()
        }

        if (searchQueryActual.isNotBlank()) {
            val q = searchQueryActual.trim().lowercase()
            resultado = resultado.filter { v ->
                v.fecha.lowercase().contains(q) || v.hora.lowercase().contains(q) || (v.nombre_residente ?: "").lowercase().contains(q)
            }
        }

        // Filtro por Tab
        resultado = when (filtroTabActual) {
            FiltroFamiliarVisitaTab.TODAS -> resultado
            FiltroFamiliarVisitaTab.PROXIMAS -> resultado.filter {
                try {
                    val d = LocalDate.parse(it.fecha.take(10))
                    !d.isBefore(hoy) && !it.estado.equals("Realizada", true) && !it.estado.equals("Cancelada", true)
                } catch (_: Exception) { true }
            }
            FiltroFamiliarVisitaTab.CONFIRMADAS -> resultado.filter { it.estado.equals("Confirmada", true) }
            FiltroFamiliarVisitaTab.REALIZADAS -> resultado.filter { it.estado.equals("Realizada", true) }
            FiltroFamiliarVisitaTab.CANCELADAS -> resultado.filter { it.estado.equals("Cancelada", true) }
        }

        // Determinar Próxima Visita
        val proxima = todasLasVisitas.filter {
            it.estado.equals("Programada", true) || it.estado.equals("Confirmada", true)
        }.minByOrNull { it.fecha }

        val proxCount = todasLasVisitas.count {
            it.estado.equals("Programada", true) || it.estado.equals("Confirmada", true)
        }
        val confCount = todasLasVisitas.count { it.estado.equals("Confirmada", true) }
        val realCount = todasLasVisitas.count { it.estado.equals("Realizada", true) }

        val kpi = ResumenVisitasFamiliarKpi(
            proximas = proxCount,
            confirmadas = confCount,
            realizadas = realCount
        )

        _uiState.value = VisitasFamiliarUiState.Success(
            listaResidentes = listaResidentesVinculados,
            residenteSeleccionado = residenteActual,
            proximaVisita = proxima,
            listaCompleta = todasLasVisitas,
            listaFiltrada = resultado,
            kpi = kpi,
            filtroTab = filtroTabActual,
            searchQuery = searchQueryActual
        )
    }

    fun obtenerVisita(idVisita: Int) {
        viewModelScope.launch {
            _detailState.value = VisitaFamiliarDetailUiState.Loading
            try {
                val visita = repository.obtenerVisitaPorId(idVisita)
                    ?: todasLasVisitas.find { it.id_visita == idVisita }

                if (visita != null) {
                    _detailState.value = VisitaFamiliarDetailUiState.Success(visita)
                } else {
                    _detailState.value = VisitaFamiliarDetailUiState.Error("Visita no encontrada.")
                }
            } catch (e: Exception) {
                val local = todasLasVisitas.find { it.id_visita == idVisita }
                if (local != null) {
                    _detailState.value = VisitaFamiliarDetailUiState.Success(local)
                } else {
                    _detailState.value = VisitaFamiliarDetailUiState.Error("Error al cargar la visita.")
                }
            }
        }
    }

    fun solicitarVisita(
        fecha: String,
        hora: String,
        numVisitantes: Int,
        observaciones: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val res = residenteActual?.takeIf { selected ->
                    listaResidentesVinculados.any { it.id_residente == selected.id_residente }
                } ?: throw IllegalStateException("No tienes un residente vinculado autorizado.")
                val familiarName = UserSessionManager.getUserFullName()

                val nextId = System.currentTimeMillis().toInt() and 0x7FFFFFFF
                val nuevaVisita = mapOf(
                    "id_visita" to nextId,
                    "id_residente" to res.id_residente,
                    "id_familiar" to idFamiliarActual,
                    "fecha" to fecha,
                    "hora" to hora,
                    "numero_visitantes" to numVisitantes,
                    "estado" to "Programada",
                    "observaciones" to observaciones,
                    "fecha_creacion" to LocalDate.now().toString(),
                    "nombre_residente" to res.nombreCompleto(),
                    "nombre_familiar" to familiarName,
                    "habitacion" to res.habitacion
                )

                db.collection("visitas").document(nextId.toString()).set(nuevaVisita).await()
                onSuccess()
            } catch (_: Exception) { }
        }
    }
}
