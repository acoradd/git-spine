package fr.accoradd.gitspine

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import fr.accoradd.gitspine.core.settings.Theme
import fr.accoradd.gitspine.infrastructure.filesystem.FileDialogs
import fr.accoradd.gitspine.ui.navigation.NavController
import fr.accoradd.gitspine.ui.navigation.Screen
import fr.accoradd.gitspine.ui.screens.add.AddRepositoryScreen
import fr.accoradd.gitspine.ui.screens.repository.RepositoryScreen
import fr.accoradd.gitspine.ui.screens.settings.SettingsScreen
import fr.accoradd.gitspine.ui.screens.welcome.WelcomeScreen
import fr.accoradd.gitspine.ui.theme.GitSpineTheme
import fr.accoradd.gitspine.ui.viewmodel.GraphViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun App(
    navController: NavController,
    onCloseRequest: () -> Unit
) {
    var currentTheme by remember { mutableStateOf(Theme.SYSTEM) }
    val scope = rememberCoroutineScope()

    GitSpineTheme(appTheme = currentTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (val screen = navController.currentScreen) {
                is Screen.Welcome -> {
                    WelcomeScreen(
                        onOpenRepository = {
                            scope.launch {
                                val path = FileDialogs.openDirectory("Open Git Repository")
                                if (path != null) {
                                    navController.navigateTo(Screen.Repository(path))
                                }
                            }
                        },
                        onCloneRepository = { navController.navigateTo(Screen.AddRepository) }
                    )
                }
                is Screen.Repository -> {
                    val graphViewModel: GraphViewModel = koinInject()
                    RepositoryScreen(
                        viewModel = graphViewModel,
                        navController = navController
                    )
                }
                is Screen.Settings -> {
                    SettingsScreen(
                        currentTheme = currentTheme,
                        onThemeChange = { currentTheme = it },
                        onBack = { navController.navigateBack() }
                    )
                }
                is Screen.AddRepository -> {
                    AddRepositoryScreen(
                        onBack = { navController.navigateBack() },
                        onCloneSuccess = { path ->
                            navController.navigateTo(Screen.Repository(path))
                        }
                    )
                }
            }
        }
    }
}
