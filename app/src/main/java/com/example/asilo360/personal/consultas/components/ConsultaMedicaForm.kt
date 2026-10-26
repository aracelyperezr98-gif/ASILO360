package com.example.asilo360.personal.consultas.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.asilo360.data.model.Residente
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

val ESTADOS_CONSULTA = listOf("Programada", "Realizada", "Cancelada")
val MOTIVOS_PREDEFINIDOS = listOf(
    "Revisión general",
    "Seguimiento médico",
    "Control de presión arterial",
    "Valoración médica",
    "Chequeo de rutina",
    "Consulta de urgencia"
)

/**
 * Componente del formulario para crear o editar una consulta médica.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultaMedicaFormContent(
    residentes: List<Residente>,
    idResidenteSeleccionado: Int,
    onResidenteSelected: (Int) -> Unit,
    fecha: String,
    onFechaChanged: (String) -> Unit,
    hora: String,
    onHoraChanged: (String) -> Unit,
    motivo: String,
    onMotivoChanged: (String) -> Unit,
    estado: String,
    onEstadoChanged: (String) -> Unit,
    observaciones: String,
    onObservacionesChanged: (String) -> Unit,
    esEdicion: Boolean = false,
    modifier: Modifier = Modifier
) {
    var expandedResidente by remember { mutableStateOf(false) }
    var expandedEstado by remember { mutableStateOf(false) }
    var expandedMotivo by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val residenteActual = residentes.find { it.id_residente == idResidenteSeleccionado }
    val textoResidente = residenteActual?.let { "${it.nombre} ${it.apellido_paterno} (Hab. ${it.habitacion ?: "N/A"})" }
        ?: "Seleccionar residente"

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // 1. RESIDENTE
        Text(
            text = "Residente",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Column {
            OutlinedTextField(
                value = textoResidente,
                onValueChange = {},
                readOnly = true,
                enabled = !esEdicion,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !esEdicion) { expandedResidente = true },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (!esEdicion) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            DropdownMenu(
                expanded = expandedResidente,
                onDismissRequest = { expandedResidente = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                if (residentes.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No hay residentes registrados") },
                        onClick = { expandedResidente = false }
                    )
                } else {
                    residentes.forEach { r ->
                        DropdownMenuItem(
                            text = {
                                Text("${r.nombre} ${r.apellido_paterno} ${r.apellido_materno ?: ""} - Hab. ${r.habitacion ?: "N/A"}")
                            },
                            onClick = {
                                onResidenteSelected(r.id_residente)
                                expandedResidente = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. FECHA Y HORA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Fecha",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = fecha,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hora",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = hora,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTimePicker = true },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. MOTIVO DE CONSULTA
        Text(
            text = "Motivo de la consulta",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Column {
            OutlinedTextField(
                value = motivo,
                onValueChange = onMotivoChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Escribe o selecciona un motivo...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { expandedMotivo = true }) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Opciones de motivo"
                        )
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            DropdownMenu(
                expanded = expandedMotivo,
                onDismissRequest = { expandedMotivo = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                MOTIVOS_PREDEFINIDOS.forEach { m ->
                    DropdownMenuItem(
                        text = { Text(m) },
                        onClick = {
                            onMotivoChanged(m)
                            expandedMotivo = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. ESTADO
        Text(
            text = "Estado",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Column {
            OutlinedTextField(
                value = estado,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedEstado = true },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                },
                shape = RoundedCornerShape(12.dp)
            )

            DropdownMenu(
                expanded = expandedEstado,
                onDismissRequest = { expandedEstado = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                ESTADOS_CONSULTA.forEach { est ->
                    DropdownMenuItem(
                        text = { Text(est) },
                        onClick = {
                            onEstadoChanged(est)
                            expandedEstado = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. OBSERVACIONES
        Text(
            text = "Observaciones (Opcional)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = observaciones,
            onValueChange = onObservacionesChanged,
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            placeholder = { Text("Escribe observaciones o notas de seguimiento...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            shape = RoundedCornerShape(12.dp)
        )
    }

    // DatePicker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val localDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            val formatted = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            onFechaChanged(formatted)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // TimePicker Dialog
    if (showTimePicker) {
        val now = LocalTime.now()
        val timePickerState = rememberTimePickerState(
            initialHour = now.hour,
            initialMinute = now.minute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val formattedTime = String.format(
                            Locale.ROOT,
                            "%02d:%02d:00",
                            timePickerState.hour,
                            timePickerState.minute
                        )
                        onHoraChanged(formattedTime)
                        showTimePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancelar")
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TimePicker(state = timePickerState)
                }
            }
        )
    }
}
