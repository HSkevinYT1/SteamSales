package me.kevinbayona.steamsalesapp.datos

import me.kevinbayona.steamsalesapp.Juego
import kotlin.math.roundToInt

fun juegosDesdeHtml(html: String, moneda: String): List<Juego> {
    val juegos = mutableListOf<Juego>()

    val bloques = html.split("<a href=\"https://store.steampowered.com/").drop(1)

    for (bloque in bloques) {
        val id = buscar(bloque, "data-ds-appid=\"(\\d+)\"").toIntOrNull() ?: continue
        val precioFinal = buscar(bloque, "data-price-final=\"(\\d+)\"").toIntOrNull() ?: 0
        val descuento = buscar(bloque, "data-discount=\"(\\d+)\"").toIntOrNull() ?: 0

        val digitosNormal = soloDigitos(buscar(bloque, "discount_original_price\">([^<]+)<"))
        val digitosFinal = soloDigitos(buscar(bloque, "discount_final_price\">([^<]+)<"))
        val precioNormal = if (descuento > 0 && digitosNormal > 0 && digitosFinal > 0) {
            digitosNormal * (precioFinal / digitosFinal)
        } else if (descuento > 0) {
            (precioFinal / (1 - descuento / 100.0)).roundToInt()
        } else {
            precioFinal
        }

        val resenas = buscar(bloque, "data-tooltip-html=\"([^&\"]*)&lt;br")

        juegos.add(
            Juego(
                id = id,
                nombre = limpiarTexto(buscar(bloque, "<span class=\"title\">(.*?)</span>")),
                imagen = buscar(bloque, "<img src=\"([^\"]+)\""),
                precioNormal = precioNormal,
                precioFinal = precioFinal,
                descuento = descuento,
                moneda = moneda,
                nota = resenas
            )
        )
    }
    return juegos
}

private fun buscar(texto: String, patron: String): String {
    return Regex(patron).find(texto)?.groupValues?.get(1) ?: ""
}

private fun soloDigitos(texto: String): Int {
    return texto.filter { it.isDigit() }.toIntOrNull() ?: 0
}

private fun limpiarTexto(texto: String): String {
    return texto
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .trim()
}
