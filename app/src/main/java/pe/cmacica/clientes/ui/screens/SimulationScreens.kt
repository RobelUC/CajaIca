package pe.cmacica.clientes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.cmacica.clientes.R
import pe.cmacica.clientes.data.model.CuentaAhorro
import androidx.compose.foundation.layout.ColumnScope
import pe.cmacica.clientes.ui.theme.CajaIcaGold
import pe.cmacica.clientes.ui.theme.CajaIcaRed
import pe.cmacica.clientes.ui.util.formatSoles
import pe.cmacica.clientes.ui.viewmodel.OperacionesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagoServiciosScreen(
    onBack: () -> Unit,
    viewModel: OperacionesViewModel = viewModel()
) {
    var servicioExpanded by remember { mutableStateOf(false) }
    var servicio by remember { mutableStateOf(viewModel.serviciosDisponibles.first()) }
    var codigo by remember { mutableStateOf("") }
    var montoText by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
        uiState.successMessage?.let { snackbar.showSnackbar(it); viewModel.consumeSuccess() }
    }

    SimulationScaffold(
        title = stringResource(R.string.menu_pay_services),
        onBack = onBack,
        snackbarHostState = snackbar
    ) {
        SaldoCard(uiState.saldo)
        Text(
            text = stringResource(R.string.sim_pay_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ExposedDropdownMenuBox(
            expanded = servicioExpanded,
            onExpandedChange = { servicioExpanded = !servicioExpanded }
        ) {
            OutlinedTextField(
                value = servicio.nombre,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                label = { Text(stringResource(R.string.sim_service_company)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = servicioExpanded) },
                colors = simFieldColors()
            )
            ExposedDropdownMenu(expanded = servicioExpanded, onDismissRequest = { servicioExpanded = false }) {
                viewModel.serviciosDisponibles.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.nombre) },
                        onClick = { servicio = item; servicioExpanded = false }
                    )
                }
            }
        }
        OutlinedTextField(
            value = codigo,
            onValueChange = { codigo = it.filter { c -> c.isDigit() } },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.sim_supply_code)) },
            placeholder = { Text(servicio.codigoEjemplo) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = simFieldColors()
        )
        OutlinedTextField(
            value = montoText,
            onValueChange = { montoText = it.filter { c -> c.isDigit() || c == '.' } },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.field_monto)) },
            prefix = { Text("S/ ") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = simFieldColors()
        )
        Button(
            onClick = {
                viewModel.pagarServicio(
                    servicio = servicio,
                    codigoCliente = codigo.trim(),
                    monto = montoText.toDoubleOrNull() ?: 0.0
                )
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = !uiState.isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = CajaIcaRed)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.sim_action_pay))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferenciasScreen(
    onBack: () -> Unit,
    viewModel: OperacionesViewModel = viewModel()
) {
    var cuentaDestino by remember { mutableStateOf("") }
    var titular by remember { mutableStateOf("") }
    var montoText by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
        uiState.successMessage?.let { snackbar.showSnackbar(it); viewModel.consumeSuccess() }
    }

    SimulationScaffold(
        title = stringResource(R.string.menu_transfers),
        onBack = onBack,
        snackbarHostState = snackbar
    ) {
        SaldoCard(uiState.saldo)
        Text(
            text = stringResource(R.string.sim_transfer_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = cuentaDestino,
            onValueChange = { cuentaDestino = it.filter { c -> c.isDigit() || c == '-' } },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.sim_dest_account)) },
            placeholder = { Text("001-1234567") },
            singleLine = true,
            colors = simFieldColors()
        )
        OutlinedTextField(
            value = titular,
            onValueChange = { titular = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.sim_dest_holder)) },
            singleLine = true,
            colors = simFieldColors()
        )
        OutlinedTextField(
            value = montoText,
            onValueChange = { montoText = it.filter { c -> c.isDigit() || c == '.' } },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.field_monto)) },
            prefix = { Text("S/ ") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = simFieldColors()
        )
        Button(
            onClick = {
                viewModel.transferir(
                    cuentaDestino = cuentaDestino.trim(),
                    titular = titular.trim(),
                    monto = montoText.toDoubleOrNull() ?: 0.0
                )
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = !uiState.isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = CajaIcaRed)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.sim_action_transfer))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AhorrosScreen(
    onBack: () -> Unit,
    viewModel: OperacionesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.cargarAhorros() }

    SimulationScaffold(
        title = stringResource(R.string.menu_savings),
        onBack = onBack
    ) {
        SaldoCard(uiState.saldo, label = stringResource(R.string.sim_total_consolidated))
        Text(
            text = stringResource(R.string.sim_savings_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (uiState.isLoading && uiState.cuentasAhorro.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = CajaIcaRed)
            }
        } else if (uiState.cuentasAhorro.isEmpty()) {
            Text(
                text = stringResource(R.string.sim_savings_empty),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            uiState.cuentasAhorro.forEach { cuenta ->
                CuentaAhorroCard(cuenta)
            }
        }
    }
}

@Composable
private fun CuentaAhorroCard(cuenta: CuentaAhorro) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(cuenta.nombre, fontWeight = FontWeight.SemiBold)
            Text(cuenta.numero, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatSoles(cuenta.saldo), color = CajaIcaRed, fontWeight = FontWeight.Bold)
                Text("TEA ${cuenta.tea}%", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SaldoCard(saldo: Double, label: String = stringResource(R.string.balance_total_label)) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CajaIcaRed)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, color = Color.White.copy(alpha = 0.9f))
            Text(
                text = formatSoles(saldo),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimulationScaffold(
    title: String,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_back))
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
private fun simFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CajaIcaRed,
    focusedLabelColor = CajaIcaRed,
    cursorColor = CajaIcaRed
)
