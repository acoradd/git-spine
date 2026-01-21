package fr.accoradd.gitspine.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.accoradd.gitspine.domain.model.Project
import fr.accoradd.gitspine.infrastructure.filesystem.FileDialogs
import fr.accoradd.gitspine.infrastructure.git.GitSession
import fr.accoradd.gitspine.ui.navigation.AppNavigator
import kotlinx.coroutines.launch
import java.nio.file.Path

class RepositoryScreenViewModel(
    private val gitSession: GitSession
) : ViewModel() {
    fun open(repoPath: Path) {
        val activeRepo = gitSession.setActiveRepo(repoPath)
        activeRepo.fileWatcher.watch()
    }

    fun close(path: Path) {
        gitSession.close(path)
    }

}
