package fr.accoradd.gitspine.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable data object Welcome : Screen
    @Serializable data class Repository(val path: String) : Screen
    @Serializable data object Settings : Screen
}
