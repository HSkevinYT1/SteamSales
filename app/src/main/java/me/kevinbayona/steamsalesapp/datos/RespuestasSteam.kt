package me.kevinbayona.steamsalesapp.datos

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RespuestaDestacados(
    @SerialName("specials") val ofertas: Categoria = Categoria(),
    @SerialName("top_sellers") val masVendidos: Categoria = Categoria()
)

@Serializable
data class Categoria(
    val items: List<ItemDestacado> = emptyList()
)

@Serializable
data class ItemDestacado(
    val id: Int,
    val name: String,
    @SerialName("discount_percent") val descuento: Int = 0,
    @SerialName("original_price") val precioNormal: Int = 0,
    @SerialName("final_price") val precioFinal: Int = 0,
    val currency: String = "USD",
    @SerialName("large_capsule_image") val imagen: String = "",
    @SerialName("discount_expiration") val terminaEn: Long = 0
)

@Serializable
data class RespuestaCatalogo(
    @SerialName("results_html") val html: String = "",
    @SerialName("total_count") val total: Int = 0
)

@Serializable
data class Plataformas(
    val windows: Boolean = false,
    val mac: Boolean = false,
    val linux: Boolean = false
)

@Serializable
data class RespuestaDetalle(
    val success: Boolean = false,
    val data: DatosJuego? = null
)

@Serializable
data class DatosJuego(
    @SerialName("steam_appid") val id: Int,
    val name: String,
    @SerialName("short_description") val descripcion: String = "",
    @SerialName("header_image") val imagen: String = "",
    @SerialName("is_free") val gratis: Boolean = false,
    val developers: List<String> = emptyList(),
    val genres: List<Genero> = emptyList(),
    @SerialName("price_overview") val precio: PrecioDetalle? = null,
    val platforms: Plataformas = Plataformas(),
    val metacritic: Metacritic? = null,
    @SerialName("release_date") val lanzamiento: Lanzamiento? = null,
    @SerialName("screenshots") val capturas: List<Captura> = emptyList()
)

@Serializable
data class Captura(
    @SerialName("path_thumbnail") val pequena: String = "",
    @SerialName("path_full") val grande: String = ""
)

@Serializable
data class Genero(val description: String = "")

@Serializable
data class PrecioDetalle(
    val currency: String = "USD",
    @SerialName("initial") val precioNormal: Int = 0,
    @SerialName("final") val precioFinal: Int = 0,
    @SerialName("discount_percent") val descuento: Int = 0
)

@Serializable
data class Metacritic(val score: Int = 0)

@Serializable
data class Lanzamiento(val date: String = "")
