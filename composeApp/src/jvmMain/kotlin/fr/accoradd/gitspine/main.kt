package fr.accoradd.gitspine

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import fr.accoradd.gitspine.core.config.AppConfig
import fr.accoradd.gitspine.core.di.appModule
import org.koin.compose.KoinApplication

fun main() = application {
    KoinApplication(application = {
        modules(appModule)
    }) {
        Window(
            onCloseRequest = ::exitApplication,
            title = AppConfig.APP_NAME,
        ) {
            App()
        }
    }
}
