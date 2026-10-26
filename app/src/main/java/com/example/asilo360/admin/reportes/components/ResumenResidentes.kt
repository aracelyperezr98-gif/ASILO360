package com.example.asilo360.admin.reportes.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.asilo360.data.model.ResidentesReporteData

/**
 * Sección de Reporte de Residentes con gráficas y estado de ocupación.
 */
@Composable
fun ResumenResidentes(
    residentesData: ResidentesReporteData,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Estado de Residentes",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Gráfica Donut de Estado Activos / Inactivos
        val slicesEstado = listOf(
            PieSliceData("Activos", residentesData.activos.toFloat(), Color(0xFF2E7D32)),
            PieSliceData("Inactivos", residentesData.inactivos.toFloat(), Color(0xFFD32F2F))
        )
        GraficaCircular(
            titulo = "Residentes Activos vs Inactivos",
            slices = slicesEstado
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Gráfica de Barras para nuevos ingresos por mes
        val barsIngresos = residentesData.por_mes.map { m ->
            BarData(label = m.mes.takeLast(2), value = m.cantidad.toFloat(), color = MaterialTheme.colorScheme.primary)
        }
        GraficaBarras(
            titulo = "Ingresos de Residentes por Mes",
            bars = barsIngresos
        )
    }
}
