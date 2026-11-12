package me.kevinbayona.steamsalesapp.datos

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

const val DOMINIO_RETORNO = "steamsalesapp.invalid"
const val DIRECCION_RETORNO = "https://$DOMINIO_RETORNO/login"

const val URL_LOGIN_STEAM = "https://steamcommunity.com/openid/login" +
        "?openid.ns=http://specs.openid.net/auth/2.0" +
        "&openid.mode=checkid_setup" +
        "&openid.return_to=$DIRECCION_RETORNO" +
        "&openid.realm=https://$DOMINIO_RETORNO" +
        "&openid.identity=http://specs.openid.net/auth/2.0/identifier_select" +
        "&openid.claimed_id=http://specs.openid.net/auth/2.0/identifier_select"

const val PREFIJO_STEAM_ID = "https://steamcommunity.com/openid/id/"

interface ComunidadApiService {

    @FormUrlEncoded
    @POST("openid/login")
    suspend fun verificarLogin(@FieldMap datos: Map<String, String>): String

    @GET("profiles/{steamId}/")
    suspend fun perfil(
        @Path("steamId") steamId: String,
        @Query("xml") xml: Int = 1
    ): String

    @GET("https://api.steampowered.com/IWishlistService/GetWishlist/v1/")
    suspend fun listaDeseos(@Query("steamid") steamId: String): RespuestaListaDeseos
}

object ComunidadApi {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://steamcommunity.com/")
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val servicio: ComunidadApiService by lazy {
        retrofit.create(ComunidadApiService::class.java)
    }
}

@Serializable
data class RespuestaListaDeseos(val response: ContenidoLista = ContenidoLista())

@Serializable
data class ContenidoLista(val items: List<ItemLista> = emptyList())

@Serializable
data class ItemLista(
    val appid: Int,
    @SerialName("date_added") val fechaAgregado: Long = 0
)

fun valorXml(xml: String, etiqueta: String): String {
    val inicio = xml.indexOf("<$etiqueta>")
    if (inicio == -1) return ""
    val fin = xml.indexOf("</$etiqueta>", inicio)
    if (fin == -1) return ""
    return xml.substring(inicio + etiqueta.length + 2, fin)
        .removePrefix("<![CDATA[")
        .removeSuffix("]]>")
        .trim()
}
