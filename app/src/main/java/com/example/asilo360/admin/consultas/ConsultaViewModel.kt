package com.example.asilo360.admin.consultas

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.asilo360.data.firebase.FirebaseClient
import com.example.asilo360.data.model.CambiarEstadoConsultaRequest
import com.example.asilo360.data.model.Consulta
import com.example.asilo360.data.model.Personal
import com.example.asilo360.data.model.Residente
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado UI para el módulo de Consultas.
 */
data class ConsultaUiState(
    val consultasList: List<Consulta> = emptyList(),
    val filteredConsultasList: List<Consulta> = emptyList(),
    val residentesDisponibles: List<Residente> = emptyList(),
    val personalDisponible: List<Personal> = emptyList(),
    val searchQuery: String = "",
    val statusFilter: String = "Todas",
    val isLoading: Boolean = false,
    val isOperating: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val selectedConsulta: Consulta? = null,
    val consultaToChangeStatus: Consulta? = null
)

/**
 * ViewModel encargado del módulo de Consultas Médicas.
 */
class ConsultaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ConsultaUiState())
    val uiState: StateFlow<ConsultaUiState> = _uiState.asStateFlow()

    init {
        cargarConsultas()
        cargarCatalogosAuxiliares()
    }

    /**
     * Carga las consultas desde la API (GET /consultas/listar.php).
     */
    fun cargarConsultas() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val response = FirebaseClient.consultasApiService.listarConsultas()
                val body = response.body()

                if (response.isSuccessful && body?.success == true && body.consultas != null) {
                    val lista = body.consultas
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            consultasList = lista,
                            filteredConsultasList = filtrarLista(lista, it.searchQuery, it.statusFilter),
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = body?.message ?: "No se pudieron cargar las consultas."
                        )
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error de red al listar consultas", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo conectar con Firestore. Revisa tu conexión a Internet."
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error inesperado al listar consultas", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Ocurrió un error inesperado al cargar las consultas."
                    )
                }
            }
        }
    }

    /**
     * Carga residentes y personal para usarlos en los selectores del formulario.
     */
    private fun cargarCatalogosAuxiliares() {
        viewModelScope.launch {
            try {
                // Cargar residentes
                val resResp = FirebaseClient.residentesApiService.listarResidentes()
                val resBody = resResp.body()
                val residentesList = if (resResp.isSuccessful && resBody?.success == true && resBody.data != null) {
                    resBody.data
                } else emptyList()

                // Cargar personal
                val perResp = FirebaseClient.personalApiService.listarPersonal()
                val perBody = perResp.body()
                val personalList = if (perResp.isSuccessful && perBody?.success == true && perBody.personal != null) {
                    perBody.personal
                } else emptyList()

                _uiState.update {
                    it.copy(
                        residentesDisponibles = residentesList,
                        personalDisponible = personalList
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar catálogos auxiliares", e)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredConsultasList = filtrarLista(it.consultasList, query, it.statusFilter)
            )
        }
    }

    fun onStatusFilterChange(status: String) {
        _uiState.update {
            it.copy(
                statusFilter = status,
                filteredConsultasList = filtrarLista(it.consultasList, it.searchQuery, status)
            )
        }
    }

    private fun filtrarLista(lista: List<Consulta>, query: String, statusFilter: String): List<Consulta> {
        var result = lista

        // Filtrar por estado
        if (!statusFilter.equals("Todas", ignoreCase = true)) {
            result = result.filter { c ->
                c.estado.equals(statusFilter, ignoreCase = true)
            }
        }

        // Filtrar por texto de búsqueda
        if (query.trim().isNotEmpty()) {
            val q = query.trim().lowercase()
            result = result.filter { c ->
                (c.nombre_residente?.lowercase()?.contains(q) == true) ||
                (c.nombre_personal?.lowercase()?.contains(q) == true) ||
                c.motivo.lowercase().contains(q)
            }
        }

        return result
    }

    /**
     * Registra una nueva consulta mediante la API (POST /consultas/crear.php).
     */
    fun crearConsulta(consulta: Consulta, onSuccess: () -> Unit) {
        _uiState.update { it.copy(isOperating = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val response = FirebaseClient.consultasApiService.crearConsulta(consulta)
                val body = response.body()

                if (response.isSuccessful && body?.success == true) {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            successMessage = body.message ?: "Consulta registrada correctamente."
                        )
                    }
                    cargarConsultas()
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            errorMessage = body?.message ?: "Error al registrar la consulta."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isOperating = false,
                        errorMessage = "Error al guardar la consulta en Firestore."
                    )
                }
            }
        }
    }

    /**
     * Actualiza una consulta mediante la API (PUT /consultas/actualizar.php).
     */
    fun actualizarConsulta(consulta: Consulta, onSuccess: () -> Unit) {
        _uiState.update { it.copy(isOperating = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val response = FirebaseClient.consultasApiService.actualizarConsulta(consulta)
                val body = response.body()

                if (response.isSuccessful && body?.success == true) {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            successMessage = body.message ?: "Consulta actualizada correctamente."
                        )
                    }
                    cargarConsultas()
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            errorMessage = body?.message ?: "Error al actualizar la consulta."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isOperating = false,
                        errorMessage = "Error al actualizar la consulta en Firestore."
                    )
                }
            }
        }
    }

    /**
     * Solicita cambio de estado para una consulta.
     */
    fun solicitarCambioEstado(consulta: Consulta) {
        _uiState.update { it.copy(consultaToChangeStatus = consulta) }
    }

    fun cancelarCambioEstado() {
        _uiState.update { it.copy(consultaToChangeStatus = null) }
    }

    /**
     * Confirma el cambio de estado (PUT /consultas/cambiar_estado.php).
     */
    fun confirmarCambioEstado(nuevoEstado: String) {
        val consulta = _uiState.value.consultaToChangeStatus ?: return
        _uiState.update { it.copy(consultaToChangeStatus = null, isOperating = true) }

        viewModelScope.launch {
            try {
                val request = CambiarEstadoConsultaRequest(
                    id_consulta = consulta.id_consulta,
                    estado = nuevoEstado
                )
                val response = FirebaseClient.consultasApiService.cambiarEstado(request)
                val body = response.body()

                if (response.isSuccessful && body?.success == true) {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            successMessage = body.message ?: "Estado de la consulta actualizado correctamente."
                        )
                    }
                    cargarConsultas()
                } else {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            errorMessage = body?.message ?: "Error al actualizar el estado de la consulta."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isOperating = false,
                        errorMessage = "Error al cambiar el estado de la consulta en Firestore."
                    )
                }
            }
        }
    }

    fun selectConsultaForDetail(consulta: Consulta?) {
        _uiState.update { it.copy(selectedConsulta = consulta) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private companion object {
        const val TAG = "ConsultaViewModel"
    }
}
