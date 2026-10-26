package com.example.asilo360.admin.dashboard

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
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
import com.example.asilo360.ui.theme.CoralRed
import com.example.asilo360.ui.theme.LuminousTurquoise
import com.example.asilo360.ui.theme.TechBlue
import com.example.asilo360.ui.theme.ThemeManager
import com.example.asilo360.ui.theme.WarningAmber

/**
 * Dashboard para usuarios con rol de Administrador (id_rol = 1).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateToResidentes: () -> Unit,
    onNavigateToPersonal: () -> Unit,
    onNavigateToFamiliares: () -> Unit = {},
    onNavigateToConsultas: () -> Unit,
    onNavigateToVisitas: () -> Unit,
    onNavigateToPagos: () -> Unit,
    onNavigateToNotificaciones: () -> Unit,
    onNavigateToReportes: () -> Unit,
    onNavigateToConfiguracion: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val adminName = UserSessionManager.getUserFullName()

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
                            text = "Panel de Administrador",
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

            // Saludo de bienvenida futurista
            item {
                FuturisticHeaderCard(
                    titulo = "Bienvenido, $adminName",
                    subtitulo = "Gestión inteligente y control total del centro de cuidado",
                    badgeText = "Administrador",
                    icon = Icons.Default.Shield
                )
            }

            // Resumen de Estadísticas Cyber KPI
            item {
                Text(
                    text = "Resumen del Asilo",
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
                        valor = "24",
                        icon = Icons.Default.People,
                        accentColor = TechBlue,
                        modifier = Modifier.weight(1f)
                    )
                    CyberStatCard(
                        titulo = "Personal",
                        valor = "12",
                        icon = Icons.Default.Badge,
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
                        titulo = "Consultas",
                        valor = "8",
                        icon = Icons.Default.MedicalServices,
                        accentColor = WarningAmber,
                        modifier = Modifier.weight(1f)
                    )
                    CyberStatCard(
                        titulo = "Pagos Pend.",
                        valor = "5",
                        icon = Icons.Default.Payments,
                        accentColor = CoralRed,
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
                            text = "Registrar residente",
                            icon = Icons.Default.PersonAdd,
                            onClick = onNavigateToResidentes,
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            text = "Registrar personal",
                            icon = Icons.Default.Add,
                            onClick = onNavigateToPersonal,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionButton(
                            text = "Registrar consulta",
                            icon = Icons.Default.MedicalServices,
                            onClick = onNavigateToConsultas,
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            text = "Registrar visita",
                            icon = Icons.Default.MeetingRoom,
                            onClick = onNavigateToVisitas,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionButton(
                            text = "Registrar pago",
                            icon = Icons.Default.Payments,
                            onClick = onNavigateToPagos,
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            text = "Enviar notificación",
                            icon = Icons.Default.Notifications,
                            onClick = onNavigateToNotificaciones,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Menú Principal
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
                        subtitle = "Ver y administrar adultos mayores",
                        icon = Icons.Default.People,
                        onClick = onNavigateToResidentes
                    )
                    MenuOptionItem(
                        title = "Personal",
                        subtitle = "Ver y administrar empleados del asilo",
                        icon = Icons.Default.Badge,
                        onClick = onNavigateToPersonal
                    )
                    MenuOptionItem(
                        title = "Familiares y Vínculos",
                        subtitle = "Gestionar cuentas familiares y permisos por residente",
                        icon = Icons.Default.FamilyRestroom,
                        onClick = onNavigateToFamiliares
                    )
                    MenuOptionItem(
                        title = "Consultas Médicas",
                        subtitle = "Historial de consultas y revisiones",
                        icon = Icons.Default.MedicalServices,
                        onClick = onNavigateToConsultas
                    )
                    MenuOptionItem(
                        title = "Visitas",
                        subtitle = "Agenda y registro de visitas de familiares",
                        icon = Icons.Default.MeetingRoom,
                        onClick = onNavigateToVisitas
                    )
                    MenuOptionItem(
                        title = "Pagos",
                        subtitle = "Registro de mensualidades y cuotas",
                        icon = Icons.Default.Payments,
                        onClick = onNavigateToPagos
                    )
                    MenuOptionItem(
                        title = "Notificaciones",
                        subtitle = "Avisos y comunicaciones enviadas",
                        icon = Icons.Default.Notifications,
                        onClick = onNavigateToNotificaciones
                    )
                    MenuOptionItem(
                        title = "Reportes",
                        subtitle = "Informes estadísticos y administrativos",
                        icon = Icons.Default.Assessment,
                        onClick = onNavigateToReportes
                    )
                    MenuOptionItem(
                        title = "Configuración",
                        subtitle = "Ajustes del sistema y usuarios",
                        icon = Icons.Default.Settings,
                        onClick = onNavigateToConfiguracion
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
