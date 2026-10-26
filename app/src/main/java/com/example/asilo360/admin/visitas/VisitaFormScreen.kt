package com.example.asilo360.admin.visitas

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.asilo360.components.CustomTextField
import com.example.asilo360.components.PrimaryButton
import com.example.asilo360.data.model.FamiliarOpcion
import com.example.asilo360.data.model.Residente
import com.example.asilo360.data.model.Visita

/**
 * Formulario para registrar una nueva visita o editar una existente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitaFormScreen(
    visitaParaEditar: Visita?,
    residentesDisponibles: List<Residente>,
    familiaresDelResidente: List<FamiliarOpcion>,
    onResidenteSelected: (Int) -> Unit,
    onSubmit: (Visita) -> Unit,
    onBackClicked: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isEditing = visitaParaEditar != null

    val listaResidentesActivos = remember(residentesDisponibles) {
        residentesDisponibles.filter { it.estado.equals("Activo", ignoreCase = true) }
    }

    var selectedResidente by remember {
        mutableStateOf<Residente?>(
            residentesDisponibles.find { it.id_residente == visitaParaEditar?.id_residente }
        )
    }

    var selectedFamiliar by remember {
        mutableStateOf<FamiliarOpcion?>(
            familiaresDelResidente.find { it.id_familiar == visitaParaEditar?.id_familiar }
        )
    }

    // Al iniciar o si el residente predeterminado cambia, cargar sus familiares
    LaunchedEffect(selectedResidente) {
        selectedResidente?.let { r ->
            onResidenteSelected(r.id_residente)
        }
    }

    // Actualizar el familiar seleccionado si se encuentra en la lista cargada
    LaunchedEffect(familiaresDelResidente) {
        if (visitaParaEditar != null && selectedFamiliar == null) {
            selectedFamiliar = familiaresDelResidente.find { it.id_familiar == visitaParaEditar.id_familiar }
        }
    }

    var fecha by remember { mutableStateOf(visitaParaEditar?.fecha ?: "2026-09-27") }
    var hora by remember { mutableStateOf(visitaParaEditar?.hora ?: "16:00:00") }
    var numeroVisitantes by remember { mutableIntStateOf(visitaParaEditar?.numero_visitantes ?: 1) }
    var estado by remember { mutableStateOf(visitaParaEditar?.estado ?: "Programada") }

    // Mensajes de error
    var residenteError by remember { mutableStateOf<String?>(null) }
    var familiarError by remember { mutableStateOf<String?>(null) }
    var fechaError by remember { mutableStateOf<String?>(null) }
    var horaError by remember { mutableStateOf<String?>(null) }

    // Menús
    var residenteExpanded by remember { mutableStateOf(false) }
    var familiarExpanded by remember { mutableStateOf(false) }
    var estadoExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Editar Visita" else "Nueva Visita",
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
                text = "Residente que Recibe la Visita",
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
                            text = { Text("No hay residentes activos registrados") },
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
                                    selectedFamiliar = null // Reset familiar al cambiar residente
                                    residenteError = null
                                    residenteExpanded = false
                                    onResidenteSelected(r.id_residente)
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

            // Selector 2: Familiar Relacionado
            Text(
                text = "Familiar Visitante",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                val textoFamiliar = selectedFamiliar?.let {
                    val par = it.parentesco?.let { p -> " ($p)" } ?: ""
                    "${it.nombre}$par"
                } ?: if (selectedResidente == null) "Primero selecciona un residente" else "Seleccionar familiar *"

                OutlinedTextField(
                    value = textoFamiliar,
                    onValueChange = {},
                    readOnly = true,
                    enabled = selectedResidente != null,
                    isError = familiarError != null,
                    leadingIcon = { Icon(imageVector = Icons.Default.FamilyRestroom, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { if (selectedResidente != null) familiarExpanded = true }) {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = selectedResidente != null) { familiarExpanded = true },
                    shape = RoundedCornerShape(12.dp)
                )

                DropdownMenu(
                    expanded = familiarExpanded,
                    onDismissRequest = { familiarExpanded = false }
                ) {
                    if (familiaresDelResidente.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Este residente no tiene familiares registrados.") },
                            onClick = { familiarExpanded = false }
                        )
                    } else {
                        familiaresDelResidente.forEach { f ->
                            val par = f.parentesco?.let { " ($it)" } ?: ""
                            DropdownMenuItem(
                                text = { Text("${f.nombre}$par") },
                                onClick = {
                                    selectedFamiliar = f
                                    familiarError = null
                                    familiarExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            if (familiarError != null) {
                Text(
                    text = familiarError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Programación: Fecha y Hora
            Text(
                text = "Programación de la Visita",
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
                placeholder = "Fecha de Visita (AAAA-MM-DD) *",
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
                placeholder = "Hora de Visita (HH:MM:SS) *",
                leadingIcon = Icons.Default.AccessTime,
                isError = horaError != null,
                errorMessage = horaError
            )

            // Número de Visitantes
            Text(
                text = "Número de Visitantes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Cantidad de personas que asistirán:",
                    style = MaterialTheme.typography.bodyLarge
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { if (numeroVisitantes > 1) numeroVisitantes-- },
                        enabled = numeroVisitantes > 1,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Restar")
                    }

                    Text(
                        text = "$numeroVisitantes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    OutlinedButton(
                        onClick = { numeroVisitantes++ },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Sumar")
                    }
                }
            }

            // Selector Estado
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = estado,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Estado de la Visita") },
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

            Spacer(modifier = Modifier.height(12.dp))

            PrimaryButton(
                text = if (isEditing) "Guardar Cambios" else "Guardar Visita",
                onClick = {
                    var isValid = true

                    if (selectedResidente == null) {
                        residenteError = "Selecciona un residente."
                        isValid = false
                    }

                    if (selectedFamiliar == null) {
                        familiarError = "Selecciona un familiar."
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

                    if (isValid) {
                        val visitaSubmit = Visita(
                            id_visita = visitaParaEditar?.id_visita ?: 0,
                            id_residente = selectedResidente!!.id_residente,
                            id_familiar = selectedFamiliar!!.id_familiar,
                            fecha = fecha.trim(),
                            hora = hora.trim(),
                            numero_visitantes = numeroVisitantes,
                            estado = estado
                        )
                        onSubmit(visitaSubmit)
                    }
                },
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
