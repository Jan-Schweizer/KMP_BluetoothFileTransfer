package de.schweizer.bft.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = darkColorScheme(
    primary = LightBlue,
    secondary = LightRed,
    tertiary = DarkBlue,
)

private val DarkColorScheme = lightColorScheme(
    primary = DarkBlue,
    secondary = DarkRed,
    tertiary = LightBlue,
)

@Composable
fun BftAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
