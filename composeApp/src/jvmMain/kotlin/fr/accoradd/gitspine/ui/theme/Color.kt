package fr.accoradd.gitspine.ui.theme

import androidx.compose.ui.graphics.Color

data class AppThemeColorsBar(
    val bg: Color
)

data class AppThemeColorsPanel(
    val bg: Color
)

data class AppThemeColorsToolwindow(
    val bg: Color
)

data class AppThemeColors(
    val bar: AppThemeColorsBar,
    val panel: AppThemeColorsPanel,
    val toolwindow: AppThemeColorsToolwindow
)

val AppLightThemeColors = AppThemeColors(
    bar = AppThemeColorsBar(
        bg = Color(0xFFEBECF0)
    ),
    panel = AppThemeColorsPanel(
        bg = Color(0xFFFFFFFF)
    ),
    toolwindow = AppThemeColorsToolwindow(
        bg = Color(0xFFF7F8FA)
    )
)

val AppDarkThemeColors = AppThemeColors(
    bar = AppThemeColorsBar(
        bg = Color(0xFF26282B)
    ),
    panel = AppThemeColorsPanel(
        bg = Color(0xFF1E1F22)
    ),
    toolwindow = AppThemeColorsToolwindow(
        bg = Color(0xFF26282B)
    )
)

val IjPanelBg = Color(0xFFF7F8FA)

// --- IntelliJ New UI: Light Theme ---
val IjLightBg = Color(0xFFF7F8FA)
val IjLightSurface = Color(0xFFFFFFFF)
val IjLightBorder = Color(0xFFEBECF0)
val IjLightText = Color(0xFF3C3F41)
val IjLightTextSecondary = Color(0xFF767A7C)
val IjLightHover = Color(0xFFEAF2FF)

// --- IntelliJ New UI: Dark Theme ---
val IjDarkBg = Color(0xFF26282B)
val IjDarkSurface = Color(0xFF191A1C)
val IjDarkBorder = Color(0xFF26282B)
val IjDarkText = Color(0xFFDFE1E5)
val IjDarkTextSecondary = Color(0xFF8C9196)
val IjDarkHover = Color(0xFF323B4F)

// --- Accent Colors ---
val IjBlue = Color(0xFF3574F0)
val IjBlueOnContainer = Color(0xFFA8C5FF)
val IjYellow = Color(0xFFFFD600) // Using the same yellow for consistency
val IjRed = Color(0xFFD50000)     // Using the same red for consistency
val White = Color(0xFFFFFFFF)
