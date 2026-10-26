package com.example.asilo360.admin.reportes.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class LineDataPoint(
    val label: String,
    val value: Float
)

/**
 * Gráfica de Líneas y Tendencia nativa en Jetpack Compose.
 */
@Composable
fun GraficaLineas(
    titulo: String,
    dataPoints: List<LineDataPoint>,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val maxValue = dataPoints.maxOfOrNull { it.value }?.coerceAtLeast(1f) ?: 1f
    val progress = remember { Animatable(0f) }

    LaunchedEffect(dataPoints) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (dataPoints.isEmpty()) {
                Text(
                    text = "No hay datos para mostrar la tendencia.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                BoxWithLineCanvas(
                    dataPoints = dataPoints,
                    maxValue = maxValue,
                    progress = progress.value,
                    lineColor = lineColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dataPoints.forEach { point ->
                        Text(
                            text = point.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxWithLineCanvas(
    dataPoints: List<LineDataPoint>,
    maxValue: Float,
    progress: Float,
    lineColor: Color
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
    ) {
        val width = size.width
        val height = size.height
        val stepX = if (dataPoints.size > 1) width / (dataPoints.size - 1) else width

        val path = Path()
        val fillPath = Path()

        dataPoints.forEachIndexed { index, point ->
            val x = index * stepX
            val normalizedY = (point.value / maxValue) * progress
            val y = height - (normalizedY * (height - 30f)) - 15f

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }

            // Dibujar nodos
            drawCircle(
                color = lineColor,
                radius = 6f,
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
        }

        if (dataPoints.isNotEmpty()) {
            val lastX = (dataPoints.size - 1) * stepX
            fillPath.lineTo(lastX, height)
            fillPath.close()

            // Dibujar degradado sombreado bajo la curva
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.35f), Color.Transparent)
                )
            )

            // Dibujar línea principal
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 6f)
            )
        }
    }
}
