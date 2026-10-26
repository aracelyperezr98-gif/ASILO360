package com.example.asilo360.admin.familiares

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.asilo360.data.model.Residente
import com.example.asilo360.data.model.ResidenteFamiliarVinculo
import com.example.asilo360.data.model.Usuario
import com.example.asilo360.data.repository.FamiliaresAdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FamiliaresUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val vinculos: List<ResidenteFamiliarVinculo> = emptyList(),
    val usuariosFamiliares: List<Usuario> = emptyList(),
    val residentes: List<Residente> = emptyList(),
    val searchQuery: String = "",
    val messageEvent: String? = null,
    val errorMessage: String? = null
)

class FamiliaresViewModel(
    private val repository: FamiliaresAdminRepository = FamiliaresAdminRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FamiliaresUiState())
    val uiState: StateFlow<FamiliaresUiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val vinculos = repository.obtenerTodosLosVinculos()
                val familiares = repository.obtenerUsuariosFamiliares()
                val residentes = repository.obtenerResidentes()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        vinculos = vinculos,
                        usuariosFamiliares = familiares,
                        residentes = residentes,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar familiares y vínculos", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudieron cargar los vínculos desde Firestore."
                    )
                }
            }
        }
    }

    fun vincularFamiliar(idResidente: Int, idUsuarioFamiliar: Int, parentesco: String) {
        if (idResidente <= 0 || idUsuarioFamiliar <= 0) {
            _uiState.update {
                it.copy(messageEvent = "Selecciona un residente y un familiar válidos.")
            }
            return
        }

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            try {
                repository.vincularResidenteFamiliar(idResidente, idUsuarioFamiliar, parentesco)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        messageEvent = "Familiar vinculado correctamente al residente."
                    )
                }
                cargarDatos()
            } catch (e: IllegalArgumentException) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        messageEvent = e.message ?: "Esta relación ya existe."
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al vincular familiar con residente", e)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        messageEvent = "No se pudo registrar la vinculación. Inténtalo de nuevo."
                    )
                }
            }
        }
    }

    fun desvincularFamiliar(docId: String) {
        if (docId.isBlank()) return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            try {
                repository.desvincularResidenteFamiliar(docId)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        messageEvent = "Vínculo retirado correctamente. La cuenta y el residente se conservan intactos."
                    )
                }
                cargarDatos()
            } catch (e: Exception) {
                Log.e(TAG, "Error al desvincular familiar", e)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        messageEvent = "No se pudo retirar el vínculo."
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun clearMessageEvent() {
        _uiState.update { it.copy(messageEvent = null) }
    }

    companion object {
        private const val TAG = "FamiliaresViewModel"
    }
}
