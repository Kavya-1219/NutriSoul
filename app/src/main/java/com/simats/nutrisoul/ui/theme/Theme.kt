package com.simats.nutrisoul.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    background = LightGreenBackground,
    onBackground = TitleColor,
    onSurfaceVariant = SubTextColor
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    background = LightGreenBackground,
    onBackground = TitleColor,
    onSurfaceVariant = SubTextColor
)

@Composable
fun NutriSoulTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}