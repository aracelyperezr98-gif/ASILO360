package com.example.asilo360.familiar.consultas.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.asilo360.components.CyberStatCard
import com.example.asilo360.ui.theme.CoralRed
import com.example.asilo360.ui.theme.LuminousTurquoise
import com.example.asilo360.ui.theme.WarningAmber

/**
 * Muestra el resumen cuantitativo en tarjetas KPI de las consultas médicas
 * del familiar (Próximas, Realizadas y Canceladas) calculadas con datos reales.
 */
@Composable
fun ConsultasResumenCard(
    proximasCount: Int,
    realizadasCount: Int,
    canceladasCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CyberStatCard(
            titulo = "Próximas",
            valor = proximasCount.toString(),
            icon = Icons.Default.EventRepeat,
            accentColor = WarningAmber,
            modifier = Modifier.weight(1f)
        )

        CyberStatCard(
            titulo = "Realizadas",
            valor = realizadasCount.toString(),
            icon = Icons.Default.CheckCircle,
            accentColor = LuminousTurquoise,
            modifier = Modifier.weight(1f)
        )

        CyberStatCard(
            titulo = "Canceladas",
            valor = canceladasCount.toString(),
            icon = Icons.Default.Cancel,
            accentColor = CoralRed,
            modifier = Modifier.weight(1f)
        )
    }
}
