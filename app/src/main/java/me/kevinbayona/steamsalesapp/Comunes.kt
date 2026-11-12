package me.kevinbayona.steamsalesapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.kevinbayona.steamsalesapp.ui.theme.Azul
import me.kevinbayona.steamsalesapp.ui.theme.Boton
import me.kevinbayona.steamsalesapp.ui.theme.Inter
import me.kevinbayona.steamsalesapp.ui.theme.JetBrainsMono
import me.kevinbayona.steamsalesapp.ui.theme.Tarjeta
import me.kevinbayona.steamsalesapp.ui.theme.TextoGris
import me.kevinbayona.steamsalesapp.ui.theme.Verde

@Composable
fun Cargando() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        CircularProgressIndicator(color = Azul)
    }
}

@Composable
fun Mensaje(texto: String, alReintentar: (() -> Unit)? = null) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .background(Tarjeta, RoundedCornerShape(10.dp))
            .padding(20.dp)
    ) {
        Text(texto, color = TextoGris, fontFamily = Inter, textAlign = TextAlign.Center)
        if (alReintentar != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Reintentar",
                color = Azul,
                fontFamily = JetBrainsMono,
                modifier = Modifier
                    .background(Boton, RoundedCornerShape(6.dp))
                    .clickable { alReintentar() }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun EtiquetaDescuento(descuento: Int) {
    Text(
        text = "-$descuento%",
        color = Tarjeta,
        fontFamily = JetBrainsMono,
        fontSize = 12.sp,
        modifier = Modifier
            .background(Verde, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
fun PrecioTachado(texto: String) {
    Text(
        text = texto,
        color = TextoGris,
        fontFamily = JetBrainsMono,
        fontSize = 12.sp,
        maxLines = 1,
        textDecoration = TextDecoration.LineThrough
    )
}
