package com.renovartech.soundapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Cores da marca Renovar Tech (as mesmas usadas no site/app web)
val RenovarTeal = Color(0xFF497073)
val RenovarOrange = Color(0xFFFF3300)
val RenovarBackground = Color(0xFFF4F6F6)

private val LightColors = lightColorScheme(
    primary = RenovarTeal,
    secondary = RenovarOrange,
    background = RenovarBackground,
    surface = Color.White
)

private val DarkColors = darkColorScheme(
    primary = RenovarTeal,
    secondary = RenovarOrange,
    background = Color(0xFF181F1F),
    surface = Color(0xFF222222)
)

@Composable
fun RenovarTechSoundAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
