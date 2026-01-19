package fr.accoradd.gitspine.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import fr.accoradd.gitspine.core.settings.Theme as AppTheme

private val LightColorScheme = lightColorScheme(
    primary = IjBlue,
    onPrimary = White,
    primaryContainer = IjLightHover, // Utiliser la couleur de survol pour la sélection
    onPrimaryContainer = IjLightText,
    
    secondary = IjBlue,
    onSecondary = White,
    
    tertiary = IjYellow,
    onTertiary = IjLightText,
    
    background = IjLightBg,
    onBackground = IjLightText,
    
    surface = IjLightSurface,
    onSurface = IjLightText,
    
    surfaceVariant = IjLightBg,
    onSurfaceVariant = IjLightTextSecondary,
    
    surfaceContainerLow = IjLightSurface,
    surfaceContainer = IjLightSurface,
    surfaceContainerHigh = IjLightHover,
    surfaceContainerHighest = IjLightHover,
    
    error = IjRed,
    onError = White,
    
    outline = IjLightBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = IjBlue,
    onPrimary = White,
    primaryContainer = IjDarkHover, // Utiliser la couleur de survol pour la sélection
    onPrimaryContainer = IjDarkText,

    secondary = IjBlue,
    onSecondary = White,

    tertiary = IjYellow,
    onTertiary = IjDarkText,

    background = IjDarkBg,
    onBackground = IjDarkText,

    surface = IjDarkSurface,
    onSurface = IjDarkText,

    surfaceVariant = IjDarkBg,
    onSurfaceVariant = IjDarkTextSecondary,
    
    surfaceContainerLow = IjDarkSurface,
    surfaceContainer = IjDarkSurface,
    surfaceContainerHigh = IjDarkHover,
    surfaceContainerHighest = IjDarkHover,

    error = IjRed,
    onError = White,

    outline = IjDarkBorder
)

@Composable
fun GitSpineTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (appTheme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
