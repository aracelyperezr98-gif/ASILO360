package com.example.asilo360.admin.personal

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.asilo360.data.firebase.FirebaseClient
import com.example.asilo360.data.model.CambiarEstadoRequest
import com.example.asilo360.data.model.Personal
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado UI para la pantalla de Administración de Personal.
 */
data class PersonalUiState(
    val personalList: List<Personal> = emptyList(),
    val filteredPersonalList: List<Personal> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isOperating: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val selectedPersonal: Personal? = null,
    val personalToToggleStatus: Personal? = null
)

/**
 * ViewModel que gestiona la lógica del módulo de Administración de Personal.
 */
class PersonalViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PersonalUiState())
    val uiState: StateFlow<PersonalUiState> = _uiState.asStateFlow()

    init {
        cargarPersonal()
    }

    /**
     * Consulta la lista del personal desde la API PHP (GET /personal/listar.php).
     */
    fun cargarPersonal() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val response = FirebaseClient.personalApiService.listarPersonal()
                val body = response.body()

                if (response.isSuccessful && body?.success == true && body.personal != null) {
                    val lista = body.personal
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            personalList = lista,
                            filteredPersonalList = filtrarLista(lista, it.searchQuery),
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = body?.message ?: "No se pudo cargar la lista del personal."
                        )
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error de conexión al cargar personal", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo conectar con Firestore. Revisa tu conexión a Internet."
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error inesperado al cargar personal", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Ocurrió un error inesperado al consultar el personal."
                    )
                }
            }
        }
    }

    /**
     * Filtra la lista en tiempo real por nombre, apellidos, puesto o correo.
     */
    fun onSearchQueryChange(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredPersonalList = filtrarLista(it.personalList, query)
            )
        }
    }

    private fun filtrarLista(lista: List<Personal>, query: String): List<Personal> {
        if (query.trim().isEmpty()) return lista
        val q = query.trim().lowercase()
        return lista.filter { p ->
            p.nombre.lowercase().contains(q) ||
            p.apellido_paterno.lowercase().contains(q) ||
            (p.apellido_materno?.lowercase()?.contains(q) == true) ||
            p.puesto.lowercase().contains(q) ||
            p.correo.lowercase().contains(q)
        }
    }

    /**
     * Registra un nuevo trabajador mediante la API (POST /personal/crear.php).
     */
    fun crearPersonal(personal: Personal, onSuccess: () -> Unit) {
        _uiState.update { it.copy(isOperating = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val response = FirebaseClient.personalApiService.crearPersonal(personal)
                val body = response.body()

                if (response.isSuccessful && body?.success == true) {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            successMessage = body.message ?: "Personal registrado correctamente."
                        )
                    }
                    cargarPersonal()
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            errorMessage = body?.message ?: "Error al registrar el personal."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isOperating = false,
                        errorMessage = e.message ?: "No se pudo crear el perfil de personal en Firebase."
                    )
                }
            }
        }
    }

    /**
     * Actualiza los datos de un trabajador existente mediante la API (PUT /personal/actualizar.php).
     */
    fun actualizarPersonal(personal: Personal, onSuccess: () -> Unit) {
        _uiState.update { it.copy(isOperating = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val response = FirebaseClient.personalApiService.actualizarPersonal(personal)
                val body = response.body()

                if (response.isSuccessful && body?.success == true) {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            successMessage = body.message ?: "Personal actualizado correctamente."
                        )
                    }
                    cargarPersonal()
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            errorMessage = body?.message ?: "Error al actualizar el personal."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isOperating = false,
                        errorMessage = "Error al actualizar el personal en Firestore."
                    )
                }
            }
        }
    }

    /**
     * Solicita la confirmación para activar / desactivar un trabajador.
     */
    fun solicitarCambioEstado(personal: Personal) {
        _uiState.update { it.copy(personalToToggleStatus = personal) }
    }

    fun cancelarCambioEstado() {
        _uiState.update { it.copy(personalToToggleStatus = null) }
    }

    /**
     * Confirma y ejecuta el cambio de estado (PUT /personal/cambiar_estado.php).
     */
    fun confirmarCambioEstado() {
        val personal = _uiState.value.personalToToggleStatus ?: return
        val nuevoEstado = if (personal.estado == 1) 0 else 1

        _uiState.update { it.copy(personalToToggleStatus = null, isOperating = true) }

        viewModelScope.launch {
            try {
                val request = CambiarEstadoRequest(
                    id_personal = personal.id_personal,
                    estado = nuevoEstado
                )
                val response = FirebaseClient.personalApiService.cambiarEstado(request)
                val body = response.body()

                if (response.isSuccessful && body?.success == true) {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            successMessage = body.message ?: "Estado actualizado correctamente."
                        )
                    }
                    cargarPersonal()
                } else {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            errorMessage = body?.message ?: "Error al cambiar el estado del trabajador."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isOperating = false,
                        errorMessage = "Error al cambiar el estado del trabajador en Firestore."
                    )
                }
            }
        }
    }

    fun selectPersonalForDetail(personal: Personal?) {
        _uiState.update { it.copy(selectedPersonal = personal) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private companion object {
        const val TAG = "PersonalViewModel"
    }
}
