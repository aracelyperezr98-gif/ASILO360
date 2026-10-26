package com.example.asilo360.admin.residentes

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
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
import com.example.asilo360.data.model.Residente

/**
 * Pantalla de formulario para registrar o editar un residente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidenteFormScreen(
    residenteParaEditar: Residente?,
    onSubmit: (Residente) -> Unit,
    onBackClicked: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isEditing = residenteParaEditar != null

    var nombre by remember { mutableStateOf(residenteParaEditar?.nombre ?: "") }
    var apellidoPaterno by remember { mutableStateOf(residenteParaEditar?.apellido_paterno ?: "") }
    var apellidoMaterno by remember { mutableStateOf(residenteParaEditar?.apellido_materno ?: "") }
    var fechaNacimiento by remember { mutableStateOf(residenteParaEditar?.fecha_nacimiento ?: "1950-01-01") }
    var sexo by remember { mutableStateOf(residenteParaEditar?.sexo ?: "Masculino") }
    var habitacion by remember { mutableStateOf(residenteParaEditar?.habitacion ?: "") }
    var fechaIngreso by remember { mutableStateOf(residenteParaEditar?.fecha_ingreso ?: "2023-01-01") }
    var estado by remember { mutableStateOf(residenteParaEditar?.estado ?: "Activo") }
    var observaciones by remember { mutableStateOf(residenteParaEditar?.observaciones ?: "") }

    var nombreError by remember { mutableStateOf<String?>(null) }
    var apellidoPaternoError by remember { mutableStateOf<String?>(null) }

    var sexoExpanded by remember { mutableStateOf(false) }
    var estadoExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Editar Residente" else "Registrar Residente",
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

            // Campo 1: Nombre (Obligatorio)
            CustomTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    if (nombreError != null) nombreError = null
                },
                placeholder = "Nombre",
                leadingIcon = Icons.Default.Person,
                isError = nombreError != null,
                errorMessage = nombreError
            )

            // Campo 2: Apellido Paterno (Obligatorio)
            CustomTextField(
                value = apellidoPaterno,
                onValueChange = {
                    apellidoPaterno = it
                    if (apellidoPaternoError != null) apellidoPaternoError = null
                },
                placeholder = "Apellido Paterno",
                leadingIcon = Icons.Default.Badge,
                isError = apellidoPaternoError != null,
                errorMessage = apellidoPaternoError
            )

            // Campo 3: Apellido Materno
            CustomTextField(
                value = apellidoMaterno,
                onValueChange = { apellidoMaterno = it },
                placeholder = "Apellido Materno (Opcional)",
                leadingIcon = Icons.Default.Badge
            )

            // Campo 4: Fecha de Nacimiento
            CustomTextField(
                value = fechaNacimiento,
                onValueChange = { fechaNacimiento = it },
                placeholder = "Fecha de Nacimiento (AAAA-MM-DD)",
                leadingIcon = Icons.Default.CalendarToday
            )

            // Campo 5: Selector de Sexo
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = sexo,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Sexo") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Sexo") },
                    trailingIcon = {
                        IconButton(onClick = { sexoExpanded = true }) {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Seleccionar Sexo")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().clickable { sexoExpanded = true },
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(
                    expanded = sexoExpanded,
                    onDismissRequest = { sexoExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Masculino") },
                        onClick = { sexo = "Masculino"; sexoExpanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Femenino") },
                        onClick = { sexo = "Femenino"; sexoExpanded = false }
                    )
                }
            }

            // Campo 6: Habitación
            CustomTextField(
                value = habitacion,
                onValueChange = { habitacion = it },
                placeholder = "Habitación (Ej: 102-A)",
                leadingIcon = Icons.Default.Home
            )

            // Campo 7: Fecha de Ingreso
            CustomTextField(
                value = fechaIngreso,
                onValueChange = { fechaIngreso = it },
                placeholder = "Fecha de Ingreso (AAAA-MM-DD)",
                leadingIcon = Icons.Default.CalendarToday
            )

            // Campo 8: Selector de Estado
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = estado,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Estado") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Info, contentDescription = "Estado") },
                    trailingIcon = {
                        IconButton(onClick = { estadoExpanded = true }) {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Seleccionar Estado")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().clickable { estadoExpanded = true },
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(
                    expanded = estadoExpanded,
                    onDismissRequest = { estadoExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Activo") },
                        onClick = { estado = "Activo"; estadoExpanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Inactivo") },
                        onClick = { estado = "Inactivo"; estadoExpanded = false }
                    )
                }
            }

            // Campo 9: Observaciones (Multilínea)
            OutlinedTextField(
                value = observaciones,
                onValueChange = { observaciones = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                placeholder = { Text("Observaciones médicas, dietas especiales, etc.") },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Botón de Enviar Formulario
            PrimaryButton(
                text = if (isEditing) "Guardar cambios" else "Registrar residente",
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

                    if (isValid) {
                        val residenteSubmit = Residente(
                            id_residente = residenteParaEditar?.id_residente ?: 0,
                            nombre = nombre.trim(),
                            apellido_paterno = apellidoPaterno.trim(),
                            apellido_materno = apellidoMaterno.trim().ifBlank { null },
                            fecha_nacimiento = fechaNacimiento.trim().ifBlank { null },
                            sexo = sexo,
                            habitacion = habitacion.trim().ifBlank { null },
                            fecha_ingreso = fechaIngreso.trim().ifBlank { null },
                            estado = estado,
                            observaciones = observaciones.trim().ifBlank { null }
                        )
                        onSubmit(residenteSubmit)
                    }
                },
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
