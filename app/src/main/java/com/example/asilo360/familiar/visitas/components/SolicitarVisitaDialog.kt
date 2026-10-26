package com.example.asilo360.familiar.visitas.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.asilo360.data.model.ResidenteVinculado
import com.example.asilo360.data.model.Visita
import com.example.asilo360.ui.theme.TechBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Diálogo interactivo para solicitar una nueva visita programada a un residente vinculado.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitarVisitaDialog(
    residentesAutorizados: List<ResidenteVinculado>,
    isSubmitting: Boolean,
    onDismissRequest: () -> Unit,
    onSubmitRequest: (Visita) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedResidente by remember {
        mutableStateOf(residentesAutorizados.firstOrNull())
    }

    val hoyStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date()) }
    var fechaInput by remember { mutableStateOf(hoyStr) }
    var horaInput by remember { mutableStateOf("16:00") }
    var numVisitantes by remember { mutableIntStateOf(1) }
    var observacionesInput by remember { mutableStateOf("") }
    var errorMensaje by remember { mutableStateOf<String?>(null) }

    var expandedDropdown by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Encabezado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MeetingRoom,
                            contentDescription = null,
                            tint = TechBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Solicitar Visita",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismissRequest, enabled = !isSubmitting) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar"
                        )
                    }
                }

                Text(
                    text = "Completa los datos para agendar tu encuentro. La solicitud quedará registrada en estado 'Programada'.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (errorMensaje != null) {
                    Text(
                        text = errorMensaje!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 1. Selector de residente
                Text(
                    text = "Residente a visitar",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                if (residentesAutorizados.size > 1) {
                    ExposedDropdownMenuBox(
                        expanded = expandedDropdown,
                        onExpandedChange = { expandedDropdown = !expandedDropdown }
                    ) {
                        OutlinedTextField(
                            value = selectedResidente?.let { "${it.residente.nombre} ${it.residente.apellido_paterno}" } ?: "Selecciona un residente",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false }
                        ) {
                            residentesAutorizados.forEach { vin ->
                                DropdownMenuItem(
                                    text = { Text("${vin.residente.nombre} ${vin.residente.apellido_paterno}") },
                                    onClick = {
                                        selectedResidente = vin
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = selectedResidente?.let { "${it.residente.nombre} ${it.residente.apellido_paterno}" } ?: "Sin residente",
                        onValueChange = {},
                        readOnly = true,
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // 2. Fecha
                OutlinedTextField(
                    value = fechaInput,
                    onValueChange = { fechaInput = it },
                    label = { Text("Fecha (AAAA-MM-DD)") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // 3. Hora
                OutlinedTextField(
                    value = horaInput,
                    onValueChange = { horaInput = it },
                    label = { Text("Hora (HH:MM)") },
                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // 4. Número de visitantes
                OutlinedTextField(
                    value = numVisitantes.toString(),
                    onValueChange = { val num = it.toIntOrNull(); if (num != null && num in 1..10) numVisitantes = num },
                    label = { Text("Número de visitantes") },
                    leadingIcon = { Icon(Icons.Default.Groups, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // 5. Observaciones
                OutlinedTextField(
                    value = observacionesInput,
                    onValueChange = { observacionesInput = it },
                    label = { Text("Notas / Comentarios (opcional)") },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Acciones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar")
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            val res = selectedResidente
                            if (res == null) {
                                errorMensaje = "Debes seleccionar un residente."
                                return@Button
                            }
                            if (fechaInput.isBlank() || fechaInput < hoyStr) {
                                errorMensaje = "Ingresa una fecha válida a partir de hoy ($hoyStr)."
                                return@Button
                            }
                            if (horaInput.isBlank()) {
                                errorMensaje = "Ingresa la hora estimada de la visita."
                                return@Button
                            }

                            errorMensaje = null

                            val nuevaVisita = Visita(
                                id_residente = res.residente.id_residente,
                                id_familiar = res.idFamiliar,
                                fecha = fechaInput.trim(),
                                hora = horaInput.trim(),
                                numero_visitantes = numVisitantes,
                                estado = "Programada",
                                observaciones = observacionesInput.takeIf { it.isNotBlank() },
                                nombre_residente = "${res.residente.nombre} ${res.residente.apellido_paterno}".trim(),
                                habitacion = res.residente.habitacion,
                                parentesco = res.parentesco
                            )

                            onSubmitRequest(nuevaVisita)
                        },
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TechBlue)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enviando...")
                        } else {
                            Text("Enviar Solicitud")
                        }
                    }
                }
            }
        }
    }
}
