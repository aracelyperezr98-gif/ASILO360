package com.example.asilo360.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.asilo360.components.CyberStatCard
import com.example.asilo360.components.FuturisticHeaderCard
import com.example.asilo360.components.MenuOptionItem
import com.example.asilo360.components.QuickActionButton
import com.example.asilo360.data.session.UserSessionManager
import com.example.asilo360.ui.theme.LuminousTurquoise
import com.example.asilo360.ui.theme.TechBlue
import com.example.asilo360.ui.theme.ThemeManager
import com.example.asilo360.ui.theme.WarningAmber

/**
 * Dashboard para usuarios con rol de Personal del Asilo (id_rol = 2).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalDashboardScreen(
    onNavigateToResidentes: () -> Unit,
    onNavigateToActividades: () -> Unit,
    onNavigateToConsultas: () -> Unit,
    onNavigateToVisitas: () -> Unit,
    onNavigateToNotificaciones: () -> Unit,
    onNavigateToPerfil: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val staffName = UserSessionManager.getUserFullName()

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
                            text = "Panel de Personal",
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

            // Encabezado futurista
            item {
                FuturisticHeaderCard(
                    titulo = "Bienvenido, $staffName",
                    subtitulo = "Atención, seguimiento y cuidado integral diario",
                    badgeText = "Personal de la Residencia",
                    icon = Icons.Default.Badge
                )
            }

            // Resumen de atención
            item {
                Text(
                    text = "Resumen del Día",
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
                        titulo = "Residentes",
                        valor = "12",
                        icon = Icons.Default.People,
                        accentColor = TechBlue,
                        modifier = Modifier.weight(1f)
                    )
                    CyberStatCard(
                        titulo = "Consultas hoy",
                        valor = "4",
                        icon = Icons.Default.MedicalServices,
                        accentColor = WarningAmber,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CyberStatCard(
                        titulo = "Visitas hoy",
                        valor = "6",
                        icon = Icons.Default.MeetingRoom,
                        accentColor = LuminousTurquoise,
                        modifier = Modifier.weight(1f)
                    )
                    CyberStatCard(
                        titulo = "Avisos",
                        valor = "3",
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
                            text = "Ver residentes",
                            icon = Icons.Default.People,
                            onClick = onNavigateToResidentes,
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            text = "Registrar actividad",
                            icon = Icons.Default.FitnessCenter,
                            onClick = onNavigateToActividades,
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
                            text = "Ver visitas",
                            icon = Icons.Default.MeetingRoom,
                            onClick = onNavigateToVisitas,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Menú de opciones
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
                        title = "Residentes",
                        subtitle = "Consulta información y ficha del residente",
                        icon = Icons.Default.People,
                        onClick = onNavigateToResidentes
                    )
                    MenuOptionItem(
                        title = "Actividades de Cuidado",
                        subtitle = "Registro de terapias, alimentos y medicamentos",
                        icon = Icons.Default.FitnessCenter,
                        onClick = onNavigateToActividades
                    )
                    MenuOptionItem(
                        title = "Consultas Médicas",
                        subtitle = "Ver y registrar revisiones de salud",
                        icon = Icons.Default.MedicalServices,
                        onClick = onNavigateToConsultas
                    )
                    MenuOptionItem(
                        title = "Visitas Programadas",
                        subtitle = "Atención y registro de familiares",
                        icon = Icons.Default.MeetingRoom,
                        onClick = onNavigateToVisitas
                    )
                    MenuOptionItem(
                        title = "Notificaciones",
                        subtitle = "Alertas del sistema e informativos",
                        icon = Icons.Default.Notifications,
                        onClick = onNavigateToNotificaciones
                    )
                    MenuOptionItem(
                        title = "Mi Perfil",
                        subtitle = "Información personal y de cuenta",
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
