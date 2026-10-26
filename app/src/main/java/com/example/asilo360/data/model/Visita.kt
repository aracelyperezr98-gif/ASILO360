package com.example.asilo360.data.model

/**
 * Modelo de datos que representa una visita agendada en la tabla `visitas`.
 */
data class Visita(
    val id_visita: Int = 0,
    val id_residente: Int,
    val id_familiar: Int,
    val fecha: String,
    val hora: String,
    val numero_visitantes: Int = 1,
    val estado: String = "Programada",
    val observaciones: String? = null,
    val fecha_creacion: String? = null,
    val nombre_residente: String? = null,
    val habitacion: String? = null,
    val nombre_familiar: String? = null,
    val parentesco: String? = null
)

/**
 * Opción de familiar relacionado devuelto para selectores.
 */
data class FamiliarOpcion(
    val id_familiar: Int,
    val id_usuario: Int,
    val nombre: String,
    val parentesco: String? = null,
    val telefono: String? = null
)

/**
 * Clases de respuesta de la API PHP para visitas.
 */
data class FamiliaresPorResidenteResponse(
    val success: Boolean,
    val message: String? = null,
    val familiares: List<FamiliarOpcion>? = null
)

data class VisitasListResponse(
    val success: Boolean,
    val message: String? = null,
    val visitas: List<Visita>? = null
)

data class VisitaDetailResponse(
    val success: Boolean,
    val message: String? = null,
    val visita: Visita? = null
)

data class VisitaOperationResponse(
    val success: Boolean,
    val message: String? = null,
    val id_visita: Int? = null
)

data class CambiarEstadoVisitaRequest(
    val id_visita: Int,
    val estado: String
)
