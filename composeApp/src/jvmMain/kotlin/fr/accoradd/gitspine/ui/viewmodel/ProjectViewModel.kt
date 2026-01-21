package fr.accoradd.gitspine.ui.viewmodel

import androidx.lifecycle.ViewModel
import fr.accoradd.gitspine.domain.model.Project
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet

data class ProjectState(
    val project: Project? = null,
    val recentsProject: List<Project> = emptyList(),
)

class ProjectViewModel() : ViewModel() {

    private val _state = MutableStateFlow(ProjectState())
    val state: StateFlow<ProjectState> = _state.asStateFlow()

    fun addAndSetProject(projectToSave: Project): ProjectState {
        if (projectToSave.path == state.value.project?.path && _state.value.recentsProject.any {it.path == projectToSave.path}) {
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
}
