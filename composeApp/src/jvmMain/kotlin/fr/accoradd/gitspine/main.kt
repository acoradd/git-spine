package fr.accoradd.gitspine

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.application
import androidx.navigation.compose.rememberNavController
import fr.accoradd.gitspine.core.config.AppConfig
import fr.accoradd.gitspine.core.di.appModule
import fr.accoradd.gitspine.core.settings.Theme
import fr.accoradd.gitspine.domain.model.Project
import fr.accoradd.gitspine.ui.components.common.AppTitleBar
import fr.accoradd.gitspine.ui.components.dialogs.CloneRepositoryDialog
import fr.accoradd.gitspine.ui.navigation.AppNavigator
import fr.accoradd.gitspine.ui.theme.GitSpineTheme
import fr.accoradd.gitspine.ui.viewmodel.ProjectViewModel
import org.jetbrains.jewel.window.DecoratedWindow
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

fun main() = application {
    KoinApplication(application = {
        modules(appModule)
    }) {
        val navController = rememberNavController()
        var currentTheme by remember { mutableStateOf(Theme.SYSTEM) }
        var showCloneDialog by remember { mutableStateOf(false) }
        val navigator: AppNavigator = koinInject()
        val projectViewModel: ProjectViewModel = koinInject()

        LaunchedEffect(Unit) {
            navigator.cloneDialogRequested.collect {
                showCloneDialog = true
            }
        }

        GitSpineTheme(appTheme = currentTheme) {
            DecoratedWindow(
                onCloseRequest = ::exitApplication,
                title = AppConfig.APP_NAME
            ) {
                AppTitleBar(navigator = navigator)
                App(
                    navController = navController,
                    currentTheme = currentTheme,
                    onThemeChange = { currentTheme = it },
                    navigator = navigator
                )

                if (showCloneDialog) {
                    CloneRepositoryDialog(
                        onDismiss = { showCloneDialog = false },
                        onCloneSuccess = { path ->
                            showCloneDialog = false
                            val newProject = Project(path = path)
                            projectViewModel.addAndSetProject(newProject)
                            navigator.navigateToRepository(newProject)
                        }
                    )
                }
            }
        }
    }
}
