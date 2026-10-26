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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.asilo360.familiar.visitas.components.VisitaEstadoBadge
import com.example.asilo360.personal.residentes.components.InfoItem
import com.example.asilo360.personal.residentes.components.ResidenteInfoCard

/**
 * Pantalla de detalle de una visita individual para el rol Familiar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitaFamiliarDetailScreen(
    idVisita: Int,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VisitasFamiliarViewModel = viewModel()
) {
    val detailState by viewModel.detailState.collectAsState()

    LaunchedEffect(idVisita) {
        viewModel.obtenerVisita(idVisita)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detalle de Visita",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
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
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = detailState) {
                is VisitaFamiliarDetailUiState.Idle,
                is VisitaFamiliarDetailUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Cargando información de la visita...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is VisitaFamiliarDetailUiState.Error -> {
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
                                onClick = { viewModel.obtenerVisita(idVisita) },
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

                is VisitaFamiliarDetailUiState.Success -> {
                    val visita = state.visita

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = visita.nombre_residente.orEmpty().ifBlank { "Mi Residente" },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            VisitaEstadoBadge(estado = visita.estado)
                        }

                        ResidenteInfoCard(titulo = "Información del Encuentro") {
                            InfoItem(
                                label = "Residente:",
                                valor = visita.nombre_residente.orEmpty().ifBlank { "N/A" }
                            )
                            InfoItem(
                                label = "Habitación:",
                                valor = visita.habitacion.orEmpty().ifBlank { "Hab. Asignada" }
                            )
                            InfoItem(
                                label = "Fecha de la visita:",
                                valor = visita.fecha
                            )
                            InfoItem(
                                label = "Hora programada:",
                                valor = visita.hora
                            )
                        }

                        ResidenteInfoCard(titulo = "Datos del Visitante") {
                            InfoItem(
                                label = "Familiar que visita:",
                                valor = visita.nombre_familiar.orEmpty().ifBlank { "Familiar Registrado" }
                            )
                            InfoItem(
                                label = "Parentesco:",
                                valor = visita.parentesco.orEmpty().ifBlank { "Familiar Autorizado" }
                            )
                            InfoItem(
                                label = "Número de visitantes:",
                                valor = "${visita.numero_visitantes} persona(s)"
                            )
                            InfoItem(
                                label = "Estado del encuentro:",
                                valor = visita.estado
                            )
                        }

                        if (!visita.observaciones.isNullOrBlank()) {
                            ResidenteInfoCard(titulo = "Observaciones") {
                                Text(
                                    text = visita.observaciones,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
