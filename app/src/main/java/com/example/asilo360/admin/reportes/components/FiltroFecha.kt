package com.example.asilo360.admin.reportes.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.asilo360.components.CustomTextField

/**
 * Barra de selección de período de fecha.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltroFechaBar(
    selectedPeriodo: String,
    onPeriodoSelected: (String) -> Unit,
    fechaInicio: String,
    fechaFin: String,
    onFechaInicioChanged: (String) -> Unit,
    onFechaFinChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val periodos = listOf(
        "Hoy",
        "Esta semana",
        "Este mes",
        "Últimos 3 meses",
        "Este año",
        "Personalizado"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Período de Análisis",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(periodos) { p ->
                FilterChip(
                    selected = (p == selectedPeriodo),
                    onClick = { onPeriodoSelected(p) },
                    label = { Text(text = p) }
                )
            }
        }

        // Si se selecciona "Personalizado", mostrar campos de fecha de inicio y fin
        if (selectedPeriodo == "Personalizado") {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CustomTextField(
                    value = fechaInicio,
                    onValueChange = onFechaInicioChanged,
                    placeholder = "Inicio (AAAA-MM-DD)",
                    leadingIcon = Icons.Default.CalendarToday,
                    modifier = Modifier.weight(1f)
                )

                CustomTextField(
                    value = fechaFin,
                    onValueChange = onFechaFinChanged,
                    placeholder = "Fin (AAAA-MM-DD)",
                    leadingIcon = Icons.Default.CalendarToday,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
