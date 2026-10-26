package com.example.asilo360.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.asilo360.ui.theme.Asilo360Theme
import com.example.asilo360.ui.theme.DeepNightBlue
import com.example.asilo360.ui.theme.LuminousTurquoise
import com.example.asilo360.ui.theme.TechBlue
import com.example.asilo360.ui.theme.ThemeOption

@Preview(showBackground = true, widthDp = 1000, heightDp = 1200)
@Composable
fun AppNavigationFlowPreview() {
    Asilo360Theme(themeOption = ThemeOption.DARK) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DeepNightBlue)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título Principal
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = TechBlue.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LuminousTurquoise)
                ) {
                    Text(
                        text = "ASILO 360 • MAPA Y FLUJO DE NAVEGACIÓN",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LuminousTurquoise,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // NODO 1: ENTRADA Y AUTENTICACIÓN
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MiniFlowBox(
                        title = "Recuperar Contraseña",
                        subtitle = "/recuperar_contrasena",
                        icon = Icons.Default.LockReset,
                        color = Color(0xFFF5B942)
                    )

                    MainFlowBox(
                        title = "PANTALLA DE LOGIN",
                        subtitle = "/login_screen (Firebase Auth)",
                        icon = Icons.Default.Login,
                        gradient = Brush.horizontalGradient(listOf(TechBlue, LuminousTurquoise))
                    )

                    MiniFlowBox(
                        title = "Seleccionar Rol",
                        subtitle = "/user_role_screen",
                        icon = Icons.Default.PersonSearch,
                        color = LuminousTurquoise
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = LuminousTurquoise,
                    modifier = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // NODO 2: LAS TRES RAMAS POR ROL
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // RAMA 1: ADMINISTRADOR
                    RoleBranchCard(
                        roleTitle = "ROL 1: ADMINISTRADOR",
                        roleBadge = "id_rol = 1",
                        accentColor = TechBlue,
                        icon = Icons.Default.AdminPanelSettings,
                        dashboardRoute = "/admin_dashboard",
                        screens = listOf(
                            "• Residentes (/residentes_screen)",
                            "• Personal (/personal_screen)",
                            "• Consultas Médicas (/consultas_screen)",
                            "• Visitas Programadas (/visitas_screen)",
                            "• Registro de Pagos (/pagos_screen)",
                            "• Notificaciones (/notificaciones_screen)",
                            "• Centro de Reportes (/reportes_screen)",
                            "• Configuración y Tema (/configuracion_screen)"
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    // RAMA 2: PERSONAL
                    RoleBranchCard(
                        roleTitle = "ROL 2: PERSONAL",
                        roleBadge = "id_rol = 2",
                        accentColor = LuminousTurquoise,
                        icon = Icons.Default.Badge,
                        dashboardRoute = "/personal_dashboard",
                        screens = listOf(
                            "• Mis Residentes (/personal/residentes)",
                            "• Detalle Residente (/personal/residentes/detalle/{id})",
                            "• Actividades Cuidado (/personal/actividades)",
                            "• Formulario Actividad (/personal/actividades/nueva)",
                            "• Consultas Médicas (/personal/consultas)",
                            "• Visitas Programadas (/personal/visitas)",
                            "• Notificaciones Cuidadores (/personal/notificaciones)",
                            "• Mi Perfil y Tema (/perfil_screen)"
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    // RAMA 3: FAMILIAR
                    RoleBranchCard(
                        roleTitle = "ROL 3: FAMILIAR",
                        roleBadge = "id_rol = 3",
                        accentColor = Color(0xFFF5B942),
                        icon = Icons.Default.FamilyRestroom,
                        dashboardRoute = "/familiar_dashboard",
                        screens = listOf(
                            "• Mi Familiar (/mi_familiar_screen)",
                            "• Consultas Médicas (/familiar/consultas)",
                            "• Mis Visitas (/familiar/visitas)",
                            "• Detalle Visita (/familiar/visitas/detalle/{id})",
                            "• Solicitar Visita (Diálogo)",
                            "• Estado de Pagos (/familiar/pagos)",
                            "• Detalle Pago (/familiar/pagos/detalle/{id})",
                            "• Notificaciones (/notificaciones_screen)",
                            "• Mi Perfil y Tema (/perfil_screen)"
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Pie de foto
                Text(
                    text = "Asilo 360 • Arquitectura de Navegación Segura con Roles y Cloud Firestore",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun MainFlowBox(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradient: Brush
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = subtitle, fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                }
            }
        }
    }
}

@Composable
private fun MiniFlowBox(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        modifier = Modifier.border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = subtitle, fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
private fun RoleBranchCard(
    roleTitle: String,
    roleBadge: String,
    accentColor: Color,
    icon: ImageVector,
    dashboardRoute: String,
    screens: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF18263E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(accentColor.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = roleTitle, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Surface(color = accentColor.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                    Text(
                        text = roleBadge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = accentColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Dashboard: $dashboardRoute",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                screens.forEach { screen ->
                    Text(
                        text = screen,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}
