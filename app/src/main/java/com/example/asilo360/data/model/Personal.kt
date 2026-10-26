package com.example.asilo360.data.model

/**
 * Modelo de datos que representa a un trabajador del asilo (Personal, id_rol = 2).
 */
data class Personal(
    val id_personal: Int = 0,
    val id_usuario: Int = 0,
    val nombre: String,
    val apellido_paterno: String,
    val apellido_materno: String? = null,
    val correo: String,
    val password: String? = null,
    val telefono: String? = null,
    val puesto: String,
    val fecha_ingreso: String? = null,
    val estado: Int = 1
)

/**
 * Clases de respuesta de la API PHP para operaciones con Personal.
 */
data class PersonalListResponse(
    val success: Boolean,
    val message: String? = null,
    val personal: List<Personal>? = null
)

data class PersonalDetailResponse(
    val success: Boolean,
    val message: String? = null,
    val personal: Personal? = null
)

data class PersonalOperationResponse(
    val success: Boolean,
    val message: String? = null,
    val id_personal: Int? = null,
    val id_usuario: Int? = null
)

data class CambiarEstadoRequest(
    val id_personal: Int,
    val estado: Int
)
