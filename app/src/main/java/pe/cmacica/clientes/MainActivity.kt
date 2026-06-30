package pe.cmacica.clientes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import pe.cmacica.clientes.navigation.CajaIcaNavHost
import pe.cmacica.clientes.ui.theme.CajaIcaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContextHolder.init(this)
        enableEdgeToEdge()
        setContent {
            CajaIcaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    CajaIcaNavHost(navController = navController)
                }
            }
        }
    }
}
