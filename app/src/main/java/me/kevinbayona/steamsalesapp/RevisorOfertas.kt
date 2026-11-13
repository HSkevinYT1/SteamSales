package me.kevinbayona.steamsalesapp

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.kevinbayona.steamsalesapp.datos.ItadApi
import me.kevinbayona.steamsalesapp.datos.PrecioDetalle
import me.kevinbayona.steamsalesapp.datos.SteamApi
import me.kevinbayona.steamsalesapp.datos.TIENDA_STEAM

class RevisorOfertas(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val memoria = context.getSharedPreferences("steam_sales", Context.MODE_PRIVATE)

    override suspend fun doWork(): Result {
        if (!memoria.getBoolean("notificaciones", true)) return Result.success()

        val deseados = leerMapa<List<Juego>>("deseados") ?: return Result.success()
        if (deseados.isEmpty()) return Result.success()

        val region = memoria.getString("region", "US") ?: "US"

        val precios = mutableMapOf<Int, PrecioDetalle>()
        try {
            for (grupo in deseados.map { it.id }.chunked(50)) {
                precios.putAll(SteamApi.preciosLocales(grupo, region))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return Result.retry()
        }

        val enOferta = deseados.filter { (precios[it.id]?.descuento ?: 0) > 0 }.map { it.id }
        val minimos = minimosHistoricos(enOferta, region)

        val avisos = leerMapa<Map<Int, String>>("avisos")?.toMutableMap() ?: mutableMapOf()
        val porAvisar = mutableListOf<Pair<Juego, Boolean>>()

        for (juego in deseados) {
            val precio = precios[juego.id] ?: continue

            if (precio.descuento <= 0) {
                avisos.remove(juego.id)
                continue
            }

            val minimo = minimos[juego.id]
            val esMinimo = minimo != null && precio.precioFinal <= minimo

            val anterior = avisos[juego.id]
            val descuentoAnterior = anterior?.substringBefore("|")?.toIntOrNull() ?: 0
            val eraMinimo = anterior?.substringAfter("|") == "1"

            if (precio.descuento > descuentoAnterior || (esMinimo && !eraMinimo)) {
                val actualizado = juego.copy(
                    precioNormal = precio.precioNormal,
                    precioFinal = precio.precioFinal,
                    descuento = precio.descuento,
                    moneda = precio.currency
                )
                porAvisar.add(actualizado to esMinimo)
            }
            avisos[juego.id] = "${precio.descuento}|${if (esMinimo) 1 else 0}"
        }

        if (porAvisar.size <= MAXIMO_INDIVIDUALES) {
            for ((juego, esMinimo) in porAvisar) mostrarOferta(applicationContext, juego, esMinimo)
        } else {
            mostrarResumen(applicationContext, porAvisar)
        }

        avisos.keys.retainAll(deseados.map { it.id }.toSet())
        memoria.edit().putString("avisos", Json.encodeToString(avisos.toMap())).apply()
        return Result.success()
    }

    private suspend fun minimosHistoricos(steamIds: List<Int>, region: String): Map<Int, Int> {
        val key = BuildConfig.ITAD_KEY
        if (key == "" || steamIds.isEmpty()) return emptyMap()

        return try {
            val idsItad = idsDeItad(steamIds, key)
            val resultado = mutableMapOf<Int, Int>()
            val steamPorItad = idsItad.entries.associate { (steam, itad) -> itad to steam }

            for (grupo in idsItad.values.toList().chunked(100)) {
                val minimos = ItadApi.servicio.minimoHistorico(key = key, pais = region, ids = grupo)
                for (minimo in minimos) {
                    val steamId = steamPorItad[minimo.id] ?: continue
                    val low = minimo.lows.find { it.shop.id == TIENDA_STEAM } ?: continue
                    resultado[steamId] = low.price.amountInt
                }
            }
            resultado
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private suspend fun idsDeItad(steamIds: List<Int>, key: String): Map<Int, String> {
        val guardados = leerMapa<Map<Int, String>>("ids_itad")?.toMutableMap() ?: mutableMapOf()
        val faltan = steamIds.filter { it !in guardados }

        if (faltan.isNotEmpty()) {
            for (grupo in faltan.chunked(200)) {
                val respuesta = ItadApi.servicio.idsDeItad(key = key, idsSteam = grupo.map { "app/$it" })
                for ((idSteam, idItad) in respuesta) {
                    val id = idSteam.removePrefix("app/").toIntOrNull() ?: continue
                    if (idItad != null) guardados[id] = idItad
                }
            }
            memoria.edit().putString("ids_itad", Json.encodeToString(guardados.toMap())).apply()
        }
        return steamIds.mapNotNull { id -> guardados[id]?.let { id to it } }.toMap()
    }

    private inline fun <reified T> leerMapa(clave: String): T? {
        val texto = memoria.getString(clave, null) ?: return null
        return try {
            Json.decodeFromString<T>(texto)
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        const val MAXIMO_INDIVIDUALES = 3
    }
}
