package me.kevinbayona.steamsalesapp

import android.app.Application
import android.content.Context
import android.net.Uri
import android.webkit.CookieManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.kevinbayona.steamsalesapp.datos.ComunidadApi
import me.kevinbayona.steamsalesapp.datos.DatosJuego
import me.kevinbayona.steamsalesapp.datos.PREFIJO_STEAM_ID
import me.kevinbayona.steamsalesapp.datos.valorXml
import me.kevinbayona.steamsalesapp.datos.ItadApi
import me.kevinbayona.steamsalesapp.datos.PedidoMinimos
import me.kevinbayona.steamsalesapp.datos.TIENDA_STEAM
import me.kevinbayona.steamsalesapp.datos.SteamApi
import me.kevinbayona.steamsalesapp.datos.juegosDesdeHtml
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

data class PuntoPrecio(
    val fecha: Long,
    val descuento: Int
)

fun diasDelPeriodo(periodo: String): Long {
    return when (periodo) {
        "30D" -> 30
        "6M" -> 182
        "1A" -> 365
        else -> 365 * 20
    }
}

@Serializable
data class Perfil(
    val steamId: String,
    val nombre: String,
    val avatar: String,
    val estado: String
)

val filtros = listOf("Todos", "En oferta", "Mínimo histórico", "Gratis")

val ordenes = listOf("Relevancia", "Mejor valorados", "Más recientes", "Menor precio")

fun codigoOrden(orden: String): String? {
    return when (orden) {
        "Mejor valorados" -> "Reviews_DESC"
        "Más recientes" -> "Released_DESC"
        "Menor precio" -> "Price_ASC"
        else -> null
    }
}

data class Region(val codigo: String, val nombre: String, val moneda: String)

val regiones = listOf(
    Region("US", "Estados Unidos", "USD"),
    Region("CO", "Colombia", "COP"),
    Region("MX", "México", "MXN"),
    Region("CL", "Chile", "CLP"),
    Region("PE", "Perú", "PEN"),
    Region("ES", "España", "EUR")
)

class SteamViewModel(application: Application) : AndroidViewModel(application) {

    private val memoria = application.getSharedPreferences("steam_sales", Context.MODE_PRIVATE)

    var region by mutableStateOf(regiones[0])
        private set

    var ofertas by mutableStateOf<List<Juego>>(emptyList())
        private set
    var cargandoOfertas by mutableStateOf(false)
        private set
    var errorOfertas by mutableStateOf("")
        private set

    var textoBusqueda by mutableStateOf("")
        private set
    var filtro by mutableStateOf(filtros[0])
        private set
    var orden by mutableStateOf(ordenes[0])
        private set
    var resultados by mutableStateOf<List<Juego>>(emptyList())
        private set
    var totalResultados by mutableStateOf(0)
        private set
    var hayMas by mutableStateOf(false)
        private set
    var cargandoBusqueda by mutableStateOf(false)
        private set
    var cargandoMas by mutableStateOf(false)
        private set
    var errorBusqueda by mutableStateOf("")
        private set

    private var trabajoBusqueda: Job? = null
    private var siguientePosicion = 0

    var detalle by mutableStateOf<DatosJuego?>(null)
        private set
    var cargandoDetalle by mutableStateOf(false)
        private set
    var errorDetalle by mutableStateOf("")
        private set

    var historial by mutableStateOf<List<PuntoPrecio>>(emptyList())
        private set
    var historialDesde by mutableStateOf(0L)
        private set
    var mayorDescuento by mutableStateOf<Int?>(null)
        private set
    var cargandoHistorial by mutableStateOf(false)
        private set
    var errorHistorial by mutableStateOf("")
        private set

    var perfil by mutableStateOf<Perfil?>(null)
        private set
    var cargandoLogin by mutableStateOf(false)
        private set
    var errorLogin by mutableStateOf("")
        private set

    val deseados = mutableStateListOf<Juego>()
    var sincronizando by mutableStateOf(false)
        private set
    var mensajeSincronizacion by mutableStateOf("")
        private set
    var ultimaSincronizacion by mutableStateOf("")
        private set

