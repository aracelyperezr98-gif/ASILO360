package com.example.asilo360.admin.visitas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.asilo360.admin.visitas.components.VisitaCard
import com.example.asilo360.data.model.Visita

/**
 * Pantalla principal de gestión de Visitas de Familiares del Administrador.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitasScreen(
    onBackClicked: () -> Unit,
    viewModel: VisitaViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var isFormOpen by remember { mutableStateOf(false) }
    var visitaParaEditar by remember { mutableStateOf<Visita?>(null) }
    var isDetailOpen by remember { mutableStateOf(false) }

    // Notificaciones emergentes
    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    // Pantalla de Detalle
    if (isDetailOpen && uiState.selectedVisita != null) {
        val visitaSeleccionada = uiState.selectedVisita!!
        VisitaDetailScreen(
            visita = visitaSeleccionada,
            onEditClick = {
                visitaParaEditar = visitaSeleccionada
                isDetailOpen = false
                isFormOpen = true
            },
            onChangeStatusClick = {
                viewModel.solicitarCambioEstado(visitaSeleccionada)
            },
            onBackClicked = {
                isDetailOpen = false
                viewModel.selectVisitaForDetail(null)
            }
        )
        return
    }

    // Pantalla de Formulario (Crear / Editar)
    if (isFormOpen) {
        VisitaFormScreen(
            visitaParaEditar = visitaParaEditar,
            residentesDisponibles = uiState.residentesDisponibles,
            familiaresDelResidente = uiState.familiaresDelResidente,
            onResidenteSelected = { idResidente ->
                viewModel.cargarFamiliaresPorResidente(idResidente)
            },
            onSubmit = { visitaSubmit ->
                if (visitaParaEditar != null) {
                    viewModel.actualizarVisita(visitaSubmit) {
                        isFormOpen = false
                        visitaParaEditar = null
                        viewModel.selectVisitaForDetail(null)
                    }
                } else {
                    viewModel.crearVisita(visitaSubmit) {
                        isFormOpen = false
                    }
                }
            },
            onBackClicked = {
                isFormOpen = false
                visitaParaEditar = null
            },
            isLoading = uiState.isOperating
        )
        return
    }

    // Diálogo de Cambio de Estado
    uiState.visitaToChangeStatus?.let { visita ->
        var selectedEstado by remember { mutableStateOf(visita.estado) }
        val opcionesEstado = listOf("Programada", "Realizada", "Cancelada")

        AlertDialog(
            onDismissRequest = { viewModel.cancelarCambioEstado() },
            title = {
                Text(
                    text = "Cambiar Estado de Visita",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    opcionesEstado.forEach { opcion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (opcion == selectedEstado),
                                    onClick = { selectedEstado = opcion }
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (opcion == selectedEstado),
                                onClick = { selectedEstado = opcion }
                            )
                            Text(
                                text = opcion,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.confirmarCambioEstado(selectedEstado) }) {
                    Text(text = "Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelarCambioEstado() }) {
                    Text(text = "Cancelar")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Visitas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Gestiona las visitas de los residentes",
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    visitaParaEditar = null
                    isFormOpen = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nueva Visita"
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Barra de Búsqueda
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar visita por residente, familiar o parentesco...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar"
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filtros de Estado [Todas] [Programadas] [Realizadas] [Canceladas]
            val filtros = listOf("Todas", "Programada", "Realizada", "Cancelada")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtros) { filtro ->
                    FilterChip(
                        selected = uiState.statusFilter.equals(filtro, ignoreCase = true),
                        onClick = { viewModel.onStatusFilterChange(filtro) },
                        label = { Text(text = filtro) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Estados de Carga, Error, Lista Vacía o Éxito
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.errorMessage != null && uiState.visitasList.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No se pudieron cargar las visitas.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = viewModel::cargarVisitas) {
                                Text(text = "Reintentar")
                            }
                        }
                    }
                }

                uiState.filteredVisitasList.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.MeetingRoom,
                                contentDescription = "Sin registros",
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No hay visitas registradas.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    visitaParaEditar = null
                                    isFormOpen = true
                                }
                            ) {
                                Text(text = "Nueva visita")
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.filteredVisitasList,
                            key = { it.id_visita }
                        ) { visita ->
                            VisitaCard(
                                visita = visita,
                                onClick = {
                                    viewModel.selectVisitaForDetail(visita)
                                    isDetailOpen = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
