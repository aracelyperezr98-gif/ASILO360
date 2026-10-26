package com.example.asilo360.ui.familiar.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.asilo360.data.model.ResidenteVinculado
import com.example.asilo360.ui.theme.LuminousTurquoise
import com.example.asilo360.ui.theme.TechBlue
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Muestra las secciones de Información General, Información Familiar y Observaciones Autorizadas del residente.
 */
@Composable
fun ResidenteInformacionSection(
    vinculado: ResidenteVinculado,
    modifier: Modifier = Modifier
) {
    val residente = vinculado.residente

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // A. Información General
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                SectionHeader(
                    title = "Información General",
                    icon = Icons.Default.Badge,
                    tint = TechBlue
                )

                Spacer(modifier = Modifier.height(12.dp))

                InfoRowItem(
                    label = "Nombre Completo",
                    value = "${residente.nombre} ${residente.apellido_paterno} ${residente.apellido_materno ?: ""}".trim(),
                    icon = Icons.Default.Person
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                InfoRowItem(
                    label = "Fecha de Nacimiento",
                    value = formatFecha(residente.fecha_nacimiento),
                    icon = Icons.Default.CalendarMonth
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                InfoRowItem(
                    label = "Sexo",
                    value = residente.sexo ?: "No especificado",
                    icon = Icons.Default.Person
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                InfoRowItem(
                    label = "Habitación Asignada",
                    value = residente.habitacion ?: "Información no disponible",
                    icon = Icons.Default.MeetingRoom
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                InfoRowItem(
                    label = "Fecha de Ingreso",
                    value = formatFecha(residente.fecha_ingreso),
                    icon = Icons.Default.CalendarMonth
                )
            }
        }

        // B. Información Familiar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                SectionHeader(
                    title = "Información Familiar y Contacto",
                    icon = Icons.Default.ContactPhone,
                    tint = LuminousTurquoise
                )

                Spacer(modifier = Modifier.height(12.dp))

                InfoRowItem(
                    label = "Residente",
                    value = "${residente.nombre} ${residente.apellido_paterno}".trim(),
                    icon = Icons.Default.Person
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                InfoRowItem(
                    label = "Parentesco Autorizado",
                    value = vinculado.parentesco,
                    icon = Icons.Default.Badge
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                InfoRowItem(
                    label = "Contacto de la Residencia",
                    value = "Asilo 360 - Recepción\nTel: (55) 5123-4567 | Atención 24/7",
                    icon = Icons.Default.Phone
                )
            }
        }

        // C. Observaciones e Información Relevante
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                SectionHeader(
                    title = "Observaciones y Cuidados Especiales",
                    icon = Icons.Default.Notes,
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                val obs = residente.observaciones?.trim()
                if (!obs.isNullOrBlank()) {
                    Text(
                        text = obs,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.25f
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "No hay observaciones o indicaciones especiales registradas en este momento.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector,
    tint: androidx.compose.ui.graphics.Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun InfoRowItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier
                .padding(top = 2.dp)
                .size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value.ifBlank { "Información no disponible" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun formatFecha(fechaStr: String?): String {
    if (fechaStr.isNullOrBlank()) return "Información no disponible"
    return try {
        val datePart = fechaStr.trim().take(10)
        val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
        val date = sdfIn.parse(datePart) ?: return fechaStr
        val sdfOut = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es", "ES"))
        sdfOut.format(date)
    } catch (_: Exception) {
        fechaStr
    }
}
