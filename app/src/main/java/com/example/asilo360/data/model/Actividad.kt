package com.example.asilo360.data.model

/**
 * Modelo de datos para las Actividades de Cuidado y Recreativas registradas en MySQL.
 */
data class Actividad(
    val id_actividad: Int = 0,
    val id_residente: Int,
    val residente: String? = null,
    val habitacion: String? = null,
    val id_personal: Int = 0,
    val personal: String? = null,
    val id_usuario: Int? = null,
    val fecha: String,
    val hora: String,
    val tipo: String,
    val descripcion: String
)

data class ActividadesListResponse(
    val success: Boolean,
    val message: String? = null,
    val data: List<Actividad>? = null
)

data class ActividadDetailResponse(
    val success: Boolean,
    val message: String? = null,
    val actividad: Actividad? = null,
    val data: Actividad? = null
)

data class ActividadOperationResponse(
    val success: Boolean,
    val message: String? = null,
    val id_actividad: Int? = null
)

data class EliminarActividadRequest(
    val id_actividad: Int,
    val id_personal: Int? = null,
    val id_usuario: Int? = null
)
