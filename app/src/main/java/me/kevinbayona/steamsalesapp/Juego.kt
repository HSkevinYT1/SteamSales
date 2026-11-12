package me.kevinbayona.steamsalesapp

import kotlinx.serialization.Serializable
import me.kevinbayona.steamsalesapp.datos.DatosJuego
import me.kevinbayona.steamsalesapp.datos.ItemDestacado
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@Serializable
data class Juego(
    val id: Int,
    val nombre: String,
    val imagen: String,
    val precioNormal: Int,
    val precioFinal: Int,
    val descuento: Int,
    val moneda: String,
    val etiquetas: List<String> = emptyList(),
    val nota: String = "",
    val terminaEn: Long = 0
)

fun juegoDesdeOferta(item: ItemDestacado): Juego {
    return Juego(
        id = item.id,
        nombre = item.name,
        imagen = item.imagen,
        precioNormal = if (item.precioNormal == 0) item.precioFinal else item.precioNormal,
        precioFinal = item.precioFinal,
        descuento = item.descuento,
        moneda = item.currency,
        terminaEn = item.terminaEn
    )
}

fun juegoDesdeDetalle(datos: DatosJuego): Juego {
    return Juego(
        id = datos.id,
        nombre = datos.name,
        imagen = datos.imagen,
        precioNormal = datos.precio?.precioNormal ?: 0,
        precioFinal = datos.precio?.precioFinal ?: 0,
        descuento = datos.precio?.descuento ?: 0,
        moneda = datos.precio?.currency ?: "USD",
        etiquetas = datos.genres.map { it.description }
    )
}

fun formatearPrecio(centavos: Int, moneda: String): String {
    if (centavos == 0) return "Gratis"

    val pais = when (moneda) {
        "COP" -> "es-CO"
        "MXN" -> "es-MX"
        "CLP" -> "es-CL"
        "PEN" -> "es-PE"
        "EUR" -> "es-ES"
        else -> "en-US"
    }
    val formato = NumberFormat.getCurrencyInstance(Locale.forLanguageTag(pais))
    formato.currency = Currency.getInstance(moneda)

    if (centavos % 100 == 0) formato.maximumFractionDigits = 0

    return formato.format(centavos / 100.0)
}

fun tiempoRestante(terminaEn: Long): String {
    if (terminaEn == 0L) return ""
    val segundos = terminaEn - System.currentTimeMillis() / 1000
    if (segundos <= 0) return "Oferta terminada"
    val dias = segundos / 86400
    val horas = (segundos % 86400) / 3600
    return if (dias > 0) "Termina en ${dias}d ${horas}h" else "Termina en ${horas}h"
}

fun urlSteam(id: Int): String = "https://store.steampowered.com/app/$id"
