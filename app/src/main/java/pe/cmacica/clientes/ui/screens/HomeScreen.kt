package pe.cmacica.clientes.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.cmacica.clientes.R
import pe.cmacica.clientes.ui.theme.CajaIcaGold
import pe.cmacica.clientes.ui.theme.CajaIcaRed
import pe.cmacica.clientes.ui.util.RegistroValidacion
import pe.cmacica.clientes.ui.util.formatSoles
import pe.cmacica.clientes.ui.viewmodel.DashboardViewModel

@Composable
fun HomeScreen(
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(color = CajaIcaRed)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SaldoCard(
                    saldo = uiState.saldo,
                    visible = uiState.saldoVisible,
                    onToggleVisibility = viewModel::toggleSaldoVisible
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.movements_section),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (uiState.movimientos.isNotEmpty()) {
                        Text(
                            text = stringResource(
                                R.string.movements_count,
                                uiState.movimientos.size
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (uiState.movimientos.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.movements_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(
                    count = uiState.movimientosPaginaActual.size,
                    key = { index -> uiState.movimientosPaginaActual[index].id }
                ) { index ->
                    MovimientoRowShared(uiState.movimientosPaginaActual[index])
                }
                item {
                    MovimientosPaginacion(
                        paginaActual = uiState.movimientosPagina + 1,
                        totalPaginas = uiState.totalPaginasMovimientos,
                        puedeAnterior = uiState.movimientosPagina > 0,
                        puedeSiguiente = uiState.movimientosPagina < uiState.totalPaginasMovimientos - 1,
                        onAnterior = viewModel::paginaAnteriorMovimientos,
                        onSiguiente = viewModel::paginaSiguienteMovimientos
                    )
                }
            }
        }
    }
}

@Composable
fun SaldoCard(
    saldo: Double,
    visible: Boolean,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CajaIcaRed)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.balance_total_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                IconButton(
                    onClick = onToggleVisibility,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = stringResource(
                            if (visible) R.string.action_hide_balance else R.string.action_show_balance
                        ),
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (visible) formatSoles(saldo) else stringResource(R.string.balance_hidden),
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MovimientosPaginacion(
    paginaActual: Int,
    totalPaginas: Int,
    puedeAnterior: Boolean,
    puedeSiguiente: Boolean,
    onAnterior: () -> Unit,
    onSiguiente: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.movements_page_info, paginaActual, totalPaginas),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onAnterior,
                enabled = puedeAnterior,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(stringResource(R.string.action_previous_page))
            }
            Spacer(modifier = Modifier.size(8.dp))
            OutlinedButton(
                onClick = onSiguiente,
                enabled = puedeSiguiente,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.action_next_page))
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

fun inicialesUsuario(nombre: String, apellidos: String): String {
    val n = nombre.trim().firstOrNull()?.uppercaseChar()?.toString().orEmpty()
    val a = apellidos.trim().firstOrNull()?.uppercaseChar()?.toString().orEmpty()
    return (n + a).ifBlank { "CI" }
}

fun formatFechaPerfil(fechaIso: String): String =
    if (fechaIso.isBlank()) "—" else RegistroValidacion.formatearFechaDisplay(fechaIso)
