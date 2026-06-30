package pe.cmacica.clientes.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import android.app.Application
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.cmacica.clientes.R
import pe.cmacica.clientes.ui.theme.CajaIcaGold
import pe.cmacica.clientes.ui.theme.CajaIcaRed
import pe.cmacica.clientes.ui.util.RegistroValidacion
import pe.cmacica.clientes.ui.viewmodel.RegisterViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: RegisterViewModel = viewModel(
        factory = RegisterViewModel.factory(LocalContext.current.applicationContext as Application)
    )
) {
    var documento by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var fechaTexto by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var confirmarClave by remember { mutableStateOf("") }
    var mostrarDatePicker by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scroll = rememberScrollState()
    val fechaDefaultMillis = remember {
        Calendar.getInstance().apply { add(Calendar.YEAR, -25) }.timeInMillis
    }

    fun abrirDatePicker() {
        if (!uiState.isLoading) mostrarDatePicker = true
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    LaunchedEffect(uiState.registerSuccess) {
        if (uiState.registerSuccess) {
            viewModel.consumeRegisterSuccess()
            onRegisterSuccess()
        }
    }

    if (mostrarDatePicker) {
        val initialMillis = RegistroValidacion.millisDesdeIso(fechaNacimiento) ?: fechaDefaultMillis
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            yearRange = IntRange(
                Calendar.getInstance().get(Calendar.YEAR) - 100,
                Calendar.getInstance().get(Calendar.YEAR)
            )
        )
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis ?: fechaDefaultMillis
                        fechaNacimiento = RegistroValidacion.fechaDesdeMillis(millis)
                        fechaTexto = RegistroValidacion.formatearFechaDisplay(fechaNacimiento)
                        mostrarDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.action_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.register_title)) },
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
                .verticalScroll(scroll)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.register_section_personal),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = documento,
                onValueChange = { documento = it.filter { c -> c.isDigit() }.take(12) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.field_documento)) },
                supportingText = { Text(stringResource(R.string.field_documento_hint)) },
                singleLine = true,
                enabled = !uiState.isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = fieldColors()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it.filter { c -> c.isLetter() || c.isWhitespace() || c == '\'' || c == '-' } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.field_first_name)) },
                singleLine = true,
                enabled = !uiState.isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                colors = fieldColors()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = apellidos,
                onValueChange = { apellidos = it.filter { c -> c.isLetter() || c.isWhitespace() || c == '\'' || c == '-' } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.field_last_name)) },
                singleLine = true,
                enabled = !uiState.isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                colors = fieldColors()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = fechaTexto,
                onValueChange = { input ->
                    fechaTexto = RegistroValidacion.filtrarEntradaFecha(input)
                    fechaNacimiento = RegistroValidacion.parseDisplayToIso(fechaTexto).orEmpty()
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.field_birth_date)) },
                supportingText = { Text(stringResource(R.string.field_birth_date_hint)) },
                placeholder = { Text("dd/mm/aaaa") },
                singleLine = true,
                enabled = !uiState.isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    IconButton(onClick = { abrirDatePicker() }, enabled = !uiState.isLoading) {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = stringResource(R.string.field_birth_date),
                            tint = CajaIcaRed
                        )
                    }
                },
                colors = fieldColors()
            )
            TextButton(
                onClick = { abrirDatePicker() },
                enabled = !uiState.isLoading,
                modifier = Modifier.align(Alignment.End)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = CajaIcaRed)
                    Text(
                        text = stringResource(R.string.action_pick_date),
                        color = CajaIcaRed,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.register_section_security),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = clave,
                onValueChange = { clave = it.filter { c -> c.isDigit() }.take(12) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.field_numeric_password)) },
                supportingText = { Text(stringResource(R.string.field_password_hint)) },
                singleLine = true,
                enabled = !uiState.isLoading,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                colors = fieldColors()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = confirmarClave,
                onValueChange = { confirmarClave = it.filter { c -> c.isDigit() }.take(12) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.field_confirm_numeric_password)) },
                singleLine = true,
                enabled = !uiState.isLoading,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                colors = fieldColors()
            )
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = {
                    viewModel.register(
                        documento = documento,
                        nombre = nombre,
                        apellidos = apellidos,
                        fechaNacimiento = fechaNacimiento,
                        password = clave,
                        confirmPassword = confirmarClave
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CajaIcaRed,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.action_create_account),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CajaIcaRed,
    focusedLabelColor = CajaIcaRed,
    cursorColor = CajaIcaRed
)
