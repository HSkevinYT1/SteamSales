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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import me.kevinbayona.steamsalesapp.ui.theme.Inter
import me.kevinbayona.steamsalesapp.ui.theme.JetBrainsMono
import me.kevinbayona.steamsalesapp.ui.theme.Rosa
import me.kevinbayona.steamsalesapp.ui.theme.SpaceGrotesk
import me.kevinbayona.steamsalesapp.ui.theme.Tarjeta
import me.kevinbayona.steamsalesapp.ui.theme.TextoBlanco
import me.kevinbayona.steamsalesapp.ui.theme.TextoGris
import me.kevinbayona.steamsalesapp.ui.theme.Verde

@Composable
fun PantallaInicio(
    vm: SteamViewModel,
    alAbrirJuego: (Juego) -> Unit,
    modifier: Modifier = Modifier
) {
    val alertas = vm.deseados.filter { it.descuento > 0 }

    LazyColumn(modifier = modifier.padding(horizontal = 16.dp)) {
        if (vm.cargandoOfertas) {
            item { Cargando() }
        } else if (vm.errorOfertas != "") {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Mensaje(texto = vm.errorOfertas, alReintentar = { vm.cargarOfertas() })
            }
        } else if (vm.ofertas.isNotEmpty()) {

            val destacada = vm.ofertas.first()
            val resto = vm.ofertas.drop(1)

            item {
                Titulo(texto = "Oferta destacada", extra = "Mayor descuento")
                Box(modifier = Modifier.clickable { alAbrirJuego(destacada) }) {
                    TarjetaDestacada(
                        juego = destacada,
                        esDeseado = vm.esDeseado(destacada.id),
                        alTocarCorazon = { vm.alternarDeseado(destacada) }
                    )
                }
            }

            if (alertas.isNotEmpty()) {
                item {
                    Titulo(texto = "Tus deseados en oferta", extra = "${alertas.size}")
                    LazyRow {
                        items(alertas) { juego ->
                            Box(modifier = Modifier.clickable { alAbrirJuego(juego) }) {
                                TarjetaDeseado(juego = juego)
                            }
                        }
                    }
                }
            }

            item {
                Titulo(texto = "Supergangas", extra = "${resto.size} ofertas")
            }

            items(resto) { juego ->
                Box(modifier = Modifier.clickable { alAbrirJuego(juego) }) {
                    FilaGanga(juego = juego)
                }
            }
        }
    }
}

@Composable
fun Titulo(texto: String, extra: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 28.dp, bottom = 12.dp)
    ) {
        Text(
            text = texto,
            color = TextoBlanco,
            fontFamily = SpaceGrotesk,
            fontSize = 20.sp,
            modifier = Modifier.weight(1f)
        )
        Text(text = extra, color = TextoGris, fontFamily = JetBrainsMono, fontSize = 12.sp)
    }
}

@Composable
fun TarjetaDestacada(juego: Juego, esDeseado: Boolean, alTocarCorazon: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Tarjeta)
    ) {
        AsyncImage(
            model = juego.imagen,
            contentDescription = juego.nombre,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(200.dp)
        )

        Column(modifier = Modifier.padding(14.dp)) {
            Row {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = juego.nombre,
                        color = TextoBlanco,
                        fontFamily = SpaceGrotesk,
                        fontSize = 22.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        EtiquetaDescuento(juego.descuento)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatearPrecio(juego.precioFinal, juego.moneda),
                            color = Verde,
                            fontFamily = SpaceGrotesk,
                            fontSize = 22.sp
                        )
                    }
                    PrecioTachado(formatearPrecio(juego.precioNormal, juego.moneda))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = tiempoRestante(juego.terminaEn),
                    color = TextoGris,
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                Box(modifier = Modifier.clickable { alTocarCorazon() }) {
                    if (esDeseado) {
                        Icon(Icons.Filled.Favorite, contentDescription = "Quitar de deseados", tint = Rosa)
                    } else {
                        Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Agregar a deseados", tint = TextoBlanco)
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaDeseado(juego: Juego) {
    Column(
        modifier = Modifier
            .padding(end = 12.dp)
            .width(260.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Tarjeta)
    ) {
        AsyncImage(
            model = juego.imagen,
            contentDescription = juego.nombre,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(120.dp)
        )
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = juego.nombre,
                color = TextoBlanco,
                fontFamily = SpaceGrotesk,
                fontSize = 17.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text("-${juego.descuento}% ahora", color = Verde, fontFamily = JetBrainsMono, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1f)) {
                    PrecioTachado(formatearPrecio(juego.precioNormal, juego.moneda))
                }
                Text(
                    text = formatearPrecio(juego.precioFinal, juego.moneda),
                    color = TextoBlanco,
                    fontFamily = SpaceGrotesk,
                    fontSize = 17.sp
                )
            }
        }
    }
}

@Composable
fun FilaGanga(juego: Juego) {
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
                .width(96.dp)
                .height(56.dp)
                .clip(RoundedCornerShape(6.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = juego.nombre,
                color = TextoBlanco,
                fontFamily = SpaceGrotesk,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            EtiquetaDescuento(juego.descuento)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatearPrecio(juego.precioFinal, juego.moneda),
                color = Verde,
                fontFamily = SpaceGrotesk,
                fontSize = 16.sp
            )
            PrecioTachado(formatearPrecio(juego.precioNormal, juego.moneda))
        }
    }
}
