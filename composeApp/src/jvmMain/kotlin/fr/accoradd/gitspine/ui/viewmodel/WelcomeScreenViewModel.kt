package fr.accoradd.gitspine.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.accoradd.gitspine.domain.model.Project
import fr.accoradd.gitspine.infrastructure.filesystem.FileDialogs
import fr.accoradd.gitspine.ui.navigation.AppNavigator
import kotlinx.coroutines.launch

class WelcomeScreenViewModel(
    private val navigator: AppNavigator
    // private val openRepositoryUseCase: OpenRepositoryUseCase // Plus tard
) : ViewModel() {

    fun openRepository() {
        viewModelScope.launch {
            // Note: FileDialogs devrait idéalement être injecté ou géré via une interface
            val path = FileDialogs.openDirectory("Open Git Repository")
            if (path != null) {
                navigator.navigateToRepository(Project(path = path))
            }
        }
    }

    fun goToClone() {
        navigator.navigateToAddRepository()
    }
}
