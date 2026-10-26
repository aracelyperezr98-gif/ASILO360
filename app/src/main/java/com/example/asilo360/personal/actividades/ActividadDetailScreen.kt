package com.example.asilo360.personal.actividades

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
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
import com.example.asilo360.data.model.Actividad
import com.example.asilo360.data.session.UserSessionManager
import com.example.asilo360.personal.actividades.components.ActividadEstadoBadge
import com.example.asilo360.personal.actividades.components.ActividadFormContent
import com.example.asilo360.personal.actividades.components.formatearFechaAmical
import com.example.asilo360.personal.actividades.components.formatearHoraAmical
import com.example.asilo360.personal.residentes.components.InfoItem
import com.example.asilo360.personal.residentes.components.ResidenteInfoCard

/**
 * Pantalla de detalle para visualizar, editar o eliminar una actividad.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActividadDetailScreen(
    idActividad: Int,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActividadesViewModel = viewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val detailState by viewModel.detailState.collectAsState()
    val formState by viewModel.formState.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }

    // Estados para edición
    var editTipo by remember { mutableStateOf("") }
    var editTipoOtro by remember { mutableStateOf("") }
    var editFecha by remember { mutableStateOf("") }
    var editHora by remember { mutableStateOf("") }
    var editDescripcion by remember { mutableStateOf("") }

    LaunchedEffect(idActividad) {
        viewModel.obtenerActividad(idActividad)
    }

    LaunchedEffect(formState) {
        when (val state = formState) {
            is FormOperacionState.Exito -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.resetFormState()
                onBackClicked()
            }
            is FormOperacionState.Error -> {
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
                        text = if (isEditing) "Editar Actividad" else "Detalle de Actividad",
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
                is ActividadDetailUiState.Idle,
                is ActividadDetailUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Cargando detalle...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is ActividadDetailUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
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
                                onClick = { viewModel.obtenerActividad(idActividad) },
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

                is ActividadDetailUiState.Success -> {
                    val actividad = state.actividad
                    val currentUserId = UserSessionManager.getUserId()

                    // Verificar si pertenece al usuario actual
                    val esPropia = (actividad.id_usuario != null && actividad.id_usuario == currentUserId) ||
                            (actividad.personal != null && actividad.personal.equals(UserSessionManager.getUserFullName(), true))

                    if (isEditing) {
                        // Modo Edición
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            ActividadFormContent(
                                residentes = emptyList(),
                                idResidenteSeleccionado = actividad.id_residente,
                                onResidenteSelected = {},
                                tipoSeleccionado = editTipo,
                                onTipoSelected = { editTipo = it },
                                tipoEspecificoOtro = editTipoOtro,
                                onTipoOtroChanged = { editTipoOtro = it },
                                fecha = editFecha,
                                onFechaChanged = { editFecha = it },
                                hora = editHora,
                                onHoraChanged = { editHora = it },
                                descripcion = editDescripcion,
                                onDescripcionChanged = { editDescripcion = it },
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
                                        val tipoFinal = if (editTipo == "Otro") editTipoOtro.trim() else editTipo.trim()
                                        val updated = actividad.copy(
                                            tipo = tipoFinal,
                                            fecha = editFecha,
                                            hora = editHora,
                                            descripcion = editDescripcion.trim()
                                        )
                                        viewModel.actualizarActividad(
                                            actividad = updated,
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
                        // Modo Visualización Detalle
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Header con Residente y Tipo
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "👤 ${actividad.residente ?: "Residente"}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (!actividad.habitacion.isNullOrBlank()) {
                                        Text(
                                            text = "Habitación: ${actividad.habitacion}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                ActividadEstadoBadge(tipo = actividad.tipo)
                            }

                            // Sección: Detalles de Actividad
                            ResidenteInfoCard(titulo = "Información de la Actividad") {
                                InfoItem(
                                    label = "Tipo de actividad:",
                                    valor = actividad.tipo
                                )
                                InfoItem(
                                    label = "Fecha:",
                                    valor = formatearFechaAmical(actividad.fecha)
                                )
                                InfoItem(
                                    label = "Hora:",
                                    valor = formatearHoraAmical(actividad.hora)
                                )
                                InfoItem(
                                    label = "Descripción:",
                                    valor = actividad.descripcion
                                )
                            }

                            // Sección: Personal
                            ResidenteInfoCard(titulo = "Información del Personal") {
                                InfoItem(
                                    label = "Registrado por:",
                                    valor = actividad.personal
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Botones Editar y Eliminar (Solo si es propia o permitido)
                            if (esPropia) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            editTipo = actividad.tipo
                                            editTipoOtro = if (actividad.tipo !in listOf("Actividad física", "Ejercicio", "Terapia", "Recreación", "Lectura", "Música", "Manualidades", "Socialización", "Alimentación", "Paseo", "Juego")) actividad.tipo else ""
                                            if (editTipoOtro.isNotEmpty()) editTipo = "Otro"
                                            editFecha = actividad.fecha
                                            editHora = actividad.hora
                                            editDescripcion = actividad.descripcion
                                            isEditing = true
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Editar")
                                    }

                                    Button(
                                        onClick = { showDeleteDialog = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Eliminar")
                                    }
                                }
                            } else {
                                Text(
                                    text = "Nota: Solo puedes modificar o eliminar actividades registradas por ti.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Diálogo de Confirmación para Eliminar
                        if (showDeleteDialog) {
                            AlertDialog(
                                onDismissRequest = { showDeleteDialog = false },
                                title = { Text("Eliminar actividad") },
                                text = { Text("¿Deseas eliminar esta actividad? Esta acción no se puede deshacer.") },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            showDeleteDialog = false
                                            viewModel.eliminarActividad(
                                                idActividad = idActividad,
                                                onSuccess = { onBackClicked() }
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Text("Eliminar")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteDialog = false }) {
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
