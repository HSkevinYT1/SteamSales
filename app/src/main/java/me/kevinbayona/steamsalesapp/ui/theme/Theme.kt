package me.kevinbayona.steamsalesapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val colores = darkColorScheme(
    primary = Azul,
    secondary = Verde,
    tertiary = Naranja,
    background = Fondo,
    surface = Tarjeta
)

@Composable
fun SteamSalesAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = colores,
        content = content
    )
}
