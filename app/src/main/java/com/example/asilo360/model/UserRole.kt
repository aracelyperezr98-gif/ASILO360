package com.example.asilo360.model

/**
 * Representa los diferentes tipos de usuario disponibles en Asilo 360.
 */
enum class UserRole(
    val id: Int,
    val title: String,
    val description: String,
) {
    ADMINISTRADOR(
        id = 1,
        title = "Administrador",
        description = "Gestiona residentes, personal, visitas, consultas, pagos y notificaciones."
    ),
    PERSONAL(
        id = 2,
        title = "Personal del asilo",
        description = "Consulta información de los residentes y registra actividades relacionadas con su cuidado."
    ),
    FAMILIAR(
        id = 3,
        title = "Familiar",
        description = "Consulta información, visitas, notificaciones y pagos relacionados con tu familiar."
    )
}
