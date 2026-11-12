package me.kevinbayona.steamsalesapp

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import me.kevinbayona.steamsalesapp.datos.DatosJuego
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun precioConDescuento(precioNormal: Int, descuento: Int): Int {
    return precioNormal * (100 - descuento) / 100
}

@Composable
fun PantallaDetalle(juego: Juego, vm: SteamViewModel, alVolver: () -> Unit) {
    LaunchedEffect(juego.id) {
        vm.cargarDetalle(juego.id)
    }

    var periodo by remember { mutableStateOf("1A") }
    LaunchedEffect(juego.id, periodo) {
        vm.cargarHistorial(juego.id, periodo)
    }

    val datos = if (vm.detalle?.id == juego.id) vm.detalle else null

    val actual = if (datos != null) {
        juegoDesdeDetalle(datos).copy(terminaEn = juego.terminaEn)
    } else {
        juego
    }

    val contexto = LocalContext.current
    val navegador = LocalUriHandler.current

    val compartir = {
        val texto = "${actual.nombre} a ${formatearPrecio(actual.precioFinal, actual.moneda)} en Steam: ${urlSteam(actual.id)}"
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, texto)
        contexto.startActivity(Intent.createChooser(intent, null))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        BarraDetalle(
            esDeseado = vm.esDeseado(actual.id),
            alVolver = alVolver,
            alCompartir = compartir,
            alTocarCorazon = { vm.alternarDeseado(actual) }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            AsyncImage(
                model = actual.imagen,
                contentDescription = actual.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(460f / 215f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Tarjeta)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(actual.nombre, color = TextoBlanco, fontFamily = SpaceGrotesk, fontSize = 24.sp)
            if (datos != null) {
                val generos = (datos.genres.map { it.description } + datos.developers.take(1)).joinToString(" · ")
                Text(generos, color = TextoGris, fontFamily = Inter, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))
            BloquePrecio(juego = actual, mayorDescuento = vm.mayorDescuento)

            if (datos != null && datos.capturas.isNotEmpty()) {
                TituloSeccionDetalle("Imágenes")
                LazyRow {
                    items(datos.capturas) { captura ->
                        AsyncImage(
                            model = captura.pequena,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .width(240.dp)
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Tarjeta)
                        )
                    }
                }
            }

            if (actual.precioNormal > 0) {
                SeccionHistorial(
                    juego = actual,
                    vm = vm,
                    periodo = periodo,
                    alCambiarPeriodo = { periodo = it }
                )
            }

            if (vm.cargandoDetalle) {
                Cargando()
            } else if (vm.errorDetalle != "") {
                Spacer(modifier = Modifier.height(16.dp))
                Mensaje(texto = vm.errorDetalle, alReintentar = { vm.cargarDetalle(juego.id) })
            } else if (datos != null) {
                SeccionAcerca(datos = datos)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        BotonSteam(alTocar = { navegador.openUri(urlSteam(actual.id)) })
    }
}

@Composable
fun BarraDetalle(
    esDeseado: Boolean,
    alVolver: () -> Unit,
    alCompartir: () -> Unit,
    alTocarCorazon: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        BotonIcono(icono = Icons.AutoMirrored.Filled.ArrowBack, color = TextoBlanco, alTocar = alVolver)
        Spacer(modifier = Modifier.weight(1f))
        BotonIcono(icono = Icons.Filled.Share, color = TextoBlanco, alTocar = alCompartir)
        Spacer(modifier = Modifier.width(8.dp))
        if (esDeseado) {
            BotonIcono(icono = Icons.Filled.Favorite, color = Rosa, alTocar = alTocarCorazon)
        } else {
            BotonIcono(icono = Icons.Outlined.FavoriteBorder, color = TextoBlanco, alTocar = alTocarCorazon)
        }
    }
}

@Composable
fun BotonIcono(icono: ImageVector, color: Color, alTocar: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Boton)
            .clickable { alTocar() }
    ) {
        Icon(icono, contentDescription = null, tint = color)
    }
}

@Composable
fun TituloSeccionDetalle(texto: String) {
    Text(
        text = texto,
        color = TextoBlanco,
        fontFamily = SpaceGrotesk,
        fontSize = 18.sp,
        modifier = Modifier.padding(top = 28.dp, bottom = 12.dp)
    )
}

@Composable
fun BloquePrecio(juego: Juego, mayorDescuento: Int?) {
    if (juego.precioFinal == 0) {
        Text("Gratis", color = Azul, fontFamily = SpaceGrotesk, fontSize = 30.sp)
        return
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = formatearPrecio(juego.precioFinal, juego.moneda),
            color = if (juego.descuento > 0) Verde else TextoBlanco,
            fontFamily = SpaceGrotesk,
            fontSize = 32.sp
        )
        if (juego.descuento > 0) {
            Spacer(modifier = Modifier.width(12.dp))
            EtiquetaDescuento(juego.descuento)
        }
    }

    if (juego.descuento > 0) {
        Text(
            text = "Precio normal ${formatearPrecio(juego.precioNormal, juego.moneda)}",
            color = TextoGris,
            fontFamily = Inter,
            fontSize = 14.sp,
            textDecoration = TextDecoration.LineThrough
        )
    }

    if (mayorDescuento != null && juego.descuento > 0 && juego.descuento >= mayorDescuento) {
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Verde.copy(alpha = 0.15f), RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = Verde, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Precio más bajo de su historia", color = Verde, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
    }

    val restante = tiempoRestante(juego.terminaEn)
    if (restante != "") {
        Spacer(modifier = Modifier.height(8.dp))
        Text(restante, color = TextoGris, fontFamily = Inter, fontSize = 13.sp)
    }
}

