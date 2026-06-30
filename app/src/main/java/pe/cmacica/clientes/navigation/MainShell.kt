package pe.cmacica.clientes.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import pe.cmacica.clientes.R
import pe.cmacica.clientes.ui.components.MainShellTopBar
import pe.cmacica.clientes.ui.screens.HomeScreen
import pe.cmacica.clientes.ui.screens.ServiciosMenuScreen
import pe.cmacica.clientes.ui.screens.inicialesUsuario
import pe.cmacica.clientes.ui.theme.CajaIcaGold
import pe.cmacica.clientes.ui.theme.CajaIcaRed
import pe.cmacica.clientes.ui.viewmodel.DashboardViewModel

object MainTabRoutes {
    const val Home = "main/home"
    const val Servicios = "main/servicios"
}

private data class BottomTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShell(
    rootNavController: NavHostController,
    onMenuOption: (String) -> Unit,
    onOpenProfile: () -> Unit
) {
    val tabNavController = rememberNavController()
    val mainEntry = checkNotNull(rootNavController.currentBackStackEntry)
    val dashboardViewModel: DashboardViewModel = viewModel(mainEntry)
    val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()

    val tabs = listOf(
        BottomTab(
            route = MainTabRoutes.Home,
            label = stringResource(R.string.nav_home),
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
        ),
        BottomTab(
            route = MainTabRoutes.Servicios,
            label = stringResource(R.string.nav_services),
            selectedIcon = Icons.Filled.Apps,
            unselectedIcon = Icons.Outlined.Apps
        )
    )

    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: MainTabRoutes.Home
    val isHome = currentRoute == MainTabRoutes.Home

    Scaffold(
        topBar = {
            MainShellTopBar(
                isHome = isHome,
                nombre = uiState.nombre,
                documento = uiState.documento,
                iniciales = inicialesUsuario(uiState.nombre, uiState.apellidos),
                onOpenProfile = onOpenProfile
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                tabs.forEach { tab ->
                    val selected = currentRoute == tab.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            tabNavController.navigate(tab.route) {
                                popUpTo(tabNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            if (tab.route == MainTabRoutes.Servicios && uiState.solicitudes.isNotEmpty()) {
                                BadgedBox(
                                    badge = { Badge { Text(uiState.solicitudes.size.toString()) } }
                                ) {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.label
                                )
                            }
                        },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CajaIcaRed,
                            selectedTextColor = CajaIcaRed,
                            indicatorColor = CajaIcaGold.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = tabNavController,
            startDestination = MainTabRoutes.Home,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(MainTabRoutes.Home) {
                HomeScreen(viewModel = dashboardViewModel)
            }
            composable(MainTabRoutes.Servicios) {
                ServiciosMenuScreen(
                    onMenuOption = onMenuOption,
                    viewModel = dashboardViewModel
                )
            }
        }
    }
}
