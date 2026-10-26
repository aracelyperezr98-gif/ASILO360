package com.example.asilo360.admin.visitas

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.asilo360.data.firebase.FirebaseClient
import com.example.asilo360.data.model.CambiarEstadoVisitaRequest
import com.example.asilo360.data.model.FamiliarOpcion
import com.example.asilo360.data.model.Residente
import com.example.asilo360.data.model.Visita
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado UI para el módulo de Visitas.
 */
data class VisitaUiState(
    val visitasList: List<Visita> = emptyList(),
    val filteredVisitasList: List<Visita> = emptyList(),
    val residentesDisponibles: List<Residente> = emptyList(),
    val familiaresDelResidente: List<FamiliarOpcion> = emptyList(),
    val searchQuery: String = "",
    val statusFilter: String = "Todas",
    val isLoading: Boolean = false,
    val isOperating: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val selectedVisita: Visita? = null,
    val visitaToChangeStatus: Visita? = null
)

/**
 * ViewModel encargado de la lógica del módulo de Visitas del Administrador.
 */
class VisitaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(VisitaUiState())
    val uiState: StateFlow<VisitaUiState> = _uiState.asStateFlow()

    init {
        cargarVisitas()
        cargarResidentesAuxiliares()
    }

    /**
     * Carga las visitas agendadas (GET /visitas/listar.php).
     */
    fun cargarVisitas() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val response = FirebaseClient.visitasApiService.listarVisitas()
                val body = response.body()

                if (response.isSuccessful && body?.success == true && body.visitas != null) {
                    val lista = body.visitas
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            visitasList = lista,
                            filteredVisitasList = filtrarLista(lista, it.searchQuery, it.statusFilter),
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = body?.message ?: "No se pudieron cargar las visitas."
                        )
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error de red al listar visitas", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo conectar con Firestore. Revisa tu conexión a Internet."
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error inesperado al listar visitas", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Ocurrió un error inesperado al consultar las visitas."
                    )
                }
            }
        }
    }

    /**
     * Carga la lista de residentes activos para los selectores.
     */
    private fun cargarResidentesAuxiliares() {
        viewModelScope.launch {
            try {
                val resResp = FirebaseClient.residentesApiService.listarResidentes()
                val resBody = resResp.body()
                val residentesList = if (resResp.isSuccessful && resBody?.success == true && resBody.data != null) {
                    resBody.data
                } else emptyList()

                _uiState.update { it.copy(residentesDisponibles = residentesList) }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar residentes para visitas", e)
            }
        }
    }

    /**
     * Carga únicamente los familiares relacionados con el residente seleccionado.
     */
    fun cargarFamiliaresPorResidente(idResidente: Int) {
        viewModelScope.launch {
            try {
                val response = FirebaseClient.visitasApiService.obtenerFamiliaresPorResidente(idResidente)
                val body = response.body()

                if (response.isSuccessful && body?.success == true && body.familiares != null) {
                    _uiState.update { it.copy(familiaresDelResidente = body.familiares) }
                } else {
                    _uiState.update { it.copy(familiaresDelResidente = emptyList()) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al consultar familiares por residente", e)
                _uiState.update { it.copy(familiaresDelResidente = emptyList()) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredVisitasList = filtrarLista(it.visitasList, query, it.statusFilter)
            )
        }
    }

    fun onStatusFilterChange(status: String) {
        _uiState.update {
            it.copy(
                statusFilter = status,
                filteredVisitasList = filtrarLista(it.visitasList, it.searchQuery, status)
            )
        }
    }

    private fun filtrarLista(lista: List<Visita>, query: String, statusFilter: String): List<Visita> {
        var result = lista

        if (!statusFilter.equals("Todas", ignoreCase = true)) {
            result = result.filter { v -> v.estado.equals(statusFilter, ignoreCase = true) }
        }

        if (query.trim().isNotEmpty()) {
            val q = query.trim().lowercase()
            result = result.filter { v ->
                (v.nombre_residente?.lowercase()?.contains(q) == true) ||
                (v.nombre_familiar?.lowercase()?.contains(q) == true) ||
                (v.parentesco?.lowercase()?.contains(q) == true)
            }
        }

        return result
    }

    /**
     * Registra una nueva visita mediante la API (POST /visitas/crear.php).
     */
    fun crearVisita(visita: Visita, onSuccess: () -> Unit) {
        _uiState.update { it.copy(isOperating = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val response = FirebaseClient.visitasApiService.crearVisita(visita)
                val body = response.body()

                if (response.isSuccessful && body?.success == true) {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            successMessage = body.message ?: "Visita registrada correctamente."
                        )
                    }
                    cargarVisitas()
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            errorMessage = body?.message ?: "Error al registrar la visita."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isOperating = false,
                        errorMessage = "Error al guardar la visita en Firestore."
                    )
                }
            }
        }
    }

    /**
     * Actualiza una visita existente mediante la API (PUT /visitas/actualizar.php).
     */
    fun actualizarVisita(visita: Visita, onSuccess: () -> Unit) {
        _uiState.update { it.copy(isOperating = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val response = FirebaseClient.visitasApiService.actualizarVisita(visita)
                val body = response.body()

                if (response.isSuccessful && body?.success == true) {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            successMessage = body.message ?: "Visita actualizada correctamente."
                        )
                    }
                    cargarVisitas()
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            errorMessage = body?.message ?: "Error al actualizar la visita."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isOperating = false,
                        errorMessage = "Error al actualizar la visita en Firestore."
                    )
                }
            }
        }
    }

    fun solicitarCambioEstado(visita: Visita) {
        _uiState.update { it.copy(visitaToChangeStatus = visita) }
    }

    fun cancelarCambioEstado() {
        _uiState.update { it.copy(visitaToChangeStatus = null) }
    }

    /**
     * Confirma el cambio de estado (PUT /visitas/cambiar_estado.php).
     */
    fun confirmarCambioEstado(nuevoEstado: String) {
        val visita = _uiState.value.visitaToChangeStatus ?: return
        _uiState.update { it.copy(visitaToChangeStatus = null, isOperating = true) }

        viewModelScope.launch {
            try {
                val request = CambiarEstadoVisitaRequest(
                    id_visita = visita.id_visita,
                    estado = nuevoEstado
                )
                val response = FirebaseClient.visitasApiService.cambiarEstado(request)
                val body = response.body()

                if (response.isSuccessful && body?.success == true) {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            successMessage = body.message ?: "Estado actualizado correctamente."
                        )
                    }
                    cargarVisitas()
                } else {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            errorMessage = body?.message ?: "Error al cambiar el estado de la visita."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isOperating = false,
                        errorMessage = "Error al cambiar el estado de la visita en Firestore."
                    )
                }
            }
        }
    }

    fun selectVisitaForDetail(visita: Visita?) {
        _uiState.update { it.copy(selectedVisita = visita) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private companion object {
        const val TAG = "VisitaViewModel"
    }
}
