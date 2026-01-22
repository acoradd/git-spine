package fr.accoradd.gitspine.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.accoradd.gitspine.core.notifications.NotificationManager
import fr.accoradd.gitspine.domain.model.Project
import fr.accoradd.gitspine.domain.usecase.workspace.CloneUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.nio.file.Path

data class ProjectState(
    val project: Project? = null,
    val recentsProject: List<Project> = emptyList(),
)

class ProjectViewModel(
    private val cloneUseCase: CloneUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProjectState())
    val state: StateFlow<ProjectState> = _state.asStateFlow()

    fun addAndSetProject(projectToSave: Project): ProjectState {
        if (projectToSave.path == state.value.project?.path && _state.value.recentsProject.any { it.path == projectToSave.path }) {
            return _state.value
        } else {
            return _state.updateAndGet {
                val project = it.recentsProject.find { it.path == projectToSave.path } ?: projectToSave
                val recentsProject = listOf(project) + it.recentsProject.filter { it.path != project.path }
                it.copy(project = project, recentsProject = recentsProject)
            }
        }
    }

    fun clear() {
        _state.update { it.copy(project = null) }
    }

    fun cloneRepository(path: Path, url: String) {
        viewModelScope.launch {
            cloneUseCase.invoke(path, url)
        }
    }
}