    init {
        cargarDesdeMemoria()
        cargarOfertas()
        buscar()
    }

    fun cargarOfertas() {
        viewModelScope.launch {
            cargandoOfertas = true
            errorOfertas = ""
            try {
                val respuesta = SteamApi.servicio.ofertas(region = region.codigo)
                val todos = respuesta.ofertas.items + respuesta.masVendidos.items
                ofertas = todos
                    .map { juegoDesdeOferta(it) }
                    .filter { it.descuento > 0 }
                    .distinctBy { it.id }
                    .sortedByDescending { it.descuento }
            } catch (e: Exception) {
                errorOfertas = "No se pudo conectar con Steam"
            }
            cargandoOfertas = false
        }
    }

    fun cambiarTexto(texto: String) {
        textoBusqueda = texto
        trabajoBusqueda?.cancel()
        trabajoBusqueda = viewModelScope.launch {
            delay(400)
            pedirPrimeraPagina()
        }
    }

    fun elegirFiltro(nuevo: String) {
        filtro = nuevo
        buscar()
    }

    fun siguienteOrden() {
        val siguiente = (ordenes.indexOf(orden) + 1) % ordenes.size
        orden = ordenes[siguiente]
        buscar()
    }

    fun buscar() {
        trabajoBusqueda?.cancel()
        trabajoBusqueda = viewModelScope.launch { pedirPrimeraPagina() }
    }

    fun cargarMas() {
        if (cargandoBusqueda || cargandoMas || !hayMas) return
        trabajoBusqueda = viewModelScope.launch {
            cargandoMas = true
            try {
                val nuevos = pedirPagina()
                resultados = (resultados + nuevos).distinctBy { it.id }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                hayMas = false
            }
            cargandoMas = false
        }
    }

    private suspend fun pedirPrimeraPagina() {
        cargandoBusqueda = true
        cargandoMas = false
        errorBusqueda = ""
        resultados = emptyList()
        siguientePosicion = 0
        try {
            resultados = pedirPagina()
        } catch (e: CancellationException) {
            throw e
        } catch (e: FaltaApiKey) {
            errorBusqueda = "Para ver los mínimos históricos agrega tu API key de IsThereAnyDeal en local.properties (ITAD_KEY=...)"
        } catch (e: Exception) {
            errorBusqueda = "No se pudo conectar"
        }
        cargandoBusqueda = false
    }

    private suspend fun pedirPagina(): List<Juego> {
        return if (filtro == "Mínimo histórico") {
            pedirPaginaMinimos()
        } else {
            pedirPaginaSteam()
        }
    }

    private suspend fun pedirPaginaSteam(): List<Juego> {
        val respuesta = SteamApi.servicio.catalogo(
            texto = textoBusqueda,
            desde = siguientePosicion,
            soloOfertas = if (filtro == "En oferta") 1 else null,
            precioMaximo = if (filtro == "Gratis") "free" else null,
            orden = codigoOrden(orden),
            region = region.codigo
        )
        siguientePosicion += 25
        totalResultados = respuesta.total
        hayMas = siguientePosicion < respuesta.total
        return juegosDesdeHtml(respuesta.html, region.moneda)
    }

    private suspend fun pedirPaginaMinimos(): List<Juego> {
        val key = BuildConfig.ITAD_KEY
        if (key == "") throw FaltaApiKey()

        val respuesta = ItadApi.servicio.ofertasEnMinimo(
            key = key,
            pedido = PedidoMinimos(country = region.codigo, offset = siguientePosicion)
        )
        siguientePosicion += 25
        totalResultados = 0
        hayMas = respuesta.hasMore

        val mapaIds = ItadApi.servicio.idsDeSteam(key = key, ids = respuesta.list.map { it.id })

        val candidatos = mutableMapOf<String, List<Int>>()
        for ((idItad, lista) in mapaIds) {
            candidatos[idItad] = (lista ?: emptyList())
                .filter { it.startsWith("app/") }
                .mapNotNull { it.removePrefix("app/").toIntOrNull() }
        }

        val locales = try {
            SteamApi.preciosLocales(candidatos.values.flatten(), region.codigo)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emptyMap()
        }

        val juegos = mutableListOf<Juego>()
        for (oferta in respuesta.list) {
            val apps = candidatos[oferta.id] ?: continue
            val idSteam = apps.firstOrNull { it in locales } ?: apps.firstOrNull() ?: continue
            val precio = locales[idSteam]

            juegos.add(
                Juego(
                    id = idSteam,
                    nombre = oferta.title,
                    imagen = oferta.assets.banner400.ifEmpty {
                        "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/$idSteam/header.jpg"
                    },
                    precioNormal = precio?.precioNormal ?: oferta.deal.regular.amountInt,
                    precioFinal = precio?.precioFinal ?: oferta.deal.price.amountInt,
                    descuento = precio?.descuento ?: oferta.deal.cut,
                    moneda = precio?.currency ?: oferta.deal.price.currency,
                    nota = "Mínimo histórico"
                )
            )
        }
        return juegos
    }

