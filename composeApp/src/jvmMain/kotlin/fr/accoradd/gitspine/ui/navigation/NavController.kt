package fr.accoradd.gitspine.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * A simple navigator that handles back stack and screen transitions.
 */
class NavController(
    initialScreen: Screen,
    private val onExit: () -> Unit
) {
    private val backStack = mutableListOf(initialScreen)
    var currentScreen by mutableStateOf(initialScreen)

    fun navigateTo(screen: Screen) {
        if (screen != currentScreen) {
            backStack.add(screen)
            currentScreen = screen
        }
    }

    fun navigateBack() {
        if (backStack.size > 1) {
            backStack.removeLast()
            currentScreen = backStack.last()
        } else {
            onExit()
        }
    }
}

@Composable
fun rememberNavController(
    initialScreen: Screen,
    onExit: () -> Unit
): NavController = remember {
    NavController(initialScreen, onExit)
}
