package fr.accoradd.gitspine.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import coil3.ImageLoader
import coil3.compose.LocalPlatformContext
import fr.accoradd.gitspine.infrastructure.image.ImageLoaderFactory
import fr.accoradd.gitspine.infrastructure.image.ImageManager
import org.jetbrains.jewel.foundation.GlobalColors
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.*
import org.jetbrains.jewel.intui.window.decoratedWindow
import org.jetbrains.jewel.intui.window.styling.dark
import org.jetbrains.jewel.intui.window.styling.lightWithLightHeader
import org.jetbrains.jewel.ui.ComponentStyling
import org.jetbrains.jewel.window.styling.TitleBarColors
import org.jetbrains.jewel.window.styling.TitleBarStyle
import fr.accoradd.gitspine.core.settings.Theme as AppTheme

val LocalImageLoader = compositionLocalOf<ImageLoader> {
    error("ImageLoader not provided")
}

val LocalImageManager = compositionLocalOf<ImageManager> {
    error("ImageManager not provided")
}

@Composable
fun AppTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    isSystemDark: Boolean,
    content: @Composable () -> Unit
) {
    val textStyle = JewelTheme.createDefaultTextStyle()
    val editorStyle = JewelTheme.createEditorTextStyle()


    val context = LocalPlatformContext.current
    val imageLoader = remember { ImageLoaderFactory.create(context) }
    val imageManager = remember { ImageManager() }

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


    CompositionLocalProvider(
        LocalImageLoader provides imageLoader,
        LocalImageManager provides imageManager
    ) {
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
}
