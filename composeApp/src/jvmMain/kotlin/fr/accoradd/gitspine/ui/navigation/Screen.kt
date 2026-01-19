package fr.accoradd.gitspine.ui.navigation

import java.nio.file.Path

/**
 * Represents the different screens in the application for navigation.
 */
sealed interface Screen {
    data object Welcome : Screen
    data class Repository(val path: Path) : Screen
    data object Settings : Screen
    data object AddRepository : Screen
}
