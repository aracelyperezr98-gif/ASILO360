package com.example.asilo360.familiar.visitas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.asilo360.components.CyberStatCard
import com.example.asilo360.familiar.visitas.components.ProximaVisitaCard
import com.example.asilo360.familiar.visitas.components.SolicitarVisitaDialog
import com.example.asilo360.familiar.visitas.components.VisitaFamiliarCard
import com.example.asilo360.ui.theme.CoralRed
import com.example.asilo360.ui.theme.LuminousTurquoise
import com.example.asilo360.ui.theme.TechBlue

/**
 * Pantalla principal de Visitas para el rol Familiar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitasFamiliarScreen(
    onVisitaClick: (Int) -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VisitasFamiliarViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSolicitarDialog by remember { mutableStateOf(false) }

    if (showSolicitarDialog) {
        val res = (uiState as? VisitasFamiliarUiState.Success)?.residenteSeleccionado
        SolicitarVisitaDialog(
            residente = res,
            onDismiss = { showSolicitarDialog = false },
            onSubmit = { fecha, hora, num, obs ->
                viewModel.solicitarVisita(fecha, hora, num, obs) {
                    showSolicitarDialog = false
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Mis Visitas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Organiza y consulta los encuentros con tu familiar",
                            style = MaterialTheme.typography.bodySmall,
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
                actions = {
                    IconButton(onClick = { viewModel.cargarDatosYEscuchar() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSolicitarDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Solicitar Visita"
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            when (val state = uiState) {
                is VisitasFamiliarUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Cargando tus visitas...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is VisitasFamiliarUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MeetingRoom,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { viewModel.cargarDatosYEscuchar() },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Reintentar")
                            }
                        }
                    }
                }

                is VisitasFamiliarUiState.Success -> {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Selector de Residente (si hay varios)
                    if (state.listaResidentes.size > 1) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(state.listaResidentes) { res ->
                                FilterChip(
                                    selected = state.residenteSeleccionado?.id_residente == res.id_residente,
                                    onClick = { viewModel.onSeleccionarResidente(res) },
                                    label = { Text("Residente: ${res.nombre}") }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Resumen KPI
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CyberStatCard(
                            titulo = "Próximas",
                            valor = "${state.kpi.proximas}",
                            icon = Icons.Default.CalendarMonth,
                            accentColor = TechBlue,
                            modifier = Modifier.weight(1f)
                        )
                        CyberStatCard(
                            titulo = "Confirmadas",
                            valor = "${state.kpi.confirmadas}",
                            icon = Icons.Default.MeetingRoom,
                            accentColor = LuminousTurquoise,
                            modifier = Modifier.weight(1f)
                        )
                        CyberStatCard(
                            titulo = "Realizadas",
                            valor = "${state.kpi.realizadas}",
                            icon = Icons.Default.MeetingRoom,
                            accentColor = CoralRed,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Próxima visita destacada si existe
                    if (state.proximaVisita != null) {
                        ProximaVisitaCard(
                            visita = state.proximaVisita,
                            onDetailClick = { onVisitaClick(state.proximaVisita.id_visita) }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Filtros Tab
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = state.filtroTab == FiltroFamiliarVisitaTab.TODAS,
                                onClick = { viewModel.onFiltroTabChange(FiltroFamiliarVisitaTab.TODAS) },
                                label = { Text("Todas") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = state.filtroTab == FiltroFamiliarVisitaTab.PROXIMAS,
                                onClick = { viewModel.onFiltroTabChange(FiltroFamiliarVisitaTab.PROXIMAS) },
                                label = { Text("Próximas") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = state.filtroTab == FiltroFamiliarVisitaTab.CONFIRMADAS,
                                onClick = { viewModel.onFiltroTabChange(FiltroFamiliarVisitaTab.CONFIRMADAS) },
                                label = { Text("Confirmadas") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = state.filtroTab == FiltroFamiliarVisitaTab.REALIZADAS,
                                onClick = { viewModel.onFiltroTabChange(FiltroFamiliarVisitaTab.REALIZADAS) },
                                label = { Text("Realizadas") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = state.filtroTab == FiltroFamiliarVisitaTab.CANCELADAS,
                                onClick = { viewModel.onFiltroTabChange(FiltroFamiliarVisitaTab.CANCELADAS) },
                                label = { Text("Canceladas") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Lista de Visitas
                    if (state.listaFiltrada.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.MeetingRoom,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No tienes visitas agendadas en esta categoría.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(
                                items = state.listaFiltrada,
                                key = { it.id_visita }
                            ) { visita ->
                                VisitaFamiliarCard(
                                    visita = visita,
                                    onClick = { onVisitaClick(visita.id_visita) }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(72.dp)) }
                        }
                    }
                }
            }
        }
    }
}
