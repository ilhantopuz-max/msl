package com.okuyazma.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object Pastel {
    val Pink = Color(0xFFFFC2DC)
    val Lavender = Color(0xFFD7C4F5)
    val Mint = Color(0xFFBDEBD8)
    val Peach = Color(0xFFFFD9B8)
    val Sky = Color(0xFFBFE3F7)
    val Yellow = Color(0xFFFFF1A8)
    val Ink = Color(0xFF3B2A4A)
}

private val LightC = lightColorScheme(
    primary = Color(0xFFE0569A), secondary = Color(0xFF9B7FD4), tertiary = Color(0xFF4DB6A0),
    background = Color(0xFFFFF4F9), surface = Color(0xFFFFFFFF), onBackground = Pastel.Ink, onSurface = Pastel.Ink,
    secondaryContainer = Pastel.Lavender, surfaceVariant = Color(0xFFE6E0EC),
)
private val DarkC = darkColorScheme(
    primary = Color(0xFFFF8FB8), secondary = Color(0xFFC3A9F0), tertiary = Color(0xFF7FD8C4),
    background = Color(0xFF1E1B2E), surface = Color(0xFF2B2540), onBackground = Color(0xFFF3EAF7), onSurface = Color(0xFFF3EAF7),
    secondaryContainer = Pastel.Lavender, surfaceVariant = Color(0xFF3A3452),
)

/** mode: 0 açık, 1 koyu, 2 sistem */
@Composable
fun KidTheme(mode: Int, content: @Composable () -> Unit) {
    val dark = when (mode) { 0 -> false; 1 -> true; else -> isSystemInDarkTheme() }
    MaterialTheme(colorScheme = if (dark) DarkC else LightC, content = content)
}
