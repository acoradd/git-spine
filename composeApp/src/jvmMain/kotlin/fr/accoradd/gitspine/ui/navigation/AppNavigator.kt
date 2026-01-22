package fr.accoradd.gitspine.ui.navigation

import fr.accoradd.gitspine.domain.model.Project
import fr.accoradd.gitspine.ui.viewmodel.ProjectViewModel
import fr.accoradd.gitspine.ui.viewmodel.TitlebarViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AppNavigator(
    val projectViewModel: ProjectViewModel
) {
    private val _navigationEvents = MutableSharedFlow<Screen>(extraBufferCapacity = 1)
    val navigationEvents = _navigationEvents.asSharedFlow()

    private val _cloneDialogRequested = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val cloneDialogRequested = _cloneDialogRequested.asSharedFlow()

    fun back() {
        // Logique de retour arrière
    }

    fun navigateToRepository(projet: Project) {
        projectViewModel.addAndSetProject(projet);
        navigateTo(Screen.Repository(projet.path.toString()))
    }

    fun navigateToWelcome() {
        projectViewModel.clear();
        navigateTo(Screen.Welcome)
    }

    fun openCloneDialog() {
        _cloneDialogRequested.tryEmit(Unit)
    }

    fun navigateToSettings() {
        navigateTo(Screen.Settings)
    }

    private fun navigateTo(screen: Screen) {
        _navigationEvents.tryEmit(screen)
    }
}
