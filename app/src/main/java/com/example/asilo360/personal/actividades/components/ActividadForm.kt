package com.example.asilo360.personal.actividades.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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

val TIPOS_ACTIVIDAD_PREDEFINIDOS = listOf(
    "Actividad física",
    "Ejercicio",
    "Terapia",
    "Recreación",
    "Lectura",
    "Música",
    "Manualidades",
    "Socialización",
    "Alimentación",
    "Paseo",
    "Juego",
    "Otro"
)

/**
 * Componente del formulario para crear/editar una actividad.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActividadFormContent(
    residentes: List<Residente>,
    idResidenteSeleccionado: Int,
    onResidenteSelected: (Int) -> Unit,
    tipoSeleccionado: String,
    onTipoSelected: (String) -> Unit,
    tipoEspecificoOtro: String,
    onTipoOtroChanged: (String) -> Unit,
    fecha: String,
    onFechaChanged: (String) -> Unit,
    hora: String,
    onHoraChanged: (String) -> Unit,
    descripcion: String,
    onDescripcionChanged: (String) -> Unit,
    esEdicion: Boolean = false,
    modifier: Modifier = Modifier
) {
    var expandedResidente by remember { mutableStateOf(false) }
    var expandedTipo by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val residenteActual = residentes.find { it.id_residente == idResidenteSeleccionado }
    val textoResidente = residenteActual?.let { "${it.nombre} ${it.apellido_paterno} (Hab. ${it.habitacion ?: "N/A"})" }
        ?: "Seleccionar residente"

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // 1. RESIDENTE (Solo editable si no es edición)
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

        // 2. TIPO DE ACTIVIDAD
        Text(
            text = "Tipo de actividad",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Column {
            OutlinedTextField(
                value = tipoSeleccionado.ifBlank { "Seleccionar tipo de actividad" },
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedTipo = true },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Category,
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
                expanded = expandedTipo,
                onDismissRequest = { expandedTipo = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                TIPOS_ACTIVIDAD_PREDEFINIDOS.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onTipoSelected(option)
                            expandedTipo = false
                        }
                    )
                }
            }
        }

        // Campo extra si elige "Otro"
        if (tipoSeleccionado == "Otro") {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = tipoEspecificoOtro,
                onValueChange = onTipoOtroChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Especifica el tipo de actividad") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. FECHA Y HORA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Fecha
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

            // Hora
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

        // 4. DESCRIPCIÓN
        Text(
            text = "Descripción",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = descripcion,
            onValueChange = onDescripcionChanged,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            placeholder = { Text("Describe la actividad realizada con el residente...") },
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
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val formattedTime = String.format(
                            java.util.Locale.ROOT,
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
