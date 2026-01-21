package fr.accoradd.gitspine.ui.navigation

import fr.accoradd.gitspine.domain.model.Project
import fr.accoradd.gitspine.ui.viewmodel.ProjectViewModel
import fr.accoradd.gitspine.ui.viewmodel.TitlebarViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AppNavigator(
    val titlebarViewModel: TitlebarViewModel,
    val projectViewModel: ProjectViewModel
) {
    private val _navigationEvents = MutableSharedFlow<Screen>(extraBufferCapacity = 1)
    val navigationEvents = _navigationEvents.asSharedFlow()

    fun back() {
        // Logique de retour arrière
    }

    fun navigateToRepository(projet: Project) {
        titlebarViewModel.setTitle(null);
        projectViewModel.addAndSetProject(projet);
        navigateTo(Screen.Repository(projet.path.toString()))
    }

    fun navigateToWelcome() {
        titlebarViewModel.setTitle(null);
        projectViewModel.clear();
        navigateTo(Screen.Welcome)
    }

    fun navigateToAddRepository() {
        navigateTo(Screen.AddRepository)
    }

    fun navigateToSettings() {
        navigateTo(Screen.Settings)
    }

    private fun navigateTo(screen: Screen) {
        _navigationEvents.tryEmit(screen)
    }
}
