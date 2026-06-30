package pe.cmacica.clientes.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CreditScore
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.cmacica.clientes.R
import pe.cmacica.clientes.ui.components.CircularMenuGrid
import pe.cmacica.clientes.ui.components.MenuOption
import pe.cmacica.clientes.ui.theme.CajaIcaGold
import pe.cmacica.clientes.ui.theme.CajaIcaRed
import pe.cmacica.clientes.ui.viewmodel.DashboardViewModel

@Composable
fun ServiciosMenuScreen(
    onMenuOption: (String) -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val menuOptions = listOf(
        MenuOption(
            id = MenuIds.PAGO_SERVICIOS,
            label = stringResource(R.string.menu_pay_services),
            icon = Icons.Outlined.ReceiptLong,
            backgroundColor = Color(0xFF1565C0)
        ),
        MenuOption(
            id = MenuIds.PRESTAMOS,
            label = stringResource(R.string.menu_loans),
            icon = Icons.Outlined.AccountBalance,
            backgroundColor = CajaIcaRed
        ),
        MenuOption(
            id = MenuIds.SOLICITUD_CREDITO,
            label = stringResource(R.string.menu_credit_request),
            icon = Icons.Outlined.CreditScore,
            backgroundColor = Color(0xFF6A1B9A)
        ),
        MenuOption(
            id = MenuIds.TRANSFERENCIAS,
            label = stringResource(R.string.menu_transfers),
            icon = Icons.Outlined.SwapHoriz,
            backgroundColor = Color(0xFF00838F)
        ),
        MenuOption(
            id = MenuIds.AHORROS,
            label = stringResource(R.string.menu_savings),
            icon = Icons.Outlined.AccountBalanceWallet,
            backgroundColor = Color(0xFF2E7D32)
        ),
        MenuOption(
            id = MenuIds.MIS_SOLICITUDES,
            label = stringResource(R.string.menu_credit_requests),
            icon = Icons.Outlined.FolderOpen,
            backgroundColor = CajaIcaGold,
            iconTint = Color.White,
            badgeCount = uiState.solicitudes.size
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.home_menu_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        CircularMenuGrid(
            options = menuOptions,
            onOptionClick = { option -> onMenuOption(option.id) }
        )
    }
}
