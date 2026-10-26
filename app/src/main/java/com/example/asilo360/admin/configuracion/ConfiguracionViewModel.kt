package com.example.asilo360.admin.configuracion

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.asilo360.data.firebase.FirebaseClient
import com.example.asilo360.data.model.ActualizarPerfilRequest
import com.example.asilo360.data.model.CambiarPasswordRequest
import com.example.asilo360.data.model.PerfilUsuario
import com.example.asilo360.data.model.PreferenciasGuardadas
import com.example.asilo360.data.session.UserSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado UI para la pantalla y subpantallas del módulo de Configuración.
 */
data class ConfiguracionUiState(
    val perfil: PerfilUsuario = PerfilUsuario(),
    val preferencias: PreferenciasGuardadas = PreferenciasGuardadas(),
    val isLoading: Boolean = false,
    val isOperating: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel encargado de la gestión de perfil, preferencias, cambio de contraseña y cierre de sesión.
 */
class ConfiguracionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ConfiguracionUiState())
    val uiState: StateFlow<ConfiguracionUiState> = _uiState.asStateFlow()

    init {
        obtenerPerfil()
    }

    /**
     * Obtiene el perfil actualizado del usuario desde la API.
     */
    fun obtenerPerfil() {
        val idUsuario = UserSessionManager.getUserId()
        if (idUsuario <= 0) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val response = FirebaseClient.configuracionApiService.obtenerPerfil(idUsuario)
                val body = response.body()

                if (response.isSuccessful && body?.success == true && body.usuario != null) {
                    val usr = body.usuario
                    UserSessionManager.saveSession(
                        com.example.asilo360.data.model.Usuario(
                            id_usuario = usr.id_usuario,
                            nombre = usr.nombre,
                            apellido_paterno = usr.apellido_paterno,
                            apellido_materno = usr.apellido_materno,
                            correo = usr.correo,
                            id_rol = usr.id_rol,
                            rol = usr.rol,
                            telefono = usr.telefono
                        )
                    )
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            perfil = usr,
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = body?.message ?: "No se pudo cargar la información del perfil."
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al consultar perfil", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error de conexión al cargar la información del perfil."
                    )
                }
            }
        }
    }

    /**
     * Actualiza la información del perfil del Administrador.
     */
    fun actualizarPerfil(request: ActualizarPerfilRequest, onSuccess: () -> Unit) {
        _uiState.update { it.copy(isOperating = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val response = FirebaseClient.configuracionApiService.actualizarPerfil(request)
                val body = response.body()

                if (response.isSuccessful && body?.success == true && body.usuario != null) {
                    val usr = body.usuario
                    UserSessionManager.saveSession(
                        com.example.asilo360.data.model.Usuario(
                            id_usuario = usr.id_usuario,
                            nombre = usr.nombre,
                            apellido_paterno = usr.apellido_paterno,
                            apellido_materno = usr.apellido_materno,
                            correo = usr.correo,
                            id_rol = usr.id_rol,
                            rol = usr.rol,
                            telefono = usr.telefono
                        )
                    )
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            perfil = usr,
                            successMessage = body.message ?: "Cambios guardados correctamente"
                        )
                    }
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            errorMessage = body?.message ?: "Error al guardar los cambios de perfil."
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al actualizar perfil", e)
                _uiState.update {
                    it.copy(
                        isOperating = false,
                        errorMessage = "Error al guardar los cambios del perfil en Firebase."
                    )
                }
            }
        }
    }

    /**
     * Cambia la contraseña del Administrador.
     */
    fun cambiarPassword(passwordActual: String, passwordNueva: String, onSuccess: () -> Unit) {
        val idUsuario = UserSessionManager.getUserId()
        if (idUsuario <= 0) return

        _uiState.update { it.copy(isOperating = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val req = CambiarPasswordRequest(
                    id_usuario = idUsuario,
                    password_actual = passwordActual,
                    password_nueva = passwordNueva
                )
                val response = FirebaseClient.configuracionApiService.cambiarPassword(req)
                val body = response.body()

                if (response.isSuccessful && body?.success == true) {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            successMessage = body.message ?: "Contraseña actualizada correctamente"
                        )
                    }
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            errorMessage = body?.message ?: "Contraseña actual incorrecta."
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cambiar contraseña", e)
                _uiState.update {
                    it.copy(
                        isOperating = false,
                        errorMessage = "Error al cambiar la contraseña en Firebase Authentication."
                    )
                }
            }
        }
    }

    fun updateNotificaciones(active: Boolean) {
        _uiState.update { it.copy(preferencias = it.preferencias.copy(notificacionesActivas = active)) }
    }

    fun updateSonido(active: Boolean) {
        _uiState.update { it.copy(preferencias = it.preferencias.copy(sonidoNotificaciones = active)) }
    }

    fun updateConfirmar(active: Boolean) {
        _uiState.update { it.copy(preferencias = it.preferencias.copy(confirmarAcciones = active)) }
    }

    fun updateAutoUpdate(active: Boolean) {
        _uiState.update { it.copy(preferencias = it.preferencias.copy(actualizarAutomaticamente = active)) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private companion object {
        const val TAG = "ConfiguracionViewModel"
    }
}
