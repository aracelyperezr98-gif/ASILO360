package com.example.asilo360.personal.consultas

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.asilo360.personal.consultas.components.ConsultaEstadoBadge
import com.example.asilo360.personal.consultas.components.ConsultaMedicaFormContent
import com.example.asilo360.personal.residentes.components.InfoItem
import com.example.asilo360.personal.residentes.components.ResidenteInfoCard

/**
 * Pantalla de detalle para visualizar, actualizar estado o editar una consulta médica.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultaMedicaDetailScreen(
    idConsulta: Int,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConsultasMedicasViewModel = viewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val detailState by viewModel.detailState.collectAsState()
    val formState by viewModel.formState.collectAsState()

    var showStatusDialog by remember { mutableStateOf(false) }
    var targetStatus by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }

    // Campos de edición
    var editFecha by remember { mutableStateOf("") }
    var editHora by remember { mutableStateOf("") }
    var editMotivo by remember { mutableStateOf("") }
    var editEstado by remember { mutableStateOf("") }
    var editObservaciones by remember { mutableStateOf("") }

    LaunchedEffect(idConsulta) {
        viewModel.obtenerConsulta(idConsulta)
    }

    LaunchedEffect(formState) {
        when (val state = formState) {
            is ConsultaFormState.Exito -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.resetFormState()
                if (isEditing) isEditing = false
            }
            is ConsultaFormState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetFormState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Editar Consulta" else "Detalle de Consulta",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isEditing) isEditing = false else onBackClicked()
                    }) {
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
                is ConsultaDetailUiState.Idle,
                is ConsultaDetailUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Cargando detalle de la consulta...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is ConsultaDetailUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
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
                                onClick = { viewModel.obtenerConsulta(idConsulta) },
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

                is ConsultaDetailUiState.Success -> {
                    val consulta = state.consulta

                    if (isEditing) {
                        // Modo Edición
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            ConsultaMedicaFormContent(
                                residentes = emptyList(),
                                idResidenteSeleccionado = consulta.id_residente,
                                onResidenteSelected = {},
                                fecha = editFecha,
                                onFechaChanged = { editFecha = it },
                                hora = editHora,
                                onHoraChanged = { editHora = it },
                                motivo = editMotivo,
                                onMotivoChanged = { editMotivo = it },
                                estado = editEstado,
                                onEstadoChanged = { editEstado = it },
                                observaciones = editObservaciones,
                                onObservacionesChanged = { editObservaciones = it },
                                esEdicion = true
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { isEditing = false },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Cancelar")
                                }

                                Button(
                                    onClick = {
                                        val updated = consulta.copy(
                                            fecha = editFecha,
                                            hora = editHora,
                                            motivo = editMotivo.trim(),
                                            estado = editEstado,
                                            observaciones = editObservaciones.trim().ifBlank { null }
                                        )
                                        viewModel.actualizarConsulta(
                                            consulta = updated,
                                            onSuccess = { isEditing = false }
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Guardar cambios", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // Modo Detalle
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
                                        text = "👤 ${consulta.obtenerNombreResidente()}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (!consulta.habitacion.isNullOrBlank()) {
                                        Text(
                                            text = "Habitación: ${consulta.habitacion}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                ConsultaEstadoBadge(estado = consulta.estado)
                            }

                            // Info Residente
                            ResidenteInfoCard(titulo = "Información del Residente") {
                                InfoItem(
                                    label = "Nombre completo:",
                                    valor = consulta.obtenerNombreResidente()
                                )
                                InfoItem(
                                    label = "Habitación:",
                                    valor = consulta.habitacion
                                )
                                if (!consulta.sexo.isNullOrBlank()) {
                                    InfoItem(
                                        label = "Sexo:",
                                        valor = consulta.sexo
                                    )
                                }
                                if (!consulta.fecha_nacimiento.isNullOrBlank()) {
                                    InfoItem(
                                        label = "Fecha de nacimiento:",
                                        valor = consulta.fecha_nacimiento
                                    )
                                }
                            }

                            // Info Consulta
                            ResidenteInfoCard(titulo = "Información de la Consulta") {
                                InfoItem(
                                    label = "Fecha:",
                                    valor = formatearFechaAmical(consulta.fecha)
                                )
                                InfoItem(
                                    label = "Hora:",
                                    valor = formatearHoraAmical(consulta.hora)
                                )
                                InfoItem(
                                    label = "Motivo de la consulta:",
                                    valor = consulta.motivo
                                )
                                InfoItem(
                                    label = "Estado actual:",
                                    valor = consulta.estado
                                )
                                InfoItem(
                                    label = "Observaciones médicas:",
                                    valor = consulta.observaciones
                                )
                            }

                            // Info Personal
                            ResidenteInfoCard(titulo = "Personal Responsable") {
                                InfoItem(
                                    label = "Nombre del personal:",
                                    valor = consulta.obtenerNombrePersonal()
                                )
                                if (!consulta.puesto_personal.isNullOrBlank()) {
                                    InfoItem(
                                        label = "Puesto:",
                                        valor = consulta.puesto_personal
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Botones de acción rápida de Estado y Edición
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    if (!consulta.estado.equals("Realizada", true)) {
                                        Button(
                                            onClick = {
                                                targetStatus = "Realizada"
                                                showStatusDialog = true
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

                                    if (!consulta.estado.equals("Cancelada", true)) {
                                        OutlinedButton(
                                            onClick = {
                                                targetStatus = "Cancelada"
                                                showStatusDialog = true
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
                                            Text("Cancelar Consulta")
                                        }
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        editFecha = consulta.fecha
                                        editHora = consulta.hora
                                        editMotivo = consulta.motivo
                                        editEstado = consulta.estado
                                        editObservaciones = consulta.observaciones.orEmpty()
                                        isEditing = true
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Editar Consulta")
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Diálogo de Confirmación de Cambio de Estado
                        if (showStatusDialog) {
                            AlertDialog(
                                onDismissRequest = { showStatusDialog = false },
                                title = { Text("Cambiar estado de consulta") },
                                text = { Text("¿Deseas cambiar el estado de esta consulta a \"$targetStatus\"?") },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            showStatusDialog = false
                                            viewModel.cambiarEstado(
                                                idConsulta = idConsulta,
                                                nuevoEstado = targetStatus,
                                                onSuccess = {}
                                            )
                                        }
                                    ) {
                                        Text("Confirmar")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showStatusDialog = false }) {
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
