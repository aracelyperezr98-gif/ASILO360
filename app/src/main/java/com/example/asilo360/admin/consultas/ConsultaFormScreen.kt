package com.example.asilo360.admin.consultas

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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
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
import com.example.asilo360.data.model.Consulta
import com.example.asilo360.data.model.Personal
import com.example.asilo360.data.model.Residente

/**
 * Formulario para registrar una nueva consulta o editar una existente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultaFormScreen(
    consultaParaEditar: Consulta?,
    residentesDisponibles: List<Residente>,
    personalDisponible: List<Personal>,
    onSubmit: (Consulta) -> Unit,
    onBackClicked: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isEditing = consultaParaEditar != null

    // Filtrar solo activos para nuevos registros
    val listaResidentesActivos = remember(residentesDisponibles) {
        residentesDisponibles.filter { it.estado.equals("Activo", ignoreCase = true) }
    }
    val listaPersonalActivo = remember(personalDisponible) {
        personalDisponible.filter { it.estado == 1 }
    }

    var selectedResidente by remember {
        mutableStateOf<Residente?>(
            residentesDisponibles.find { it.id_residente == consultaParaEditar?.id_residente }
        )
    }

    var selectedPersonal by remember {
        mutableStateOf<Personal?>(
            personalDisponible.find { it.id_personal == consultaParaEditar?.id_personal }
        )
    }

    var fecha by remember { mutableStateOf(consultaParaEditar?.fecha ?: "2026-09-25") }
    var hora by remember { mutableStateOf(consultaParaEditar?.hora ?: "10:30:00") }
    var motivo by remember { mutableStateOf(consultaParaEditar?.motivo ?: "") }
    var observaciones by remember { mutableStateOf(consultaParaEditar?.observaciones ?: "") }
    var estado by remember { mutableStateOf(consultaParaEditar?.estado ?: "Programada") }

    // Errores
    var residenteError by remember { mutableStateOf<String?>(null) }
    var personalError by remember { mutableStateOf<String?>(null) }
    var fechaError by remember { mutableStateOf<String?>(null) }
    var horaError by remember { mutableStateOf<String?>(null) }
    var motivoError by remember { mutableStateOf<String?>(null) }

    // Menús desplegables
    var residenteExpanded by remember { mutableStateOf(false) }
    var personalExpanded by remember { mutableStateOf(false) }
    var estadoExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Editar Consulta" else "Nueva Consulta",
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

            // Selector 1: Residente
            Text(
                text = "Residente Atendido",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                val textoResidente = selectedResidente?.let {
                    "${it.nombre} ${it.apellido_paterno} (Hab: ${it.habitacion ?: "Sin asignar"})"
                } ?: "Seleccionar residente *"

                OutlinedTextField(
                    value = textoResidente,
                    onValueChange = {},
                    readOnly = true,
                    isError = residenteError != null,
                    leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { residenteExpanded = true }) {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { residenteExpanded = true },
                    shape = RoundedCornerShape(12.dp)
                )

                DropdownMenu(
                    expanded = residenteExpanded,
                    onDismissRequest = { residenteExpanded = false }
                ) {
                    if (listaResidentesActivos.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No hay residentes activos") },
                            onClick = { residenteExpanded = false }
                        )
                    } else {
                        listaResidentesActivos.forEach { r ->
                            val itemNombre = "${r.nombre} ${r.apellido_paterno} ${r.apellido_materno ?: ""}".trim()
                            val hab = r.habitacion?.let { " - Hab: $it" } ?: ""
                            DropdownMenuItem(
                                text = { Text("$itemNombre$hab") },
                                onClick = {
                                    selectedResidente = r
                                    residenteError = null
                                    residenteExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            if (residenteError != null) {
                Text(
                    text = residenteError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Selector 2: Personal Médico Responsable
            Text(
                text = "Personal Responsable",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                val textoPersonal = selectedPersonal?.let {
                    "${it.nombre} ${it.apellido_paterno} (${it.puesto})"
                } ?: "Seleccionar personal responsable *"

                OutlinedTextField(
                    value = textoPersonal,
                    onValueChange = {},
                    readOnly = true,
                    isError = personalError != null,
                    leadingIcon = { Icon(imageVector = Icons.Default.Badge, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { personalExpanded = true }) {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { personalExpanded = true },
                    shape = RoundedCornerShape(12.dp)
                )

                DropdownMenu(
                    expanded = personalExpanded,
                    onDismissRequest = { personalExpanded = false }
                ) {
                    if (listaPersonalActivo.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No hay personal activo") },
                            onClick = { personalExpanded = false }
                        )
                    } else {
                        listaPersonalActivo.forEach { p ->
                            val itemNombre = "${p.nombre} ${p.apellido_paterno}".trim()
                            DropdownMenuItem(
                                text = { Text("$itemNombre (${p.puesto})") },
                                onClick = {
                                    selectedPersonal = p
                                    personalError = null
                                    personalExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            if (personalError != null) {
                Text(
                    text = personalError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Fecha y Hora
            Text(
                text = "Programación de Cita",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            CustomTextField(
                value = fecha,
                onValueChange = {
                    fecha = it
                    if (fechaError != null) fechaError = null
                },
                placeholder = "Fecha (AAAA-MM-DD) *",
                leadingIcon = Icons.Default.CalendarToday,
                isError = fechaError != null,
                errorMessage = fechaError
            )

            CustomTextField(
                value = hora,
                onValueChange = {
                    hora = it
                    if (horaError != null) horaError = null
                },
                placeholder = "Hora (HH:MM:SS) *",
                leadingIcon = Icons.Default.AccessTime,
                isError = horaError != null,
                errorMessage = horaError
            )

            // Motivo
            CustomTextField(
                value = motivo,
                onValueChange = {
                    motivo = it
                    if (motivoError != null) motivoError = null
                },
                placeholder = "Motivo de la Consulta (Ej: Revisión general) *",
                leadingIcon = Icons.Default.MedicalServices,
                isError = motivoError != null,
                errorMessage = motivoError
            )

            // Selector Estado
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = estado,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Estado de la Consulta") },
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
                        text = { Text("Programada") },
                        onClick = { estado = "Programada"; estadoExpanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Realizada") },
                        onClick = { estado = "Realizada"; estadoExpanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Cancelada") },
                        onClick = { estado = "Cancelada"; estadoExpanded = false }
                    )
                }
            }

            // Observaciones
            OutlinedTextField(
                value = observaciones,
                onValueChange = { observaciones = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                placeholder = { Text("Observaciones médicas adicionales...") },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            PrimaryButton(
                text = if (isEditing) "Guardar Cambios" else "Guardar Consulta",
                onClick = {
                    var isValid = true

                    if (selectedResidente == null) {
                        residenteError = "Selecciona un residente."
                        isValid = false
                    }

                    if (selectedPersonal == null) {
                        personalError = "Selecciona al personal responsable."
                        isValid = false
                    }

                    if (fecha.trim().isEmpty()) {
                        fechaError = "La fecha es obligatoria."
                        isValid = false
                    }

                    if (hora.trim().isEmpty()) {
                        horaError = "La hora es obligatoria."
                        isValid = false
                    }

                    if (motivo.trim().isEmpty()) {
                        motivoError = "El motivo es obligatorio."
                        isValid = false
                    }

                    if (isValid) {
                        val consultaSubmit = Consulta(
                            id_consulta = consultaParaEditar?.id_consulta ?: 0,
                            id_residente = selectedResidente!!.id_residente,
                            id_personal = selectedPersonal!!.id_personal,
                            fecha = fecha.trim(),
                            hora = hora.trim(),
                            motivo = motivo.trim(),
                            observaciones = observaciones.trim().ifBlank { null },
                            estado = estado
                        )
                        onSubmit(consultaSubmit)
                    }
                },
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
