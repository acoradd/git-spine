package fr.accoradd.gitspine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import fr.accoradd.gitspine.core.settings.Theme
import fr.accoradd.gitspine.ui.navigation.AppNavigator
import fr.accoradd.gitspine.ui.navigation.Screen
import fr.accoradd.gitspine.ui.screens.repository.RepositoryScreen
import fr.accoradd.gitspine.ui.screens.settings.SettingsScreen
import fr.accoradd.gitspine.ui.screens.welcome.WelcomeScreen
import org.jetbrains.jewel.foundation.theme.JewelTheme

@Composable
fun App(
    navController: NavHostController,
    navigator: AppNavigator
) {
    LaunchedEffect(Unit) {
        navigator.navigationEvents.collect { route ->
            navController.navigate(route)
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JewelTheme.globalColors.panelBackground) // Fond par défaut d'IntelliJ
    ) {
        NavHost(navController = navController, startDestination = Screen.Welcome) {
            composable<Screen.Welcome> { WelcomeScreen() }
            composable<Screen.Repository> { backStackEntry ->
                val repositoryRoute = backStackEntry.toRoute<Screen.Repository>()
                RepositoryScreen(path = repositoryRoute.path)
            }
            composable<Screen.Settings> {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
