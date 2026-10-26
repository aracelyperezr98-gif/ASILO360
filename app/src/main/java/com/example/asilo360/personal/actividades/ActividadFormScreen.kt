package com.example.asilo360.personal.actividades

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.asilo360.personal.actividades.components.ActividadFormContent
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Pantalla para registrar una nueva actividad de cuidado realizada por el personal.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActividadFormScreen(
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActividadesViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()

    val hoyStr = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) }
    val horaStr = remember { LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) }

    var idResidenteSeleccionado by remember { mutableIntStateOf(0) }
    var tipoSeleccionado by remember { mutableStateOf("") }
    var tipoOtroEspecifico by remember { mutableStateOf("") }
    var fechaSeleccionada by remember { mutableStateOf(hoyStr) }
    var horaSeleccionada by remember { mutableStateOf(horaStr) }
    var descripcionText by remember { mutableStateOf("") }

    val residentes = when (val state = uiState) {
        is ActividadesUiState.Success -> state.residentesDisponibles
        else -> emptyList()
    }

    LaunchedEffect(formState) {
        when (val state = formState) {
            is FormOperacionState.Exito -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.resetFormState()
                onBackClicked()
            }
            is FormOperacionState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetFormState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Registrar actividad",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                ActividadFormContent(
                    residentes = residentes,
                    idResidenteSeleccionado = idResidenteSeleccionado,
                    onResidenteSelected = { idResidenteSeleccionado = it },
                    tipoSeleccionado = tipoSeleccionado,
                    onTipoSelected = { tipoSeleccionado = it },
                    tipoEspecificoOtro = tipoOtroEspecifico,
                    onTipoOtroChanged = { tipoOtroEspecifico = it },
                    fecha = fechaSeleccionada,
                    onFechaChanged = { fechaSeleccionada = it },
                    hora = horaSeleccionada,
                    onHoraChanged = { horaSeleccionada = it },
                    descripcion = descripcionText,
                    onDescripcionChanged = { descripcionText = it },
                    esEdicion = false
                )

                Spacer(modifier = Modifier.height(24.dp))

                val isGuardando = formState is FormOperacionState.Guardando

                Button(
                    onClick = {
                        val tipoFinal = if (tipoSeleccionado == "Otro") tipoOtroEspecifico.trim() else tipoSeleccionado.trim()

                        // Validaciones
                        if (idResidenteSeleccionado <= 0) {
                            scope.launch { snackbarHostState.showSnackbar("Selecciona un residente.") }
                            return@Button
                        }
                        if (tipoSeleccionado.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Selecciona un tipo de actividad.") }
                            return@Button
                        }
                        if (tipoSeleccionado == "Otro" && tipoOtroEspecifico.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Especifica el tipo de actividad.") }
                            return@Button
                        }
                        if (fechaSeleccionada.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Selecciona una fecha.") }
                            return@Button
                        }
                        if (horaSeleccionada.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Selecciona una hora.") }
                            return@Button
                        }
                        if (descripcionText.trim().isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Escribe una descripción de la actividad.") }
                            return@Button
                        }

                        viewModel.crearActividad(
                            idResidente = idResidenteSeleccionado,
                            tipo = tipoFinal,
                            fecha = fechaSeleccionada,
                            hora = horaSeleccionada,
                            descripcion = descripcionText.trim(),
                            onSuccess = {}
                        )
                    },
                    enabled = !isGuardando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isGuardando) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                        Text("Guardando actividad...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text(
                            text = "Guardar actividad",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
