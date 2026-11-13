package me.kevinbayona.steamsalesapp

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
fun PantallaAjustes(
    vm: SteamViewModel,
    alIniciarSesion: () -> Unit,
    alCambiarNotificaciones: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        TituloSeccion(texto = "CUENTA")
        TarjetaCuenta(vm = vm, alIniciarSesion = alIniciarSesion)

        TituloSeccion(texto = "REGIÓN Y MONEDA")
        SelectorRegion(regionActual = vm.region, alElegir = { vm.cambiarRegion(it) })
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Cambia los precios de toda la app a la tienda de ese país.",
            color = TextoGris,
            fontFamily = Inter,
            fontSize = 12.sp
        )

        TituloSeccion(texto = "NOTIFICACIONES")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Tarjeta)
                .clickable { alCambiarNotificaciones(!vm.notificaciones) }
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Avisarme de ofertas y mínimos históricos",
                color = TextoBlanco,
                fontFamily = Inter,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = vm.notificaciones,
                onCheckedChange = { alCambiarNotificaciones(it) },
                colors = SwitchDefaults.colors(checkedTrackColor = Azul, checkedThumbColor = Fondo)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Revisamos tu lista de deseados cada 6 horas y te avisamos cuando un juego sale en oferta o llega a su precio más bajo.",
            color = TextoGris,
            fontFamily = Inter,
            fontSize = 12.sp
        )

        if (BuildConfig.DEBUG) {
            val contexto = LocalContext.current
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Probar notificaciones ahora (debug)",
                color = Azul,
                fontFamily = Inter,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Boton)
                    .clickable {
                        if (!vm.notificaciones) alCambiarNotificaciones(true)
                        probarRevision(contexto)
                        Toast.makeText(contexto, "Revisando ofertas…", Toast.LENGTH_SHORT).show()
                    }
                    .padding(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Precios: Steam · Historial: IsThereAnyDeal",
            color = TextoGris,
            fontFamily = JetBrainsMono,
            fontSize = 11.sp
        )
    }
}

@Composable
fun TituloSeccion(texto: String) {
    Text(
        text = texto,
        color = TextoGris,
        fontFamily = JetBrainsMono,
        fontSize = 11.sp,
        modifier = Modifier.padding(top = 16.dp, bottom = 10.dp)
    )
}

@Composable
fun TarjetaCuenta(vm: SteamViewModel, alIniciarSesion: () -> Unit) {
    val usuario = vm.perfil

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Tarjeta, RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        if (vm.cargandoLogin) {
            Cargando()
        } else if (usuario == null) {
            SinSesion(error = vm.errorLogin, alIniciarSesion = alIniciarSesion)
        } else {
            ConSesion(usuario = usuario, alCerrarSesion = { vm.cerrarSesion() })
        }
    }
}

@Composable
fun SinSesion(error: String, alIniciarSesion: () -> Unit) {
    Text("Conecta tu cuenta de Steam", color = TextoBlanco, fontFamily = SpaceGrotesk, fontSize = 18.sp)
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "Tu lista de deseados de Steam se agregará a la app.",
        color = TextoGris,
        fontFamily = Inter,
        fontSize = 13.sp
    )

    if (error != "") {
        Spacer(modifier = Modifier.height(8.dp))
        Text(error, color = Rosa, fontFamily = Inter, fontSize = 13.sp)
    }

    Spacer(modifier = Modifier.height(14.dp))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Azul)
            .clickable { alIniciarSesion() }
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, tint = Fondo, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Iniciar sesión con Steam", color = Fondo, fontFamily = Inter, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun ConSesion(usuario: Perfil, alCerrarSesion: () -> Unit) {
    val enLinea = usuario.estado != "offline"

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Boton)
        ) {
            Icon(Icons.Filled.Person, contentDescription = null, tint = TextoGris)
            AsyncImage(
                model = usuario.avatar,
                contentDescription = usuario.nombre,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = usuario.nombre,
                color = TextoBlanco,
                fontFamily = SpaceGrotesk,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row {
                Text("● ", color = if (enLinea) Verde else TextoGris, fontSize = 12.sp)
                Text(
                    text = if (enLinea) "En línea" else "Desconectado",
                    color = TextoGris,
                    fontFamily = Inter,
                    fontSize = 13.sp
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    Text(
        text = "Cerrar sesión",
        color = Rosa,
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Boton)
            .clickable { alCerrarSesion() }
            .padding(12.dp)
    )
}

@Composable
fun SelectorRegion(regionActual: Region, alElegir: (Region) -> Unit) {
    var menuAbierto by remember { mutableStateOf(false) }

    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Tarjeta)
                .clickable { menuAbierto = true }
                .padding(16.dp)
        ) {
            Text(
                text = "${regionActual.nombre} (${regionActual.moneda})",
                color = TextoBlanco,
                fontFamily = Inter,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = TextoGris)
        }

        DropdownMenu(
            expanded = menuAbierto,
            onDismissRequest = { menuAbierto = false }
        ) {
            for (region in regiones) {
                DropdownMenuItem(
                    text = { Text("${region.nombre} (${region.moneda})") },
                    onClick = {
                        alElegir(region)
                        menuAbierto = false
                    }
                )
            }
        }
    }
}
