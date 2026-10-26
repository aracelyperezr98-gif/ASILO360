package com.example.asilo360.data.model

/**
 * Modelo de datos que representa un registro en Firebase Firestore.
 */
data class RegistroFirebase(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val tipo: String = "",
    val fecha: String = "",
    val usuario: String = "",
)
