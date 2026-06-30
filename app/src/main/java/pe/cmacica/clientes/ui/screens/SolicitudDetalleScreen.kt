package pe.cmacica.clientes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import pe.cmacica.clientes.data.model.SolicitudCredito
import pe.cmacica.clientes.ui.theme.CajaIcaGold
import pe.cmacica.clientes.ui.theme.CajaIcaPositive
import pe.cmacica.clientes.ui.theme.CajaIcaRed
import pe.cmacica.clientes.ui.util.FlujoCreditoUi
import pe.cmacica.clientes.ui.util.formatSoles
import pe.cmacica.clientes.ui.viewmodel.SolicitudDetalleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudDetalleScreen(
    solicitudId: String,
    onBack: () -> Unit,
    viewModel: SolicitudDetalleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(solicitudId) {
        viewModel.observe(solicitudId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.credit_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
        }
    ) { innerPadding ->
        if (uiState.isLoading || uiState.solicitud == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = CajaIcaRed)
            }
        } else {
            SolicitudDetalleContent(
                solicitud = uiState.solicitud!!,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun SolicitudDetalleContent(
    solicitud: SolicitudCredito,
    modifier: Modifier = Modifier
) {
    val estadoActual = FlujoCreditoUi.estadoActual(solicitud.estado)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CajaIcaRed)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = solicitud.expediente,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = FlujoCreditoUi.etiquetaEstado(solicitud.estado),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = formatSoles(solicitud.monto),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${solicitud.plazoMeses} meses · ${solicitud.destino}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(
                        text = "Garantía: ${solicitud.garantia}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(
                        text = "Canal: ${solicitud.canal}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        item {
            Text(
                text = stringResource(R.string.credit_flow_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        items(FlujoCreditoUi.pasos) { paso ->
            val completado = FlujoCreditoUi.pasoCompletado(paso, estadoActual)
            val enCurso = FlujoCreditoUi.pasoEnCurso(paso, estadoActual)
            PasoFlujoRow(
                paso = paso.titulo,
                descripcion = paso.descripcion,
                completado = completado,
                enCurso = enCurso
            )
        }

        if (solicitud.visita.visitado) {
            item {
                InfoCard(
                    titulo = stringResource(R.string.credit_visit_title),
                    lineas = listOf(
                        solicitud.visita.observacion,
                        "Coordenadas: ${solicitud.visita.latitud}, ${solicitud.visita.longitud}"
                    )
                )
            }
        }

        if (solicitud.preEvaluacion.capacidadPagoOk) {
            item {
                InfoCard(
                    titulo = stringResource(R.string.credit_preeval_title),
                    lineas = listOf(
                        "Capacidad de pago: OK",
                        "Buró: ${if (solicitud.preEvaluacion.buroOk) "OK" else "Pendiente"}",
                        "Listas: ${if (solicitud.preEvaluacion.listasOk) "OK" else "Pendiente"}"
                    )
                )
            }
        }

        if (solicitud.decision.tipo.isNotBlank()) {
            item {
                InfoCard(
                    titulo = stringResource(R.string.credit_decision_title),
                    lineas = listOf(
                        "Decisión: ${solicitud.decision.tipo}",
                        solicitud.decision.motivo
                    )
                )
            }
        }

        solicitud.desembolso?.let { desembolso ->
            item {
                InfoCard(
                    titulo = stringResource(R.string.credit_disbursement_title),
                    lineas = listOf(
                        "Monto: ${formatSoles(desembolso.monto)}",
                        "Fecha: ${desembolso.fecha}",
                        "Cuotas: ${desembolso.cronograma.size}"
                    )
                )
            }
        }
    }
}

@Composable
fun SolicitudListItem(
    solicitud: SolicitudCredito,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = solicitud.expediente,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = FlujoCreditoUi.etiquetaEstado(solicitud.estado),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = formatSoles(solicitud.monto),
                style = MaterialTheme.typography.titleMedium,
                color = CajaIcaRed,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun PasoFlujoRow(
    paso: String,
    descripcion: String,
    completado: Boolean,
    enCurso: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        val color = when {
            enCurso -> CajaIcaGold
            completado -> CajaIcaPositive
            else -> MaterialTheme.colorScheme.outline
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            BoxCircle(color = color, completado = completado)
            Spacer(modifier = Modifier.height(4.dp))
        }
        Column(modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)) {
            Text(
                text = paso,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (enCurso) FontWeight.Bold else FontWeight.Normal,
                color = if (completado || enCurso) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BoxCircle(color: Color, completado: Boolean) {
    Column(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(color),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (completado) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun InfoCard(titulo: String, lineas: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            lineas.filter { it.isNotBlank() }.forEach { linea ->
                Text(
                    text = linea,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
