package com.example.asilo360.familiar.visitas.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.asilo360.ui.theme.CoralRed
import com.example.asilo360.ui.theme.LuminousTurquoise
import com.example.asilo360.ui.theme.TechBlue
import com.example.asilo360.ui.theme.WarningAmber

/**
 * Insignia de estado para las visitas del Familiar (Programada, Confirmada, Realizada, Cancelada).
 */
@Composable
fun VisitaEstadoBadge(
    estado: String,
    modifier: Modifier = Modifier
) {
    val (containerColor, contentColor) = when (estado.lowercase()) {
        "confirmada" -> Pair(LuminousTurquoise.copy(alpha = 0.2f), LuminousTurquoise)
        "realizada" -> Pair(TechBlue.copy(alpha = 0.2f), TechBlue)
        "cancelada" -> Pair(CoralRed.copy(alpha = 0.2f), CoralRed)
        else -> Pair(WarningAmber.copy(alpha = 0.2f), WarningAmber) // Programada / Pendiente
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = containerColor
    ) {
        Text(
            text = estado,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}
