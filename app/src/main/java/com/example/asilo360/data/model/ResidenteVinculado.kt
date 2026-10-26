package com.example.asilo360.data.model

/**
 * Representa la información completa de un residente vinculado a un usuario familiar,
 * incluyendo su parentesco registrado y un resumen en tiempo real de sus eventos
 * (consultas médicas, visitas programadas, actividades y pagos).
 */
data class ResidenteVinculado(
    val residente: Residente,
    val parentesco: String = "Familiar",
    val idFamiliar: Int = 0,
    val totalConsultas: Int = 0,
    val proximaConsulta: Consulta? = null,
    val totalVisitas: Int = 0,
    val proximaVisita: Visita? = null,
    val totalActividades: Int = 0,
    val pagosPendientesCount: Int = 0,
    val ultimoPago: Pago? = null
)
