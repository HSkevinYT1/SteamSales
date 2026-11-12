package me.kevinbayona.steamsalesapp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import me.kevinbayona.steamsalesapp.ui.theme.Azul
import me.kevinbayona.steamsalesapp.ui.theme.Fondo
import me.kevinbayona.steamsalesapp.ui.theme.JetBrainsMono
import me.kevinbayona.steamsalesapp.ui.theme.TextoBlanco
import me.kevinbayona.steamsalesapp.ui.theme.Verde

@Composable
fun BarraInferior(seleccionada: String, cantidadDeseados: Int, alCambiar: (String) -> Unit) {
    val colores = NavigationBarItemDefaults.colors(
        selectedIconColor = Azul,
        selectedTextColor = Azul,
        unselectedIconColor = TextoBlanco,
        unselectedTextColor = TextoBlanco,
        indicatorColor = Color.Transparent
    )

    NavigationBar(containerColor = Fondo) {
        NavigationBarItem(
            selected = seleccionada == "Inicio",
            onClick = { alCambiar("Inicio") },
            icon = { Icon(Icons.Outlined.LocalFireDepartment, contentDescription = null) },
            label = { Text("Inicio", fontFamily = JetBrainsMono) },
            colors = colores
        )
        NavigationBarItem(
            selected = seleccionada == "Explorar",
            onClick = { alCambiar("Explorar") },
            icon = { Icon(Icons.Outlined.Explore, contentDescription = null) },
            label = { Text("Explorar", fontFamily = JetBrainsMono) },
            colors = colores
        )
        NavigationBarItem(
            selected = seleccionada == "Deseados",
            onClick = { alCambiar("Deseados") },
            icon = {
                BadgedBox(badge = {
                    if (cantidadDeseados > 0) {
                        Badge(containerColor = Verde) { Text("$cantidadDeseados") }
                    }
                }) {
                    Icon(Icons.Outlined.FavoriteBorder, contentDescription = null)
                }
            },
            label = { Text("Deseados", fontFamily = JetBrainsMono) },
            colors = colores
        )
        NavigationBarItem(
            selected = seleccionada == "Ajustes",
            onClick = { alCambiar("Ajustes") },
            icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
            label = { Text("Ajustes", fontFamily = JetBrainsMono) },
            colors = colores
        )
    }
}
