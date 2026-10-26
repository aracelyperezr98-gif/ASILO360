package com.example.asilo360.familiar.consultas.components

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
import com.example.asilo360.ui.theme.CoralRed
import com.example.asilo360.ui.theme.LuminousTurquoise
import com.example.asilo360.ui.theme.WarningAmber

/**
 * Chip / Badge con diseño limpio y color distintivo para representar el estado
 * de una consulta médica (Programada, Realizada/Completada, Cancelada).
 */
@Composable
fun ConsultaEstadoBadge(
    estado: String,
    modifier: Modifier = Modifier
) {
    val (color, label) = when {
        estado.contains("Realizada", ignoreCase = true) || estado.contains("Completada", ignoreCase = true) -> {
            Pair(LuminousTurquoise, "Realizada")
        }
        estado.contains("Cancelada", ignoreCase = true) -> {
            Pair(CoralRed, "Cancelada")
        }
        else -> {
            Pair(WarningAmber, "Programada")
        }
    }

    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
