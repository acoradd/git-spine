package fr.accoradd.gitspine.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.accoradd.gitspine.domain.model.Project
import fr.accoradd.gitspine.domain.model.RecentRepository
import fr.accoradd.gitspine.domain.repository.RecentRepositoryStore
import fr.accoradd.gitspine.domain.usecase.workspace.CreateBranchUseCase
import fr.accoradd.gitspine.domain.usecase.workspace.FetchUseCase
import fr.accoradd.gitspine.domain.usecase.workspace.PullUseCase
import fr.accoradd.gitspine.domain.usecase.workspace.PushUseCase
import fr.accoradd.gitspine.domain.usecase.workspace.StashUseCase
import fr.accoradd.gitspine.domain.usecase.workspace.UnStashUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.nio.file.Path

data class ProjectState(
    val project: Project? = null,
    val recentsProject: List<Project> = emptyList(),
)

class ProjectViewModel(
    private val recentRepositoryStore: RecentRepositoryStore,
    private val fetchUseCase: FetchUseCase,
    private val pullUseCase: PullUseCase,
    private val pushUseCase: PushUseCase,
    private val stashUseCase: StashUseCase,
    private val unstashUseCase: UnStashUseCase,
    private val createBranchUseCase: CreateBranchUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProjectState())
    val state: StateFlow<ProjectState> = _state.asStateFlow()

    init {
        // Load recent repositories from persistent storage
        viewModelScope.launch {
            recentRepositoryStore.recentRepositories.collect { recentRepos ->
                val projects = recentRepos.map { repo ->
                    Project(
                        path = Path.of(repo.path),
                        name = repo.name
                    )
                }
                _state.update { it.copy(recentsProject = projects) }
            }
        }
    }

    fun addAndSetProject(projectToSave: Project): ProjectState {
        // Persist to storage
        viewModelScope.launch {
            recentRepositoryStore.addOrUpdate(
                RecentRepository.create(
                    path = projectToSave.path.toString(),
                    name = projectToSave.name
                )
            )
        }

        // Update current project immediately
        return _state.updateAndGet {
            it.copy(project = projectToSave)
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
