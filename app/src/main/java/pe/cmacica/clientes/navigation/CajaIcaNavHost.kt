package pe.cmacica.clientes.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import pe.cmacica.clientes.ui.screens.DashboardScreen
import pe.cmacica.clientes.ui.screens.LoginScreen
import pe.cmacica.clientes.ui.screens.RegisterScreen

object Routes {
    const val Login = "login"
    const val Register = "register"
    const val Dashboard = "dashboard"
}

@Composable
fun CajaIcaNavHost(navController: NavHostController) {
    val startDestination = remember {
        if (FirebaseAuth.getInstance().currentUser != null) Routes.Dashboard else Routes.Login
    }
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.Login) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.Dashboard) {
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
                    navController.navigate(Routes.Dashboard) {
                        popUpTo(Routes.Login) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.Dashboard) {
            DashboardScreen(
                onLogout = {
                    navController.navigate(Routes.Login) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
