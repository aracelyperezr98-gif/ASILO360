package com.example.asilo360.admin.pagos

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.asilo360.data.firebase.FirebaseClient
import com.example.asilo360.data.model.CambiarEstadoPagoRequest
import com.example.asilo360.data.model.Pago
import com.example.asilo360.data.model.Residente
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado UI para la gestión de pagos.
 */
data class PagoUiState(
    val pagosList: List<Pago> = emptyList(),
    val filteredPagosList: List<Pago> = emptyList(),
    val residentesDisponibles: List<Residente> = emptyList(),
    val searchQuery: String = "",
    val statusFilter: String = "Todos",
    val isLoading: Boolean = false,
    val isOperating: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val selectedPago: Pago? = null,
    val pagoToChangeStatus: Pago? = null,
    val pagoToDelete: Pago? = null
)

/**
 * ViewModel encargado del módulo de Pagos del Administrador.
 */
class PagoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PagoUiState())
    val uiState: StateFlow<PagoUiState> = _uiState.asStateFlow()

    init {
        cargarPagos()
        cargarResidentesAuxiliares()
    }

    /**
     * Carga la lista de pagos desde la API (GET /pagos/listar.php).
     */
    fun cargarPagos() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val response = FirebaseClient.pagosApiService.listarPagos()
                val body = response.body()

                if (response.isSuccessful && body?.success == true && body.pagos != null) {
                    val lista = body.pagos
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pagosList = lista,
                            filteredPagosList = filtrarLista(lista, it.searchQuery, it.statusFilter),
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = body?.message ?: "No se pudieron cargar los pagos."
                        )
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error de red al listar pagos", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo conectar con Firestore. Revisa tu conexión a Internet."
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error inesperado al listar pagos", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Ocurrió un error inesperado al cargar los pagos."
                    )
                }
            }
        }
    }

    /**
     * Carga la lista de residentes activos para el selector del formulario.
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
                Log.e(TAG, "Error al cargar residentes para el módulo de pagos", e)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredPagosList = filtrarLista(it.pagosList, query, it.statusFilter)
            )
        }
    }

    fun onStatusFilterChange(status: String) {
        _uiState.update {
            it.copy(
                statusFilter = status,
                filteredPagosList = filtrarLista(it.pagosList, it.searchQuery, status)
            )
        }
    }

    private fun filtrarLista(lista: List<Pago>, query: String, statusFilter: String): List<Pago> {
        var result = lista

        if (!statusFilter.equals("Todos", ignoreCase = true)) {
            result = result.filter { p -> p.estado.equals(statusFilter, ignoreCase = true) }
        }

        if (query.trim().isNotEmpty()) {
            val q = query.trim().lowercase()
            result = result.filter { p ->
                (p.residente?.lowercase()?.contains(q) == true) ||
                p.concepto.lowercase().contains(q) ||
                p.metodo_pago.lowercase().contains(q) ||
                p.estado.lowercase().contains(q)
            }
        }

        return result
    }

    /**
     * Registra un pago mediante la API (POST /pagos/crear.php).
     */
    fun crearPago(pago: Pago, onSuccess: () -> Unit) {
        _uiState.update { it.copy(isOperating = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val response = FirebaseClient.pagosApiService.crearPago(pago)
                val body = response.body()

                if (response.isSuccessful && body?.success == true) {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            successMessage = body.message ?: "Pago registrado correctamente."
                        )
                    }
                    cargarPagos()
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            errorMessage = body?.message ?: "Error al registrar el pago."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isOperating = false,
                        errorMessage = "Error al guardar el pago en Firestore."
                    )
                }
            }
        }
    }

    /**
     * Actualiza un pago existente mediante la API (PUT /pagos/actualizar.php).
     */
    fun actualizarPago(pago: Pago, onSuccess: () -> Unit) {
        _uiState.update { it.copy(isOperating = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val response = FirebaseClient.pagosApiService.actualizarPago(pago)
                val body = response.body()

                if (response.isSuccessful && body?.success == true) {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            successMessage = body.message ?: "Pago actualizado correctamente."
                        )
                    }
                    cargarPagos()
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            errorMessage = body?.message ?: "Error al actualizar el pago."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isOperating = false,
                        errorMessage = "Error al actualizar el pago en Firestore."
                    )
                }
            }
        }
    }

    fun solicitarCambioEstado(pago: Pago) {
        _uiState.update { it.copy(pagoToChangeStatus = pago) }
    }

    fun cancelarCambioEstado() {
        _uiState.update { it.copy(pagoToChangeStatus = null) }
    }

    /**
     * Confirma el cambio de estado (PUT /pagos/cambiar_estado.php).
     */
    fun confirmarCambioEstado(nuevoEstado: String) {
        val pago = _uiState.value.pagoToChangeStatus ?: return
        _uiState.update { it.copy(pagoToChangeStatus = null, isOperating = true) }

        viewModelScope.launch {
            try {
                val request = CambiarEstadoPagoRequest(
                    id_pago = pago.id_pago,
                    estado = nuevoEstado
                )
                val response = FirebaseClient.pagosApiService.cambiarEstado(request)
                val body = response.body()

                if (response.isSuccessful && body?.success == true) {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            successMessage = body.message ?: "Estado del pago actualizado correctamente."
                        )
                    }
                    cargarPagos()
                } else {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            errorMessage = body?.message ?: "Error al cambiar el estado del pago."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isOperating = false,
                        errorMessage = "Error al cambiar el estado del pago en Firestore."
                    )
                }
            }
        }
    }

    fun solicitarEliminacion(pago: Pago) {
        _uiState.update { it.copy(pagoToDelete = pago) }
    }

    fun cancelarEliminacion() {
        _uiState.update { it.copy(pagoToDelete = null) }
    }

    /**
     * Confirma y ejecuta la eliminación (DELETE /pagos/eliminar.php?id=X).
     */
    fun confirmarEliminacion(onSuccess: () -> Unit) {
        val pago = _uiState.value.pagoToDelete ?: return
        _uiState.update { it.copy(pagoToDelete = null, isOperating = true) }

        viewModelScope.launch {
            try {
                val response = FirebaseClient.pagosApiService.eliminarPago(pago.id_pago)
                val body = response.body()

                if (response.isSuccessful && body?.success == true) {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            successMessage = body.message ?: "Pago eliminado correctamente."
                        )
                    }
                    cargarPagos()
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            errorMessage = body?.message ?: "Error al eliminar el pago."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isOperating = false,
                        errorMessage = "Error al eliminar el pago de Firestore."
                    )
                }
            }
        }
    }

    fun selectPagoForDetail(pago: Pago?) {
        _uiState.update { it.copy(selectedPago = pago) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private companion object {
        const val TAG = "PagoViewModel"
    }
}
