package fr.accoradd.gitspine.domain.usecase.workspace

import fr.accoradd.gitspine.domain.model.Project
import fr.accoradd.gitspine.infrastructure.filesystem.FileDialogs
import fr.accoradd.gitspine.ui.navigation.AppNavigator
import fr.accoradd.gitspine.ui.viewmodel.ProjectViewModel

class OpenProjectUseCase(
    private val projectViewModel: ProjectViewModel,
    private val navigator: AppNavigator
) {
    suspend operator fun invoke() {
        val path = FileDialogs.openDirectory("Open Git Repository")
        if (path != null) {
            var newProject = Project(path = path)
            newProject = projectViewModel.addAndSetProject(newProject).project!!
            navigator.navigateToRepository(newProject)
        }
    }
}
