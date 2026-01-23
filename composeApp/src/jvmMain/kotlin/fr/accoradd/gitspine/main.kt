package fr.accoradd.gitspine

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.application
import androidx.navigation.compose.rememberNavController
import fr.accoradd.gitspine.core.config.AppConfig
import fr.accoradd.gitspine.core.di.appModule
import fr.accoradd.gitspine.core.notifications.NotificationManager
import fr.accoradd.gitspine.core.settings.Settings
import fr.accoradd.gitspine.core.settings.Theme
import fr.accoradd.gitspine.infrastructure.system.SystemThemeDetector
import fr.accoradd.gitspine.ui.components.common.AppBottomBar
import fr.accoradd.gitspine.ui.components.common.AppTitleBar
import fr.accoradd.gitspine.ui.components.dialogs.CloneRepositoryDialog
import fr.accoradd.gitspine.ui.components.notifications.NotificationsContainer
import fr.accoradd.gitspine.ui.navigation.AppNavigator
import fr.accoradd.gitspine.ui.theme.AppTheme
import fr.accoradd.gitspine.ui.viewmodel.AppViewModel
import org.jetbrains.jewel.window.DecoratedWindow
import org.jetbrains.skiko.currentSystemTheme
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

fun main() = application {
    KoinApplication(application = {
        modules(appModule)
    }) {
        val navController = rememberNavController()
        val settings: Settings = koinInject()
        val navigator: AppNavigator = koinInject()
        val appViewModel: AppViewModel = koinInject()
        val systemThemeDetector: SystemThemeDetector = koinInject()
        val notificationManager: NotificationManager = koinInject()

        val currentTheme = settings.theme.collectAsState(Theme.SYSTEM)
        val isSystemDark = systemThemeDetector.isDarkTheme.collectAsState()
        var showCloneDialog by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            systemThemeDetector.startListening(this)
            navigator.cloneDialogRequested.collect {
                showCloneDialog = true
            }
        }

        AppTheme(appTheme = currentTheme.value, isSystemDark = isSystemDark.value) {
            DecoratedWindow(
                onCloseRequest = ::exitApplication,
                title = AppConfig.APP_NAME
            ) {
                AppTitleBar(navigator = navigator, navController = navController)
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        App(
                            navController = navController,
                            navigator = navigator
                        )

                        NotificationsContainer(notificationManager = notificationManager)
                    }

                    AppBottomBar(notificationManager = notificationManager)
                }

                if (showCloneDialog) {
                    CloneRepositoryDialog(
                        onDismiss = { showCloneDialog = false },
                        onClone = { path, url ->
                            showCloneDialog = false
                            appViewModel.cloneRepository(path, url)
                        }
                    )
                }
            }
        }
    }
}