    class FaltaApiKey : Exception()

    fun cargarDetalle(id: Int) {
        viewModelScope.launch {
            detalle = null
            cargandoDetalle = true
            errorDetalle = ""
            try {
                val respuesta = SteamApi.servicio.detalle(id = id, region = region.codigo)
                detalle = respuesta[id.toString()]?.data
                if (detalle == null) errorDetalle = "Steam no tiene datos de este juego"
            } catch (e: Exception) {
                errorDetalle = "No se pudo conectar con Steam"
            }
            cargandoDetalle = false
        }
    }

    fun cargarHistorial(steamId: Int, periodo: String) {
        val key = BuildConfig.ITAD_KEY
        historial = emptyList()
        mayorDescuento = null
        historialDesde = Instant.now().minus(diasDelPeriodo(periodo), ChronoUnit.DAYS).epochSecond

        if (key == "") {
            errorHistorial = "Falta la API key de IsThereAnyDeal.\nAgrégala en local.properties (ITAD_KEY=...)"
            return
        }

        viewModelScope.launch {
            cargandoHistorial = true
            errorHistorial = ""
            try {
                val busqueda = ItadApi.servicio.buscarJuego(key = key, steamId = steamId)
                val idItad = busqueda.game?.id

                if (!busqueda.found || idItad == null) {
                    errorHistorial = "IsThereAnyDeal no tiene historial de este juego"
                } else {
                    val desde = Instant.ofEpochSecond(historialDesde).toString()
                    val cambios = ItadApi.servicio.historial(
                        key = key, id = idItad, pais = region.codigo, desde = desde
                    )
                    historial = cambios
                        .map { cambio ->
                            PuntoPrecio(
                                fecha = OffsetDateTime.parse(cambio.timestamp).toEpochSecond(),
                                descuento = cambio.deal.cut
                            )
                        }
                        .sortedBy { it.fecha }

                    val minimos = ItadApi.servicio.minimoHistorico(
                        key = key, pais = region.codigo, ids = listOf(idItad)
                    )
                    val minimoSteam = minimos.firstOrNull()?.lows?.find { it.shop.id == TIENDA_STEAM }
                    mayorDescuento = minimoSteam?.cut

                }
            } catch (e: HttpException) {
                errorHistorial = if (e.code() == 403) "La API key de IsThereAnyDeal no es válida" else "Error de IsThereAnyDeal (${e.code()})"
            } catch (e: Exception) {
                errorHistorial = "No se pudo conectar con IsThereAnyDeal"
            }
            cargandoHistorial = false
        }
    }

    fun terminarLogin(respuesta: Uri) {
        viewModelScope.launch {
            cargandoLogin = true
            errorLogin = ""
            try {
                val datos = mutableMapOf<String, String>()
                for (nombre in respuesta.queryParameterNames) {
                    if (nombre.startsWith("openid.")) {
                        datos[nombre] = respuesta.getQueryParameter(nombre) ?: ""
                    }
                }

                datos["openid.mode"] = "check_authentication"
                val verificacion = ComunidadApi.servicio.verificarLogin(datos)

                val claimedId = datos["openid.claimed_id"] ?: ""

                if (!verificacion.contains("is_valid:true") || !claimedId.startsWith(PREFIJO_STEAM_ID)) {
                    errorLogin = "Steam no confirmó el inicio de sesión"
                } else {
                    val steamId = claimedId.removePrefix(PREFIJO_STEAM_ID)

                    val xml = ComunidadApi.servicio.perfil(steamId)
                    perfil = Perfil(
                        steamId = steamId,
                        nombre = valorXml(xml, "steamID"),
                        avatar = valorXml(xml, "avatarFull"),
                        estado = valorXml(xml, "onlineState")
                    )
                    guardarEnMemoria()
                    sincronizarDeseados()
                }
            } catch (e: Exception) {
                errorLogin = "No se pudo conectar con Steam"
            }
            cargandoLogin = false
        }
    }

