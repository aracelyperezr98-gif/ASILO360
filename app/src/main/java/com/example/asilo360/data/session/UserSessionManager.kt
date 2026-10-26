package com.example.asilo360.data.session

import com.example.asilo360.data.model.Usuario

/**
 * Gestor de sesión de usuario autenticado en la aplicación.
 * Mantiene en memoria la información del usuario que ha iniciado sesión.
 */
object UserSessionManager {

    var currentUser: Usuario? = null
        private set

    fun saveSession(usuario: Usuario) {
        currentUser = usuario
    }

    fun clearSession() {
        currentUser = null
    }

    fun isLoggedIn(): Boolean {
        return currentUser != null
    }

    fun getUserId(): Int {
        return currentUser?.id_usuario ?: 0
    }

    fun getUserRoleId(): Int {
        return currentUser?.id_rol ?: -1
    }

    fun getUserFullName(): String {
        val u = currentUser ?: return "Usuario"
        val paterno = u.apellido_paterno ?: ""
        return "${u.nombre} $paterno".trim()
    }
}
