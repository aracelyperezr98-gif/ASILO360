package com.example.asilo360.data.model

/**
 * Modelos de datos para el Centro de Reportes y Estadísticas Administrativas.
 */

data class PeriodoReporte(
    val fecha_inicio: String,
    val fecha_fin: String
)

data class ResumenKpi(
    val residentes: Int = 0,
    val residentes_activos: Int = 0,
    val residentes_inactivos: Int = 0,
    val personal: Int = 0,
    val consultas: Int = 0,
    val visitas: Int = 0,
    val ingresos: Double = 0.0,
    val pagos_pendientes_count: Int = 0,
    val pagos_pendientes_monto: Double = 0.0,
    val notificaciones_total: Int = 0,
    val notificaciones_leidas: Int = 0,
    val notificaciones_no_leidas: Int = 0
)

data class ComparacionesReporte(
    val ingresos_pct: Double? = null,
    val consultas_pct: Double? = null
)

data class MesCantidad(
    val mes: String,
    val cantidad: Int
)

data class MesMonto(
    val mes: String,
    val monto: Double
)

data class EstadoCantidad(
    val estado: String,
    val cantidad: Int,
    val visitantes: Int? = null
)

data class MetodoPagoResumen(
    val metodo_pago: String,
    val cantidad: Int,
    val monto: Double
)

data class ActividadItem(
    val tipo_actividad: String,
    val titulo: String,
    val detalle: String,
    val fecha: String
)

data class ResidentesReporteData(
    val total: Int = 0,
    val activos: Int = 0,
    val inactivos: Int = 0,
    val por_mes: List<MesCantidad> = emptyList()
)

data class ConsultasReporteData(
    val total: Int = 0,
    val por_estado: List<EstadoCantidad> = emptyList(),
    val por_mes: List<MesCantidad> = emptyList()
)

data class VisitasReporteData(
    val total: Int = 0,
    val total_visitantes: Int = 0,
    val promedio_visitantes: Double = 0.0,
    val por_estado: List<EstadoCantidad> = emptyList(),
    val por_mes: List<MesCantidad> = emptyList()
)

data class FinancieroReporteData(
    val ingresos_totales: Double = 0.0,
    val monto_pendiente: Double = 0.0,
    val pagos_pendientes_count: Int = 0,
    val ingresos_por_mes: List<MesMonto> = emptyList(),
    val pagos_por_metodo: List<MetodoPagoResumen> = emptyList()
)

/**
 * Respuesta principal devuelta por GET reportes/dashboard.php
 */
data class ReporteDashboardResponse(
    val success: Boolean,
    val message: String? = null,
    val fecha_actualizacion: String? = null,
    val periodo: PeriodoReporte? = null,
    val resumen: ResumenKpi? = null,
    val comparaciones: ComparacionesReporte? = null,
    val residentes_data: ResidentesReporteData? = null,
    val consultas_data: ConsultasReporteData? = null,
    val visitas_data: VisitasReporteData? = null,
    val financiero_data: FinancieroReporteData? = null,
    val actividad_reciente: List<ActividadItem>? = null
)
