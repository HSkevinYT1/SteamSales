package me.kevinbayona.steamsalesapp.datos

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface SteamApiService {

    @GET("api/featuredcategories")
    suspend fun ofertas(
        @Query("cc") region: String,
        @Query("l") idioma: String = "spanish"
    ): RespuestaDestacados

    @GET("search/results/")
    suspend fun catalogo(
        @Query("term") texto: String,
        @Query("start") desde: Int,
        @Query("specials") soloOfertas: Int? = null,
        @Query("maxprice") precioMaximo: String? = null,
        @Query("sort_by") orden: String? = null,
        @Query("cc") region: String,
        @Query("count") cantidad: Int = 25,
        @Query("category1") categoria: Int = 998,
        @Query("infinite") infinito: Int = 1,
        @Query("l") idioma: String = "spanish"
    ): RespuestaCatalogo

    @GET("api/appdetails")
    suspend fun precios(
        @Query("appids") ids: String,
        @Query("cc") region: String,
        @Query("filters") filtro: String = "price_overview"
    ): Map<String, RespuestaPrecio>

    @GET("https://api.steampowered.com/IStoreBrowseService/GetItems/v1/")
    suspend fun articulos(@Query("input_json") entrada: String): RespuestaArticulos

    @GET("api/appdetails")
    suspend fun detalle(
        @Query("appids") id: Int,
        @Query("cc") region: String,
        @Query("l") idioma: String = "spanish"
    ): Map<String, RespuestaDetalle>
}

object SteamApi {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://store.steampowered.com/")
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val servicio: SteamApiService by lazy {
        retrofit.create(SteamApiService::class.java)
    }

    suspend fun preciosLocales(ids: List<Int>, region: String): Map<Int, PrecioDetalle> {
        val resultado = mutableMapOf<Int, PrecioDetalle>()
        if (ids.isEmpty()) return resultado

        val respuesta = servicio.precios(ids = ids.joinToString(","), region = region)
        for ((id, item) in respuesta) {
            val datos = item.data as? JsonObject ?: continue
            val precio = datos["price_overview"] ?: continue
            resultado[id.toInt()] = json.decodeFromJsonElement(PrecioDetalle.serializer(), precio)
        }
        return resultado
    }

    suspend fun articulosEnLote(ids: List<Int>, region: String): List<ArticuloTienda> {
        val resultado = mutableListOf<ArticuloTienda>()
        for (grupo in ids.chunked(100)) {
            val pedido = PedidoArticulos(
                ids = grupo.map { IdArticulo(it) },
                context = ContextoArticulos(country_code = region)
            )
            val respuesta = servicio.articulos(json.encodeToString(PedidoArticulos.serializer(), pedido))
            resultado.addAll(respuesta.response.store_items.filter { it.success == 1 && it.name != "" })
        }
        return resultado
    }
}

@Serializable
data class PedidoArticulos(
    val ids: List<IdArticulo>,
    val context: ContextoArticulos,
    val data_request: DatosPedidos = DatosPedidos()
)

@Serializable
data class IdArticulo(val appid: Int)

@Serializable
data class ContextoArticulos(val language: String = "spanish", val country_code: String)

@Serializable
data class DatosPedidos(val include_assets: Boolean = true)

@Serializable
data class RespuestaArticulos(val response: ListaArticulos = ListaArticulos())

@Serializable
data class ListaArticulos(val store_items: List<ArticuloTienda> = emptyList())

@Serializable
data class ArticuloTienda(
    val appid: Int = 0,
    val success: Int = 0,
    val name: String = "",
    val assets: ImagenesArticulo? = null,
    @SerialName("best_purchase_option") val compra: OpcionCompra? = null
) {
    val imagen: String
        get() {
            val formato = assets?.asset_url_format
            val header = assets?.header
            return if (formato != null && header != null) {
                "https://shared.akamai.steamstatic.com/store_item_assets/" + formato.replace("\${FILENAME}", header)
            } else {
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/$appid/header.jpg"
            }
        }
}

@Serializable
data class ImagenesArticulo(val asset_url_format: String? = null, val header: String? = null)

@Serializable
data class OpcionCompra(
    @SerialName("final_price_in_cents") val textoFinal: String = "0",
    @SerialName("original_price_in_cents") val textoNormal: String? = null,
    @SerialName("discount_pct") val descuento: Int = 0
) {
    val precioFinal: Int get() = textoFinal.toIntOrNull() ?: 0
    val precioNormal: Int get() = textoNormal?.toIntOrNull() ?: precioFinal
}

@Serializable
data class RespuestaPrecio(
    val success: Boolean = false,
    val data: JsonElement? = null
)
