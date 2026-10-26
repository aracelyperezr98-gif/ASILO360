package com.example.asilo360.data.model

/**
 * Modelo de datos que representa una notificación en la tabla `notificaciones`.
 */
data class Notificacion(
    val id_notificacion: Int = 0,
    val id_usuario: Int = 0,
    val destinatario: String? = null,
    val rol: String? = null,
    val titulo: String,
    val mensaje: String,
    val tipo: String = "General",
    val leida: Boolean = false,
    val fecha: String? = null,
    val id_referencia: Int? = null,
    val tipo_referencia: String? = null
)

/**
 * Opción de usuario destinatario para el selector.
 */
data class UsuarioDestinatarioOpcion(
    val id_usuario: Int,
    val nombre: String,
    val rol: String
)

/**
 * Petición para crear una nueva notificación (individual o grupal).
 */
data class CrearNotificacionRequest(
    val id_usuario: Int? = null,
    val destinatario_tipo: String? = null,
    val titulo: String,
    val mensaje: String,
    val tipo: String
)

/**
 * Clases de respuesta de la API PHP.
 */
data class NotificacionesListResponse(
    val success: Boolean,
    val message: String? = null,
    val notificaciones: List<Notificacion>? = null
)

data class NotificacionDetailResponse(
    val success: Boolean,
    val message: String? = null,
    val notificacion: Notificacion? = null
)

data class NotificacionOperationResponse(
    val success: Boolean,
    val message: String? = null,
    val id_notificacion: Int? = null
)

data class MarcarLeidaRequest(
    val id_notificacion: Int
)
