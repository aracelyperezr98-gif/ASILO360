package com.example.asilo360.screens.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.asilo360.data.firebase.FirebaseAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RecuperarContrasenaUiState {
    object Idle : RecuperarContrasenaUiState
    object Loading : RecuperarContrasenaUiState
    data class Exito(val message: String) : RecuperarContrasenaUiState
    data class Error(val message: String) : RecuperarContrasenaUiState
}

class RecuperarContrasenaViewModel(
    private val authRepository: FirebaseAuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    private val _correo = MutableStateFlow("")
    val correo: StateFlow<String> = _correo.asStateFlow()

    private val _correoError = MutableStateFlow<String?>(null)
    val correoError: StateFlow<String?> = _correoError.asStateFlow()

    private val _uiState = MutableStateFlow<RecuperarContrasenaUiState>(RecuperarContrasenaUiState.Idle)
    val uiState: StateFlow<RecuperarContrasenaUiState> = _uiState.asStateFlow()

    fun onCorreoChange(value: String) {
        _correo.value = value
        _correoError.value = null
        if (_uiState.value !is RecuperarContrasenaUiState.Idle) {
            _uiState.value = RecuperarContrasenaUiState.Idle
        }
    }

    fun enviarInstrucciones() {
        val email = _correo.value.trim()
        if (email.isBlank()) {
            _correoError.value = "Por favor ingresa tu correo electrónico."
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _correoError.value = "Ingresa un correo electrónico válido (ej. usuario@dominio.com)."
            return
        }

        viewModelScope.launch {
            _uiState.value = RecuperarContrasenaUiState.Loading
            try {
                authRepository.enviarCorreoRecuperacion(email)
                _uiState.value = RecuperarContrasenaUiState.Exito(
                    "Si existe una cuenta asociada a este correo, recibirás instrucciones para restablecer tu contraseña. Revisa también tu carpeta de correo no deseado."
                )
            } catch (e: Exception) {
                // Mensaje neutral por privacidad y seguridad
                _uiState.value = RecuperarContrasenaUiState.Exito(
                    "Si existe una cuenta asociada a este correo, recibirás instrucciones para restablecer tu contraseña. Revisa también tu carpeta de correo no deseado."
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = RecuperarContrasenaUiState.Idle
    }
}
