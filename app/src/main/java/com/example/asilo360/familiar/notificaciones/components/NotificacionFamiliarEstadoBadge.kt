package com.example.asilo360.familiar.notificaciones.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun obtenerEmojiCategoriaFamiliar(tipo: String?): String {
    val t = tipo?.lowercase() ?: ""
    return when {
        t.contains("visita") -> "👥"
        t.contains("consulta") || t.contains("médica") || t.contains("medica") || t.contains("salud") -> "🩺"
        t.contains("actividad") -> "🎨"
        t.contains("pago") || t.contains("mensualidad") -> "💳"
        t.contains("aviso") || t.contains("comunicado") -> "📢"
        else -> "🔔"
    }
}

/**
 * Indicador visual de lectura para Notificaciones del Familiar.
 */
@Composable
fun NotificacionFamiliarEstadoBadge(
    leida: Boolean,
    modifier: Modifier = Modifier
) {
    val containerColor = if (leida) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer
    val contentColor = if (leida) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
    val texto = if (leida) "Leída" else "Sin leer"

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!leida) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = texto,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}
