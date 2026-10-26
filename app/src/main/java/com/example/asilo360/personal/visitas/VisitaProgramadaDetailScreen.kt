package com.example.asilo360.personal.visitas

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.asilo360.personal.actividades.components.formatearFechaAmical
import com.example.asilo360.personal.actividades.components.formatearHoraAmical
import com.example.asilo360.personal.residentes.components.InfoItem
import com.example.asilo360.personal.residentes.components.ResidenteInfoCard
import com.example.asilo360.personal.visitas.components.VisitaEstadoBadge

/**
 * Pantalla de detalle para visualizar y gestionar el seguimiento de una Visita Programada.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitaProgramadaDetailScreen(
    idVisita: Int,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VisitasProgramadasViewModel = viewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val detailState by viewModel.detailState.collectAsState()
    val operacionState by viewModel.operacionState.collectAsState()

    var showConfirmDialog by remember { mutableStateOf(false) }
    var targetAction by remember { mutableStateOf("") } // "Confirmar", "Realizada", "Cancelada"
    var observacionesText by remember { mutableStateOf("") }

    LaunchedEffect(idVisita) {
        viewModel.cargarVisita(idVisita)
    }

    LaunchedEffect(operacionState) {
        when (val state = operacionState) {
            is VisitaOperacionState.Exito -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.resetOperacionState()
            }
            is VisitaOperacionState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetOperacionState()
            }
            else -> {}
        }
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = detailState) {
                is VisitaDetailUiState.Idle,
                is VisitaDetailUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Cargando detalle de la visita...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is VisitaDetailUiState.Error -> {
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
                                onClick = { viewModel.cargarVisita(idVisita) },
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

                is VisitaDetailUiState.Success -> {
                    val visita = state.visita

                    LaunchedEffect(visita.observaciones) {
                        observacionesText = visita.observaciones.orEmpty()
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "👤 ${visita.nombre_residente ?: "Residente"}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (!visita.habitacion.isNullOrBlank()) {
                                    Text(
                                        text = "Habitación: ${visita.habitacion}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            VisitaEstadoBadge(estado = visita.estado)
                        }

                        // Sección: Residente
                        ResidenteInfoCard(titulo = "Información del Residente") {
                            InfoItem(
                                label = "Nombre completo:",
                                valor = visita.nombre_residente
                            )
                            InfoItem(
                                label = "Habitación:",
                                valor = visita.habitacion
                            )
                        }

                        // Sección: Visitante
                        ResidenteInfoCard(titulo = "Información del Visitante") {
                            InfoItem(
                                label = "Nombre del familiar:",
                                valor = visita.nombre_familiar
                            )
                            InfoItem(
                                label = "Parentesco:",
                                valor = visita.parentesco
                            )
                            InfoItem(
                                label = "Número de visitantes:",
                                valor = "${visita.numero_visitantes} ${if (visita.numero_visitantes == 1) "persona" else "personas"}"
                            )
                        }

                        // Sección: Detalle de la Visita
                        ResidenteInfoCard(titulo = "Detalle de la Programación") {
                            InfoItem(
                                label = "Fecha de la visita:",
                                valor = formatearFechaAmical(visita.fecha)
                            )
                            InfoItem(
                                label = "Hora programada:",
                                valor = formatearHoraAmical(visita.hora)
                            )
                            InfoItem(
                                label = "Estado de la visita:",
                                valor = visita.estado
                            )
                        }

                        // Sección: Observaciones del Personal
                        ResidenteInfoCard(titulo = "Observaciones de Seguimiento") {
                            OutlinedTextField(
                                value = observacionesText,
                                onValueChange = { observacionesText = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                placeholder = { Text("Ej: El familiar se presentó con identificación. Visita sin contratiempos...") },
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.guardarObservaciones(idVisita, observacionesText, onSuccess = {})
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Save,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Guardar observaciones")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Botones de Cambios de Estado
                        val estadoActual = visita.estado.trim()

                        if (!estadoActual.equals("Realizada", true) && !estadoActual.equals("Cancelada", true)) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Confirmar visita (Si está Programada)
                                    if (estadoActual.equals("Programada", true)) {
                                        Button(
                                            onClick = {
                                                targetAction = "Confirmar"
                                                showConfirmDialog = true
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Confirmar Visita")
                                        }
                                    }

                                    // Marcar Realizada (Si está Confirmada)
                                    if (estadoActual.equals("Confirmada", true)) {
                                        Button(
                                            onClick = {
                                                targetAction = "Realizada"
                                                showConfirmDialog = true
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Marcar Realizada")
                                        }
                                    }

                                    // Cancelar Visita (Programada o Confirmada)
                                    OutlinedButton(
                                        onClick = {
                                            targetAction = "Cancelada"
                                            showConfirmDialog = true
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Cancel,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Cancelar Visita")
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = if (estadoActual.equals("Realizada", true)) "La visita ya ha sido completada." else "La visita fue cancelada.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Diálogo de Confirmación de Acción
                        if (showConfirmDialog) {
                            val promptTexto = when (targetAction) {
                                "Confirmar" -> "¿Deseas confirmar esta visita?"
                                "Realizada" -> "¿Deseas marcar esta visita como realizada?"
                                "Cancelada" -> "¿Deseas cancelar esta visita?"
                                else -> "¿Deseas cambiar el estado de la visita?"
                            }

                            AlertDialog(
                                onDismissRequest = { showConfirmDialog = false },
                                title = { Text("Confirmar acción") },
                                text = { Text(promptTexto) },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            showConfirmDialog = false
                                            when (targetAction) {
                                                "Confirmar" -> viewModel.confirmarVisita(idVisita, onSuccess = {})
                                                "Realizada" -> viewModel.marcarRealizada(idVisita, onSuccess = {})
                                                "Cancelada" -> viewModel.cancelarVisita(idVisita, onSuccess = {})
                                            }
                                        }
                                    ) {
                                        Text("Confirmar")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showConfirmDialog = false }) {
                                        Text("Cancelar")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
