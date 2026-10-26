package com.example.asilo360.screens.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.asilo360.data.firebase.FirebaseAuthRepository
import com.example.asilo360.data.model.Usuario
import com.example.asilo360.data.session.UserSessionManager
import com.example.asilo360.model.UserRole
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado de la interfaz de usuario para la pantalla de inicio de sesión.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val roleError: String? = null,
    val selectedRole: UserRole? = UserRole.ADMINISTRADOR, // Rol por defecto: Administrador
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val usuario: Usuario? = null,
    val messageEvent: String? = null,
)

/**
 * ViewModel que gestiona la lógica de negocio y el estado del inicio de sesión.
 */
class LoginViewModel : ViewModel() {

    private val authRepository = FirebaseAuthRepository()
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(newEmail: String) {
        // Auto-detectar rol si el usuario escribe un correo conocido (admin2 es multi-rol)
        val inferredRole = when {
            newEmail.contains("admin2", ignoreCase = true) -> _uiState.value.selectedRole ?: UserRole.ADMINISTRADOR
            newEmail.contains("personal", ignoreCase = true) -> UserRole.PERSONAL
            newEmail.contains("familiar", ignoreCase = true) -> UserRole.FAMILIAR
            newEmail.contains("admin", ignoreCase = true) -> UserRole.ADMINISTRADOR
            else -> _uiState.value.selectedRole ?: UserRole.ADMINISTRADOR
        }

        _uiState.update {
            it.copy(
                email = newEmail,
                selectedRole = inferredRole,
                emailError = if (newEmail.isNotBlank()) null else it.emailError
            )
        }
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.update {
            it.copy(
                password = newPassword,
                passwordError = if (newPassword.isNotBlank()) null else it.passwordError
            )
        }
    }

    fun setSelectedRole(role: UserRole?) {
        _uiState.update {
            it.copy(
                selectedRole = role ?: UserRole.ADMINISTRADOR,
                roleError = null
            )
        }
    }

    fun onLoginClick() {
        val currentState = _uiState.value
        val emailBlank = currentState.email.trim().isEmpty()
        val passwordBlank = currentState.password.trim().isEmpty()

        val emailErr = if (emailBlank) "Ingresa tu correo" else null
        val passwordErr = if (passwordBlank) "Ingresa tu contraseña" else null

        if (emailBlank || passwordBlank) {
            _uiState.update {
                it.copy(
                    emailError = emailErr,
                    passwordError = passwordErr,
                    messageEvent = "Ingresa tu correo y contraseña"
                )
            }
            return
        }

        if (_uiState.value.isLoading) return

        _uiState.update {
            it.copy(
                isLoading = true,
                success = false,
                usuario = null,
                messageEvent = null
            )
        }

        viewModelScope.launch {
            try {
                val usuario = authRepository.iniciarSesion(
                    correo = currentState.email.trim(),
                    password = currentState.password,
                    targetRoleId = currentState.selectedRole?.id
                )
                UserSessionManager.saveSession(usuario)
                val rolAutorizado = UserRole.entries.first { it.id == usuario.id_rol }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        success = true,
                        usuario = usuario,
                        selectedRole = rolAutorizado,
                        messageEvent = "Inicio de sesión correcto"
                    )
                }
            } catch (exception: FirebaseAuthException) {
                Log.e(TAG, "Error de autenticación con Firebase", exception)
                val message = when (exception.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "El formato del correo no es válido."
                    "ERROR_USER_NOT_FOUND", "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" ->
                        "Correo o contraseña incorrectos."
                    "ERROR_USER_DISABLED" -> "Esta cuenta está deshabilitada."
                    "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos. Espera un momento y vuelve a probar."
                    else -> "No se pudo autenticar con Firebase. Inténtalo de nuevo."
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        success = false,
                        usuario = null,
                        messageEvent = message
                    )
                }
            } catch (exception: FirebaseNetworkException) {
                Log.e(TAG, "Error de red durante el login con Firebase", exception)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        success = false,
                        usuario = null,
                        messageEvent = "No se pudo conectar con Firebase. Revisa tu conexión a Internet."
                    )
                }
            } catch (exception: FirebaseFirestoreException) {
                Log.e(TAG, "Error al leer el perfil en Firestore", exception)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        success = false,
                        usuario = null,
                        messageEvent = if (exception.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                            "Firebase no permite leer tu perfil. Revisa las reglas de Firestore."
                        } else {
                            "No se pudo cargar tu perfil desde Firestore."
                        }
                    )
                }
            } catch (exception: IllegalStateException) {
                Log.e(TAG, "Perfil de usuario no válido en Firestore", exception)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        success = false,
                        usuario = null,
                        messageEvent = exception.message ?: "No se encontró un perfil válido en Firestore."
                    )
                }
            } catch (exception: Exception) {
                Log.e(TAG, "Error inesperado durante el login con Firebase", exception)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        success = false,
                        usuario = null,
                        messageEvent = "Ocurrió un error inesperado al iniciar sesión."
                    )
                }
            }
        }
    }

    fun onForgotPasswordClick() {
        _uiState.update {
            it.copy(
                messageEvent = "La recuperación de contraseña estará disponible próximamente."
            )
        }
    }

    fun clearMessageEvent() {
        _uiState.update {
            it.copy(messageEvent = null)
        }
    }

    fun resetLoginState() {
        _uiState.value = LoginUiState()
        authRepository.cerrarSesion()
        UserSessionManager.clearSession()
    }

    private companion object {
        const val TAG = "Asilo360Login"
    }
}
