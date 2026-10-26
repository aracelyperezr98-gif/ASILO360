package com.example.asilo360.ui.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.asilo360.data.model.ResidenteVinculado
import com.example.asilo360.data.repository.FamiliarRepository
import com.example.asilo360.data.session.UserSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado UI del Dashboard para el rol Familiar.
 */
data class FamiliarDashboardUiState(
    val isLoading: Boolean = true,
    val familiarNombre: String = "",
    val vinculado: ResidenteVinculado? = null,
    val tieneResidentes: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel para conectar el Dashboard de Familiar con Firestore en tiempo real.
 */
class FamiliarDashboardViewModel(
    private val repository: FamiliarRepository = FamiliarRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FamiliarDashboardUiState())
    val uiState: StateFlow<FamiliarDashboardUiState> = _uiState.asStateFlow()

    init {
        cargarDatosDashboard()
    }

    fun cargarDatosDashboard() {
        val currentUserId = UserSessionManager.getUserId()
        val nombre = UserSessionManager.getUserFullName()

        _uiState.update {
            it.copy(
                isLoading = true,
                familiarNombre = nombre.ifBlank { "Familiar" },
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val lista = repository.obtenerResidentesVinculados(currentUserId)
                val primerVinculado = lista.firstOrNull()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        vinculado = primerVinculado,
                        tieneResidentes = lista.isNotEmpty(),
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar dashboard del familiar", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No fue posible actualizar el resumen desde la base de datos."
                    )
                }
            }
        }
    }

    companion object {
        private const val TAG = "FamiliarDashboardViewModel"
    }
}
