package com.example.asilo360.admin.personal

import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Work
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.asilo360.components.CustomTextField
import com.example.asilo360.components.PrimaryButton
import com.example.asilo360.data.model.Personal

/**
 * Formulario para registrar un nuevo trabajador o actualizar uno existente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalFormScreen(
    personalParaEditar: Personal?,
    onSubmit: (Personal) -> Unit,
    onBackClicked: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isEditing = personalParaEditar != null

    var nombre by remember { mutableStateOf(personalParaEditar?.nombre ?: "") }
    var apellidoPaterno by remember { mutableStateOf(personalParaEditar?.apellido_paterno ?: "") }
    var apellidoMaterno by remember { mutableStateOf(personalParaEditar?.apellido_materno ?: "") }
    var telefono by remember { mutableStateOf(personalParaEditar?.telefono ?: "") }

    var correo by remember { mutableStateOf(personalParaEditar?.correo ?: "") }
    var password by remember { mutableStateOf("") }

    var puesto by remember { mutableStateOf(personalParaEditar?.puesto ?: "") }
    var fechaIngreso by remember { mutableStateOf(personalParaEditar?.fecha_ingreso ?: "2026-01-01") }
    var estado by remember { mutableStateOf(personalParaEditar?.estado ?: 1) }

    // Mensajes de error por campo
    var nombreError by remember { mutableStateOf<String?>(null) }
    var apellidoPaternoError by remember { mutableStateOf<String?>(null) }
    var correoError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var puestoError by remember { mutableStateOf<String?>(null) }
    var fechaIngresoError by remember { mutableStateOf<String?>(null) }

    var estadoExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Editar Trabajador" else "Registrar Personal",
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

            // Sección 1: Datos Personales
            Text(
                text = "Datos Personales",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            CustomTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    if (nombreError != null) nombreError = null
                },
                placeholder = "Nombre(s) *",
                leadingIcon = Icons.Default.Person,
                isError = nombreError != null,
                errorMessage = nombreError
            )

            CustomTextField(
                value = apellidoPaterno,
                onValueChange = {
                    apellidoPaterno = it
                    if (apellidoPaternoError != null) apellidoPaternoError = null
                },
                placeholder = "Apellido Paterno *",
                leadingIcon = Icons.Default.Badge,
                isError = apellidoPaternoError != null,
                errorMessage = apellidoPaternoError
            )

            CustomTextField(
                value = apellidoMaterno,
                onValueChange = { apellidoMaterno = it },
                placeholder = "Apellido Materno (Opcional)",
                leadingIcon = Icons.Default.Badge
            )

            CustomTextField(
                value = telefono,
                onValueChange = { telefono = it },
                placeholder = "Teléfono de Contacto",
                leadingIcon = Icons.Default.Phone,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            // Sección 2: Datos de Acceso
            Text(
                text = "Datos de Acceso a la Cuenta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            CustomTextField(
                value = correo,
                onValueChange = {
                    correo = it
                    if (correoError != null) correoError = null
                },
                placeholder = "Correo Electrónico *",
                leadingIcon = Icons.Default.Email,
                isError = correoError != null,
                errorMessage = correoError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            // Contraseña sólo requerida al registrar nuevo personal
            if (!isEditing) {
                CustomTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (passwordError != null) passwordError = null
                    },
                    placeholder = "Contraseña de Acceso *",
                    leadingIcon = Icons.Default.Lock,
                    isPassword = true,
                    isError = passwordError != null,
                    errorMessage = passwordError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
            }

            // Sección 3: Datos Laborales
            Text(
                text = "Información Laboral",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            CustomTextField(
                value = puesto,
                onValueChange = {
                    puesto = it
                    if (puestoError != null) puestoError = null
                },
                placeholder = "Puesto (Ej: Enfermera, Médico) *",
                leadingIcon = Icons.Default.Work,
                isError = puestoError != null,
                errorMessage = puestoError
            )

            CustomTextField(
                value = fechaIngreso,
                onValueChange = {
                    fechaIngreso = it
                    if (fechaIngresoError != null) fechaIngresoError = null
                },
                placeholder = "Fecha de Ingreso (AAAA-MM-DD) *",
                leadingIcon = Icons.Default.CalendarToday,
                isError = fechaIngresoError != null,
                errorMessage = fechaIngresoError
            )

            // Selector de Estado
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = if (estado == 1) "Activo" else "Inactivo",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Estado del Trabajador") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Info, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { estadoExpanded = true }) {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { estadoExpanded = true },
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(
                    expanded = estadoExpanded,
                    onDismissRequest = { estadoExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Activo") },
                        onClick = { estado = 1; estadoExpanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Inactivo") },
                        onClick = { estado = 0; estadoExpanded = false }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón de Enviar
            PrimaryButton(
                text = if (isEditing) "Guardar Cambios" else "Registrar Trabajador",
                onClick = {
                    var isValid = true

                    if (nombre.trim().isEmpty()) {
                        nombreError = "El nombre es obligatorio."
                        isValid = false
                    }

                    if (apellidoPaterno.trim().isEmpty()) {
                        apellidoPaternoError = "El apellido paterno es obligatorio."
                        isValid = false
                    }

                    if (correo.trim().isEmpty()) {
                        correoError = "Ingresa un correo electrónico."
                        isValid = false
                    } else if (!Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches()) {
                        correoError = "Ingresa un correo electrónico válido."
                        isValid = false
                    }

                    if (!isEditing && password.trim().isEmpty()) {
                        passwordError = "La contraseña es obligatoria."
                        isValid = false
                    }

                    if (puesto.trim().isEmpty()) {
                        puestoError = "El puesto es obligatorio."
                        isValid = false
                    }

                    if (fechaIngreso.trim().isEmpty()) {
                        fechaIngresoError = "La fecha de ingreso es obligatoria."
                        isValid = false
                    }

                    if (isValid) {
                        val personalSubmit = Personal(
                            id_personal = personalParaEditar?.id_personal ?: 0,
                            id_usuario = personalParaEditar?.id_usuario ?: 0,
                            nombre = nombre.trim(),
                            apellido_paterno = apellidoPaterno.trim(),
                            apellido_materno = apellidoMaterno.trim().ifBlank { null },
                            correo = correo.trim(),
                            password = if (!isEditing) password.trim() else null,
                            telefono = telefono.trim().ifBlank { null },
                            puesto = puesto.trim(),
                            fecha_ingreso = fechaIngreso.trim(),
                            estado = estado
                        )
                        onSubmit(personalSubmit)
                    }
                },
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
