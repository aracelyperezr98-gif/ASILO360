package com.example.asilo360.admin.personal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.asilo360.admin.personal.components.PersonalCard
import com.example.asilo360.data.model.Personal

/**
 * Pantalla principal de administración del Personal del Asilo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalScreen(
    onBackClicked: () -> Unit,
    viewModel: PersonalViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var isFormOpen by remember { mutableStateOf(false) }
    var personalParaEditar by remember { mutableStateOf<Personal?>(null) }
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
    if (isDetailOpen && uiState.selectedPersonal != null) {
        val trabajador = uiState.selectedPersonal!!
        PersonalDetailScreen(
            personal = trabajador,
            onEditClick = {
                personalParaEditar = trabajador
                isDetailOpen = false
                isFormOpen = true
            },
            onChangeStatusClick = {
                viewModel.solicitarCambioEstado(trabajador)
            },
            onBackClicked = {
                isDetailOpen = false
                viewModel.selectPersonalForDetail(null)
            }
        )
        return
    }

    // Pantalla de Formulario (Crear / Editar)
    if (isFormOpen) {
        PersonalFormScreen(
            personalParaEditar = personalParaEditar,
            onSubmit = { personalSubmit ->
                if (personalParaEditar != null) {
                    viewModel.actualizarPersonal(personalSubmit) {
                        isFormOpen = false
                        personalParaEditar = null
                        viewModel.selectPersonalForDetail(null)
                    }
                } else {
                    viewModel.crearPersonal(personalSubmit) {
                        isFormOpen = false
                    }
                }
            },
            onBackClicked = {
                isFormOpen = false
                personalParaEditar = null
            },
            isLoading = uiState.isOperating
        )
        return
    }

    // Diálogo de confirmación para Activar / Desactivar
    uiState.personalToToggleStatus?.let { trabajador ->
        val isActivo = trabajador.estado == 1
        val titulo = if (isActivo) "¿Desactivar trabajador?" else "¿Activar trabajador?"
        val mensaje = if (isActivo) {
            "El trabajador ${trabajador.nombre} dejará de aparecer como activo."
        } else {
            "El trabajador ${trabajador.nombre} volverá a estar activo."
        }
        val botonTexto = if (isActivo) "Desactivar" else "Activar"

        AlertDialog(
            onDismissRequest = { viewModel.cancelarCambioEstado() },
            title = {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = mensaje,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.confirmarCambioEstado() }) {
                    Text(text = botonTexto)
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
                            text = "Personal",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Gestiona al personal del asilo",
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
                    personalParaEditar = null
                    isFormOpen = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar Personal"
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
                placeholder = { Text("Buscar personal por nombre, puesto o correo...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar"
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

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

                uiState.errorMessage != null && uiState.personalList.isEmpty() -> {
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
                                text = "No se pudo cargar el personal.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = viewModel::cargarPersonal) {
                                Text(text = "Reintentar")
                            }
                        }
                    }
                }

                uiState.filteredPersonalList.isEmpty() -> {
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
                                imageVector = Icons.Default.Badge,
                                contentDescription = "Sin registros",
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No hay personal registrado.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    personalParaEditar = null
                                    isFormOpen = true
                                }
                            ) {
                                Text(text = "Agregar personal")
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
                            items = uiState.filteredPersonalList,
                            key = { it.id_personal }
                        ) { trabajador ->
                            PersonalCard(
                                personal = trabajador,
                                onClick = {
                                    viewModel.selectPersonalForDetail(trabajador)
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
