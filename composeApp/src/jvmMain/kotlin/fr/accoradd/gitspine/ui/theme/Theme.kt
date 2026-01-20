package fr.accoradd.gitspine.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import fr.accoradd.gitspine.core.settings.Theme as AppTheme
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme

@Composable
fun GitSpineTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (appTheme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    IntUiTheme(
        isDark = isDark,
        content = content
    )
}