    fun cerrarSesion() {
        perfil = null
        mensajeSincronizacion = ""
        CookieManager.getInstance().removeAllCookies(null)
        guardarEnMemoria()
    }

    fun esDeseado(id: Int): Boolean {
        return deseados.any { it.id == id }
    }

    fun alternarDeseado(juego: Juego) {
        if (esDeseado(juego.id)) {
            deseados.removeAll { it.id == juego.id }
        } else {
            deseados.add(juego)
        }
        guardarEnMemoria()
    }

    fun sincronizarDeseados() {
        viewModelScope.launch {
            sincronizando = true
            mensajeSincronizacion = ""
            actualizarPrecios()
            val usuario = perfil
            if (usuario != null) {
                importarDesdeSteam(usuario.steamId)
            }
            ultimaSincronizacion = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            sincronizando = false
            guardarEnMemoria()
        }
    }

    private suspend fun actualizarPrecios() {
        for (i in deseados.indices) {
            try {
                val id = deseados[i].id
                val datos = SteamApi.servicio.detalle(id = id, region = region.codigo)[id.toString()]?.data
                if (datos != null) {
                    val nuevo = juegoDesdeDetalle(datos)
                    deseados[i] = deseados[i].copy(
                        precioNormal = nuevo.precioNormal,
                        precioFinal = nuevo.precioFinal,
                        descuento = nuevo.descuento,
                        moneda = nuevo.moneda
                    )
                }
            } catch (e: Exception) {
            }
        }
    }

    private suspend fun importarDesdeSteam(steamId: String) {
        try {
            val lista = ComunidadApi.servicio.listaDeseos(steamId).response.items
            if (lista.isEmpty()) {
                mensajeSincronizacion = "Tu lista de deseados de Steam es privada o está vacía"
                return
            }
            val recientes = lista.sortedByDescending { it.fechaAgregado }.take(30)
            var nuevos = 0
            for (item in recientes) {
                if (esDeseado(item.appid)) continue
                try {
                    val datos = SteamApi.servicio.detalle(id = item.appid, region = region.codigo)[item.appid.toString()]?.data
                    if (datos != null) {
                        deseados.add(juegoDesdeDetalle(datos))
                        nuevos++
                    }
                } catch (e: Exception) {
                }
            }
            if (nuevos > 0) mensajeSincronizacion = "$nuevos juegos nuevos desde Steam"
        } catch (e: Exception) {
            mensajeSincronizacion = "No se pudo leer tu lista de Steam"
        }
    }

    fun cambiarRegion(nueva: Region) {
        region = nueva
        guardarEnMemoria()
        cargarOfertas()
        buscar()
        sincronizarDeseados()
    }

    private fun guardarEnMemoria() {
        val textoPerfil = if (perfil != null) Json.encodeToString(perfil) else ""
        memoria.edit()
            .putString("deseados", Json.encodeToString(deseados.toList()))
            .putString("region", region.codigo)
            .putString("perfil", textoPerfil)
            .apply()
    }

    private fun cargarDesdeMemoria() {
        val codigo = memoria.getString("region", "US")
        region = regiones.find { it.codigo == codigo } ?: regiones[0]

        val textoPerfil = memoria.getString("perfil", "") ?: ""
        if (textoPerfil != "") {
            perfil = Json.decodeFromString<Perfil>(textoPerfil)
        }

        val texto = memoria.getString("deseados", null)
        if (texto != null) {
            deseados.addAll(Json.decodeFromString<List<Juego>>(texto))
        }
    }
}
