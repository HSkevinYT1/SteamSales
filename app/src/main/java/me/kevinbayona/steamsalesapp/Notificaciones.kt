package me.kevinbayona.steamsalesapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

const val CANAL_OFERTAS = "ofertas_deseados"
const val TRABAJO_OFERTAS = "revisar_ofertas"

fun crearCanal(context: Context) {
    val canal = NotificationChannel(
        CANAL_OFERTAS,
        "Ofertas de deseados",
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        description = "Avisa cuando un juego de tu lista de deseados sale en oferta o llega a su mínimo histórico"
    }
    context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
}

fun tienePermisoNotificaciones(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED
}

fun mostrarOferta(context: Context, juego: Juego, esMinimo: Boolean) {
    if (!tienePermisoNotificaciones(context)) return

    val abrir = PendingIntent.getActivity(
        context,
        juego.id,
        Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val titulo = if (esMinimo) "🔥 Mínimo histórico: ${juego.nombre}" else "${juego.nombre} está en oferta"
    val precio = formatearPrecio(juego.precioFinal, juego.moneda)
    val antes = formatearPrecio(juego.precioNormal, juego.moneda)
    val texto = "-${juego.descuento}% · $precio (antes $antes)"

    val notificacion = NotificationCompat.Builder(context, CANAL_OFERTAS)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(titulo)
        .setContentText(texto)
        .setContentIntent(abrir)
        .setAutoCancel(true)
        .build()

    try {
        NotificationManagerCompat.from(context).notify(juego.id, notificacion)
    } catch (e: SecurityException) {
    }
}

const val ID_RESUMEN = 0

fun mostrarResumen(context: Context, juegos: List<Pair<Juego, Boolean>>) {
    if (!tienePermisoNotificaciones(context)) return

    val abrir = PendingIntent.getActivity(
        context,
        ID_RESUMEN,
        Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val ordenados = juegos.sortedWith(compareByDescending<Pair<Juego, Boolean>> { it.second }.thenByDescending { it.first.descuento })
    val cantidadMinimos = juegos.count { it.second }

    val titulo = "${juegos.size} juegos de tu lista están en oferta"
    val resumen = if (cantidadMinimos > 0) "🔥 $cantidadMinimos en mínimo histórico" else "Toca para verlos"

    val estilo = NotificationCompat.InboxStyle().setSummaryText(resumen)
    for ((juego, esMinimo) in ordenados.take(6)) {
        val marca = if (esMinimo) "🔥 " else ""
        estilo.addLine("$marca${juego.nombre}  -${juego.descuento}% · ${formatearPrecio(juego.precioFinal, juego.moneda)}")
    }
    if (ordenados.size > 6) estilo.addLine("y ${ordenados.size - 6} más…")

    val notificacion = NotificationCompat.Builder(context, CANAL_OFERTAS)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(titulo)
        .setContentText(resumen)
        .setStyle(estilo)
        .setContentIntent(abrir)
        .setAutoCancel(true)
        .build()

    try {
        NotificationManagerCompat.from(context).notify(ID_RESUMEN, notificacion)
    } catch (e: SecurityException) {
    }
}

fun programarRevision(context: Context) {
    val condiciones = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
    val pedido = PeriodicWorkRequestBuilder<RevisorOfertas>(6, TimeUnit.HOURS)
        .setConstraints(condiciones)
        .build()
    WorkManager.getInstance(context)
        .enqueueUniquePeriodicWork(TRABAJO_OFERTAS, ExistingPeriodicWorkPolicy.KEEP, pedido)
}

fun probarRevision(context: Context) {
    context.getSharedPreferences("steam_sales", Context.MODE_PRIVATE)
        .edit().remove("avisos").apply()
    WorkManager.getInstance(context)
        .enqueueUniqueWork("probar_ofertas", ExistingWorkPolicy.REPLACE, OneTimeWorkRequestBuilder<RevisorOfertas>().build())
}

fun cancelarRevision(context: Context) {
    WorkManager.getInstance(context).cancelUniqueWork(TRABAJO_OFERTAS)
}
