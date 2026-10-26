package com.example.asilo360.personal.visitas.components

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
import com.example.asilo360.ui.theme.CoralRedDark
import com.example.asilo360.ui.theme.MintGreenDark

data class ResumenVisitasKpi(
    val hoy: Int = 0,
    val programadas: Int = 0,
    val confirmadas: Int = 0,
    val realizadas: Int = 0,
    val canceladas: Int = 0
)

@Composable
fun StatMiniCard(
    titulo: String,
    valor: Int,
    colorTexto: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelSmall,
                color = colorTexto
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$valor",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorTexto
            )
        }
    }
}

/**
 * Tarjeta contenedora del resumen KPI de las visitas programadas.
 */
@Composable
fun VisitaResumenCard(
    resumen: ResumenVisitasKpi,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        StatMiniCard(
            titulo = "Hoy",
            valor = resumen.hoy,
            colorTexto = MaterialTheme.colorScheme.primary,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.weight(1f)
        )
        StatMiniCard(
            titulo = "Programadas",
            valor = resumen.programadas,
            colorTexto = Color(0xFFF57F17),
            containerColor = Color(0xFFFFF8E1),
            modifier = Modifier.weight(1.1f)
        )
        StatMiniCard(
            titulo = "Confirmadas",
            valor = resumen.confirmadas,
            colorTexto = Color(0xFF1565C0),
            containerColor = Color(0xFFE3F2FD),
            modifier = Modifier.weight(1.1f)
        )
        StatMiniCard(
            titulo = "Realizadas",
            valor = resumen.realizadas,
            colorTexto = MintGreenDark,
            containerColor = Color(0xFFE0F2F1),
            modifier = Modifier.weight(1f)
        )
        StatMiniCard(
            titulo = "Canceladas",
            valor = resumen.canceladas,
            colorTexto = CoralRedDark,
            containerColor = Color(0xFFFFEBEE),
            modifier = Modifier.weight(1f)
        )
    }
}
