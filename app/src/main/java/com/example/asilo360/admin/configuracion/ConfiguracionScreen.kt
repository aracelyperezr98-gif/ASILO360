package com.example.asilo360.admin.configuracion

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.asilo360.admin.configuracion.components.ConfiguracionItem
import com.example.asilo360.admin.configuracion.components.ConfiguracionSection
import com.example.asilo360.admin.configuracion.components.ConfirmacionDialog
import com.example.asilo360.data.session.UserSessionManager

/**
 * Pantalla principal de Configuración y Preferencias del Administrador.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionScreen(
    onLogout: () -> Unit,
    onBackClicked: () -> Unit,
    viewModel: ConfiguracionViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var isProfileOpen by remember { mutableStateOf(false) }
    var isPasswordOpen by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    // Diálogo de Confirmar Cierre de Sesión
    if (showLogoutDialog) {
        ConfirmacionDialog(
            title = "¿Deseas cerrar sesión?",
            message = "Se cerrará tu sesión activa y regresarás a la pantalla de inicio.",
            confirmText = "Sí, cerrar sesión",
            isDestructive = true,
            onConfirm = {
                showLogoutDialog = false
                UserSessionManager.clearSession()
                onLogout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    // Subpantalla de Editar Perfil
    if (isProfileOpen) {
        PerfilAdministradorScreen(
            perfil = uiState.perfil,
            onSaveProfile = { req ->
                viewModel.actualizarPerfil(req) {
                    isProfileOpen = false
                }
            },
            onBackClicked = { isProfileOpen = false },
            isLoading = uiState.isOperating
        )
        return
    }

    // Subpantalla de Cambiar Contraseña
    if (isPasswordOpen) {
        CambiarPasswordScreen(
            onChangePassword = { passActual, passNueva ->
                viewModel.cambiarPassword(passActual, passNueva) {
                    isPasswordOpen = false
                }
            },
            onBackClicked = { isPasswordOpen = false },
            isLoading = uiState.isOperating
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Configuración",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Administra las preferencias y seguridad",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Tarjeta Superior de Avatar y Datos del Administrador
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        val nombreComp = "${uiState.perfil.nombre} ${uiState.perfil.apellido_paterno}".trim()
                        Text(
                            text = nombreComp.ifBlank { "Administrador" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = uiState.perfil.correo,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = uiState.perfil.rol,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // SECCIÓN 1: MI CUENTA
            ConfiguracionSection(title = "Mi Cuenta") {
                ConfiguracionItem(
                    title = "Mi perfil",
                    subtitle = "Editar nombre, correo y teléfono",
                    icon = Icons.Default.Person,
                    onClick = { isProfileOpen = true }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ConfiguracionItem(
                    title = "Cambiar contraseña",
                    subtitle = "Actualiza tu clave de acceso",
                    icon = Icons.Default.Lock,
                    onClick = { isPasswordOpen = true }
                )
            }

            // SECCIÓN 2: PREFERENCIAS
            ConfiguracionSection(title = "Preferencias") {
                ConfiguracionItem(
                    title = "Notificaciones",
                    subtitle = "Recibir avisos en la aplicación",
                    icon = Icons.Default.Notifications,
                    trailingContent = {
                        Switch(
                            checked = uiState.preferencias.notificacionesActivas,
                            onCheckedChange = viewModel::updateNotificaciones
                        )
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ConfiguracionItem(
                    title = "Sonido de notificaciones",
                    subtitle = "Reproducir tono al recibir avisos",
                    icon = Icons.Default.VolumeUp,
                    trailingContent = {
                        Switch(
                            checked = uiState.preferencias.sonidoNotificaciones,
                            onCheckedChange = viewModel::updateSonido
                        )
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ConfiguracionItem(
                    title = "Confirmar acciones importantes",
                    subtitle = "Mostrar diálogo antes de guardar o eliminar",
                    icon = Icons.Default.Security,
                    trailingContent = {
                        Switch(
                            checked = uiState.preferencias.confirmarAcciones,
                            onCheckedChange = viewModel::updateConfirmar
                        )
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ConfiguracionItem(
                    title = "Actualizar automáticamente",
                    subtitle = "Recargar datos en segundo plano",
                    icon = Icons.Default.Refresh,
                    trailingContent = {
                        Switch(
                            checked = uiState.preferencias.actualizarAutomaticamente,
                            onCheckedChange = viewModel::updateAutoUpdate
                        )
                    }
                )
            }

            // SECCIÓN 3: SEGURIDAD
            ConfiguracionSection(title = "Seguridad") {
                ConfiguracionItem(
                    title = "Cambiar contraseña",
                    subtitle = "Renovar credenciales de autenticación",
                    icon = Icons.Default.Lock,
                    onClick = { isPasswordOpen = true }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ConfiguracionItem(
                    title = "Cerrar sesión",
                    subtitle = "Finalizar la sesión activa del usuario",
                    icon = Icons.Default.Logout,
                    onClick = { showLogoutDialog = true }
                )
            }

            // SECCIÓN 4: INFORMACIÓN DEL SISTEMA
            ConfiguracionSection(title = "Información del Sistema") {
                ConfiguracionItem(
                    title = "Asilo 360",
                    subtitle = "Versión 1.0.0 • Android",
                    icon = Icons.Default.Info
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ConfiguracionItem(
                    title = "Rol actual",
                    subtitle = "Administrador",
                    icon = Icons.Default.Badge
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Acerca de Asilo 360",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Asilo 360 es una plataforma orientada a mejorar la administración, organización y comunicación dentro de los centros de cuidado para adultos mayores.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "\"La tecnología conecta, el cuidado transforma.\"",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
