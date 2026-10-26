package com.example.asilo360.ui.familiar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.asilo360.data.model.ResidenteVinculado
import com.example.asilo360.ui.theme.CoralRed
import com.example.asilo360.ui.theme.LuminousTurquoise
import com.example.asilo360.ui.theme.TechBlue
import com.example.asilo360.ui.theme.WarningAmber

/**
 * Tarjeta de accesos visuales y resumen de seguimiento para el residente seleccionado.
 */
@Composable
fun SeguimientoAccesosCard(
    vinculado: ResidenteVinculado,
    modifier: Modifier = Modifier,
    onNavigateToConsultas: () -> Unit = {},
    onNavigateToVisitas: () -> Unit = {},
    onNavigateToPagos: () -> Unit = {},
    onNavigateToNotificaciones: () -> Unit = {}
) {
    val proxConsulta = vinculado.proximaConsulta
    val consultaText = if (proxConsulta != null) {
        "Próxima: ${proxConsulta.fecha} (${proxConsulta.hora.take(5)})"
    } else if (vinculado.totalConsultas > 0) {
        "${vinculado.totalConsultas} consultas registradas"
    } else {
        "Sin consultas agendadas"
    }

    val proxVisita = vinculado.proximaVisita
    val visitaText = if (proxVisita != null) {
        "Próxima: ${proxVisita.fecha} (${proxVisita.hora.take(5)})"
    } else if (vinculado.totalVisitas > 0) {
        "${vinculado.totalVisitas} visitas registradas"
    } else {
        "Sin visitas programadas"
    }

    val tienePagosPendientes = vinculado.pagosPendientesCount > 0
    val pagoText = if (tienePagosPendientes) {
        "${vinculado.pagosPendientesCount} pago pendiente"
    } else if (vinculado.ultimoPago != null) {
        "Al corriente (${vinculado.ultimoPago.fecha_pago})"
    } else {
        "Sin adeudos"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Resumen de Seguimiento",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Consultas Médicas
            TrackingItemRow(
                title = "Consultas Médicas",
                subtitle = consultaText,
                icon = Icons.Default.MedicalServices,
                accentColor = WarningAmber,
                onClick = onNavigateToConsultas
            )

            // Visitas Programadas
            TrackingItemRow(
                title = "Visitas Programadas",
                subtitle = visitaText,
                icon = Icons.Default.MeetingRoom,
                accentColor = LuminousTurquoise,
                onClick = onNavigateToVisitas
            )

            // Estado de Pagos
            TrackingItemRow(
                title = "Estado de Pagos",
                subtitle = pagoText,
                icon = Icons.Default.Payments,
                accentColor = if (tienePagosPendientes) CoralRed else LuminousTurquoise,
                onClick = onNavigateToPagos
            )

            // Notificaciones
            TrackingItemRow(
                title = "Avisos y Comunicados",
                subtitle = "Ver notificaciones enviadas",
                icon = Icons.Default.Notifications,
                accentColor = TechBlue,
                onClick = onNavigateToNotificaciones
            )
        }
    }
}

@Composable
private fun TrackingItemRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .size(40.dp)
                .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Ir a $title",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
