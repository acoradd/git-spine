package fr.accoradd.gitspine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.application
import fr.accoradd.gitspine.core.config.AppConfig
import fr.accoradd.gitspine.core.di.appModule
import fr.accoradd.gitspine.core.settings.Theme
import fr.accoradd.gitspine.ui.components.common.AppTitleBar
import fr.accoradd.gitspine.ui.navigation.Screen
import fr.accoradd.gitspine.ui.navigation.rememberNavController
import fr.accoradd.gitspine.ui.theme.GitSpineTheme
import org.jetbrains.jewel.window.DecoratedWindow
import org.koin.compose.KoinApplication

fun main() = application {
    KoinApplication(application = {
        modules(appModule)
    }) {
        val navController = rememberNavController(
            initialScreen = Screen.Welcome,
            onExit = ::exitApplication
        )
        var currentTheme by remember { mutableStateOf(Theme.SYSTEM) }

        GitSpineTheme(appTheme = currentTheme) {
            DecoratedWindow(
                onCloseRequest = ::exitApplication,
                title = AppConfig.APP_NAME
            ) {
                AppTitleBar()
                App(
                    navController = navController,
                    currentTheme = currentTheme,
                    onThemeChange = { currentTheme = it }
                )
            }
        }
    }
}
