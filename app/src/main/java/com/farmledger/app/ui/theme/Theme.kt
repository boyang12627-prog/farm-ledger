package com.farmledger.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Pixel farm palette (像素美術管家)
val FarmBg = Color(0xFFFFF8E7)
val FarmStroke = Color(0xFF6B4A2E)
val FarmText = Color(0xFF6B4A2E)
val FarmGrowth = Color(0xFF8FBF6A)
val FarmIncome = Color(0xFF4F7A45)
val FarmExpense = Color(0xFFC75B39)
val FarmSelected = Color(0xFFE8A838) // replaces pink
val FarmSoil = Color(0xFF8B6914)

// Aliases for existing call sites / older names
val SoilBrown = FarmSoil
val WarmCream = FarmBg
val LeafGreen = FarmGrowth
val SoftTerracotta = FarmExpense
val SkyMist = Color(0xFFA8C5D4)
val PixelGold = FarmSelected
val InkBrown = FarmText

private val LightColors = lightColorScheme(
    primary = FarmGrowth,
    onPrimary = Color.White,
    secondary = FarmSelected,
    onSecondary = FarmText,
    tertiary = FarmIncome,
    background = FarmBg,
    onBackground = FarmText,
    surface = Color(0xFFFFFBF0),
    onSurface = FarmText,
    surfaceVariant = Color(0xFFE8D9B5),
    primaryContainer = Color(0xFFD4E8C0),
    onPrimaryContainer = FarmText,
    secondaryContainer = Color(0xFFF5D9A0),
    onSecondaryContainer = FarmText,
    error = FarmExpense,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = FarmGrowth,
    onPrimary = Color.White,
    secondary = FarmSelected,
    background = Color(0xFF2A2418),
    onBackground = FarmBg,
    surface = Color(0xFF3A3224),
    onSurface = FarmBg
)

@Composable
fun FarmLedgerTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        content = content
    )
}
