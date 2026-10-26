package com.example.asilo360.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.asilo360.R

/**
 * Componente de logotipo para la aplicación Asilo 360.
 * En modo claro cambia automáticamente al logo de fondo claro (Light Mode Icon)
 * y en modo oscuro cambia al logo de fondo oscuro (Dark Mode Icon).
 */
@Composable
fun AsiloLogo(
    modifier: Modifier = Modifier,
    useDarkTheme: Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f || isSystemInDarkTheme()
) {
    val logoResource = if (useDarkTheme) {
        R.drawable.ic_asilo360_icon_dark
    } else {
        R.drawable.ic_asilo360_icon_light
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = logoResource),
            contentDescription = "Logo de Asilo 360",
            modifier = Modifier.size(96.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Título de la aplicación
        Text(
            text = "Asilo 360",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Eslogan oficial
        Text(
            text = "\"Cuidado, organización y comunicación\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
