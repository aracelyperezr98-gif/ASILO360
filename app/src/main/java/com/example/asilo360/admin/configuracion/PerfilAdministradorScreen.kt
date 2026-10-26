package com.example.asilo360.admin.configuracion

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import com.example.asilo360.admin.configuracion.components.ConfirmacionDialog
import com.example.asilo360.components.CustomTextField
import com.example.asilo360.components.PrimaryButton
import com.example.asilo360.data.model.ActualizarPerfilRequest
import com.example.asilo360.data.model.PerfilUsuario

/**
 * Pantalla para ver y editar el perfil de información del Administrador.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilAdministradorScreen(
    perfil: PerfilUsuario,
    onSaveProfile: (ActualizarPerfilRequest) -> Unit,
    onBackClicked: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var nombre by remember(perfil) { mutableStateOf(perfil.nombre) }
    var apellidoPaterno by remember(perfil) { mutableStateOf(perfil.apellido_paterno) }
    var apellidoMaterno by remember(perfil) { mutableStateOf(perfil.apellido_materno) }
    var correo by remember(perfil) { mutableStateOf(perfil.correo) }
    var telefono by remember(perfil) { mutableStateOf(perfil.telefono) }

    var nombreError by remember { mutableStateOf<String?>(null) }
    var apellidoPaternoError by remember { mutableStateOf<String?>(null) }
    var correoError by remember { mutableStateOf<String?>(null) }
    var telefonoError by remember { mutableStateOf<String?>(null) }

    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        ConfirmacionDialog(
            title = "¿Deseas guardar los cambios?",
            message = "Tus datos de perfil se actualizarán en el sistema.",
            confirmText = "Guardar",
            onConfirm = {
                showConfirmDialog = false
                val req = ActualizarPerfilRequest(
                    id_usuario = perfil.id_usuario,
                    nombre = nombre.trim(),
                    apellido_paterno = apellidoPaterno.trim(),
                    apellido_materno = apellidoMaterno.trim(),
                    correo = correo.trim(),
                    telefono = telefono.trim()
                )
                onSaveProfile(req)
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mi Perfil",
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
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Información Personal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Nombre
            CustomTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    if (nombreError != null) nombreError = null
                },
                placeholder = "Nombre *",
                leadingIcon = Icons.Default.Person,
                isError = nombreError != null,
                errorMessage = nombreError
            )

            // Apellido Paterno
            CustomTextField(
                value = apellidoPaterno,
                onValueChange = {
                    apellidoPaterno = it
                    if (apellidoPaternoError != null) apellidoPaternoError = null
                },
                placeholder = "Apellido Paterno *",
                leadingIcon = Icons.Default.Person,
                isError = apellidoPaternoError != null,
                errorMessage = apellidoPaternoError
            )

            // Apellido Materno
            CustomTextField(
                value = apellidoMaterno,
                onValueChange = { apellidoMaterno = it },
                placeholder = "Apellido Materno",
                leadingIcon = Icons.Default.Person
            )

            // Correo Electrónico
            CustomTextField(
                value = correo,
                onValueChange = {
                    correo = it
                    if (correoError != null) correoError = null
                },
                placeholder = "Correo Electrónico *",
                leadingIcon = Icons.Default.Email,
                isError = correoError != null,
                errorMessage = correoError
            )

            // Teléfono
            CustomTextField(
                value = telefono,
                onValueChange = {
                    telefono = it
                    if (telefonoError != null) telefonoError = null
                },
                placeholder = "Teléfono de contacto",
                leadingIcon = Icons.Default.Phone,
                isError = telefonoError != null,
                errorMessage = telefonoError
            )

            // Rol (Solo lectura)
            Text(
                text = "Rol de usuario (No modificable)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = perfil.rol,
                onValueChange = {},
                readOnly = true,
                leadingIcon = { Icon(imageVector = Icons.Default.Badge, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            PrimaryButton(
                text = "Guardar cambios",
                onClick = {
                    var isValid = true

                    if (nombre.trim().isEmpty()) {
                        nombreError = "Ingresa tu nombre"
                        isValid = false
                    }

                    if (apellidoPaterno.trim().isEmpty()) {
                        apellidoPaternoError = "Ingresa tu apellido paterno"
                        isValid = false
                    }

                    if (correo.trim().isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches()) {
                        correoError = "Ingresa un correo electrónico válido"
                        isValid = false
                    }

                    if (isValid) {
                        showConfirmDialog = true
                    }
                },
                enabled = !isLoading
            )
        }
    }
}
