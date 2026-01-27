package fr.accoradd.gitspine.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.accoradd.gitspine.domain.model.Branch
import fr.accoradd.gitspine.domain.model.Commit
import fr.accoradd.gitspine.domain.model.CommitOrWip
import fr.accoradd.gitspine.domain.model.GraphResult
import fr.accoradd.gitspine.domain.repository.GitRepository
import fr.accoradd.gitspine.domain.usecase.graph.GraphUseCase
import fr.accoradd.gitspine.infrastructure.git.GitSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.nio.file.Path

class RepositoryScreenViewModel(
    private val gitSession: GitSession,
    private val gitRepository: GitRepository,
    private val graphUseCase: GraphUseCase
) : ViewModel() {

    private val _localBranches = MutableStateFlow<List<Branch>>(emptyList())
    private val _remoteBranches = MutableStateFlow<List<Branch>>(emptyList())
    private val _tags = MutableStateFlow<List<String>>(emptyList())

    private val _commits = MutableStateFlow<List<Commit>>(emptyList())
    private val _commit = MutableStateFlow<CommitOrWip?>(null)
    private val _graphResult = MutableStateFlow<GraphResult?>(null)


    private val _searchQuery = MutableStateFlow("")
    private val _hasMoreRemoteBranch = MutableStateFlow(false)
    private val _hasMoreTags = MutableStateFlow(false)
    private val _hasMoreCommits = MutableStateFlow(false)

    private var isLoadingLocalBranch = false
    private var isLoadingRemoteBranch = false
    private var isLoadingTag = false
    private var isLoadingCommit = false

    val localBranches: StateFlow<List<Branch>> = _localBranches.asStateFlow()
    val remoteBranches: StateFlow<List<Branch>> = _remoteBranches.asStateFlow()
    val tags: StateFlow<List<String>> = _tags.asStateFlow()
    val commits: StateFlow<List<Commit>> = _commits.asStateFlow()
    val commit: StateFlow<CommitOrWip?> = _commit.asStateFlow()
    val graphResult: StateFlow<GraphResult?> = _graphResult.asStateFlow()

    val hasMoreRemoteBranch: StateFlow<Boolean> = _hasMoreRemoteBranch.asStateFlow()
    val hasMoreTags: StateFlow<Boolean> = _hasMoreTags.asStateFlow()
    val hasMoreCommits: StateFlow<Boolean> = _hasMoreCommits.asStateFlow()


    fun open(repoPath: Path) {
        val activeRepo = gitSession.setActiveRepo(repoPath)
        activeRepo.fileWatcher.watch()

        loadCommits(reset = true)
        loadLocalBranches()
    }

    fun close(path: Path?) {
        gitSession.close(path)
        clearLocalBranches()
        clearRemoteBranches()
        clearTags()
        clearCommits()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadLocalBranches() {
        if (isLoadingLocalBranch) return
        isLoadingLocalBranch = true

        viewModelScope.launch {
            try {
                gitRepository.getLocalBranches(search = _searchQuery.value).collect { loadedBranches ->
                    _localBranches.value = loadedBranches
                    isLoadingLocalBranch = false
                }
            } catch (e: Exception) {
                println("Error loading local branches: ${e.message}")
                isLoadingLocalBranch = false
            }
        }
    }

    fun loadRemoteBranches(reset: Boolean = false) {
        if (isLoadingRemoteBranch) return
        isLoadingRemoteBranch = true

        val skip = if (reset) 0 else _remoteBranches.value.size
        val limit = 100

        viewModelScope.launch {
            try {
                gitRepository.getRemoteBranches(skip = skip, limit = limit, search = _searchQuery.value)
                    .collect { loadedBranches ->
                        if (reset) {
                            _remoteBranches.value = loadedBranches
                        } else {
                            _remoteBranches.value += loadedBranches
                        }
                        _hasMoreRemoteBranch.value = loadedBranches.size == limit
                        isLoadingRemoteBranch = false
                    }
            } catch (e: Exception) {
                println("Error loading remote branches: ${e.message}")
                isLoadingRemoteBranch = false
            }
        }
    }

    fun loadTags(reset: Boolean = false) {
        if (isLoadingTag) return

        isLoadingTag = true
        val skip = if (reset) 0 else _tags.value.size
        val limit = 100

        viewModelScope.launch {
            try {
                gitRepository.getTags(skip = skip, limit = limit, search = _searchQuery.value).collect { loadedTags ->
                    if (reset) {
                        _tags.value = loadedTags
                    } else {
                        _tags.value += loadedTags
                    }
                    _hasMoreTags.value = loadedTags.size == limit
                    isLoadingTag = false
                }
            } catch (e: Exception) {
                println("Error loading tags: ${e.message}")
                isLoadingTag = false
            }
        }
    }

    fun loadCommits(reset: Boolean = false) {
        if (isLoadingCommit) return
        isLoadingCommit = true

        val skip = if (reset) 0 else _commits.value.size
        val limit = 100

        viewModelScope.launch {
            try {
                gitRepository.getCommits(skip = skip, limit = limit).collect { loadedCommits ->
                    _commits.value += loadedCommits
                    _hasMoreCommits.value = loadedCommits.size == limit
                    isLoadingCommit = false
                }
            } catch (e: Exception) {
                println("Error loading more commits: ${e.message}")
                isLoadingCommit = false
            }
        }.invokeOnCompletion {
            updateGraph(false, _commits.value)
        }
    }

    fun clearLocalBranches() {
        _localBranches.value = emptyList()
    }

    fun clearRemoteBranches() {
        _remoteBranches.value = emptyList()
    }

    fun clearTags() {
        _tags.value = emptyList()
    }

    private fun clearCommits() {
        _commits.value = emptyList()
    }

    override fun onCleared() {
        close(null)
    }

    fun setCommit(commit: CommitOrWip?) {
        _commit.value = commit
    }

    fun onClickCommit(item: CommitOrWip) {
        _commit.update { selectedItem ->
            when (selectedItem) {
                is CommitOrWip.Wip if item is CommitOrWip.Wip -> null
                is CommitOrWip.CommitItem
                    if item is CommitOrWip.CommitItem && selectedItem.commit.id == item.commit.id
                        -> null
                else -> item
            }
        }
    }

    fun refreshGraph(hasWip: Boolean) {
        updateGraph(hasWip, _commits.value)
    }

    private fun updateGraph(hasWip: Boolean, commits: List<Commit>) {
        viewModelScope.launch {
            val headCommitId = gitRepository.getHeadCommitId()
            val result = graphUseCase.invoke(hasWip, commits, headCommitId)
            _graphResult.value = result
        }
    }
}
