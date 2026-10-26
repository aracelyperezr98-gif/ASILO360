package com.example.asilo360.admin.residentes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.asilo360.data.firebase.FirebaseClient
import com.example.asilo360.data.model.Residente
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado UI para la gestión de residentes.
 */
data class ResidenteUiState(
    val residentes: List<Residente> = emptyList(),
    val filteredResidentes: List<Residente> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isOperating: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val selectedResidente: Residente? = null,
    val residenteToDelete: Residente? = null
)

/**
 * ViewModel encargado de la gestión de residentes por parte del Administrador.
 */
class ResidenteViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ResidenteUiState())
    val uiState: StateFlow<ResidenteUiState> = _uiState.asStateFlow()

    init {
        cargarResidentes()
    }

    /**
    * Obtiene la lista de residentes desde Cloud Firestore.
     */
    fun cargarResidentes() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val response = FirebaseClient.residentesApiService.listarResidentes()
                val body = response.body()

                if (response.isSuccessful && body?.success == true && body.data != null) {
                    val lista = body.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            residentes = lista,
                            filteredResidentes = filtrarLista(lista, it.searchQuery),
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = body?.message ?: "No se pudieron cargar los residentes."
                        )
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error de red al listar residentes", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo conectar con Firestore. Revisa tu conexión a Internet."
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error inesperado al listar residentes", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Ocurrió un error inesperado al cargar la lista de residentes."
                    )
                }
            }
        }
    }

    /**
     * Filtra la lista de residentes en memoria por nombre o apellidos.
     */
    fun onSearchQueryChange(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredResidentes = filtrarLista(it.residentes, query)
            )
        }
    }

    private fun filtrarLista(lista: List<Residente>, query: String): List<Residente> {
        if (query.trim().isEmpty()) return lista
        val q = query.trim().lowercase()
        return lista.filter { r ->
            r.nombre.lowercase().contains(q) ||
            r.apellido_paterno.lowercase().contains(q) ||
            (r.apellido_materno?.lowercase()?.contains(q) == true)
        }
    }

    /**
     * Registra un nuevo residente mediante la API (POST).
     */
    fun crearResidente(residente: Residente, onSuccess: () -> Unit) {
        _uiState.update { it.copy(isOperating = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val response = FirebaseClient.residentesApiService.crearResidente(residente)
                val body = response.body()

                if (response.isSuccessful && body?.success == true) {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            successMessage = body.message ?: "Residente registrado correctamente."
                        )
                    }
                    cargarResidentes()
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            errorMessage = body?.message ?: "Error al registrar el residente."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isOperating = false,
                        errorMessage = "Error al guardar el residente en Firestore."
                    )
                }
            }
        }
    }

    /**
     * Actualiza los datos de un residente existente mediante la API (PUT).
     */
    fun actualizarResidente(residente: Residente, onSuccess: () -> Unit) {
        _uiState.update { it.copy(isOperating = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val response = FirebaseClient.residentesApiService.actualizarResidente(residente)
                val body = response.body()

                if (response.isSuccessful && body?.success == true) {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            successMessage = body.message ?: "Residente actualizado correctamente."
                        )
                    }
                    cargarResidentes()
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            errorMessage = body?.message ?: "Error al actualizar el residente."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isOperating = false,
                        errorMessage = "Error al actualizar el residente en Firestore."
                    )
                }
            }
        }
    }

    /**
     * Solicita la confirmación de eliminación para un residente.
     */
    fun solicitarEliminacion(residente: Residente) {
        _uiState.update { it.copy(residenteToDelete = residente) }
    }

    fun cancelarEliminacion() {
        _uiState.update { it.copy(residenteToDelete = null) }
    }

    /**
     * Confirma y ejecuta la eliminación del residente mediante la API (DELETE).
     */
    fun confirmarEliminacion() {
        val residente = _uiState.value.residenteToDelete ?: return
        _uiState.update { it.copy(residenteToDelete = null, isOperating = true) }

        viewModelScope.launch {
            try {
                val response = FirebaseClient.residentesApiService.eliminarResidente(residente.id_residente)
                val body = response.body()

                if (response.isSuccessful && body?.success == true) {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            successMessage = body.message ?: "Residente eliminado correctamente."
                        )
                    }
                    cargarResidentes()
                } else {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            errorMessage = body?.message ?: "Error al eliminar el residente."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isOperating = false,
                        errorMessage = "Error al eliminar el residente de Firestore."
                    )
                }
            }
        }
    }

    fun selectResidenteForDetail(residente: Residente?) {
        _uiState.update { it.copy(selectedResidente = residente) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private companion object {
        const val TAG = "ResidenteViewModel"
    }
}
