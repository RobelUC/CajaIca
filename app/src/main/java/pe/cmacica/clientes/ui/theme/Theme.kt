package pe.cmacica.clientes.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CajaIcaRed = Color(0xFFE30613)
val CajaIcaGold = Color(0xFFC5A021)
val CajaIcaText = Color(0xFF1C1B1F)
val CajaIcaPositive = Color(0xFF1B5E20)

private val LightColorScheme = lightColorScheme(
    primary = CajaIcaRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    secondary = CajaIcaGold,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF5E6B8),
    onSecondaryContainer = Color(0xFF221A00),
    tertiary = CajaIcaGold,
    onTertiary = Color.White,
    background = Color.White,
    onBackground = CajaIcaText,
    surface = Color.White,
    onSurface = CajaIcaText,
    surfaceVariant = Color(0xFFF4F4F4),
    onSurfaceVariant = Color(0xFF454545),
    outline = Color(0xFFBDBDBD)
)

@Composable
fun CajaIcaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = CajaIcaTypography,
        content = content
    )
}
