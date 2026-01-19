package fr.accoradd.gitspine

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import fr.accoradd.gitspine.core.config.AppConfig
import fr.accoradd.gitspine.core.di.appModule
import fr.accoradd.gitspine.ui.navigation.Screen
import fr.accoradd.gitspine.ui.navigation.rememberNavController
import org.koin.compose.KoinApplication

fun main() = application {
    KoinApplication(application = {
        modules(appModule)
    }) {
        val navController = rememberNavController(
            initialScreen = Screen.Welcome,
            onExit = ::exitApplication
        )

        Window(
            onCloseRequest = ::exitApplication,
            title = AppConfig.APP_NAME,
        ) {
            App(
                navController = navController,
                onCloseRequest = ::exitApplication
            )
        }
    }
}
