package com.example.asilo360.familiar.pagos.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.asilo360.ui.theme.CoralRed
import com.example.asilo360.ui.theme.LuminousTurquoise
import java.text.NumberFormat
import java.util.Locale

data class ResumenPagosKpi(
    val totalRegistros: Int = 0,
    val totalPagado: Double = 0.0,
    val totalPendiente: Double = 0.0,
    val ultimoPagoMonto: Double = 0.0
)

fun formatMoneda(monto: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    return formatter.format(monto)
}

@Composable
fun ResumenPagosCard(
    kpi: ResumenPagosKpi,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatBox(
                label = "Total Pagado",
                valor = formatMoneda(kpi.totalPagado),
                colorValor = LuminousTurquoise,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.weight(1f)
            )
            StatBox(
                label = "Total Pendiente",
                valor = formatMoneda(kpi.totalPendiente),
                colorValor = if (kpi.totalPendiente > 0) CoralRed else LuminousTurquoise,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatBox(
                label = "Movimientos",
                valor = "${kpi.totalRegistros}",
                colorValor = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.weight(1f)
            )
            StatBox(
                label = "Último Pago",
                valor = if (kpi.ultimoPagoMonto > 0) formatMoneda(kpi.ultimoPagoMonto) else "N/A",
                colorValor = MaterialTheme.colorScheme.onSurface,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatBox(
    label: String,
    valor: String,
    colorValor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = valor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorValor
            )
        }
    }
}
