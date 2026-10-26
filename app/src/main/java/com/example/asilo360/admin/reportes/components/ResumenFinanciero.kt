package com.example.asilo360.admin.reportes.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.asilo360.data.model.FinancieroReporteData

/**
 * Sección de Resumen Financiero con tarjeta de pagos pendientes, gráfica de ingresos y métodos de pago.
 */
@Composable
fun ResumenFinanciero(
    financieroData: FinancieroReporteData,
    onNavigateToPagos: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Resumen Financiero",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tarjeta Pagos Pendientes
        if (financieroData.pagos_pendientes_count > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pagos Pendientes (${financieroData.pagos_pendientes_count})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                        Text(
                            text = "Monto pendiente por cobrar: ${String.format("$%,.2f", financieroData.monto_pendiente)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFBF360C)
                        )
                    }

                    Button(
                        onClick = onNavigateToPagos,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Ver pagos")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Gráfica de Tendencia de Ingresos
        val lineData = financieroData.ingresos_por_mes.map { m ->
            LineDataPoint(label = m.mes.takeLast(2), value = m.monto.toFloat())
        }
        GraficaLineas(
            titulo = "Ingresos Recaudados por Mes",
            dataPoints = lineData,
            lineColor = Color(0xFF2E7D32)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Gráfica Circular de Métodos de Pago
        val slicesMetodo = financieroData.pagos_por_metodo.mapIndexed { index, pm ->
            val color = when (index % 4) {
                0 -> Color(0xFF0288D1)
                1 -> Color(0xFF2E7D32)
                2 -> Color(0xFFE65100)
                else -> Color(0xFF7B1FA2)
            }
            PieSliceData(label = pm.metodo_pago, value = pm.cantidad.toFloat(), color = color)
        }
        GraficaCircular(
            titulo = "Distribución por Método de Pago",
            slices = slicesMetodo
        )
    }
}
