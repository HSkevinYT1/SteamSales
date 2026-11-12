package me.kevinbayona.steamsalesapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import me.kevinbayona.steamsalesapp.ui.theme.Fondo
import me.kevinbayona.steamsalesapp.ui.theme.SteamSalesAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SteamSalesAppTheme {
                App()
            }
        }
    }
}

@Composable
fun App() {
    val vm: SteamViewModel = viewModel()

    var pestana by remember { mutableStateOf("Inicio") }
    var juegoAbierto by remember { mutableStateOf<Juego?>(null) }
    var mostrarLogin by remember { mutableStateOf(false) }

    val abrirJuego: (Juego) -> Unit = { juego -> juegoAbierto = juego }

    if (mostrarLogin) {
        PantallaLogin(
            alTerminar = { respuesta ->
                vm.terminarLogin(respuesta)
                mostrarLogin = false
            },
            alCancelar = { mostrarLogin = false }
        )
        return
    }

    val juego = juegoAbierto
    if (juego != null) {
        BackHandler { juegoAbierto = null }
        PantallaDetalle(juego = juego, vm = vm, alVolver = { juegoAbierto = null })
        return
    }

    Scaffold(
        containerColor = Fondo,
        topBar = {
            BarraSuperior(
                titulo = pestana,
                avatar = vm.perfil?.avatar,
                alTocarAvatar = { pestana = "Ajustes" }
            )
        },
        bottomBar = {
            BarraInferior(
                seleccionada = pestana,
                cantidadDeseados = vm.deseados.size,
                alCambiar = { pestana = it }
            )
        }
    ) { espacio ->
        val modifier = Modifier.padding(espacio)

        if (pestana == "Inicio") {
            PantallaInicio(vm = vm, alAbrirJuego = abrirJuego, modifier = modifier)
        } else if (pestana == "Explorar") {
            PantallaExplorar(vm = vm, alAbrirJuego = abrirJuego, modifier = modifier)
        } else if (pestana == "Deseados") {
            PantallaDeseados(vm = vm, alAbrirJuego = abrirJuego, modifier = modifier)
        } else {
            PantallaAjustes(vm = vm, alIniciarSesion = { mostrarLogin = true }, modifier = modifier)
        }
    }
}
