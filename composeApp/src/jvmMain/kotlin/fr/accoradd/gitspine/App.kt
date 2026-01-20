package fr.accoradd.gitspine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import fr.accoradd.gitspine.core.settings.Theme
import fr.accoradd.gitspine.infrastructure.filesystem.FileDialogs
import fr.accoradd.gitspine.ui.navigation.NavController
import fr.accoradd.gitspine.ui.navigation.Screen
import fr.accoradd.gitspine.ui.screens.add.AddRepositoryScreen
import fr.accoradd.gitspine.ui.screens.repository.RepositoryScreen
import fr.accoradd.gitspine.ui.screens.settings.SettingsScreen
import fr.accoradd.gitspine.ui.screens.welcome.WelcomeScreen
import fr.accoradd.gitspine.ui.viewmodel.GraphViewModel
import kotlinx.coroutines.launch
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.koin.compose.koinInject

@Composable
fun App(
    navController: NavController,
    currentTheme: Theme,
    onThemeChange: (Theme) -> Unit,
) {
    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JewelTheme.globalColors.panelBackground) // Fond par défaut d'IntelliJ
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
                    onThemeChange = onThemeChange,
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
