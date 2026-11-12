package me.kevinbayona.steamsalesapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import me.kevinbayona.steamsalesapp.ui.theme.Azul
import me.kevinbayona.steamsalesapp.ui.theme.Boton
import me.kevinbayona.steamsalesapp.ui.theme.Fondo
import me.kevinbayona.steamsalesapp.ui.theme.SpaceGrotesk
import me.kevinbayona.steamsalesapp.ui.theme.TextoBlanco
import me.kevinbayona.steamsalesapp.ui.theme.TextoGris
import me.kevinbayona.steamsalesapp.ui.theme.Verde

@Composable
fun BarraSuperior(titulo: String, avatar: String?, alTocarAvatar: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(Fondo)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.SportsEsports,
            contentDescription = null,
            tint = Azul,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = titulo,
            color = TextoBlanco,
            fontFamily = SpaceGrotesk,
            fontSize = 24.sp,
            modifier = Modifier.weight(1f)
        )

        Box(modifier = Modifier.clickable { alTocarAvatar() }) {
            if (avatar != null) {
                AsyncImage(
                    model = avatar,
                    contentDescription = "Perfil",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Boton)
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .background(Boton, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Filled.Person, contentDescription = "Perfil", tint = TextoBlanco)
                }
            }
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .align(Alignment.BottomEnd)
                    .background(if (avatar != null) Verde else TextoGris, CircleShape)
            )
        }
    }
}

@Preview
@Composable
fun BarraSuperiorPreview() {
    BarraSuperior(titulo = "Ajustes", avatar = null, alTocarAvatar = {})
}
