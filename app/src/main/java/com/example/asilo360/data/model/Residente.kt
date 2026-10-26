package com.example.asilo360.data.model

/**
 * Modelo de datos que representa a un residente guardado en la colección `residentes`.
 *
 * Explicación de los tipos utilizados:
 * - `id_residente`: Int = 0 (Valor numérico incremental, por defecto 0 para registros nuevos).
 * - `nombre`, `apellido_paterno`: String (Campos obligatorios en la base de datos).
 * - `apellido_materno`, `fecha_nacimiento`, `sexo`, `habitacion`, `fecha_ingreso`, `estado`, `observaciones`: String?
 *   (Tipos que permiten null porque esos campos son opcionales en algunos registros).
 */
data class Residente(
    val id_residente: Int = 0,
    val nombre: String,
    val apellido_paterno: String,
    val apellido_materno: String? = null,
    val fecha_nacimiento: String? = null,
    val sexo: String? = null,
    val habitacion: String? = null,
    val fecha_ingreso: String? = null,
    val estado: String? = "Activo",
    val observaciones: String? = null,
) {
    fun nombreCompleto(): String = "${nombre} ${apellido_paterno} ${apellido_materno.orEmpty()}".trim()
}

/**
 * Respuestas de la API PHP para operaciones de residentes.
 */
data class ResidentesListResponse(
    val success: Boolean,
    val message: String? = null,
    val data: List<Residente>? = null,
)

data class ResidenteDetailResponse(
    val success: Boolean,
    val message: String? = null,
    val data: Residente? = null,
    val residente: Residente? = null,
)

data class ResidenteOperationResponse(
    val success: Boolean,
    val message: String? = null,
    val data: ResidenteCreatedData? = null,
)

data class ResidenteCreatedData(
    val id_residente: Int? = null,
)
