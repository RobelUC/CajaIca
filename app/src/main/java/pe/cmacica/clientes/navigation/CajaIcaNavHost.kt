package pe.cmacica.clientes.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import pe.cmacica.clientes.R
import pe.cmacica.clientes.ui.screens.AhorrosScreen
import pe.cmacica.clientes.ui.screens.ConfirmacionExpedienteScreen
import pe.cmacica.clientes.ui.screens.LoginScreen
import pe.cmacica.clientes.ui.screens.MenuIds
import pe.cmacica.clientes.ui.screens.MisSolicitudesScreen
import pe.cmacica.clientes.ui.screens.ModulePlaceholderScreen
import pe.cmacica.clientes.ui.screens.NuevaSolicitudScreen
import pe.cmacica.clientes.ui.screens.PagoServiciosScreen
import pe.cmacica.clientes.ui.screens.ProfileScreen
import pe.cmacica.clientes.ui.screens.RegisterScreen
import pe.cmacica.clientes.ui.screens.SolicitudDetalleScreen
import pe.cmacica.clientes.ui.screens.TransferenciasScreen
import pe.cmacica.clientes.ui.viewmodel.DashboardViewModel

object Routes {
    const val Login = "login"
    const val Register = "register"
    const val Main = "main"
    const val NuevaSolicitud = "nueva_solicitud"
    const val MisSolicitudes = "mis_solicitudes"
    const val PagoServicios = "pago_servicios"
    const val Transferencias = "transferencias"
    const val Ahorros = "ahorros"
    const val Perfil = "perfil"
    const val Placeholder = "placeholder/{moduleId}"
    const val ConfirmacionExpediente = "confirmacion_expediente/{expediente}/{solicitudId}"
    const val SolicitudDetalle = "solicitud_detalle/{solicitudId}"

    fun confirmacionExpediente(expediente: String, solicitudId: String) =
        "confirmacion_expediente/$expediente/$solicitudId"

    fun solicitudDetalle(solicitudId: String) = "solicitud_detalle/$solicitudId"

    fun placeholder(moduleId: String) = "placeholder/$moduleId"
}

@Composable
fun CajaIcaNavHost(navController: NavHostController) {
    val startDestination = remember {
        if (FirebaseAuth.getInstance().currentUser != null) Routes.Main else Routes.Login
    }
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.Login) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.Main) {
                        popUpTo(Routes.Login) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onRegister = {
                    navController.navigate(Routes.Register)
                }
            )
        }
        composable(Routes.Register) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.Main) {
                        popUpTo(Routes.Login) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.Main) {
            MainShell(
                rootNavController = navController,
                onOpenProfile = { navController.navigate(Routes.Perfil) },
                onMenuOption = { menuId ->
                    when (menuId) {
                        MenuIds.SOLICITUD_CREDITO -> navController.navigate(Routes.NuevaSolicitud)
                        MenuIds.MIS_SOLICITUDES -> navController.navigate(Routes.MisSolicitudes)
                        MenuIds.PAGO_SERVICIOS -> navController.navigate(Routes.PagoServicios)
                        MenuIds.TRANSFERENCIAS -> navController.navigate(Routes.Transferencias)
                        MenuIds.AHORROS -> navController.navigate(Routes.Ahorros)
                        else -> navController.navigate(Routes.placeholder(menuId))
                    }
                }
            )
        }
        composable(Routes.MisSolicitudes) { entry ->
            val dashboardViewModel = sharedDashboardViewModel(entry, navController)
            MisSolicitudesScreen(
                onBack = { navController.popBackStack() },
                onVerSolicitud = { solicitudId ->
                    navController.navigate(Routes.solicitudDetalle(solicitudId))
                },
                viewModel = dashboardViewModel
            )
        }
        composable(Routes.PagoServicios) {
            PagoServiciosScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.Transferencias) {
            TransferenciasScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.Ahorros) {
            AhorrosScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.Perfil) { entry ->
            val dashboardViewModel = sharedDashboardViewModel(entry, navController)
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Routes.Login) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onMisSolicitudes = {
                    navController.navigate(Routes.MisSolicitudes) {
                        popUpTo(Routes.Main) { inclusive = false }
                    }
                },
                onNuevaSolicitud = {
                    navController.navigate(Routes.NuevaSolicitud) {
                        popUpTo(Routes.Main) { inclusive = false }
                    }
                },
                viewModel = dashboardViewModel
            )
        }
        composable(
            route = Routes.Placeholder,
            arguments = listOf(navArgument("moduleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val moduleId = backStackEntry.arguments?.getString("moduleId").orEmpty()
            PlaceholderContent(
                moduleId = moduleId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.NuevaSolicitud) {
            NuevaSolicitudScreen(
                onBack = { navController.popBackStack() },
                onSolicitudRegistrada = { solicitudId, expediente ->
                    navController.navigate(Routes.confirmacionExpediente(expediente, solicitudId)) {
                        popUpTo(Routes.Main) { inclusive = false }
                    }
                }
            )
        }
        composable(
            route = Routes.ConfirmacionExpediente,
            arguments = listOf(
                navArgument("expediente") { type = NavType.StringType },
                navArgument("solicitudId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val expediente = backStackEntry.arguments?.getString("expediente").orEmpty()
            val solicitudId = backStackEntry.arguments?.getString("solicitudId").orEmpty()
            ConfirmacionExpedienteScreen(
                expediente = expediente,
                onGoHome = {
                    navController.navigate(Routes.Main) {
                        popUpTo(Routes.Main) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onVerDetalle = {
                    navController.navigate(Routes.solicitudDetalle(solicitudId)) {
                        popUpTo(Routes.Main) { inclusive = false }
                    }
                }
            )
        }
        composable(
            route = Routes.SolicitudDetalle,
            arguments = listOf(
                navArgument("solicitudId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val solicitudId = backStackEntry.arguments?.getString("solicitudId").orEmpty()
            SolicitudDetalleScreen(
                solicitudId = solicitudId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun sharedDashboardViewModel(
    entry: NavBackStackEntry,
    navController: NavHostController
): DashboardViewModel {
    val mainEntry = remember(entry) {
        navController.getBackStackEntry(Routes.Main)
    }
    return viewModel(mainEntry)
}

@Composable
private fun PlaceholderContent(moduleId: String, onBack: () -> Unit) {
    val (title, description) = when (moduleId) {
        MenuIds.PRESTAMOS -> stringResource(R.string.menu_loans) to
            stringResource(R.string.module_loans_desc)
        else -> stringResource(R.string.home_menu_title) to
            stringResource(R.string.module_coming_soon_desc)
    }
    ModulePlaceholderScreen(
        title = title,
        description = description,
        onBack = onBack
    )
}
