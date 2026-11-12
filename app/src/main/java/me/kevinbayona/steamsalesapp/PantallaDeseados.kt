package me.kevinbayona.steamsalesapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import me.kevinbayona.steamsalesapp.ui.theme.Boton
import me.kevinbayona.steamsalesapp.ui.theme.Inter
import me.kevinbayona.steamsalesapp.ui.theme.JetBrainsMono
import me.kevinbayona.steamsalesapp.ui.theme.Rosa
import me.kevinbayona.steamsalesapp.ui.theme.SpaceGrotesk
import me.kevinbayona.steamsalesapp.ui.theme.Tarjeta
import me.kevinbayona.steamsalesapp.ui.theme.TextoBlanco
import me.kevinbayona.steamsalesapp.ui.theme.TextoGris
import me.kevinbayona.steamsalesapp.ui.theme.Verde

val ordenesDeseados = listOf("Mayor descuento", "Menor precio", "Nombre")

@Composable
fun PantallaDeseados(
    vm: SteamViewModel,
    alAbrirJuego: (Juego) -> Unit,
    modifier: Modifier = Modifier
) {
    var soloOfertas by remember { mutableStateOf(false) }
    var orden by remember { mutableStateOf("Mayor descuento") }

    val todos = vm.deseados
    val enOferta = todos.filter { it.descuento > 0 }

    var lista = if (soloOfertas) enOferta else todos.toList()

    lista = when (orden) {
        "Menor precio" -> lista.sortedBy { it.precioFinal }
        "Nombre" -> lista.sortedBy { it.nombre }
        else -> lista.sortedByDescending { it.descuento }
    }

    val ahorro = enOferta.sumOf { it.precioNormal - it.precioFinal }

    LazyColumn(modifier = modifier.padding(horizontal = 16.dp)) {

        item {
            Spacer(modifier = Modifier.height(12.dp))

            if (todos.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Pestana("Todos", todos.size, TextoGris, !soloOfertas) { soloOfertas = false }
                    Pestana("En oferta", enOferta.size, Verde, soloOfertas) { soloOfertas = true }
                }
                Spacer(modifier = Modifier.height(12.dp))

                BotonOrden(
                    orden = orden,
                    alTocar = {
                        val siguiente = (ordenesDeseados.indexOf(orden) + 1) % ordenesDeseados.size
                        orden = ordenesDeseados[siguiente]
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (ahorro > 0) {
                Text(
                    text = "Hoy ahorras ${formatearPrecio(ahorro, vm.region.moneda)} en ${enOferta.size} juegos",
                    color = Verde,
                    fontFamily = Inter,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (todos.isEmpty()) {
            item {
                val texto = if (vm.perfil == null) {
                    "Toca ♥ en un juego para seguir su precio, o inicia sesión en Ajustes para traer tu lista de Steam."
                } else {
                    "Toca ♥ en un juego para seguir su precio, o sincroniza tu lista de Steam."
                }
                Mensaje(texto = texto)
                Spacer(modifier = Modifier.height(12.dp))
            }
        } else if (lista.isEmpty()) {
            item { Mensaje(texto = "Ninguno de tus deseados está en oferta") }
        }

        items(lista) { juego ->
            Box(modifier = Modifier.clickable { alAbrirJuego(juego) }) {
                TarjetaDeseadoLista(juego = juego, alQuitar = { vm.alternarDeseado(juego) })
            }
        }

        if (todos.isNotEmpty() || vm.perfil != null) {
            item {
                BotonSincronizar(
                    conSesion = vm.perfil != null,
                    sincronizando = vm.sincronizando,
                    ultima = vm.ultimaSincronizacion,
                    mensaje = vm.mensajeSincronizacion,
                    alTocar = { vm.sincronizarDeseados() }
                )
            }
        }
    }
}

@Composable
fun Pestana(texto: String, numero: Int, colorNumero: Color, activa: Boolean, alTocar: () -> Unit) {
    val colorFondo = if (activa) Boton else Color.Transparent

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(colorFondo)
            .clickable { alTocar() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(texto, color = TextoBlanco, fontFamily = JetBrainsMono, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text("$numero", color = colorNumero, fontFamily = JetBrainsMono, fontSize = 14.sp)
    }
}

@Composable
fun TarjetaDeseadoLista(juego: Juego, alQuitar: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(bottom = 10.dp)
            .fillMaxWidth()
            .background(Tarjeta, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        AsyncImage(
            model = juego.imagen,
            contentDescription = juego.nombre,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(110.dp)
                .height(64.dp)
                .clip(RoundedCornerShape(6.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row {
                Text(
                    text = juego.nombre,
                    color = TextoBlanco,
                    fontFamily = SpaceGrotesk,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = "Quitar de deseados",
                    tint = Rosa,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { alQuitar() }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (juego.descuento > 0) {
                    EtiquetaDescuento(juego.descuento)
                    Spacer(modifier = Modifier.width(8.dp))
                    PrecioTachado(formatearPrecio(juego.precioNormal, juego.moneda))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = formatearPrecio(juego.precioFinal, juego.moneda),
                    color = if (juego.descuento > 0) Verde else TextoBlanco,
                    fontFamily = SpaceGrotesk,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun BotonSincronizar(
    conSesion: Boolean,
    sincronizando: Boolean,
    ultima: String,
    mensaje: String,
    alTocar: () -> Unit
) {
    val texto = if (sincronizando) {
        "Sincronizando..."
    } else if (conSesion) {
        "Sincronizar con Steam"
    } else {
        "Actualizar precios"
    }

    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Boton)
                .clickable { if (!sincronizando) alTocar() }
                .padding(14.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Filled.Refresh, contentDescription = null, tint = TextoBlanco, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(texto, color = TextoBlanco, fontFamily = JetBrainsMono, fontSize = 14.sp)
            Spacer(modifier = Modifier.weight(1f))
        }

        val detalle = if (mensaje != "") mensaje else if (ultima != "") "Actualizado a las $ultima" else ""
        if (detalle != "") {
            Spacer(modifier = Modifier.height(8.dp))
            Text(detalle, color = TextoGris, fontFamily = Inter, fontSize = 12.sp)
        }
    }
}
