package com.farmledger.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Warm pixel palette placeholders
val SoilBrown = Color(0xFF8B6914)
val WarmCream = Color(0xFFF4E8C8)
val LeafGreen = Color(0xFF6B8F4E)
val SoftTerracotta = Color(0xFFC4785A)
val SkyMist = Color(0xFFA8C5D4)
val PixelGold = Color(0xFFD4A84B)
val InkBrown = Color(0xFF3E2F1C)

private val LightColors = lightColorScheme(
    primary = LeafGreen,
    onPrimary = Color.White,
    secondary = SoftTerracotta,
    onSecondary = Color.White,
    tertiary = PixelGold,
    background = WarmCream,
    onBackground = InkBrown,
    surface = Color(0xFFFFF8EC),
    onSurface = InkBrown,
    surfaceVariant = Color(0xFFE8D9B5),
    primaryContainer = Color(0xFFC5D9A8),
    secondaryContainer = Color(0xFFF0C9B8)
)

private val DarkColors = darkColorScheme(
    primary = LeafGreen,
    onPrimary = Color.White,
    secondary = SoftTerracotta,
    background = Color(0xFF2A2418),
    onBackground = WarmCream,
    surface = Color(0xFF3A3224),
    onSurface = WarmCream
)

@Composable
fun FarmLedgerTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        content = content
    )
}
