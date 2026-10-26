package com.example.asilo360.data.model

/**
 * Modelo que representa el vínculo oficial entre un residente y un familiar.
 */
data class ResidenteFamiliarVinculo(
    val docId: String = "",
    val idResidente: Int = 0,
    val nombreResidente: String = "",
    val habitacionResidente: String = "",
    val idFamiliar: Int = 0,
    val idUsuarioFamiliar: Int = 0,
    val nombreFamiliar: String = "",
    val correoFamiliar: String = "",
    val telefonoFamiliar: String = "",
    val parentesco: String = "Familiar"
)
