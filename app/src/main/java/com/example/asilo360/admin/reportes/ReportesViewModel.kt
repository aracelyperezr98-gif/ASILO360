package com.example.asilo360.admin.reportes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.asilo360.data.firebase.FirebaseClient
import com.example.asilo360.data.model.ReporteDashboardResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Estado UI para el Centro de Reportes.
 */
data class ReportesUiState(
    val selectedPeriodo: String = "Este año",
    val fechaInicio: String = "",
    val fechaFin: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val ultimaActualizacion: String = "",
    val reportData: ReporteDashboardResponse? = null
)

/**
 * ViewModel que calcula reportes a partir de los datos de Firestore.
 */
class ReportesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReportesUiState())
    val uiState: StateFlow<ReportesUiState> = _uiState.asStateFlow()

    init {
        // Establecer rango por defecto (Este año)
        seleccionarPeriodo("Este año")
    }

    /**
    * Carga los datos de reporte desde Cloud Firestore.
     */
    fun cargarReportes() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val inicio = _uiState.value.fechaInicio.takeIf { it.isNotBlank() }
                val fin = _uiState.value.fechaFin.takeIf { it.isNotBlank() }

                val response = FirebaseClient.reportesApiService.obtenerDashboardReportes(
                    fechaInicio = inicio,
                    fechaFin = fin
                )
                val body = response.body()

                if (response.isSuccessful && body?.success == true) {
                    val horaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            reportData = body,
                            ultimaActualizacion = body.fecha_actualizacion ?: horaActual,
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = body?.message ?: "No se pudieron cargar los reportes."
                        )
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error de conexión al cargar reportes", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo conectar con Firestore. Revisa tu conexión a Internet."
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error inesperado al cargar reportes", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Ocurrió un error inesperado al generar los reportes."
                    )
                }
            }
        }
    }

    /**
     * Aplica la selección de período de fecha y actualiza los reportes.
     */
    fun seleccionarPeriodo(periodo: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        val hoyStr = sdf.format(cal.time)
        var inicioStr = hoyStr

        when (periodo) {
            "Hoy" -> {
                inicioStr = hoyStr
            }
            "Esta semana" -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                inicioStr = sdf.format(cal.time)
            }
            "Este mes" -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                inicioStr = sdf.format(cal.time)
            }
            "Últimos 3 meses" -> {
                cal.add(Calendar.MONTH, -3)
                inicioStr = sdf.format(cal.time)
            }
            "Este año" -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                inicioStr = sdf.format(cal.time)
            }
            "Personalizado" -> {
                inicioStr = _uiState.value.fechaInicio.ifBlank { hoyStr }
            }
        }

        _uiState.update {
            it.copy(
                selectedPeriodo = periodo,
                fechaInicio = inicioStr,
                fechaFin = hoyStr
            )
        }

        cargarReportes()
    }

    fun onFechaInicioChanged(fecha: String) {
        _uiState.update { it.copy(fechaInicio = fecha) }
        cargarReportes()
    }

    fun onFechaFinChanged(fecha: String) {
        _uiState.update { it.copy(fechaFin = fecha) }
        cargarReportes()
    }

    private companion object {
        const val TAG = "ReportesViewModel"
    }
}
