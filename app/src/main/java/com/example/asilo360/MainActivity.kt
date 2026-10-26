package com.example.asilo360

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.asilo360.data.notification.NotificationHelper
import com.example.asilo360.navigation.AppNavigation
import com.example.asilo360.ui.theme.Asilo360Theme
import com.example.asilo360.ui.theme.ThemeManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicializar gestor de tema y permisos
        ThemeManager.init(this)
        NotificationHelper.solicitarPermisoNotificacion(this)
        NotificationHelper.crearCanalNotificaciones(this)

        setContent {
            val themeOption by ThemeManager.themeOption.collectAsState()
            Asilo360Theme(themeOption = themeOption) {
                AppNavigation()
            }
        }
    }
}
