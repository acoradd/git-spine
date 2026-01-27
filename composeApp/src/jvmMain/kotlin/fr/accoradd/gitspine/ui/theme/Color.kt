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

data class GraphColors(
    val border: Color,
    val bg: Color,
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



val graphColors = listOf(
    GraphColors(Color(0xFF17A1C0), Color(0xFFC2E5ED)),
    GraphColors(Color(0xFF237BF8), Color(0xFFC0D7FB)),
    GraphColors(Color(0xFF8F02C2), Color(0xFFE1BEEE)),
    GraphColors(Color(0xFFCF43C3), Color(0xFFEEC3EB)),
    GraphColors(Color(0xFFD90372), Color(0xFFF3BED9)),
    GraphColors(Color(0xFFDA4646), Color(0xFFF0BEBE)),
    GraphColors(Color(0xFFF25E30), Color(0xFFFAD4C9)),
    GraphColors(Color(0xFFF2CA35), Color(0xFFFAF0CA)),
    GraphColors(Color(0xFF7CD93A), Color(0xFFDCF3CB)),
    GraphColors(Color(0xFF30CE9E), Color(0xFFC9F1E4)),
)
