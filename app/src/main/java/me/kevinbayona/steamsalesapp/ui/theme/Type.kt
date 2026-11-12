package me.kevinbayona.steamsalesapp.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import me.kevinbayona.steamsalesapp.R

// Los archivos .ttf están en res/font

// Títulos
val SpaceGrotesk = FontFamily(
    Font(R.font.space_grotesk_bold, FontWeight.Bold)
)

// Texto normal
val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_semibold, FontWeight.SemiBold)
)

// Etiquetas pequeñas
val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono_bold, FontWeight.Bold)
)
