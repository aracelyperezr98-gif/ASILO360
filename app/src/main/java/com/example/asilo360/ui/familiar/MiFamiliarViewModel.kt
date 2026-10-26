package com.example.asilo360.ui.familiar

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
 * Estado UI para la pantalla "Mi familiar".
 */
data class MiFamiliarUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val residentesVinculados: List<ResidenteVinculado> = emptyList(),
    val selectedIndex: Int = 0,
    val usuarioNombre: String = "",
) {
    val selectedResidenteVinculado: ResidenteVinculado?
        get() = residentesVinculados.getOrNull(selectedIndex)

    val tieneMultiplesResidentes: Boolean
        get() = residentesVinculados.size > 1

    val estaVacio: Boolean
        get() = (!isLoading) && (errorMessage == null) && residentesVinculados.isEmpty()
}

/**
 * ViewModel que gestiona la consulta del residente vinculado al familiar autenticado.
 */
class MiFamiliarViewModel(
    private val familiarRepository: FamiliarRepository = FamiliarRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MiFamiliarUiState())
    val uiState: StateFlow<MiFamiliarUiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    /**
     * Carga la lista de residentes vinculados a la cuenta del familiar autenticado desde Firestore.
     */
    fun cargarDatos(isRefresh: Boolean = false) {
        val currentUserId = UserSessionManager.getUserId()
        val nombreUsuario = UserSessionManager.getUserFullName()

        _uiState.update {
            it.copy(
                isLoading = !isRefresh && it.residentesVinculados.isEmpty(),
                isRefreshing = isRefresh,
                errorMessage = null,
                usuarioNombre = nombreUsuario.ifBlank { "Familiar" }
            )
        }

        viewModelScope.launch {
            try {
                val lista = familiarRepository.obtenerResidentesVinculados(currentUserId)
                _uiState.update { state ->
                    val newIndex = if (state.selectedIndex in lista.indices) state.selectedIndex else 0
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        residentesVinculados = lista,
                        selectedIndex = newIndex,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar la información del residente vinculado", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = "No fue posible conectar con la base de datos. Verifica tu conexión a Internet."
                    )
                }
            }
        }
    }

    /**
     * Cambia el residente seleccionado cuando existen varios familiares vinculados a la misma cuenta.
     */
    fun seleccionarResidente(index: Int) {
        val lista = _uiState.value.residentesVinculados
        if (index in lista.indices) {
            _uiState.update { it.copy(selectedIndex = index) }
        }
    }

    /**
     * Reintenta la carga de datos en caso de error.
     */
    fun reintentar() {
        cargarDatos(isRefresh = false)
    }

    companion object {
        private const val TAG = "MiFamiliarViewModel"
    }
}
