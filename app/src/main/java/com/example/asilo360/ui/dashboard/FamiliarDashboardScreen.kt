package com.example.asilo360.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.asilo360.components.CyberStatCard
import com.example.asilo360.components.FuturisticHeaderCard
import com.example.asilo360.components.MenuOptionItem
import com.example.asilo360.components.QuickActionButton
import com.example.asilo360.ui.theme.CoralRed
import com.example.asilo360.ui.theme.LuminousTurquoise
import com.example.asilo360.ui.theme.TechBlue
import com.example.asilo360.ui.theme.ThemeManager
import com.example.asilo360.ui.theme.WarningAmber

/**
 * Dashboard para usuarios con rol de Familiar (id_rol = 3) conectado dinámicamente con Firestore.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamiliarDashboardScreen(
    onNavigateToMiFamiliar: () -> Unit,
    onNavigateToConsultas: () -> Unit,
    onNavigateToVisitas: () -> Unit,
    onNavigateToPagos: () -> Unit,
    onNavigateToNotificaciones: () -> Unit,
    onNavigateToPerfil: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FamiliarDashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val vinculado = uiState.vinculado
    val residente = vinculado?.residente

    val resNombre = if (residente != null) {
        "${residente.nombre} ${residente.apellido_paterno}".trim()
    } else {
        "Mi Familiar"
    }

    val resHabitacion = residente?.habitacion ?: "Sin asignar"
    val resEstado = residente?.estado ?: "Activo"

    val proxConsultaText = vinculado?.proximaConsulta?.let {
        "${it.fecha} (${it.hora.take(5)})"
    } ?: "Sin agendar"

    val proxVisitaText = vinculado?.proximaVisita?.let {
        "${it.fecha} (${it.hora.take(5)})"
    } ?: "Sin agendar"

    val estadoPagoText = if ((vinculado?.pagosPendientesCount ?: 0) > 0) {
        "Pendiente"
    } else {
        "Al corriente"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Asilo 360",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Portal Familiar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    val context = LocalContext.current
                    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

                    IconButton(onClick = { ThemeManager.toggleDarkMode(context, !isDark) }) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Cambiar tema"
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Header futurista
            item {
                FuturisticHeaderCard(
                    titulo = "Bienvenido, ${uiState.familiarNombre}",
                    subtitulo = "Conexión, atención y tranquilidad para tu familia",
                    badgeText = "Portal Familiar",
                    icon = Icons.Default.FamilyRestroom
                )
            }

            // Tarjeta destacada del residente vinculado
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FamilyRestroom,
                                contentDescription = "Mi familiar",
                                tint = TechBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Mi Familiar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TechBlue
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = resNombre,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Habitación: $resHabitacion",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Estado: $resEstado",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = LuminousTurquoise
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Próxima consulta:",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = proxConsultaText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Resumen en tarjetas dinámicas
            item {
                Text(
                    text = "Resumen de Eventos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CyberStatCard(
                        titulo = "Próx. consulta",
                        valor = proxConsultaText,
                        icon = Icons.Default.MedicalServices,
                        accentColor = WarningAmber,
                        modifier = Modifier.weight(1f)
                    )
                    CyberStatCard(
                        titulo = "Próx. visita",
                        valor = proxVisitaText,
                        icon = Icons.Default.MeetingRoom,
                        accentColor = LuminousTurquoise,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CyberStatCard(
                        titulo = "Estado Pago",
                        valor = estadoPagoText,
                        icon = Icons.Default.Payments,
                        accentColor = if (estadoPagoText == "Pendiente") CoralRed else LuminousTurquoise,
                        modifier = Modifier.weight(1f)
                    )
                    CyberStatCard(
                        titulo = "Avisos",
                        valor = "Notificaciones",
                        icon = Icons.Default.Notifications,
                        accentColor = TechBlue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Acciones Rápidas
            item {
                Text(
                    text = "Acciones Rápidas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionButton(
                            text = "Info. de mi familiar",
                            icon = Icons.Default.FamilyRestroom,
                            onClick = onNavigateToMiFamiliar,
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            text = "Agendar visita",
                            icon = Icons.Default.CalendarMonth,
                            onClick = onNavigateToVisitas,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionButton(
                            text = "Ver consultas",
                            icon = Icons.Default.MedicalServices,
                            onClick = onNavigateToConsultas,
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            text = "Ver pagos",
                            icon = Icons.Default.Payments,
                            onClick = onNavigateToPagos,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Menú
            item {
                Text(
                    text = "Menú Principal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MenuOptionItem(
                        title = "Mi Familiar",
                        subtitle = "Ficha médica, datos generales y cuidados",
                        icon = Icons.Default.FamilyRestroom,
                        onClick = onNavigateToMiFamiliar
                    )
                    MenuOptionItem(
                        title = "Consultas Médicas",
                        subtitle = "Historial de revisiones de salud",
                        icon = Icons.Default.MedicalServices,
                        onClick = onNavigateToConsultas
                    )
                    MenuOptionItem(
                        title = "Visitas",
                        subtitle = "Agendar y consultar visitas programadas",
                        icon = Icons.Default.MeetingRoom,
                        onClick = onNavigateToVisitas
                    )
                    MenuOptionItem(
                        title = "Estado de Pagos",
                        subtitle = "Historial y estado de mensualidades",
                        icon = Icons.Default.Payments,
                        onClick = onNavigateToPagos
                    )
                    MenuOptionItem(
                        title = "Notificaciones",
                        subtitle = "Avisos y comunicados del asilo",
                        icon = Icons.Default.Notifications,
                        onClick = onNavigateToNotificaciones
                    )
                    MenuOptionItem(
                        title = "Mi Perfil",
                        subtitle = "Datos de contacto e información personal",
                        icon = Icons.Default.AccountCircle,
                        onClick = onNavigateToPerfil
                    )
                }
            }

            // Botón Cerrar Sesión
            item {
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Cerrar sesión"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cerrar sesión",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
