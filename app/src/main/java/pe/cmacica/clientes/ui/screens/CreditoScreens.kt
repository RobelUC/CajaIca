package pe.cmacica.clientes.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.cmacica.clientes.R
import pe.cmacica.clientes.ui.theme.CajaIcaGold
import pe.cmacica.clientes.ui.theme.CajaIcaRed
import pe.cmacica.clientes.ui.viewmodel.NuevaSolicitudViewModel

private val destinosCredito = listOf(
    "Capital de trabajo",
    "Activo fijo",
    "Consumo",
    "Vivienda",
    "Educación",
    "Refinanciamiento"
)

private val tiposGarantia = listOf(
    "Personal",
    "Prendaria",
    "Hipotecaria",
    "Fondo de garantía",
    "Sin garantía real"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaSolicitudScreen(
    onBack: () -> Unit,
    onSolicitudRegistrada: (solicitudId: String, expediente: String) -> Unit,
    viewModel: NuevaSolicitudViewModel = viewModel()
) {
    var montoText by remember { mutableStateOf("") }
    var plazoText by remember { mutableStateOf("") }
    var destino by remember { mutableStateOf(destinosCredito.first()) }
    var garantia by remember { mutableStateOf(tiposGarantia.first()) }
    var destinoExpanded by remember { mutableStateOf(false) }
    var garantiaExpanded by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    LaunchedEffect(uiState.solicitudCreada) {
        uiState.solicitudCreada?.let { solicitud ->
            onSolicitudRegistrada(solicitud.id, solicitud.expediente)
            viewModel.consumeSolicitudCreada()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.credit_new_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !uiState.isLoading) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CajaIcaGold,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.credit_new_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = montoText,
                onValueChange = { montoText = it.filter { c -> c.isDigit() || c == '.' } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.field_monto)) },
                prefix = { Text("S/ ") },
                singleLine = true,
                enabled = !uiState.isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = fieldColors()
            )

            OutlinedTextField(
                value = plazoText,
                onValueChange = { plazoText = it.filter { c -> c.isDigit() } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.field_plazo)) },
                suffix = { Text(stringResource(R.string.field_plazo_suffix)) },
                singleLine = true,
                enabled = !uiState.isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = fieldColors()
            )

            ExposedDropdownMenuBox(
                expanded = destinoExpanded,
                onExpandedChange = { destinoExpanded = !destinoExpanded }
            ) {
                OutlinedTextField(
                    value = destino,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    label = { Text(stringResource(R.string.field_destino)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = destinoExpanded) },
                    colors = fieldColors()
                )
                ExposedDropdownMenu(
                    expanded = destinoExpanded,
                    onDismissRequest = { destinoExpanded = false }
                ) {
                    destinosCredito.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                destino = option
                                destinoExpanded = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = garantiaExpanded,
                onExpandedChange = { garantiaExpanded = !garantiaExpanded }
            ) {
                OutlinedTextField(
                    value = garantia,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    label = { Text(stringResource(R.string.field_garantia)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = garantiaExpanded) },
                    colors = fieldColors()
                )
                ExposedDropdownMenu(
                    expanded = garantiaExpanded,
                    onDismissRequest = { garantiaExpanded = false }
                ) {
                    tiposGarantia.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                garantia = option
                                garantiaExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.registrarSolicitud(
                        monto = montoText.toDoubleOrNull() ?: 0.0,
                        plazoMeses = plazoText.toIntOrNull() ?: 0,
                        destino = destino,
                        garantia = garantia
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = CajaIcaRed)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.action_send_request))
                }
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CajaIcaRed,
    focusedLabelColor = CajaIcaRed,
    cursorColor = CajaIcaRed
)

@Composable
fun ConfirmacionExpedienteScreen(
    expediente: String,
    onGoHome: () -> Unit,
    onVerDetalle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .background(CajaIcaRed.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.credit_success_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = CajaIcaRed
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.credit_expediente_label),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = expediente,
                    style = MaterialTheme.typography.headlineMedium,
                    color = CajaIcaRed
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.credit_success_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onVerDetalle,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = CajaIcaGold)
        ) {
            Text(stringResource(R.string.action_view_detail))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onGoHome,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = CajaIcaRed)
        ) {
            Text(stringResource(R.string.action_go_home))
        }
    }
}
