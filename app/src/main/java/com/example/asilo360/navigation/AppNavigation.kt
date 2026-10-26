package com.example.asilo360.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.asilo360.admin.configuracion.ConfiguracionScreen
import com.example.asilo360.admin.consultas.ConsultasScreen
import com.example.asilo360.admin.dashboard.AdminDashboardScreen
import com.example.asilo360.admin.notificaciones.NotificacionesScreen
import com.example.asilo360.admin.pagos.PagosScreen
import com.example.asilo360.admin.personal.PersonalScreen
import com.example.asilo360.admin.reportes.ReportesScreen
import com.example.asilo360.admin.residentes.ResidentesScreen
import com.example.asilo360.admin.visitas.VisitasScreen
import com.example.asilo360.data.session.UserSessionManager
import com.example.asilo360.model.UserRole
import com.example.asilo360.personal.actividades.ActividadDetailScreen
import com.example.asilo360.personal.actividades.ActividadFormScreen
import com.example.asilo360.personal.actividades.ActividadesScreen
import com.example.asilo360.personal.consultas.ConsultaMedicaDetailScreen
import com.example.asilo360.personal.consultas.ConsultaMedicaFormScreen
import com.example.asilo360.personal.consultas.ConsultasMedicasScreen
import com.example.asilo360.personal.notificaciones.NotificacionPersonalDetailScreen
import com.example.asilo360.personal.notificaciones.NotificacionesPersonalScreen
import com.example.asilo360.personal.residentes.ResidentePersonalDetailScreen
import com.example.asilo360.personal.residentes.ResidentesPersonalScreen
import com.example.asilo360.personal.visitas.VisitaProgramadaDetailScreen
import com.example.asilo360.personal.visitas.VisitasProgramadasScreen
import com.example.asilo360.screens.login.LoginScreen
import com.example.asilo360.screens.login.LoginViewModel
import com.example.asilo360.screens.login.RecuperarContrasenaScreen
import com.example.asilo360.screens.login.RecuperarContrasenaViewModel
import com.example.asilo360.screens.login.UserRoleScreen
import com.example.asilo360.ui.actividades.ActividadesScreen
import com.example.asilo360.familiar.notificaciones.NotificacionFamiliarDetailScreen
import com.example.asilo360.familiar.notificaciones.NotificacionesFamiliarScreen
import com.example.asilo360.familiar.pagos.DetallePagoFamiliarScreen
import com.example.asilo360.familiar.pagos.EstadoPagosFamiliarScreen
import com.example.asilo360.familiar.visitas.VisitaFamiliarDetailScreen
import com.example.asilo360.familiar.visitas.VisitasFamiliarScreen
import com.example.asilo360.ui.dashboard.FamiliarDashboardScreen
import com.example.asilo360.ui.dashboard.PersonalDashboardScreen
import com.example.asilo360.ui.familiar.MiFamiliarScreen
import com.example.asilo360.admin.familiares.FamiliaresScreen
import com.example.asilo360.admin.familiares.FamiliaresViewModel
import com.example.asilo360.familiar.consultas.ConsultaMedicaFamiliarDetailScreen
import com.example.asilo360.familiar.consultas.ConsultasMedicasFamiliarScreen
import com.example.asilo360.ui.perfil.PerfilScreen

/**
 * Rutas de navegación de la aplicación Asilo 360.
 */
object AppScreens {
    const val LOGIN = "login_screen"
    const val USER_ROLE = "user_role_screen"
    const val RECUPERAR_CONTRASENA = "recuperar_contrasena"
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val ADMIN_FAMILIARES = "admin/familiares"
    const val PERSONAL_DASHBOARD = "personal_dashboard"
    const val FAMILIAR_DASHBOARD = "familiar_dashboard"

