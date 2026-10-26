package com.example.asilo360.admin.pagos

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
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.asilo360.admin.pagos.components.PagoCard
import com.example.asilo360.data.model.Pago

/**
 * Pantalla principal del módulo de gestión de Pagos del Administrador.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagosScreen(
    onBackClicked: () -> Unit,
    viewModel: PagoViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var isFormOpen by remember { mutableStateOf(false) }
    var pagoParaEditar by remember { mutableStateOf<Pago?>(null) }
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
    if (isDetailOpen && uiState.selectedPago != null) {
        val pagoSeleccionado = uiState.selectedPago!!
        PagoDetailScreen(
            pago = pagoSeleccionado,
            onEditClick = {
                pagoParaEditar = pagoSeleccionado
                isDetailOpen = false
                isFormOpen = true
            },
            onChangeStatusClick = {
                viewModel.solicitarCambioEstado(pagoSeleccionado)
            },
            onDeleteClick = {
                viewModel.solicitarEliminacion(pagoSeleccionado)
            },
            onBackClicked = {
                isDetailOpen = false
                viewModel.selectPagoForDetail(null)
            }
        )
        return
    }

    // Pantalla de Formulario (Crear / Editar)
    if (isFormOpen) {
        PagoFormScreen(
            pagoParaEditar = pagoParaEditar,
            residentesDisponibles = uiState.residentesDisponibles,
            onSubmit = { pagoSubmit ->
                if (pagoParaEditar != null) {
                    viewModel.actualizarPago(pagoSubmit) {
                        isFormOpen = false
                        pagoParaEditar = null
                        viewModel.selectPagoForDetail(null)
                    }
                } else {
                    viewModel.crearPago(pagoSubmit) {
                        isFormOpen = false
                    }
                }
            },
            onBackClicked = {
                isFormOpen = false
                pagoParaEditar = null
            },
            isLoading = uiState.isOperating
        )
        return
    }

    // Diálogo de Cambio de Estado
    uiState.pagoToChangeStatus?.let { pago ->
        var selectedEstado by remember { mutableStateOf(pago.estado) }
        val opcionesEstado = listOf("Pagado", "Pendiente", "Cancelado")

        AlertDialog(
            onDismissRequest = { viewModel.cancelarCambioEstado() },
            title = {
                Text(
                    text = "Cambiar Estado del Pago",
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
                    Text(text = "Confirmar")
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

    // Diálogo de Confirmación para Eliminar
    uiState.pagoToDelete?.let { pago ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelarEliminacion() },
            title = {
                Text(
                    text = "¿Eliminar pago?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Esta acción no se puede deshacer.",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.confirmarEliminacion {
                            isDetailOpen = false
                            viewModel.selectPagoForDetail(null)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(text = "Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelarEliminacion() }) {
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
                            text = "Pagos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Administración de pagos de residentes",
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
                    pagoParaEditar = null
                    isFormOpen = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Registrar Pago"
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
                placeholder = { Text("Buscar pago por residente, concepto o método...") },
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

            // Filtros de Estado [Todos] [Pagados] [Pendientes] [Cancelados]
            val filtros = listOf("Todos", "Pagado", "Pendiente", "Cancelado")
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

                uiState.errorMessage != null && uiState.pagosList.isEmpty() -> {
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
                                text = "No se pudieron cargar los pagos.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = viewModel::cargarPagos) {
                                Text(text = "Reintentar")
                            }
                        }
                    }
                }

                uiState.filteredPagosList.isEmpty() -> {
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
                                imageVector = Icons.Default.Payments,
                                contentDescription = "Sin registros",
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No hay pagos registrados.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    pagoParaEditar = null
                                    isFormOpen = true
                                }
                            ) {
                                Text(text = "Registrar primer pago")
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
                            items = uiState.filteredPagosList,
                            key = { it.id_pago }
                        ) { pago ->
                            PagoCard(
                                pago = pago,
                                onClick = {
                                    viewModel.selectPagoForDetail(pago)
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
