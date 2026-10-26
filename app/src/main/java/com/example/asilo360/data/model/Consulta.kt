package com.example.asilo360.data.model

/**
 * Modelo de datos que representa una consulta médica en la tabla `consultas`.
 */
data class Consulta(
    val id_consulta: Int = 0,
    val id_residente: Int,
    val id_personal: Int = 0,
    val fecha: String,
    val hora: String,
    val motivo: String,
    val observaciones: String? = null,
    val estado: String = "Programada",
    val residente: String? = null,
    val nombre_residente: String? = null,
    val habitacion: String? = null,
    val sexo: String? = null,
    val fecha_nacimiento: String? = null,
    val personal: String? = null,
    val nombre_personal: String? = null,
    val puesto_personal: String? = null,
    val id_usuario: Int? = null
) {
    fun obtenerNombreResidente(): String = residente ?: nombre_residente ?: "Residente"
    fun obtenerNombrePersonal(): String = personal ?: nombre_personal ?: "Personal"
}

data class ResumenConsultasKpi(
    val hoy: Int = 0,
    val programadas: Int = 0,
    val realizadas: Int = 0,
    val canceladas: Int = 0
)

/**
 * Clases de respuesta de la API PHP para consultas.
 */
data class ConsultasListResponse(
    val success: Boolean,
    val message: String? = null,
    val resumen: ResumenConsultasKpi? = null,
    val data: List<Consulta>? = null,
    val consultas: List<Consulta>? = null
)

data class ConsultaDetailResponse(
    val success: Boolean,
    val message: String? = null,
    val consulta: Consulta? = null,
    val data: Consulta? = null
)

data class ConsultaOperationResponse(
    val success: Boolean,
    val message: String? = null,
    val id_consulta: Int? = null
)

data class CambiarEstadoConsultaRequest(
    val id_consulta: Int,
    val estado: String,
    val observaciones: String? = null,
    val id_personal: Int? = null,
    val id_usuario: Int? = null
)
