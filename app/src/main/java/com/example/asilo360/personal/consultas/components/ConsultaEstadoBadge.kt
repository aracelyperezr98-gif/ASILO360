package com.example.asilo360.personal.consultas.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.asilo360.ui.theme.CoralRedDark
import com.example.asilo360.ui.theme.MintGreenDark

/**
 * Badge para representar el estado de la consulta médica (Programada, Realizada, Cancelada).
 */
@Composable
fun ConsultaEstadoBadge(
    estado: String?,
    modifier: Modifier = Modifier
) {
    val est = estado?.ifBlank { "Programada" } ?: "Programada"
    val (containerColor, contentColor, dotColor) = when {
        est.equals("Realizada", ignoreCase = true) -> Triple(Color(0xFFE0F2F1), MintGreenDark, Color(0xFF00796B))
        est.equals("Cancelada", ignoreCase = true) -> Triple(Color(0xFFFFEBEE), CoralRedDark, Color(0xFFD32F2F))
        else -> Triple(Color(0xFFFFF8E1), Color(0xFFF57F17), Color(0xFFFFB300)) // Programada
    }

    val emojiDot = when {
        est.equals("Realizada", ignoreCase = true) -> "🟢"
        est.equals("Cancelada", ignoreCase = true) -> "🔴"
        else -> "🟡"
    }

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
                text = emojiDot,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = est,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}
