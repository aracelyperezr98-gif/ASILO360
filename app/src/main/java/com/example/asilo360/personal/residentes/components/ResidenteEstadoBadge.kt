package com.example.asilo360.personal.residentes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
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
 * Insignia de estado del residente (Activo / Inactivo).
 */
@Composable
fun ResidenteEstadoBadge(
    estado: String?,
    modifier: Modifier = Modifier
) {
    val esActivo = estado?.equals("Activo", ignoreCase = true) == true
    val containerColor = if (esActivo) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val contentColor = if (esActivo) Color(0xFF2E7D32) else Color(0xFFC62828)
    val dotColor = if (esActivo) Color(0xFF4CAF50) else Color(0xFFE53935)
    val textoEstado = estado?.ifBlank { "Sin estado" } ?: "Sin estado"

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color = dotColor, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = textoEstado,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}
