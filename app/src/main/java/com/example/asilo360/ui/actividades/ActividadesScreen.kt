package com.example.asilo360.ui.actividades

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.runtime.Composable
import com.example.asilo360.components.PlaceholderScreen

@Composable
fun ActividadesScreen(
    onBackClicked: () -> Unit
) {
    PlaceholderScreen(
        title = "Actividades",
        icon = Icons.Default.FitnessCenter,
        onBackClicked = onBackClicked
    )
}