    // Módulos funcionales
    const val RESIDENTES = "residentes_screen"
    const val PERSONAL_RESIDENTES = "personal/residentes"
    const val PERSONAL_RESIDENTE_DETALLE = "personal/residentes/detalle/{id}"
    const val PERSONAL = "personal_screen"
    const val CONSULTAS = "consultas_screen"
    const val PERSONAL_CONSULTAS = "personal/consultas"
    const val PERSONAL_CONSULTAS_NUEVA = "personal/consultas/nueva"
    const val PERSONAL_CONSULTAS_DETALLE = "personal/consultas/detalle/{id}"
    const val VISITAS = "visitas_screen"
    const val PERSONAL_VISITAS = "personal/visitas"
    const val PERSONAL_VISITA_DETALLE = "personal/visitas/detalle/{id}"
    const val PAGOS = "pagos_screen"
    const val ACTIVIDADES = "actividades_screen"
    const val PERSONAL_ACTIVIDADES = "personal/actividades"
    const val PERSONAL_ACTIVIDADES_NUEVA = "personal/actividades/nueva"
    const val PERSONAL_ACTIVIDADES_DETALLE = "personal/actividades/detalle/{id}"
    const val NOTIFICACIONES = "notificaciones_screen"
    const val PERSONAL_NOTIFICACIONES = "personal/notificaciones"
    const val PERSONAL_NOTIFICACION_DETALLE = "personal/notificaciones/detalle/{id}"
    const val REPORTES = "reportes_screen"
    const val CONFIGURACION = "configuracion_screen"
    const val PERFIL = "perfil_screen"
    const val MI_FAMILIAR = "mi_familiar_screen"
    const val FAMILIAR_CONSULTAS = "familiar/consultas"
    const val FAMILIAR_CONSULTA_DETALLE = "familiar/consultas/detalle/{id}"
    const val FAMILIAR_VISITAS = "familiar/visitas"
    const val FAMILIAR_VISITA_DETALLE = "familiar/visitas/detalle/{id}"
    const val FAMILIAR_PAGOS = "familiar/pagos"
    const val FAMILIAR_PAGO_DETALLE = "familiar/pagos/detalle/{id}"
    const val FAMILIAR_NOTIFICACIONES = "familiar/notificaciones"
    const val FAMILIAR_NOTIFICACION_DETALLE = "familiar/notificaciones/detalle/{id}"
}

