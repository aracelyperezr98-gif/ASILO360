package com.example.asilo360.admin.pagos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.asilo360.components.CustomTextField
import com.example.asilo360.components.PrimaryButton
import com.example.asilo360.data.model.Pago
import com.example.asilo360.data.model.Residente

/**
 * Formulario para registrar un nuevo pago o editar uno existente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagoFormScreen(
    pagoParaEditar: Pago?,
    residentesDisponibles: List<Residente>,
    onSubmit: (Pago) -> Unit,
    onBackClicked: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isEditing = pagoParaEditar != null

    val listaResidentesActivos = remember(residentesDisponibles) {
        residentesDisponibles.filter { it.estado.equals("Activo", ignoreCase = true) }
    }

    var selectedResidente by remember {
        mutableStateOf<Residente?>(
            residentesDisponibles.find { it.id_residente == pagoParaEditar?.id_residente }
        )
    }

    var concepto by remember { mutableStateOf(pagoParaEditar?.concepto ?: "Mensualidad") }
    var montoText by remember { mutableStateOf(pagoParaEditar?.monto?.toString() ?: "5500") }
    var fechaPago by remember { mutableStateOf(pagoParaEditar?.fecha_pago ?: "2026-09-25") }
    var metodoPago by remember { mutableStateOf(pagoParaEditar?.metodo_pago ?: "Transferencia") }
    var estado by remember { mutableStateOf(pagoParaEditar?.estado ?: "Pagado") }

    // Mensajes de error
    var residenteError by remember { mutableStateOf<String?>(null) }
    var conceptoError by remember { mutableStateOf<String?>(null) }
    var montoError by remember { mutableStateOf<String?>(null) }
    var fechaError by remember { mutableStateOf<String?>(null) }

    // Dropdowns
    var residenteExpanded by remember { mutableStateOf(false) }
    var metodoExpanded by remember { mutableStateOf(false) }
    var estadoExpanded by remember { mutableStateOf(false) }

    val metodosList = listOf("Efectivo", "Transferencia", "Tarjeta", "Depósito")
    val estadosList = listOf("Pagado", "Pendiente", "Cancelado")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Editar Pago" else "Registrar Pago",
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
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Selector 1: Residente
            Text(
                text = "Residente",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                val textoResidente = selectedResidente?.let {
                    "${it.nombre} ${it.apellido_paterno} (Hab: ${it.habitacion ?: "Sin asignar"})"
                } ?: "Seleccionar residente *"

                OutlinedTextField(
                    value = textoResidente,
                    onValueChange = {},
                    readOnly = true,
                    isError = residenteError != null,
                    leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { residenteExpanded = true }) {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { residenteExpanded = true },
                    shape = RoundedCornerShape(12.dp)
                )

                DropdownMenu(
                    expanded = residenteExpanded,
                    onDismissRequest = { residenteExpanded = false }
                ) {
                    if (listaResidentesActivos.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No hay residentes activos registrados") },
                            onClick = { residenteExpanded = false }
                        )
                    } else {
                        listaResidentesActivos.forEach { r ->
                            val itemNombre = "${r.nombre} ${r.apellido_paterno} ${r.apellido_materno ?: ""}".trim()
                            val hab = r.habitacion?.let { " - Hab: $it" } ?: ""
                            DropdownMenuItem(
                                text = { Text("$itemNombre$hab") },
                                onClick = {
                                    selectedResidente = r
                                    residenteError = null
                                    residenteExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            if (residenteError != null) {
                Text(
                    text = residenteError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Concepto
            Text(
                text = "Concepto de Pago",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            CustomTextField(
                value = concepto,
                onValueChange = {
                    concepto = it
                    if (conceptoError != null) conceptoError = null
                },
                placeholder = "Concepto (Ej: Mensualidad, Medicamentos) *",
                leadingIcon = Icons.Default.Description,
                isError = conceptoError != null,
                errorMessage = conceptoError
            )

            // Monto
            Text(
                text = "Monto a Pagar ($)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            CustomTextField(
                value = montoText,
                onValueChange = {
                    montoText = it
                    if (montoError != null) montoError = null
                },
                placeholder = "Monto en Pesos (Ej: 5500.00) *",
                leadingIcon = Icons.Default.AttachMoney,
                isError = montoError != null,
                errorMessage = montoError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Fecha de Pago
            Text(
                text = "Fecha de Registro",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            CustomTextField(
                value = fechaPago,
                onValueChange = {
                    fechaPago = it
                    if (fechaError != null) fechaError = null
                },
                placeholder = "Fecha (AAAA-MM-DD) *",
                leadingIcon = Icons.Default.CalendarToday,
                isError = fechaError != null,
                errorMessage = fechaError
            )

            // Método de Pago
            Text(
                text = "Método y Estado de Pago",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = metodoPago,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Método de Pago") },
                    leadingIcon = { Icon(imageVector = Icons.Default.CreditCard, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { metodoExpanded = true }) {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { metodoExpanded = true },
                    shape = RoundedCornerShape(12.dp)
                )

                DropdownMenu(
                    expanded = metodoExpanded,
                    onDismissRequest = { metodoExpanded = false }
                ) {
                    metodosList.forEach { metodo ->
                        DropdownMenuItem(
                            text = { Text(metodo) },
                            onClick = {
                                metodoPago = metodo
                                metodoExpanded = false
                            }
                        )
                    }
                }
            }

            // Estado de Pago
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = estado,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Estado del Pago") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Info, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { estadoExpanded = true }) {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { estadoExpanded = true },
                    shape = RoundedCornerShape(12.dp)
                )

                DropdownMenu(
                    expanded = estadoExpanded,
                    onDismissRequest = { estadoExpanded = false }
                ) {
                    estadosList.forEach { e ->
                        DropdownMenuItem(
                            text = { Text(e) },
                            onClick = {
                                estado = e
                                estadoExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            PrimaryButton(
                text = if (isEditing) "Guardar Cambios" else "Registrar Pago",
                onClick = {
                    var isValid = true

                    if (selectedResidente == null) {
                        residenteError = "Selecciona un residente."
                        isValid = false
                    }

                    if (concepto.trim().isEmpty()) {
                        conceptoError = "Ingresa el concepto."
                        isValid = false
                    }

                    val parsedMonto = montoText.toDoubleOrNull()
                    if (parsedMonto == null || parsedMonto <= 0.0) {
                        montoError = "Ingresa un monto válido mayor a $0."
                        isValid = false
                    }

                    if (fechaPago.trim().isEmpty()) {
                        fechaError = "La fecha es obligatoria."
                        isValid = false
                    }

                    if (isValid) {
                        val pagoSubmit = Pago(
                            id_pago = pagoParaEditar?.id_pago ?: 0,
                            id_residente = selectedResidente!!.id_residente,
                            residente = "${selectedResidente!!.nombre} ${selectedResidente!!.apellido_paterno}",
                            monto = parsedMonto!!,
                            fecha_pago = fechaPago.trim(),
                            concepto = concepto.trim(),
                            metodo_pago = metodoPago,
                            estado = estado
                        )
                        onSubmit(pagoSubmit)
                    }
                },
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
