package fr.accoradd.gitspine.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.accoradd.gitspine.domain.model.WorkspaceStatus
import fr.accoradd.gitspine.domain.repository.GitRepository
import fr.accoradd.gitspine.domain.usecase.workspace.*
import fr.accoradd.gitspine.infrastructure.git.JGitRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class WorkspaceState(
    val status: WorkspaceStatus = WorkspaceStatus(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class WorkspaceViewModel(
    private val gitRepository: GitRepository,
    private val getStatusUseCase: GetStatusUseCase,
    private val stageFileUseCase: StageFileUseCase,
    private val unstageFileUseCase: UnstageFileUseCase,
    private val stageAllUseCase: StageAllUseCase,
    private val unstageAllUseCase: UnstageAllUseCase,
    private val discardChangesUseCase: DiscardChangesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(WorkspaceState())
    val state: StateFlow<WorkspaceState> = _state.asStateFlow()

    init {
        // Listen to file watcher events
        viewModelScope.launch {
            (gitRepository as? JGitRepository)?.statusUpdates?.collect {
                loadStatus()
            }
        }
    }

    fun loadStatus() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val status = getStatusUseCase()
                _state.update { it.copy(status = status, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun stageFile(path: String) = executeOperation { stageFileUseCase(path) }

    fun unstageFile(path: String) = executeOperation { unstageFileUseCase(path) }

    fun stageAll() = executeOperation { stageAllUseCase() }

    fun unstageAll() = executeOperation { unstageAllUseCase() }

    fun discardChanges(path: String, staged: Boolean) =
        executeOperation { discardChangesUseCase(path, staged) }

    private fun executeOperation(operation: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(error = null) }
                operation()
                loadStatus()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }
}