/**
 * Grafo de navegación principal con control de acceso por roles.
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    loginViewModel: LoginViewModel = viewModel()
) {
    // Función para cerrar sesión limpiando todo el stack de pantallas
    val logoutAction: () -> Unit = {
        loginViewModel.resetLoginState()
        navController.navigate(AppScreens.LOGIN) {
            popUpTo(0) { inclusive = true }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppScreens.LOGIN
    ) {
        // Pantalla de Login
        composable(route = AppScreens.LOGIN) { backStackEntry ->
            val uiState by loginViewModel.uiState.collectAsState()

            // Escuchar si regresa un rol seleccionado desde UserRoleScreen
            val roleNameFlow = backStackEntry.savedStateHandle.getStateFlow<String?>("selected_role", null)
            val roleName by roleNameFlow.collectAsState()

            LaunchedEffect(roleName) {
                roleName?.let { name ->
                    try {
                        val role = UserRole.valueOf(name)
                        loginViewModel.setSelectedRole(role)
                    } catch (_: IllegalArgumentException) { }
                }
            }

            LoginScreen(
                uiState = uiState,
                onEmailChange = loginViewModel::onEmailChange,
                onPasswordChange = loginViewModel::onPasswordChange,
                onLoginClick = loginViewModel::onLoginClick,
                onForgotPasswordClick = {
                    navController.navigate(AppScreens.RECUPERAR_CONTRASENA)
                },
                onMessageShown = loginViewModel::clearMessageEvent,
                onNavigateToUserRoleScreen = {
                    navController.navigate(AppScreens.USER_ROLE)
                },
                onLoginSuccess = { roleId ->
                    val destination = when (roleId) {
                        1 -> AppScreens.ADMIN_DASHBOARD
                        2 -> AppScreens.PERSONAL_DASHBOARD
                        3 -> AppScreens.FAMILIAR_DASHBOARD
                        else -> AppScreens.LOGIN
                    }
                    navController.navigate(destination) {
                        popUpTo(AppScreens.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // Pantalla de Selección de Tipo de Usuario
        composable(route = AppScreens.USER_ROLE) {
            val uiState by loginViewModel.uiState.collectAsState()

            UserRoleScreen(
                initialRole = uiState.selectedRole,
                onRoleSelected = { role ->
                    loginViewModel.setSelectedRole(role)
                    navController.popBackStack()
                },
                onBackClicked = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla de Recuperación de Contraseña
        composable(route = AppScreens.RECUPERAR_CONTRASENA) {
            RecuperarContrasenaScreen(
                onBackClicked = {
                    navController.popBackStack()
                }
            )
        }

        // DASHBOARD ADMINISTRADOR (id_rol = 1)
        composable(route = AppScreens.ADMIN_DASHBOARD) {
            if (UserSessionManager.getUserRoleId() != 1) {
                LaunchedEffect(Unit) {
                    logoutAction()
                }
            } else {
                AdminDashboardScreen(
                    onNavigateToResidentes = { navController.navigate(AppScreens.RESIDENTES) },
                    onNavigateToPersonal = { navController.navigate(AppScreens.PERSONAL) },
                    onNavigateToFamiliares = { navController.navigate(AppScreens.ADMIN_FAMILIARES) },
                    onNavigateToConsultas = { navController.navigate(AppScreens.CONSULTAS) },
                    onNavigateToVisitas = { navController.navigate(AppScreens.VISITAS) },
                    onNavigateToPagos = { navController.navigate(AppScreens.PAGOS) },
                    onNavigateToNotificaciones = { navController.navigate(AppScreens.NOTIFICACIONES) },
                    onNavigateToReportes = { navController.navigate(AppScreens.REPORTES) },
                    onNavigateToConfiguracion = { navController.navigate(AppScreens.CONFIGURACION) },
                    onLogout = logoutAction
                )
            }
        }

        // Módulo Administrador -> Familiares y Vínculos (id_rol = 1)
        composable(route = AppScreens.ADMIN_FAMILIARES) {
            if (UserSessionManager.getUserRoleId() != 1) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            } else {
                FamiliaresScreen(
                    onBackClicked = { navController.popBackStack() }
                )
            }
        }

        // DASHBOARD PERSONAL DEL ASILO (id_rol = 2)
        composable(route = AppScreens.PERSONAL_DASHBOARD) {
            if (UserSessionManager.getUserRoleId() != 2) {
                LaunchedEffect(Unit) {
                    logoutAction()
                }
            } else {
                PersonalDashboardScreen(
                    onNavigateToResidentes = { navController.navigate(AppScreens.PERSONAL_RESIDENTES) },
                    onNavigateToActividades = { navController.navigate(AppScreens.PERSONAL_ACTIVIDADES) },
                    onNavigateToConsultas = { navController.navigate(AppScreens.PERSONAL_CONSULTAS) },
                    onNavigateToVisitas = { navController.navigate(AppScreens.PERSONAL_VISITAS) },
                    onNavigateToNotificaciones = { navController.navigate(AppScreens.PERSONAL_NOTIFICACIONES) },
                    onNavigateToPerfil = { navController.navigate(AppScreens.PERFIL) },
                    onLogout = logoutAction
                )
            }
        }

        // DASHBOARD FAMILIAR (id_rol = 3)
        composable(route = AppScreens.FAMILIAR_DASHBOARD) {
            if (UserSessionManager.getUserRoleId() != 3) {
                LaunchedEffect(Unit) {
                    logoutAction()
                }
            } else {
                FamiliarDashboardScreen(
                    onNavigateToMiFamiliar = { navController.navigate(AppScreens.MI_FAMILIAR) },
                    onNavigateToConsultas = { navController.navigate(AppScreens.FAMILIAR_CONSULTAS) },
                    onNavigateToVisitas = { navController.navigate(AppScreens.FAMILIAR_VISITAS) },
                    onNavigateToPagos = { navController.navigate(AppScreens.FAMILIAR_PAGOS) },
                    onNavigateToNotificaciones = { navController.navigate(AppScreens.FAMILIAR_NOTIFICACIONES) },
                    onNavigateToPerfil = { navController.navigate(AppScreens.PERFIL) },
                    onLogout = logoutAction
                )
            }
        }

        // Módulos
        composable(route = AppScreens.RESIDENTES) {
            if (UserSessionManager.getUserRoleId() != 1) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            } else {
                ResidentesScreen(onBackClicked = { navController.popBackStack() })
            }
        }

        // Módulo Personal -> Residentes (id_rol = 2)
        composable(route = AppScreens.PERSONAL_RESIDENTES) {
            val roleId = UserSessionManager.getUserRoleId()
            if (roleId != 2) {
                LaunchedEffect(roleId) {
                    val redirect = when (roleId) {
                        1 -> AppScreens.ADMIN_DASHBOARD
                        3 -> AppScreens.FAMILIAR_DASHBOARD
                        else -> AppScreens.LOGIN
                    }
                    navController.navigate(redirect) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                ResidentesPersonalScreen(
                    onResidenteClick = { idResidente ->
                        navController.navigate("personal/residentes/detalle/$idResidente")
                    },
                    onBackClicked = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = AppScreens.PERSONAL_RESIDENTE_DETALLE,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val roleId = UserSessionManager.getUserRoleId()
            val idResidente = backStackEntry.arguments?.getInt("id") ?: 0
            if (roleId != 2) {
                LaunchedEffect(roleId) {
                    val redirect = when (roleId) {
                        1 -> AppScreens.ADMIN_DASHBOARD
                        3 -> AppScreens.FAMILIAR_DASHBOARD
                        else -> AppScreens.LOGIN
                    }
                    navController.navigate(redirect) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                ResidentePersonalDetailScreen(
                    idResidente = idResidente,
                    onBackClicked = { navController.popBackStack() }
                )
            }
        }
        composable(route = AppScreens.PERSONAL) {
            if (UserSessionManager.getUserRoleId() != 1) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            } else {
                PersonalScreen(onBackClicked = { navController.popBackStack() })
            }
        }
        composable(route = AppScreens.CONSULTAS) {
            if (UserSessionManager.getUserRoleId() != 1) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            } else {
                ConsultasScreen(onBackClicked = { navController.popBackStack() })
            }
        }

        // Módulo Personal -> Consultas Médicas (id_rol = 2)
        composable(route = AppScreens.PERSONAL_CONSULTAS) {
            val roleId = UserSessionManager.getUserRoleId()
            if (roleId != 2) {
                LaunchedEffect(roleId) {
                    val redirect = when (roleId) {
                        1 -> AppScreens.ADMIN_DASHBOARD
                        3 -> AppScreens.FAMILIAR_DASHBOARD
                        else -> AppScreens.LOGIN
                    }
                    navController.navigate(redirect) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                ConsultasMedicasScreen(
                    onNavigateToNuevaConsulta = { navController.navigate(AppScreens.PERSONAL_CONSULTAS_NUEVA) },
                    onConsultaClick = { idConsulta ->
                        navController.navigate("personal/consultas/detalle/$idConsulta")
                    },
                    onBackClicked = { navController.popBackStack() }
                )
            }
        }

        composable(route = AppScreens.PERSONAL_CONSULTAS_NUEVA) {
            val roleId = UserSessionManager.getUserRoleId()
            if (roleId != 2) {
                LaunchedEffect(roleId) {
                    val redirect = when (roleId) {
                        1 -> AppScreens.ADMIN_DASHBOARD
                        3 -> AppScreens.FAMILIAR_DASHBOARD
                        else -> AppScreens.LOGIN
                    }
                    navController.navigate(redirect) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                ConsultaMedicaFormScreen(
                    onBackClicked = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = AppScreens.PERSONAL_CONSULTAS_DETALLE,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val roleId = UserSessionManager.getUserRoleId()
            val idConsulta = backStackEntry.arguments?.getInt("id") ?: 0
            if (roleId != 2) {
                LaunchedEffect(roleId) {
                    val redirect = when (roleId) {
                        1 -> AppScreens.ADMIN_DASHBOARD
                        3 -> AppScreens.FAMILIAR_DASHBOARD
                        else -> AppScreens.LOGIN
                    }
                    navController.navigate(redirect) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                ConsultaMedicaDetailScreen(
                    idConsulta = idConsulta,
                    onBackClicked = { navController.popBackStack() }
                )
            }
        }
        composable(route = AppScreens.VISITAS) {
            if (UserSessionManager.getUserRoleId() != 1) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            } else {
                VisitasScreen(onBackClicked = { navController.popBackStack() })
            }
        }

        // Módulo Personal -> Visitas Programadas (id_rol = 2)
        composable(route = AppScreens.PERSONAL_VISITAS) {
            val roleId = UserSessionManager.getUserRoleId()
            if (roleId != 2) {
                LaunchedEffect(roleId) {
                    val redirect = when (roleId) {
                        1 -> AppScreens.ADMIN_DASHBOARD
                        3 -> AppScreens.FAMILIAR_DASHBOARD
                        else -> AppScreens.LOGIN
                    }
                    navController.navigate(redirect) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                VisitasProgramadasScreen(
                    onVisitaClick = { idVisita ->
                        navController.navigate("personal/visitas/detalle/$idVisita")
                    },
                    onBackClicked = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = AppScreens.PERSONAL_VISITA_DETALLE,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val roleId = UserSessionManager.getUserRoleId()
            val idVisita = backStackEntry.arguments?.getInt("id") ?: 0
            if (roleId != 2) {
                LaunchedEffect(roleId) {
                    val redirect = when (roleId) {
                        1 -> AppScreens.ADMIN_DASHBOARD
                        3 -> AppScreens.FAMILIAR_DASHBOARD
                        else -> AppScreens.LOGIN
                    }
                    navController.navigate(redirect) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                VisitaProgramadaDetailScreen(
                    idVisita = idVisita,
                    onBackClicked = { navController.popBackStack() }
                )
            }
        }
        composable(route = AppScreens.PAGOS) {
            if (UserSessionManager.getUserRoleId() != 1) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            } else {
                PagosScreen(onBackClicked = { navController.popBackStack() })
            }
        }
        // Módulo Personal -> Actividades (id_rol = 2)
        composable(route = AppScreens.PERSONAL_ACTIVIDADES) {
            val roleId = UserSessionManager.getUserRoleId()
            if (roleId != 2) {
                LaunchedEffect(roleId) {
                    val redirect = when (roleId) {
                        1 -> AppScreens.ADMIN_DASHBOARD
                        3 -> AppScreens.FAMILIAR_DASHBOARD
                        else -> AppScreens.LOGIN
                    }
                    navController.navigate(redirect) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                ActividadesScreen(
                    onNavigateToNuevaActividad = { navController.navigate(AppScreens.PERSONAL_ACTIVIDADES_NUEVA) },
                    onActividadClick = { idActividad ->
                        navController.navigate("personal/actividades/detalle/$idActividad")
                    },
                    onBackClicked = { navController.popBackStack() }
                )
            }
        }

        composable(route = AppScreens.PERSONAL_ACTIVIDADES_NUEVA) {
            val roleId = UserSessionManager.getUserRoleId()
            if (roleId != 2) {
                LaunchedEffect(roleId) {
                    val redirect = when (roleId) {
                        1 -> AppScreens.ADMIN_DASHBOARD
                        3 -> AppScreens.FAMILIAR_DASHBOARD
                        else -> AppScreens.LOGIN
                    }
                    navController.navigate(redirect) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                ActividadFormScreen(
                    onBackClicked = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = AppScreens.PERSONAL_ACTIVIDADES_DETALLE,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val roleId = UserSessionManager.getUserRoleId()
            val idActividad = backStackEntry.arguments?.getInt("id") ?: 0
            if (roleId != 2) {
                LaunchedEffect(roleId) {
                    val redirect = when (roleId) {
                        1 -> AppScreens.ADMIN_DASHBOARD
                        3 -> AppScreens.FAMILIAR_DASHBOARD
                        else -> AppScreens.LOGIN
                    }
                    navController.navigate(redirect) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                ActividadDetailScreen(
                    idActividad = idActividad,
                    onBackClicked = { navController.popBackStack() }
                )
            }
        }
        composable(route = AppScreens.NOTIFICACIONES) {
            if (UserSessionManager.getUserRoleId() != 1) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            } else {
                NotificacionesScreen(onBackClicked = { navController.popBackStack() })
            }
        }

        // Módulo Personal -> Notificaciones (id_rol = 2)
        composable(route = AppScreens.PERSONAL_NOTIFICACIONES) {
            val roleId = UserSessionManager.getUserRoleId()
            if (roleId != 2) {
                LaunchedEffect(roleId) {
                    val redirect = when (roleId) {
                        1 -> AppScreens.ADMIN_DASHBOARD
                        3 -> AppScreens.FAMILIAR_DASHBOARD
                        else -> AppScreens.LOGIN
                    }
                    navController.navigate(redirect) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                NotificacionesPersonalScreen(
                    onNotificacionClick = { idNotificacion ->
                        navController.navigate("personal/notificaciones/detalle/$idNotificacion")
                    },
                    onBackClicked = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = AppScreens.PERSONAL_NOTIFICACION_DETALLE,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val roleId = UserSessionManager.getUserRoleId()
            val idNotificacion = backStackEntry.arguments?.getInt("id") ?: 0
            if (roleId != 2) {
                LaunchedEffect(roleId) {
                    val redirect = when (roleId) {
                        1 -> AppScreens.ADMIN_DASHBOARD
                        3 -> AppScreens.FAMILIAR_DASHBOARD
                        else -> AppScreens.LOGIN
                    }
                    navController.navigate(redirect) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                NotificacionPersonalDetailScreen(
                    idNotificacion = idNotificacion,
                    onNavigateToReferencia = { tipoRef, idRef ->
                        if (idRef != null) {
                            val targetRoute = when {
                                tipoRef.orEmpty().contains("actividad", true) -> "personal/actividades/detalle/$idRef"
                                tipoRef.orEmpty().contains("consulta", true) -> "personal/consultas/detalle/$idRef"
                                tipoRef.orEmpty().contains("visita", true) -> "personal/visitas/detalle/$idRef"
                                tipoRef.orEmpty().contains("residente", true) -> "personal/residentes/detalle/$idRef"
                                else -> null
                            }
                            targetRoute?.let { navController.navigate(it) }
                        }
                    },
                    onBackClicked = { navController.popBackStack() }
                )
            }
        }
        composable(route = AppScreens.REPORTES) {
            if (UserSessionManager.getUserRoleId() != 1) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            } else {
                ReportesScreen(
                    onNavigateToResidentes = { navController.navigate(AppScreens.RESIDENTES) },
                    onNavigateToPersonal = { navController.navigate(AppScreens.PERSONAL) },
                    onNavigateToConsultas = { navController.navigate(AppScreens.CONSULTAS) },
                    onNavigateToVisitas = { navController.navigate(AppScreens.VISITAS) },
                    onNavigateToPagos = { navController.navigate(AppScreens.PAGOS) },
                    onNavigateToNotificaciones = { navController.navigate(AppScreens.NOTIFICACIONES) },
                    onBackClicked = { navController.popBackStack() }
                )
            }
        }
        composable(route = AppScreens.CONFIGURACION) {
            if (UserSessionManager.getUserRoleId() != 1) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            } else {
                ConfiguracionScreen(
                    onLogout = logoutAction,
                    onBackClicked = { navController.popBackStack() }
                )
            }
        }
        composable(route = AppScreens.PERFIL) {
            PerfilScreen(onBackClicked = { navController.popBackStack() })
        }
        composable(route = AppScreens.MI_FAMILIAR) {
            val roleId = UserSessionManager.getUserRoleId()
            if (roleId != 3) {
                LaunchedEffect(roleId) {
                    val redirect = when (roleId) {
                        1 -> AppScreens.ADMIN_DASHBOARD
                        2 -> AppScreens.PERSONAL_DASHBOARD
                        else -> AppScreens.LOGIN
                    }
                    navController.navigate(redirect) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                MiFamiliarScreen(
                    onBackClicked = { navController.popBackStack() },
                    onNavigateToConsultas = { navController.navigate(AppScreens.FAMILIAR_CONSULTAS) },
                    onNavigateToVisitas = { navController.navigate(AppScreens.FAMILIAR_VISITAS) },
                    onNavigateToPagos = { navController.navigate(AppScreens.PAGOS) },
                    onNavigateToNotificaciones = { navController.navigate(AppScreens.NOTIFICACIONES) }
                )
            }
        }

        composable(route = AppScreens.FAMILIAR_CONSULTAS) {
            val roleId = UserSessionManager.getUserRoleId()
            if (roleId != 3) {
                LaunchedEffect(roleId) {
                    val redirect = when (roleId) {
                        1 -> AppScreens.ADMIN_DASHBOARD
                        2 -> AppScreens.PERSONAL_DASHBOARD
                        else -> AppScreens.LOGIN
                    }
                    navController.navigate(redirect) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                ConsultasMedicasFamiliarScreen(
                    onConsultaClick = { idConsulta ->
                        navController.navigate("familiar/consultas/detalle/$idConsulta")
                    },
                    onBackClicked = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = AppScreens.FAMILIAR_CONSULTA_DETALLE,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val roleId = UserSessionManager.getUserRoleId()
            val idConsulta = backStackEntry.arguments?.getInt("id") ?: 0
            if (roleId != 3) {
                LaunchedEffect(roleId) {
                    val redirect = when (roleId) {
                        1 -> AppScreens.ADMIN_DASHBOARD
                        2 -> AppScreens.PERSONAL_DASHBOARD
                        else -> AppScreens.LOGIN
                    }
                    navController.navigate(redirect) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                ConsultaMedicaFamiliarDetailScreen(
                    idConsulta = idConsulta,
                    onBackClicked = { navController.popBackStack() }
                )
            }
        }

        composable(route = AppScreens.FAMILIAR_VISITAS) {
            val roleId = UserSessionManager.getUserRoleId()
            if (roleId != 3) {
                LaunchedEffect(roleId) {
                    val redirect = when (roleId) {
                        1 -> AppScreens.ADMIN_DASHBOARD
                        2 -> AppScreens.PERSONAL_DASHBOARD
                        else -> AppScreens.LOGIN
                    }
                    navController.navigate(redirect) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                VisitasFamiliarScreen(
                    onVisitaClick = { idVisita ->
                        navController.navigate("familiar/visitas/detalle/$idVisita")
                    },
                    onBackClicked = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = AppScreens.FAMILIAR_VISITA_DETALLE,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val roleId = UserSessionManager.getUserRoleId()
            val idVisita = backStackEntry.arguments?.getInt("id") ?: 0
            if (roleId != 3) {
                LaunchedEffect(roleId) {
                    val redirect = when (roleId) {
                        1 -> AppScreens.ADMIN_DASHBOARD
                        2 -> AppScreens.PERSONAL_DASHBOARD
                        else -> AppScreens.LOGIN
                    }
                    navController.navigate(redirect) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                VisitaFamiliarDetailScreen(
                    idVisita = idVisita,
                    onBackClicked = { navController.popBackStack() }
                )
            }
        }

        composable(route = AppScreens.FAMILIAR_PAGOS) {
            val roleId = UserSessionManager.getUserRoleId()
            if (roleId != 3) {
                LaunchedEffect(roleId) {
                    val redirect = when (roleId) {
                        1 -> AppScreens.ADMIN_DASHBOARD
                        2 -> AppScreens.PERSONAL_DASHBOARD
                        else -> AppScreens.LOGIN
                    }
                    navController.navigate(redirect) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                EstadoPagosFamiliarScreen(
                    onPagoClick = { idPago ->
                        navController.navigate("familiar/pagos/detalle/$idPago")
                    },
                    onBackClicked = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = AppScreens.FAMILIAR_PAGO_DETALLE,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val roleId = UserSessionManager.getUserRoleId()
            val idPago = backStackEntry.arguments?.getInt("id") ?: 0
            if (roleId != 3) {
                LaunchedEffect(roleId) {
                    val redirect = when (roleId) {
                        1 -> AppScreens.ADMIN_DASHBOARD
                        2 -> AppScreens.PERSONAL_DASHBOARD
                        else -> AppScreens.LOGIN
                    }
                    navController.navigate(redirect) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                DetallePagoFamiliarScreen(
                    idPago = idPago,
                    onBackClicked = { navController.popBackStack() }
                )
            }
        }

        composable(route = AppScreens.FAMILIAR_NOTIFICACIONES) {
            val roleId = UserSessionManager.getUserRoleId()
            if (roleId != 3) {
                LaunchedEffect(roleId) {
                    val redirect = when (roleId) {
                        1 -> AppScreens.ADMIN_DASHBOARD
                        2 -> AppScreens.PERSONAL_DASHBOARD
                        else -> AppScreens.LOGIN
                    }
                    navController.navigate(redirect) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                NotificacionesFamiliarScreen(
                    onNotificacionClick = { idNotif ->
                        navController.navigate("familiar/notificaciones/detalle/$idNotif")
                    },
                    onBackClicked = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = AppScreens.FAMILIAR_NOTIFICACION_DETALLE,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val roleId = UserSessionManager.getUserRoleId()
            val idNotif = backStackEntry.arguments?.getInt("id") ?: 0
            if (roleId != 3) {
                LaunchedEffect(roleId) {
                    val redirect = when (roleId) {
                        1 -> AppScreens.ADMIN_DASHBOARD
                        2 -> AppScreens.PERSONAL_DASHBOARD
                        else -> AppScreens.LOGIN
                    }
                    navController.navigate(redirect) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                NotificacionFamiliarDetailScreen(
                    idNotificacion = idNotif,
                    onNavigateToReferencia = { tipoRef, idRef ->
                        if (idRef != null) {
                            val targetRoute = when {
                                tipoRef.orEmpty().contains("visita", true) -> "familiar/visitas/detalle/$idRef"
                                tipoRef.orEmpty().contains("consulta", true) -> "familiar/consultas/detalle/$idRef"
                                tipoRef.orEmpty().contains("pago", true) -> "familiar/pagos/detalle/$idRef"
                                else -> null
                            }
                            targetRoute?.let { navController.navigate(it) }
                        }
                    },
                    onBackClicked = { navController.popBackStack() }
                )
            }
        }
    }
}
