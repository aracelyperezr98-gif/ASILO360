package com.example.asilo360.personal.actividades.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Función que devuelve el icono/emoji según el tipo de actividad.
 */
fun obtenerEmojiTipoActividad(tipo: String?): String {
    val t = tipo?.lowercase() ?: ""
    return when {
        t.contains("física") || t.contains("fisica") || t.contains("ejercicio") -> "🏃"
        t.contains("terapia") -> "🩺"
        t.contains("recreación") || t.contains("recreacion") -> "🎯"
        t.contains("lectura") -> "📖"
        t.contains("música") || t.contains("musica") -> "🎵"
        t.contains("manualidades") -> "🎨"
        t.contains("socialización") || t.contains("socializacion") -> "👥"
        t.contains("alimentación") || t.contains("alimentacion") -> "🥗"
        t.contains("paseo") -> "🚶"
        t.contains("juego") -> "🧩"
        else -> "📌"
    }
}

/**
 * Insignia para mostrar el tipo de actividad con icono y color de fondo.
 */
@Composable
fun ActividadEstadoBadge(
    tipo: String?,
    modifier: Modifier = Modifier
) {
    val emoji = obtenerEmojiTipoActividad(tipo)
    val texto = tipo?.ifBlank { "General" } ?: "General"
    val containerColor = MaterialTheme.colorScheme.secondaryContainer
    val contentColor = MaterialTheme.colorScheme.onSecondaryContainer

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = texto,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}
