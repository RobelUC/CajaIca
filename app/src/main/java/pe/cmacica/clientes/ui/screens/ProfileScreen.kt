package pe.cmacica.clientes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CreditScore
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.cmacica.clientes.R
import pe.cmacica.clientes.ui.theme.CajaIcaGold
import pe.cmacica.clientes.ui.theme.CajaIcaRed
import pe.cmacica.clientes.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onMisSolicitudes: () -> Unit,
    onNuevaSolicitud: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val nombreCompleto = listOf(uiState.nombre, uiState.apellidos)
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifBlank { uiState.nombre.ifBlank { "Cliente" } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            ProfileHeader(
                nombreCompleto = nombreCompleto,
                documento = uiState.documento,
                email = uiState.email,
                iniciales = inicialesUsuario(uiState.nombre, uiState.apellidos)
            )

            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.profile_section_account),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )

                ProfileMenuCard {
                    ProfileMenuItem(
                        icon = Icons.Outlined.Person,
                        title = stringResource(R.string.profile_personal_data),
                        subtitle = stringResource(
                            R.string.profile_birth_date_value,
                            formatFechaPerfil(uiState.fechaNacimiento)
                        ),
                        onClick = { }
                    )
                    ProfileDivider()
                    ProfileMenuItem(
                        icon = Icons.Outlined.VerifiedUser,
                        title = stringResource(R.string.profile_document),
                        subtitle = uiState.documento.ifBlank { "—" },
                        onClick = { }
                    )
                    ProfileDivider()
                    ProfileMenuItem(
                        icon = Icons.Outlined.Lock,
                        title = stringResource(R.string.profile_security),
                        subtitle = stringResource(R.string.profile_security_hint),
                        onClick = { }
                    )
                }

                Text(
                    text = stringResource(R.string.profile_section_banking),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                ProfileMenuCard {
                    ProfileMenuItem(
                        icon = Icons.Outlined.FolderOpen,
                        title = stringResource(R.string.menu_credit_requests),
                        subtitle = stringResource(
                            R.string.profile_requests_count,
                            uiState.solicitudes.size
                        ),
                        onClick = onMisSolicitudes
                    )
                    ProfileDivider()
                    ProfileMenuItem(
                        icon = Icons.Outlined.CreditScore,
                        title = stringResource(R.string.menu_credit_request),
                        subtitle = stringResource(R.string.profile_new_request_hint),
                        onClick = onNuevaSolicitud
                    )
                }

                Text(
                    text = stringResource(R.string.profile_section_help),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                ProfileMenuCard {
                    ProfileMenuItem(
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        title = stringResource(R.string.profile_help),
                        subtitle = stringResource(R.string.profile_help_hint),
                        onClick = { }
                    )
                }

                ProfileMenuCard(
                    containerColor = CajaIcaRed.copy(alpha = 0.08f)
                ) {
                    ProfileMenuItem(
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        title = stringResource(R.string.action_logout),
                        subtitle = stringResource(R.string.profile_logout_hint),
                        titleColor = CajaIcaRed,
                        showArrow = false,
                        onClick = {
                            viewModel.logout()
                            onLogout()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    nombreCompleto: String,
    documento: String,
    email: String,
    iniciales: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CajaIcaGold)
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iniciales,
                    style = MaterialTheme.typography.headlineMedium,
                    color = CajaIcaRed,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = nombreCompleto,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            if (documento.isNotBlank()) {
                Text(
                    text = "DNI $documento",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            if (email.isNotBlank()) {
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun ProfileMenuCard(
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            content()
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    showArrow: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(CajaIcaGold.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CajaIcaGold,
                modifier = Modifier.size(22.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = titleColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (showArrow) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProfileDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    )
}
