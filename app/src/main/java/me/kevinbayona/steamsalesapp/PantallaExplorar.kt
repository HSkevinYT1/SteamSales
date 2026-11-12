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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import me.kevinbayona.steamsalesapp.ui.theme.Azul
import me.kevinbayona.steamsalesapp.ui.theme.Boton
import me.kevinbayona.steamsalesapp.ui.theme.Fondo
import me.kevinbayona.steamsalesapp.ui.theme.Inter
import me.kevinbayona.steamsalesapp.ui.theme.JetBrainsMono
import me.kevinbayona.steamsalesapp.ui.theme.Rosa
import me.kevinbayona.steamsalesapp.ui.theme.SpaceGrotesk
import me.kevinbayona.steamsalesapp.ui.theme.Tarjeta
import me.kevinbayona.steamsalesapp.ui.theme.TextoBlanco
import me.kevinbayona.steamsalesapp.ui.theme.TextoGris
import me.kevinbayona.steamsalesapp.ui.theme.Verde

@Composable
fun PantallaExplorar(
    vm: SteamViewModel,
    alAbrirJuego: (Juego) -> Unit,
    modifier: Modifier = Modifier
) {
    val enMinimos = vm.filtro == "Mínimo histórico"

    LazyColumn(modifier = modifier.padding(horizontal = 16.dp)) {

        item {
            Spacer(modifier = Modifier.height(12.dp))

            LazyRow {
                items(filtros) { opcion ->
                    BotonFiltro(texto = opcion, activo = vm.filtro == opcion, alTocar = { vm.elegirFiltro(opcion) })
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (enMinimos) {
                Text(
                    text = "Juegos que hoy están en el precio más bajo de su historia en Steam.",
                    color = TextoGris,
                    fontFamily = Inter,
                    fontSize = 13.sp
                )
            } else {
                Buscador(texto = vm.textoBusqueda, alCambiar = { vm.cambiarTexto(it) })
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f)) {
                        BotonOrden(orden = vm.orden, alTocar = { vm.siguienteOrden() })
                    }
                    if (!vm.cargandoBusqueda && vm.errorBusqueda == "" && vm.totalResultados > 0) {
                        ContadorResultados(cantidad = vm.totalResultados)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (vm.cargandoBusqueda) {
            item { Cargando() }
        } else if (vm.errorBusqueda != "") {
            item { Mensaje(texto = vm.errorBusqueda, alReintentar = { vm.buscar() }) }
        } else if (vm.resultados.isEmpty()) {
            item { Mensaje(texto = "No hay juegos que coincidan") }
        } else {
            items(vm.resultados) { juego ->
                Box(modifier = Modifier.clickable { alAbrirJuego(juego) }) {
                    TarjetaResultado(
                        juego = juego,
                        esDeseado = vm.esDeseado(juego.id),
                        alTocarCorazon = { vm.alternarDeseado(juego) }
                    )
                }
            }

            if (vm.hayMas) {
                item {
                    LaunchedEffect(vm.resultados.size) {
                        vm.cargarMas()
                    }
                    Cargando()
                }
            }
        }
    }
}

@Composable
fun Buscador(texto: String, alCambiar: (String) -> Unit) {
    val foco = LocalFocusManager.current

    TextField(
        value = texto,
        onValueChange = alCambiar,
        singleLine = true,
        placeholder = { Text("Buscar en Steam...") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (texto != "") {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Borrar",
                    modifier = Modifier.clickable { alCambiar("") }
                )
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { foco.clearFocus() }),
        shape = RoundedCornerShape(10.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Tarjeta,
            unfocusedContainerColor = Tarjeta,
            focusedTextColor = TextoBlanco,
            unfocusedTextColor = TextoBlanco,
            focusedLeadingIconColor = TextoGris,
            unfocusedLeadingIconColor = TextoGris,
            focusedTrailingIconColor = TextoGris,
            unfocusedTrailingIconColor = TextoGris,
            focusedPlaceholderColor = TextoGris,
            unfocusedPlaceholderColor = TextoGris,
            cursorColor = Azul,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun BotonFiltro(texto: String, activo: Boolean, alTocar: () -> Unit) {
    val colorFondo = if (activo) Verde else Boton
    val colorTexto = if (activo) Fondo else TextoBlanco

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(end = 8.dp)
            .clip(RoundedCornerShape(50))
            .background(colorFondo)
            .clickable { alTocar() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        if (activo) {
            Icon(
                Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = colorTexto,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(texto, color = colorTexto, fontFamily = JetBrainsMono, fontSize = 13.sp)
    }
}

@Composable
fun BotonOrden(orden: String, alTocar: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Ordenar:", color = TextoGris, fontFamily = JetBrainsMono, fontSize = 12.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Boton, RoundedCornerShape(6.dp))
                .clickable { alTocar() }
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(orden, color = Azul, fontFamily = JetBrainsMono, fontSize = 12.sp)
            Icon(Icons.Filled.UnfoldMore, contentDescription = null, tint = Azul, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun ContadorResultados(cantidad: Int) {
    val conPuntos = "%,d".format(cantidad).replace(",", ".")
    Text("$conPuntos juegos", color = TextoGris, fontFamily = JetBrainsMono, fontSize = 12.sp)
}

@Composable
fun TarjetaResultado(juego: Juego, esDeseado: Boolean, alTocarCorazon: () -> Unit) {
    Row(
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

        Spacer(modifier = Modifier.width(10.dp))

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
                Box(modifier = Modifier.clickable { alTocarCorazon() }) {
                    if (esDeseado) {
                        Icon(Icons.Filled.Favorite, contentDescription = "Quitar de deseados", tint = Rosa)
                    } else {
                        Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Agregar a deseados", tint = TextoGris)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = juego.nota,
                    color = if (juego.nota == "Mínimo histórico") Verde else TextoGris,
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (juego.descuento > 0) {
                    EtiquetaDescuento(juego.descuento)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (juego.precioFinal == 0) "—" else formatearPrecio(juego.precioFinal, juego.moneda),
                    color = if (juego.descuento > 0) Verde else TextoBlanco,
                    fontFamily = SpaceGrotesk,
                    fontSize = 16.sp
                )
            }
        }
    }
}
