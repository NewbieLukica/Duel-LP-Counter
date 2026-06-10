package com.example.duellpcounter.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DuelColorScheme = darkColorScheme(
    primary = Player1Purple,
    secondary = Player2Blue,
    tertiary = GoldAccent,
    background = DarkCharcoal,
    surface = SurfacePanel,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = TextWhite,
    onSurface = TextWhite,
    error = ResetRed
)

@Composable
fun DuelLPCounterTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DuelColorScheme,
        typography = Typography,
        content = content
    )
}
