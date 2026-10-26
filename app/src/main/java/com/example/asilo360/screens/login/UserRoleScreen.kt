package com.example.asilo360.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.asilo360.components.PrimaryButton
import com.example.asilo360.components.UserRoleCard
import com.example.asilo360.model.UserRole

/**
 * Pantalla de selección de tipo de usuario (Administrador, Personal del asilo, Familiar).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserRoleScreen(
    initialRole: UserRole?,
    onRoleSelected: (UserRole) -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Estado local para almacenar el rol seleccionado temporalmente
    var tempSelectedRole by remember { mutableStateOf(initialRole) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Título principal
                Text(
                    text = "¿Cómo ingresarás?",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtítulo
                Text(
                    text = "Selecciona el tipo de usuario que corresponde a tu cuenta.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Lista de tarjetas para cada tipo de usuario
                UserRole.entries.forEach { role ->
                    UserRoleCard(
                        role = role,
                        isSelected = tempSelectedRole == role,
                        onSelect = { tempSelectedRole = role }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Column(
                modifier = Modifier.padding(bottom = 24.dp, top = 16.dp)
            ) {
                // Botón Continuar (deshabilitado hasta seleccionar un rol)
                PrimaryButton(
                    text = "Continuar",
                    onClick = {
                        tempSelectedRole?.let { role ->
                            onRoleSelected(role)
                        }
                    },
                    enabled = tempSelectedRole != null
                )
            }
        }
    }
}
