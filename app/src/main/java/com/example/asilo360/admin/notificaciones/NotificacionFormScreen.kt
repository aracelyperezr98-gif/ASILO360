package com.example.asilo360.admin.notificaciones

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.asilo360.components.CustomTextField
import com.example.asilo360.components.PrimaryButton
import com.example.asilo360.data.model.CrearNotificacionRequest
import com.example.asilo360.data.model.UsuarioDestinatarioOpcion

/**
 * Formulario para la creación y envío de notificaciones.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionFormScreen(
    usuariosDisponibles: List<UsuarioDestinatarioOpcion>,
    onSubmit: (CrearNotificacionRequest) -> Unit,
    onBackClicked: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Destinatario: ID de usuario específico O bien grupo ("todos", "familiares", "personal")
    var selectedUserId by remember { mutableStateOf<Int?>(null) }
    var selectedGroupKey by remember { mutableStateOf<String?>("todos") } // Por defecto: Todos
    var selectedDestinatarioText by remember { mutableStateOf("Todos los usuarios activos") }

    var titulo by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("General") }

    // Mensajes de error por validación
    var destinatarioError by remember { mutableStateOf<String?>(null) }
    var tituloError by remember { mutableStateOf<String?>(null) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    // Dropdowns
    var destinatarioExpanded by remember { mutableStateOf(false) }
    var tipoExpanded by remember { mutableStateOf(false) }

    val tiposList = listOf(
        "General",
        "Residente",
        "Consulta",
        "Visita",
        "Pago",
        "Actividad",
        "Aviso"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Enviar Notificación",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Selector 1: Destinatario
            Text(
                text = "Destinatario",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedDestinatarioText,
                    onValueChange = {},
                    readOnly = true,
                    isError = destinatarioError != null,
                    leadingIcon = { Icon(imageVector = Icons.Default.Group, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { destinatarioExpanded = true }) {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { destinatarioExpanded = true },
                    shape = RoundedCornerShape(12.dp)
                )

                DropdownMenu(
                    expanded = destinatarioExpanded,
                    onDismissRequest = { destinatarioExpanded = false }
                ) {
                    // Grupos globales
                    DropdownMenuItem(
                        text = { Text("📢 Todos los usuarios activos") },
                        onClick = {
                            selectedUserId = null
                            selectedGroupKey = "todos"
                            selectedDestinatarioText = "Todos los usuarios activos"
                            destinatarioError = null
                            destinatarioExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("👨‍👩‍👧 Todos los Familiares") },
                        onClick = {
                            selectedUserId = null
                            selectedGroupKey = "familiares"
                            selectedDestinatarioText = "Todos los Familiares"
                            destinatarioError = null
                            destinatarioExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("👨‍⚕️ Todo el Personal") },
                        onClick = {
                            selectedUserId = null
                            selectedGroupKey = "personal"
                            selectedDestinatarioText = "Todo el Personal"
                            destinatarioError = null
                            destinatarioExpanded = false
                        }
                    )

                    // Usuarios específicos
                    if (usuariosDisponibles.isNotEmpty()) {
                        usuariosDisponibles.forEach { u ->
                            DropdownMenuItem(
                                text = { Text("👤 ${u.nombre} - ${u.rol}") },
                                onClick = {
                                    selectedUserId = u.id_usuario
                                    selectedGroupKey = null
                                    selectedDestinatarioText = "${u.nombre} (${u.rol})"
                                    destinatarioError = null
                                    destinatarioExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            if (destinatarioError != null) {
                Text(
                    text = destinatarioError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Título
            Text(
                text = "Título de la Notificación",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            CustomTextField(
                value = titulo,
                onValueChange = {
                    if (it.length <= 150) {
                        titulo = it
                        if (tituloError != null) tituloError = null
                    }
                },
                placeholder = "Título (Máx. 150 caracteres) *",
                leadingIcon = Icons.Default.Title,
                isError = tituloError != null,
                errorMessage = tituloError
            )

            // Mensaje
            Text(
                text = "Mensaje",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Column {
                OutlinedTextField(
                    value = mensaje,
                    onValueChange = {
                        mensaje = it
                        if (mensajeError != null) mensajeError = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("Escribe el mensaje o aviso aquí... *") },
                    isError = mensajeError != null,
                    shape = RoundedCornerShape(12.dp)
                )
                if (mensajeError != null) {
                    Text(
                        text = mensajeError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }

            // Tipo de Notificación
            Text(
                text = "Tipo de Notificación",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = tipo,
                    onValueChange = {},
                    readOnly = true,
                    leadingIcon = { Icon(imageVector = Icons.Default.Category, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { tipoExpanded = true }) {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { tipoExpanded = true },
                    shape = RoundedCornerShape(12.dp)
                )

                DropdownMenu(
                    expanded = tipoExpanded,
                    onDismissRequest = { tipoExpanded = false }
                ) {
                    tiposList.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(t) },
                            onClick = {
                                tipo = t
                                tipoExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            PrimaryButton(
                text = "Enviar Notificación",
                onClick = {
                    var isValid = true

                    if (selectedUserId == null && selectedGroupKey == null) {
                        destinatarioError = "Selecciona un destinatario."
                        isValid = false
                    }

                    if (titulo.trim().isEmpty()) {
                        tituloError = "Ingresa un título."
                        isValid = false
                    }

                    if (mensaje.trim().isEmpty()) {
                        mensajeError = "Ingresa el mensaje."
                        isValid = false
                    }

                    if (isValid) {
                        val requestSubmit = CrearNotificacionRequest(
                            id_usuario = selectedUserId,
                            destinatario_tipo = selectedGroupKey,
                            titulo = titulo.trim(),
                            mensaje = mensaje.trim(),
                            tipo = tipo
                        )
                        onSubmit(requestSubmit)
                    }
                },
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
