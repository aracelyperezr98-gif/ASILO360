package com.example.asilo360.admin.notificaciones

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.asilo360.data.firebase.FirebaseClient
import com.example.asilo360.data.model.CrearNotificacionRequest
import com.example.asilo360.data.model.MarcarLeidaRequest
import com.example.asilo360.data.model.Notificacion
import com.example.asilo360.data.model.UsuarioDestinatarioOpcion
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado UI para el módulo de Notificaciones.
 */
data class NotificacionUiState(
    val notificacionesList: List<Notificacion> = emptyList(),
    val filteredNotificacionesList: List<Notificacion> = emptyList(),
    val usuariosDisponibles: List<UsuarioDestinatarioOpcion> = emptyList(),
    val searchQuery: String = "",
    val statusFilter: String = "Todas",
    val typeFilter: String = "Todos",
    val isLoading: Boolean = false,
    val isOperating: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val selectedNotificacion: Notificacion? = null,
    val notificacionToDelete: Notificacion? = null
)

/**
 * ViewModel encargado de la gestión de notificaciones.
 */
class NotificacionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NotificacionUiState())
    val uiState: StateFlow<NotificacionUiState> = _uiState.asStateFlow()

    init {
        cargarNotificaciones()
        cargarUsuariosAuxiliares()
    }

    /**
     * Carga las notificaciones desde la API (GET /notificaciones/listar.php).
     */
    fun cargarNotificaciones() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val response = FirebaseClient.notificacionesApiService.listarNotificaciones()
                val body = response.body()

                if (response.isSuccessful && body?.success == true && body.notificaciones != null) {
                    val lista = body.notificaciones
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notificacionesList = lista,
                            filteredNotificacionesList = filtrarLista(lista, it.searchQuery, it.statusFilter, it.typeFilter),
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = body?.message ?: "No se pudieron cargar las notificaciones."
                        )
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error de red al listar notificaciones", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo conectar con Firestore. Revisa tu conexión a Internet."
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error inesperado al listar notificaciones", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Ocurrió un error inesperado al consultar las notificaciones."
                    )
                }
            }
        }
    }

    /**
     * Carga la lista de usuarios para el selector de destinatarios.
     */
    private fun cargarUsuariosAuxiliares() {
        viewModelScope.launch {
            try {
                val response = FirebaseClient.apiService.listarUsuarios()
                val body = response.body()

                if (response.isSuccessful && body?.success == true && body.data != null) {
                    val listaUsuarios = body.data.map { u ->
                        val nombreComp = "${u.nombre} ${u.apellido_paterno ?: ""}".trim()
                        UsuarioDestinatarioOpcion(
                            id_usuario = u.id_usuario,
                            nombre = nombreComp,
                            rol = u.rol
                        )
                    }
                    _uiState.update { it.copy(usuariosDisponibles = listaUsuarios) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar usuarios auxiliares para notificaciones", e)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredNotificacionesList = filtrarLista(it.notificacionesList, query, it.statusFilter, it.typeFilter)
            )
        }
    }

    fun onStatusFilterChange(status: String) {
        _uiState.update {
            it.copy(
                statusFilter = status,
                filteredNotificacionesList = filtrarLista(it.notificacionesList, it.searchQuery, status, it.typeFilter)
            )
        }
    }

    fun onTypeFilterChange(type: String) {
        _uiState.update {
            it.copy(
                typeFilter = type,
                filteredNotificacionesList = filtrarLista(it.notificacionesList, it.searchQuery, it.statusFilter, type)
            )
        }
    }

    private fun filtrarLista(
        lista: List<Notificacion>,
        query: String,
        statusFilter: String,
        typeFilter: String
    ): List<Notificacion> {
        var result = lista

        // Filtrar por leída / no leída
        if (statusFilter.equals("No leídas", ignoreCase = true)) {
            result = result.filter { !it.leida }
        } else if (statusFilter.equals("Leídas", ignoreCase = true)) {
            result = result.filter { it.leida }
        }

        // Filtrar por tipo
        if (!typeFilter.equals("Todos", ignoreCase = true)) {
            result = result.filter { it.tipo.equals(typeFilter, ignoreCase = true) }
        }

        // Búsqueda por texto
        if (query.trim().isNotEmpty()) {
            val q = query.trim().lowercase()
            result = result.filter { n ->
                (n.destinatario?.lowercase()?.contains(q) == true) ||
                n.titulo.lowercase().contains(q) ||
                n.mensaje.lowercase().contains(q) ||
                n.tipo.lowercase().contains(q)
            }
        }

        return result
    }

    /**
     * Envía una nueva notificación (POST /notificaciones/crear.php)
     * y genera una notificación real en la barra de estado del teléfono.
     */
    fun crearNotificacion(
        context: android.content.Context,
        request: CrearNotificacionRequest,
        onSuccess: () -> Unit
    ) {
        _uiState.update { it.copy(isOperating = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val response = FirebaseClient.notificacionesApiService.crearNotificacion(request)
                val body = response.body()

                if (response.isSuccessful && body?.success == true) {
                    // Generar notificación real en la barra de estado del teléfono
                    com.example.asilo360.data.notification.NotificationHelper.mostrarNotificacionSistema(
                        context = context,
                        titulo = request.titulo,
                        mensaje = request.mensaje
                    )

                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            successMessage = body.message ?: "Notificación enviada correctamente."
                        )
                    }
                    cargarNotificaciones()
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            errorMessage = body?.message ?: "Error al enviar la notificación."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isOperating = false,
                        errorMessage = "Error al guardar la notificación en Firestore."
                    )
                }
            }
        }
    }

    /**
     * Marca una notificación como leída (PUT /notificaciones/marcar_leida.php).
     */
    fun marcarLeida(idNotificacion: Int) {
        _uiState.update { it.copy(isOperating = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val response = FirebaseClient.notificacionesApiService.marcarLeida(MarcarLeidaRequest(idNotificacion))
                val body = response.body()

                if (response.isSuccessful && body?.success == true) {
                    _uiState.update { state ->
                        val updatedList = state.notificacionesList.map { n ->
                            if (n.id_notificacion == idNotificacion) n.copy(leida = true) else n
                        }
                        val selected = state.selectedNotificacion?.let {
                            if (it.id_notificacion == idNotificacion) it.copy(leida = true) else it
                        }
                        state.copy(
                            isOperating = false,
                            notificacionesList = updatedList,
                            filteredNotificacionesList = filtrarLista(updatedList, state.searchQuery, state.statusFilter, state.typeFilter),
                            selectedNotificacion = selected,
                            successMessage = "Notificación marcada como leída."
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            errorMessage = body?.message ?: "Error al marcar como leída."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isOperating = false,
                        errorMessage = "Error al actualizar la notificación en Firestore."
                    )
                }
            }
        }
    }

    fun solicitarEliminacion(notificacion: Notificacion) {
        _uiState.update { it.copy(notificacionToDelete = notificacion) }
    }

    fun cancelarEliminacion() {
        _uiState.update { it.copy(notificacionToDelete = null) }
    }

    /**
     * Confirma la eliminación (DELETE /notificaciones/eliminar.php?id=X).
     */
    fun confirmarEliminacion(onSuccess: () -> Unit) {
        val notificacion = _uiState.value.notificacionToDelete ?: return
        _uiState.update { it.copy(notificacionToDelete = null, isOperating = true) }

        viewModelScope.launch {
            try {
                val response = FirebaseClient.notificacionesApiService.eliminarNotificacion(notificacion.id_notificacion)
                val body = response.body()

                if (response.isSuccessful && body?.success == true) {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            successMessage = body.message ?: "Notificación eliminada correctamente."
                        )
                    }
                    cargarNotificaciones()
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            errorMessage = body?.message ?: "Error al eliminar la notificación."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isOperating = false,
                        errorMessage = "Error al eliminar la notificación de Firestore."
                    )
                }
            }
        }
    }

    fun selectNotificacionForDetail(notificacion: Notificacion?) {
        _uiState.update { it.copy(selectedNotificacion = notificacion) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private companion object {
        const val TAG = "NotificacionViewModel"
    }
}