@Composable
fun SeccionHistorial(juego: Juego, vm: SteamViewModel, periodo: String, alCambiarPeriodo: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(1f)) {
            TituloSeccionDetalle("Historial de precio")
        }
        Row(modifier = Modifier.padding(top = 16.dp)) {
            for (opcion in listOf("6M", "1A", "Todo")) {
                val elegido = periodo == opcion
                Text(
                    text = opcion,
                    color = if (elegido) Fondo else TextoGris,
                    fontFamily = JetBrainsMono,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (elegido) Azul else Color.Transparent)
                        .clickable { alCambiarPeriodo(opcion) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }

    if (vm.cargandoHistorial) {
        Cargando()
        return
    }
    if (vm.errorHistorial != "") {
        Text(vm.errorHistorial, color = TextoGris, fontFamily = Inter, fontSize = 13.sp)
        return
    }

    val mayor = maxOf(vm.mayorDescuento ?: 0, juego.descuento)
    val precioMasBajo = precioConDescuento(juego.precioNormal, mayor)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Tarjeta, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        GraficoPrecio(
            historial = vm.historial,
            desde = vm.historialDesde,
            descuentoHoy = juego.descuento,
            mayorDescuento = mayor
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DatoPrecio(
                titulo = "Hoy",
                valor = formatearPrecio(juego.precioFinal, juego.moneda),
                color = if (juego.descuento > 0) Verde else TextoBlanco,
                modifier = Modifier.weight(1f)
            )
            DatoPrecio(
                titulo = "Más bajo",
                valor = formatearPrecio(precioMasBajo, juego.moneda),
                color = Verde,
                modifier = Modifier.weight(1f)
            )
            DatoPrecio(
                titulo = "Normal",
                valor = formatearPrecio(juego.precioNormal, juego.moneda),
                color = TextoBlanco,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun DatoPrecio(titulo: String, valor: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Fondo, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(titulo, color = TextoGris, fontFamily = Inter, fontSize = 12.sp)
        Text(
            text = valor,
            color = color,
            fontFamily = SpaceGrotesk,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun GraficoPrecio(
    historial: List<PuntoPrecio>,
    desde: Long,
    descuentoHoy: Int,
    mayorDescuento: Int
) {
    val ahora = System.currentTimeMillis() / 1000

    val puntos = mutableListOf<PuntoPrecio>()
    if (historial.isEmpty()) {
        puntos.add(PuntoPrecio(fecha = desde, descuento = descuentoHoy))
    } else {
        puntos.addAll(historial)
    }
    puntos.add(PuntoPrecio(fecha = ahora, descuento = descuentoHoy))
    val inicio = puntos.first().fecha

    val fondo = maxOf(mayorDescuento, puntos.maxOf { it.descuento }, 1)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        fun x(fecha: Long): Float {
            val total = maxOf(ahora - inicio, 1L)
            return (fecha - inicio).toFloat() / total * size.width
        }
        fun y(descuento: Int): Float {
            return size.height * 0.1f + (descuento.toFloat() / fondo) * size.height * 0.8f
        }

        val linea = Path()
        linea.moveTo(x(puntos[0].fecha), y(puntos[0].descuento))
        for (i in 1 until puntos.size) {
            linea.lineTo(x(puntos[i].fecha), y(puntos[i - 1].descuento))
            linea.lineTo(x(puntos[i].fecha), y(puntos[i].descuento))
        }

        val relleno = Path()
        relleno.addPath(linea)
        relleno.lineTo(size.width, size.height)
        relleno.lineTo(x(puntos[0].fecha), size.height)
        relleno.close()
        drawPath(relleno, Brush.verticalGradient(listOf(Azul.copy(alpha = 0.25f), Color.Transparent)))

        drawPath(linea, color = Azul, style = Stroke(width = 3.dp.toPx()))

        drawLine(
            color = Verde,
            start = Offset(0f, y(mayorDescuento)),
            end = Offset(size.width, y(mayorDescuento)),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
        )

        drawCircle(color = Verde, radius = 6.dp.toPx(), center = Offset(size.width, y(descuentoHoy)))
    }

    Spacer(modifier = Modifier.height(6.dp))

    Row {
        val formatoFecha = SimpleDateFormat("MMM yyyy", Locale.forLanguageTag("es"))
        Text(
            text = formatoFecha.format(Date(inicio * 1000)),
            color = TextoGris,
            fontFamily = Inter,
            fontSize = 11.sp,
            modifier = Modifier.weight(1f)
        )
        Text("Hoy", color = TextoGris, fontFamily = Inter, fontSize = 11.sp)
    }
}

@Composable
fun SeccionAcerca(datos: DatosJuego) {
    TituloSeccionDetalle("Acerca del juego")
    Text(datos.descripcion, color = TextoGris, fontFamily = Inter, fontSize = 14.sp)
    Spacer(modifier = Modifier.height(12.dp))
    Dato(titulo = "Lanzamiento", valor = datos.lanzamiento?.date ?: "-")
    Dato(titulo = "Desarrollador", valor = datos.developers.firstOrNull() ?: "-")
    if (datos.metacritic != null) {
        Dato(titulo = "Metacritic", valor = "${datos.metacritic.score}")
    }
}

@Composable
fun Dato(titulo: String, valor: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(titulo, color = TextoGris, fontFamily = Inter, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(valor, color = TextoBlanco, fontFamily = Inter, fontSize = 13.sp)
    }
}

@Composable
fun BotonSteam(alTocar: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(Fondo)
            .padding(16.dp)
            .height(52.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Azul)
            .clickable { alTocar() }
            .padding(horizontal = 18.dp)
    ) {
        Text(
            text = "Ver en Steam Store",
            color = Fondo,
            fontFamily = Inter,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.Filled.NorthEast, contentDescription = null, tint = Fondo)
    }
}
