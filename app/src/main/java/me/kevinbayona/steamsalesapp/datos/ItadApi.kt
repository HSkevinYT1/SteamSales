package me.kevinbayona.steamsalesapp.datos

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

const val TIENDA_STEAM = 61

interface ItadApiService {

    @GET("games/lookup/v1")
    suspend fun buscarJuego(
        @Query("key") key: String,
        @Query("appid") steamId: Int
    ): RespuestaBusquedaItad

    @GET("games/history/v2")
    suspend fun historial(
        @Query("key") key: String,
        @Query("id") id: String,
        @Query("country") pais: String,
        @Query("since") desde: String,
        @Query("shops") tienda: Int = TIENDA_STEAM
    ): List<CambioPrecio>

    @POST("games/storelow/v2")
    suspend fun minimoHistorico(
        @Query("key") key: String,
        @Query("country") pais: String,
        @Body ids: List<String>,
        @Query("shops") tienda: Int = TIENDA_STEAM
    ): List<MinimoHistorico>

    @POST("deals/v2")
    suspend fun ofertasEnMinimo(
        @Query("key") key: String,
        @Body pedido: PedidoMinimos
    ): RespuestaOfertasItad

    @POST("lookup/id/shop/$TIENDA_STEAM/v1")
    suspend fun idsDeItad(
        @Query("key") key: String,
        @Body idsSteam: List<String>
    ): Map<String, String?>

    @POST("lookup/shop/$TIENDA_STEAM/id/v1")
    suspend fun idsDeSteam(
        @Query("key") key: String,
        @Body ids: List<String>
    ): Map<String, List<String>?>
}

object ItadApi {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    private val cliente = OkHttpClient.Builder()
        .addInterceptor { cadena ->
            val pedido = cadena.request()
            val cuerpo = pedido.body
            if (cuerpo == null) {
                cadena.proceed(pedido)
            } else {
                val bytes = Buffer().also { cuerpo.writeTo(it) }.readByteArray()
                val nuevoCuerpo = bytes.toRequestBody("application/json".toMediaType())
                cadena.proceed(pedido.newBuilder().method(pedido.method, nuevoCuerpo).build())
            }
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.isthereanydeal.com/")
        .client(cliente)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val servicio: ItadApiService by lazy {
        retrofit.create(ItadApiService::class.java)
    }
}

@Serializable
data class PedidoMinimos(
    val country: String,
    val offset: Int,
    val limit: Int = 25,
    val sort: String = "rank",
    val shops: List<Int> = listOf(TIENDA_STEAM),
    val filter: FiltroMinimos = FiltroMinimos()
)

@Serializable
data class FiltroMinimos(
    val flag: String = "S",
    val type: List<Int> = listOf(1)
)

@Serializable
data class RespuestaOfertasItad(
    val hasMore: Boolean = false,
    val list: List<OfertaItad> = emptyList()
)

@Serializable
data class OfertaItad(
    val id: String,
    val title: String = "",
    val assets: ImagenesItad = ImagenesItad(),
    val deal: Oferta
)

@Serializable
data class ImagenesItad(val banner400: String = "")

@Serializable
data class RespuestaBusquedaItad(
    val found: Boolean = false,
    val game: JuegoItad? = null
)

@Serializable
data class JuegoItad(val id: String)

@Serializable
data class CambioPrecio(
    val timestamp: String,
    val deal: Oferta
)

@Serializable
data class Oferta(
    val price: Monto,
    val regular: Monto,
    val cut: Int = 0
)

@Serializable
data class Monto(
    val amountInt: Int = 0,
    val currency: String = "USD"
)

@Serializable
data class MinimoHistorico(
    val id: String,
    val lows: List<OfertaMinima> = emptyList()
)

@Serializable
data class OfertaMinima(
    val shop: Tienda,
    val price: Monto,
    val cut: Int = 0,
    val timestamp: String = ""
)

@Serializable
data class Tienda(val id: Int = 0, val name: String = "")
