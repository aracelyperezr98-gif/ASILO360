package com.example.asilo360.admin.configuracion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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

/**
 * Pantalla para cambiar la contraseña de acceso del Administrador.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CambiarPasswordScreen(
    onChangePassword: (passwordActual: String, passwordNueva: String) -> Unit,
    onBackClicked: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var passwordActual by remember { mutableStateOf("") }
    var passwordNueva by remember { mutableStateOf("") }
    var passwordConfirmar by remember { mutableStateOf("") }

    var passwordActualError by remember { mutableStateOf<String?>(null) }
    var passwordNuevaError by remember { mutableStateOf<String?>(null) }
    var passwordConfirmarError by remember { mutableStateOf<String?>(null) }

    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        ConfirmacionDialog(
            title = "¿Deseas cambiar tu contraseña?",
            message = "A partir de tu próximo inicio de sesión deberás ingresar tu nueva contraseña.",
            confirmText = "Cambiar",
            onConfirm = {
                showConfirmDialog = false
                onChangePassword(passwordActual, passwordNueva)
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cambiar Contraseña",
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
                text = "Seguridad de la Cuenta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Contraseña Actual
            CustomTextField(
                value = passwordActual,
                onValueChange = {
                    passwordActual = it
                    if (passwordActualError != null) passwordActualError = null
                },
                placeholder = "Contraseña actual *",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                isError = passwordActualError != null,
                errorMessage = passwordActualError
            )

            // Nueva Contraseña
            CustomTextField(
                value = passwordNueva,
                onValueChange = {
                    passwordNueva = it
                    if (passwordNuevaError != null) passwordNuevaError = null
                },
                placeholder = "Nueva contraseña (mín. 8 caracteres) *",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                isError = passwordNuevaError != null,
                errorMessage = passwordNuevaError
            )

            // Confirmar Nueva Contraseña
            CustomTextField(
                value = passwordConfirmar,
                onValueChange = {
                    passwordConfirmar = it
                    if (passwordConfirmarError != null) passwordConfirmarError = null
                },
                placeholder = "Confirmar nueva contraseña *",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                isError = passwordConfirmarError != null,
                errorMessage = passwordConfirmarError
            )

            Spacer(modifier = Modifier.height(12.dp))

            PrimaryButton(
                text = "Cambiar contraseña",
                onClick = {
                    var isValid = true

                    if (passwordActual.isEmpty()) {
                        passwordActualError = "Ingresa tu contraseña actual"
                        isValid = false
                    }

                    if (passwordNueva.length < 8) {
                        passwordNuevaError = "La contraseña debe tener al menos 8 caracteres"
                        isValid = false
                    }

                    if (passwordConfirmar != passwordNueva) {
                        passwordConfirmarError = "Las contraseñas no coinciden"
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
