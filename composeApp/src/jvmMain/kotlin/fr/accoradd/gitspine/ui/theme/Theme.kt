package fr.accoradd.gitspine.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import org.jetbrains.jewel.foundation.GlobalColors
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.*
import org.jetbrains.jewel.intui.window.decoratedWindow
import org.jetbrains.jewel.intui.window.styling.dark
import org.jetbrains.jewel.intui.window.styling.lightWithLightHeader
import org.jetbrains.jewel.ui.ComponentStyling
import org.jetbrains.jewel.window.styling.TitleBarStyle
import fr.accoradd.gitspine.core.settings.Theme as AppTheme

@Composable
fun GitSpineTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    content: @Composable () -> Unit
) {
    val textStyle = JewelTheme.createDefaultTextStyle()
    val editorStyle = JewelTheme.createEditorTextStyle()

    val isDark = when (appTheme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    // Couleurs personnalisées
    val defaultColors = if (isDark) GlobalColors.dark() else GlobalColors.light()
    val customColors = GlobalColors(
        borders = defaultColors.borders,
        outlines = defaultColors.outlines,
        text = defaultColors.text,
        panelBackground = if (isDark) IjDarkBg else IjLightBg,
        toolwindowBackground = if (isDark) IjDarkSurface else IjLightSurface
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
                TitleBarStyle.dark()
            } else {
                TitleBarStyle.lightWithLightHeader()
            },
        ),
        content = content
    )
}
