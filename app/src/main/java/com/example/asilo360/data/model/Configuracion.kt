package com.example.asilo360.data.model

/**
 * Modelos de datos para el módulo de Configuración y Perfil de Administrador.
 */

data class PerfilUsuario(
    val id_usuario: Int = 0,
    val nombre: String = "",
    val apellido_paterno: String = "",
    val apellido_materno: String = "",
    val correo: String = "",
    val telefono: String = "",
    val id_rol: Int = 1,
    val rol: String = "Administrador"
)

data class ActualizarPerfilRequest(
    val id_usuario: Int,
    val nombre: String,
    val apellido_paterno: String,
    val apellido_materno: String,
    val correo: String,
    val telefono: String
)

data class CambiarPasswordRequest(
    val id_usuario: Int,
    val password_actual: String,
    val password_nueva: String
)

data class PreferenciasGuardadas(
    val notificacionesActivas: Boolean = true,
    val sonidoNotificaciones: Boolean = true,
    val confirmarAcciones: Boolean = true,
    val actualizarAutomaticamente: Boolean = true
)

data class PerfilResponse(
    val success: Boolean,
    val message: String? = null,
    val usuario: PerfilUsuario? = null
)

data class ConfiguracionOperationResponse(
    val success: Boolean,
    val message: String? = null
)
