package me.kevinbayona.steamsalesapp.datos

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
}

@Serializable
data class RespuestaPrecio(
    val success: Boolean = false,
    val data: JsonElement? = null
)
