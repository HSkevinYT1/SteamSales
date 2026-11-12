package me.kevinbayona.steamsalesapp

import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import me.kevinbayona.steamsalesapp.datos.DOMINIO_RETORNO
import me.kevinbayona.steamsalesapp.datos.URL_LOGIN_STEAM
import me.kevinbayona.steamsalesapp.ui.theme.Fondo
import me.kevinbayona.steamsalesapp.ui.theme.Inter
import me.kevinbayona.steamsalesapp.ui.theme.JetBrainsMono
import me.kevinbayona.steamsalesapp.ui.theme.TextoBlanco
import me.kevinbayona.steamsalesapp.ui.theme.TextoGris
import me.kevinbayona.steamsalesapp.ui.theme.Verde

@Composable
fun PantallaLogin(alTerminar: (Uri) -> Unit, alCancelar: () -> Unit) {
    BackHandler { alCancelar() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            BotonIcono(icono = Icons.AutoMirrored.Filled.ArrowBack, color = TextoBlanco, alTocar = alCancelar)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Iniciar sesión con Steam", color = TextoBlanco, fontFamily = Inter, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Lock, contentDescription = null, tint = Verde, modifier = Modifier.padding(end = 4.dp).size(12.dp))
                    Text("steamcommunity.com", color = TextoGris, fontFamily = JetBrainsMono, fontSize = 11.sp)
                }
            }
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { contexto ->
                WebView(contexto).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true

                    var yaTermino = false

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                            val direccion = request.url
                            if (direccion.host == DOMINIO_RETORNO) {
                                if (!yaTermino) {
                                    yaTermino = true
                                    alTerminar(direccion)
                                }
                                return true
                            }
                            return false
                        }

                        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                            val direccion = Uri.parse(url)
                            if (direccion.host == DOMINIO_RETORNO) {
                                view.stopLoading()
                                if (!yaTermino) {
                                    yaTermino = true
                                    alTerminar(direccion)
                                }
                            }
                        }
                    }

                    loadUrl(URL_LOGIN_STEAM)
                }
            }
        )
    }
}
