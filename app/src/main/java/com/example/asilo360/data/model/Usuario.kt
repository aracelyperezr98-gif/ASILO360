package com.example.asilo360.data.model

data class Usuario(
    val id_usuario: Int,
    val nombre: String,
    val apellido_paterno: String? = null,
    val apellido_materno: String? = null,
    val correo: String,
    val id_rol: Int,
    val rol: String,
    val telefono: String? = null
)

data class UsuariosListResponse(
    val success: Boolean,
    val message: String? = null,
    val data: List<Usuario>? = null
)
