package fr.accoradd.gitspine.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.accoradd.gitspine.domain.model.Project
import fr.accoradd.gitspine.domain.usecase.workspace.CreateBranchUseCase
import fr.accoradd.gitspine.domain.usecase.workspace.FetchUseCase
import fr.accoradd.gitspine.domain.usecase.workspace.PullUseCase
import fr.accoradd.gitspine.domain.usecase.workspace.PushUseCase
import fr.accoradd.gitspine.domain.usecase.workspace.StashUseCase
import fr.accoradd.gitspine.domain.usecase.workspace.UnStashUseCase
import fr.accoradd.gitspine.infrastructure.filesystem.FileDialogs
import fr.accoradd.gitspine.ui.navigation.AppNavigator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProjectState(
    val project: Project? = null,
    val recentsProject: List<Project> = emptyList(),
)

class ProjectViewModel(
    private val fetchUseCase: FetchUseCase,
    private val pullUseCase: PullUseCase,
    private val pushUseCase: PushUseCase,
    private val stashUseCase: StashUseCase,
    private val unstashUseCase: UnStashUseCase,
    private val createBranchUseCase: CreateBranchUseCase
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

    fun fetch() {
        viewModelScope.launch {
            fetchUseCase.invoke()
        }
    }

    fun pull() {
        viewModelScope.launch {
            pullUseCase.invoke()
        }
    }

    fun push() {
        viewModelScope.launch {
            // TODO open modal to select where to push if no distant branch
//            pushUseCase.invoke()
        }
    }

    fun stash() {
        viewModelScope.launch {
            stashUseCase.invoke()
        }
    }

    fun unstash() {
        viewModelScope.launch {
            // todo open modal to select
//            unstashUseCase.invoke("")
        }
    }

    fun createBranch() {
        viewModelScope.launch {
            // todo open modal to select
//            createBranchUseCase.invoke("")
        }
    }
}
