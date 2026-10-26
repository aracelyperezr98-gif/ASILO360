package com.example.asilo360.data.model

/**
 * Modelo de datos que representa un registro de pago en la tabla `pagos`.
 */
data class Pago(
    val id_pago: Int = 0,
    val id_residente: Int,
    val residente: String? = null,
    val monto: Double,
    val fecha_pago: String,
    val concepto: String,
    val metodo_pago: String,
    val estado: String = "Pagado"
)

/**
 * Clases de respuesta de la API PHP para pagos.
 */
data class PagosListResponse(
    val success: Boolean,
    val message: String? = null,
    val pagos: List<Pago>? = null
)

data class PagoDetailResponse(
    val success: Boolean,
    val message: String? = null,
    val pago: Pago? = null
)

data class PagoOperationResponse(
    val success: Boolean,
    val message: String? = null,
    val id_pago: Int? = null
)

data class CambiarEstadoPagoRequest(
    val id_pago: Int,
    val estado: String
)
