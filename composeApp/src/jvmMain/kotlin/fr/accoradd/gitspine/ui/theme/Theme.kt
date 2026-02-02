package fr.accoradd.gitspine.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.GlobalColors
import org.jetbrains.jewel.foundation.GlobalMetrics
import org.jetbrains.jewel.foundation.GlobalMetrics.Companion
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.*
import org.jetbrains.jewel.intui.window.decoratedWindow
import org.jetbrains.jewel.intui.window.styling.dark
import org.jetbrains.jewel.intui.window.styling.lightWithLightHeader
import org.jetbrains.jewel.ui.ComponentStyling
import org.jetbrains.jewel.window.styling.TitleBarColors
import org.jetbrains.jewel.window.styling.TitleBarStyle
import fr.accoradd.gitspine.core.settings.Theme as AppTheme

@Composable
fun AppTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    isSystemDark: Boolean,
    content: @Composable () -> Unit
) {
    val textStyle = JewelTheme.createDefaultTextStyle()
    val editorStyle = JewelTheme.createEditorTextStyle()

    val isDark = when (appTheme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> isSystemDark
    }

    // Couleurs personnalisées
    val themeColors = if (isDark) AppDarkThemeColors else AppLightThemeColors
    val customColors = if (isDark) GlobalColors.dark(
        panelBackground = themeColors.panel.bg,
        toolwindowBackground = themeColors.toolwindow.bg
    ) else GlobalColors.light(
        panelBackground = themeColors.panel.bg,
        toolwindowBackground = themeColors.toolwindow.bg
    )

    val theme = if (isDark) {
        JewelTheme.darkThemeDefinition(
            colors = customColors,
            defaultTextStyle = textStyle,
            editorTextStyle = editorStyle
        )
    } else {
        JewelTheme.lightThemeDefinition(
            colors = customColors,
            defaultTextStyle = textStyle,
            editorTextStyle = editorStyle
        )
    }

    IntUiTheme(
        theme = theme,
        styling = ComponentStyling.default().decoratedWindow(
            titleBarStyle = if (isDark) {
                TitleBarStyle.dark(
                    colors = TitleBarColors.dark(
                        backgroundColor = themeColors.bar.bg,
                        borderColor = themeColors.bar.bg,
                    )
                )
            } else {
                TitleBarStyle.lightWithLightHeader(
                    colors = TitleBarColors.lightWithLightHeader(
                        backgroundColor = themeColors.bar.bg,
                        borderColor = themeColors.bar.bg
                    )
                )
            },
        ),
        content = content
    )
}
