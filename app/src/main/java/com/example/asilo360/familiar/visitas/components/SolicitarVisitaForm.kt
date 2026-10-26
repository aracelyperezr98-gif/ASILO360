package com.example.asilo360.familiar.visitas.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.asilo360.components.CustomTextField
import com.example.asilo360.data.model.Residente
import java.time.LocalDate

/**
 * Diálogo/Formulario para que el Familiar solicite/programe una nueva visita.
 */
@Composable
fun SolicitarVisitaDialog(
    residente: Residente?,
    onDismiss: () -> Unit,
    onSubmit: (fecha: String, hora: String, numVisitantes: Int, observaciones: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val hoy = LocalDate.now().toString()
    var fecha by remember { mutableStateOf(hoy) }
    var hora by remember { mutableStateOf("11:00 AM") }
    var numVisitantes by remember { mutableStateOf("2") }
    var observaciones by remember { mutableStateOf("") }

    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Solicitar Nueva Visita",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (residente != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Residente: ${residente.nombreCompleto()}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                CustomTextField(
                    value = fecha,
                    onValueChange = { fecha = it },
                    placeholder = "Fecha (YYYY-MM-DD)",
                    leadingIcon = Icons.Default.CalendarMonth
                )

                CustomTextField(
                    value = hora,
                    onValueChange = { hora = it },
                    placeholder = "Hora (ej. 11:00 AM / 16:30)",
                    leadingIcon = Icons.Default.Schedule
                )

                CustomTextField(
                    value = numVisitantes,
                    onValueChange = { numVisitantes = it },
                    placeholder = "Número de visitantes",
                    leadingIcon = Icons.Default.Group,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                CustomTextField(
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    placeholder = "Observaciones o notas adicionales (opcional)",
                    leadingIcon = Icons.Default.Comment
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val num = numVisitantes.toIntOrNull() ?: 0
                            if (fecha.isBlank()) {
                                errorMsg = "Ingresa la fecha de la visita."
                                return@Button
                            }
                            if (hora.isBlank()) {
                                errorMsg = "Ingresa la hora estimada."
                                return@Button
                            }
                            if (num <= 0) {
                                errorMsg = "El número de visitantes debe ser al menos 1."
                                return@Button
                            }
                            onSubmit(fecha.trim(), hora.trim(), num, observaciones.trim())
                        }
                    ) {
                        Text("Enviar Solicitud", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
