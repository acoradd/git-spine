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
    val branchBg: Color,
    val bg: Color,
)

data class GraphColorsAlpha(
    val alphaBorder: Float,
    val alphaBranchBg: Float,
    val alphaBg: Float,
    val colors: List<Color>
) {
    val size: Int get() = colors.size
}

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
    GraphColors(Color(0xFF30CE9E), Color(0xFFC9F1E4), Color(0xFFE7F7F2)),
    GraphColors(Color(0xFF17A1C0), Color(0xFFC2E5ED), Color(0xFFE4F2F5)),
    GraphColors(Color(0xFF237BF8), Color(0xFFC0D7FB), Color(0xFFE3EDFB)),
    GraphColors(Color(0xFF8F02C2), Color(0xFFE1BEEE), Color(0xFFF0E2F6)),
    GraphColors(Color(0xFFCF43C3), Color(0xFFEEC3EB), Color(0xFFF6E4F5)),
    GraphColors(Color(0xFFD90372), Color(0xFFF3BED9), Color(0xFFF8E2EE)),
    GraphColors(Color(0xFFDA4646), Color(0xFFF0BEBE), Color(0xFFF7E2E2)),
    GraphColors(Color(0xFFF25E30), Color(0xFFFAD4C9), Color(0xFFFBEBE7)),
    GraphColors(Color(0xFFF2CA35), Color(0xFFFAF0CA), Color(0xFFFBF7E7)),
    GraphColors(Color(0xFF7CD93A), Color(0xFFDCF3CB), Color(0xFFEFF8E8)),
)

val graphColorsAlpha = GraphColorsAlpha(
    1f, 0.2f, 0.1f,
    listOf(
        Color(0xFF30CE9E),
        Color(0xFF17A1C0),
        Color(0xFF237BF8),
        Color(0xFF8F02C2),
        Color(0xFFCF43C3),
        Color(0xFFD90372),
        Color(0xFFDA4646),
        Color(0xFFF25E30),
        Color(0xFFF2CA35),
        Color(0xFF7CD93A)
    )
)
