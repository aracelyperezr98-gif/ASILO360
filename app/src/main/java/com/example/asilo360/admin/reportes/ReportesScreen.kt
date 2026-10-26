package com.example.asilo360.admin.reportes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.asilo360.admin.reportes.components.BarData
import com.example.asilo360.admin.reportes.components.FiltroFechaBar
import com.example.asilo360.admin.reportes.components.GraficaBarras
import com.example.asilo360.admin.reportes.components.GraficaCircular
import com.example.asilo360.admin.reportes.components.KpiCard
import com.example.asilo360.admin.reportes.components.PieSliceData
import com.example.asilo360.admin.reportes.components.ReporteHeader
import com.example.asilo360.admin.reportes.components.ResumenActividad
import com.example.asilo360.admin.reportes.components.ResumenFinanciero
import com.example.asilo360.admin.reportes.components.ResumenResidentes
import com.example.asilo360.components.QuickActionButton

/**
 * Módulo del Centro de Reportes y Análisis Administrativo de Asilo 360.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesScreen(
    onNavigateToResidentes: () -> Unit,
    onNavigateToPersonal: () -> Unit,
    onNavigateToConsultas: () -> Unit,
    onNavigateToVisitas: () -> Unit,
    onNavigateToPagos: () -> Unit,
    onNavigateToNotificaciones: () -> Unit,
    onBackClicked: () -> Unit,
    viewModel: ReportesViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Centro de Reportes",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Análisis administrativo y financiero",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. Encabezado de Reportes y Botón Actualizar
            item {
                ReporteHeader(
                    ultimaActualizacion = uiState.ultimaActualizacion,
                    onRefreshClick = viewModel::cargarReportes,
                    isLoading = uiState.isLoading
                )
            }

            // 2. Barra de Selección de Período / Fechas
            item {
                FiltroFechaBar(
                    selectedPeriodo = uiState.selectedPeriodo,
                    onPeriodoSelected = viewModel::seleccionarPeriodo,
                    fechaInicio = uiState.fechaInicio,
                    fechaFin = uiState.fechaFin,
                    onFechaInicioChanged = viewModel::onFechaInicioChanged,
                    onFechaFinChanged = viewModel::onFechaFinChanged
                )
            }

            // Estados de Carga o Contenido
            if (uiState.isLoading && uiState.reportData == null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.reportData != null) {
                val data = uiState.reportData!!
                val resumen = data.resumen
                val comp = data.comparaciones

                // 3. Matriz de KPIs Principales
                item {
                    Text(
                        text = "Indicadores Clave (KPI)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            KpiCard(
                                title = "Residentes",
                                value = "${resumen?.residentes ?: 0}",
                                icon = Icons.Default.People,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            KpiCard(
                                title = "Personal",
                                value = "${resumen?.personal ?: 0}",
                                icon = Icons.Default.Badge,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            KpiCard(
                                title = "Consultas",
                                value = "${resumen?.consultas ?: 0}",
                                icon = Icons.Default.MedicalServices,
                                color = Color(0xFFE65100),
                                comparacionPct = comp?.consultas_pct,
                                modifier = Modifier.weight(1f)
                            )
                            KpiCard(
                                title = "Visitas",
                                value = "${resumen?.visitas ?: 0}",
                                icon = Icons.Default.MeetingRoom,
                                color = Color(0xFF00897B),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            KpiCard(
                                title = "Ingresos",
                                value = String.format("$%,.0f", resumen?.ingresos ?: 0.0),
                                icon = Icons.Default.Payments,
                                color = Color(0xFF2E7D32),
                                comparacionPct = comp?.ingresos_pct,
                                modifier = Modifier.weight(1f)
                            )
                            KpiCard(
                                title = "Pagos Pend.",
                                value = "${resumen?.pagos_pendientes_count ?: 0}",
                                icon = Icons.Default.Payments,
                                color = Color(0xFFC2185B),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 4. Sección de Resumen Financiero
                if (data.financiero_data != null) {
                    item {
                        ResumenFinanciero(
                            financieroData = data.financiero_data,
                            onNavigateToPagos = onNavigateToPagos
                        )
                    }
                }

                // 5. Sección de Reporte de Residentes
                if (data.residentes_data != null) {
                    item {
                        ResumenResidentes(
                            residentesData = data.residentes_data
                        )
                    }
                }

                // 6. Sección de Reporte de Consultas por Estado
                if (data.consultas_data != null) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Reporte de Consultas Médicas",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            val barsConsultas = data.consultas_data.por_estado.map { e ->
                                val color = when (e.estado.lowercase()) {
                                    "realizada" -> Color(0xFF2E7D32)
                                    "cancelada" -> Color(0xFFD32F2F)
                                    else -> MaterialTheme.colorScheme.primary
                                }
                                BarData(label = e.estado, value = e.cantidad.toFloat(), color = color)
                            }
                            GraficaBarras(
                                titulo = "Consultas por Estado",
                                bars = barsConsultas
                            )
                        }
                    }
                }

                // 7. Sección de Notificaciones
                if (resumen != null) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Comunicación y Avisos",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            val slicesNotif = listOf(
                                PieSliceData("Leídas", resumen.notificaciones_leidas.toFloat(), Color(0xFF2E7D32)),
                                PieSliceData("No leídas", resumen.notificaciones_no_leidas.toFloat(), MaterialTheme.colorScheme.primary)
                            )
                            GraficaCircular(
                                titulo = "Notificaciones Leídas vs No Leídas",
                                slices = slicesNotif
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Gestión de Notificaciones",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Button(onClick = onNavigateToNotificaciones) {
                                        Text(text = "Ver notificaciones")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                                    }
                                }
                            }
                        }
                    }
                }

                // 8. Actividad Reciente del Asilo
                if (!data.actividad_reciente.isNullOrEmpty()) {
                    item {
                        ResumenActividad(actividades = data.actividad_reciente)
                    }
                }

                // 9. Acciones Rápidas del Administrador
                item {
                    Text(
                        text = "Acciones Rápidas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            QuickActionButton(
                                text = "Residentes",
                                icon = Icons.Default.People,
                                onClick = onNavigateToResidentes,
                                modifier = Modifier.weight(1f)
                            )
                            QuickActionButton(
                                text = "Personal",
                                icon = Icons.Default.Badge,
                                onClick = onNavigateToPersonal,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            QuickActionButton(
                                text = "Consultas",
                                icon = Icons.Default.MedicalServices,
                                onClick = onNavigateToConsultas,
                                modifier = Modifier.weight(1f)
                            )
                            QuickActionButton(
                                text = "Visitas",
                                icon = Icons.Default.MeetingRoom,
                                onClick = onNavigateToVisitas,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            QuickActionButton(
                                text = "Pagos",
                                icon = Icons.Default.Payments,
                                onClick = onNavigateToPagos,
                                modifier = Modifier.weight(1f)
                            )
                            QuickActionButton(
                                text = "Notificaciones",
                                icon = Icons.Default.Notifications,
                                onClick = onNavigateToNotificaciones,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
