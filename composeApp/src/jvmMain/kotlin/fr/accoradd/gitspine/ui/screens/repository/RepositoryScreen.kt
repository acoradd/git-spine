package fr.accoradd.gitspine.ui.screens.repository

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.accoradd.gitspine.domain.model.Branch
import fr.accoradd.gitspine.domain.model.Commit
import fr.accoradd.gitspine.domain.repository.GitRepository
import fr.accoradd.gitspine.infrastructure.git.JGitRepository
import fr.accoradd.gitspine.ui.components.common.ThreeColumnResizablePanes
import fr.accoradd.gitspine.ui.components.repository.CommitData
import fr.accoradd.gitspine.ui.components.repository.CommitList
import fr.accoradd.gitspine.ui.components.repository.RepositoryLeftPanel
import fr.accoradd.gitspine.ui.components.workspace.WorkspaceChangesPanel
import fr.accoradd.gitspine.ui.navigation.NavController
import fr.accoradd.gitspine.ui.navigation.Screen
import fr.accoradd.gitspine.ui.theme.JetBrainsMonoFamily
import fr.accoradd.gitspine.ui.viewmodel.GraphViewModel
import fr.accoradd.gitspine.ui.viewmodel.WorkspaceViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.time.format.DateTimeFormatter

// Sealed class to represent either a commit or WIP
sealed class CommitOrWip {
    data object Wip : CommitOrWip()
    data class CommitItem(val commit: Commit) : CommitOrWip()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepositoryScreen(
    viewModel: GraphViewModel,
    navController: NavController
) {
    val gitRepository: GitRepository = koinInject()
    val workspaceViewModel: WorkspaceViewModel = koinInject()
    val scope = rememberCoroutineScope()

    val screenState = navController.currentScreen as Screen.Repository
    val workspaceState by workspaceViewModel.state.collectAsState()

    // Data states
    var localBranches by remember { mutableStateOf<List<Branch>>(emptyList()) }
    var remoteBranches by remember { mutableStateOf<List<Branch>>(emptyList()) }
    var commits by remember { mutableStateOf<List<Commit>>(emptyList()) }
    var tags by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedItem by remember { mutableStateOf<CommitOrWip?>(null) }
    
    // Pagination & Search states
    var isLoadingMoreCommits by remember { mutableStateOf(false) }
    var hasMoreCommits by remember { mutableStateOf(true) }
    
    var localBranchSearchQuery by remember { mutableStateOf("") }
    var remoteBranchSearchQuery by remember { mutableStateOf("") }
    var tagSearchQuery by remember { mutableStateOf("") }
    
    var hasMoreRemoteBranches by remember { mutableStateOf(true) }
    var hasMoreTags by remember { mutableStateOf(true) }
    
    var isLoadingLocalBranches by remember { mutableStateOf(false) }
    var isLoadingRemoteBranches by remember { mutableStateOf(false) }
    var isLoadingTags by remember { mutableStateOf(false) }
    
    var isRepoReady by remember { mutableStateOf(false) }

    // Helper functions for loading data
    fun loadLocalBranches() {
        if (isLoadingLocalBranches) return
        isLoadingLocalBranches = true
        
        scope.launch {
            try {
                gitRepository.getLocalBranches(search = localBranchSearchQuery).collect { loadedBranches ->
                    localBranches = loadedBranches
                    isLoadingLocalBranches = false
                }
            } catch (e: Exception) {
                println("Error loading local branches: ${e.message}")
                isLoadingLocalBranches = false
            }
        }
    }

    fun loadRemoteBranches(reset: Boolean = false) {
        if (isLoadingRemoteBranches || (!reset && !hasMoreRemoteBranches)) return
        
        isLoadingRemoteBranches = true
        val skip = if (reset) 0 else remoteBranches.size
        val limit = 100
        
        scope.launch {
            try {
                gitRepository.getRemoteBranches(skip = skip, limit = limit, search = remoteBranchSearchQuery).collect { loadedBranches ->
                    if (reset) {
                        remoteBranches = loadedBranches
                    } else {
                        remoteBranches = remoteBranches + loadedBranches
                    }
                    hasMoreRemoteBranches = loadedBranches.size == limit
                    isLoadingRemoteBranches = false
                }
            } catch (e: Exception) {
                println("Error loading remote branches: ${e.message}")
                isLoadingRemoteBranches = false
            }
        }
    }

    fun loadTags(reset: Boolean = false) {
        if (isLoadingTags || (!reset && !hasMoreTags)) return
        
        isLoadingTags = true
        val skip = if (reset) 0 else tags.size
        val limit = 100
        
        scope.launch {
            try {
                gitRepository.getTags(skip = skip, limit = limit, search = tagSearchQuery).collect { loadedTags ->
                    if (reset) {
                        tags = loadedTags
                    } else {
                        tags = tags + loadedTags
                    }
                    hasMoreTags = loadedTags.size == limit
                    isLoadingTags = false
                }
            } catch (e: Exception) {
                println("Error loading tags: ${e.message}")
                isLoadingTags = false
            }
        }
    }

    // Auto-select WIP when changes appear
    LaunchedEffect(workspaceState.status.hasChanges) {
        if (workspaceState.status.hasChanges && selectedItem == null) {
            selectedItem = CommitOrWip.Wip
        } else if (!workspaceState.status.hasChanges && selectedItem is CommitOrWip.Wip) {
            selectedItem = if (commits.isNotEmpty()) {
                CommitOrWip.CommitItem(commits.first())
            } else {
                null
            }
        }
    }

    // Load repository data when path changes
    LaunchedEffect(screenState.path) {
        val jgitRepo = gitRepository as? JGitRepository
        jgitRepo?.close()
        jgitRepo?.open(screenState.path)
        isRepoReady = true

        workspaceViewModel.loadStatus()

        try {
            gitRepository.getCommits(skip = 0, limit = 100).collect { loadedCommits ->
                commits = loadedCommits
                hasMoreCommits = loadedCommits.size == 100
                selectedItem = if (workspaceViewModel.state.value.status.hasChanges) {
                    CommitOrWip.Wip
                } else if (loadedCommits.isNotEmpty()) {
                    CommitOrWip.CommitItem(loadedCommits.first())
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            println("Error loading commits: ${e.message}")
        }
        
        loadLocalBranches()
        loadRemoteBranches(reset = true)
        loadTags(reset = true)
    }

    // Function to load more commits
    fun loadMoreCommits() {
        if (isLoadingMoreCommits || !hasMoreCommits) return

        isLoadingMoreCommits = true
        CoroutineScope(Dispatchers.Main).launch {
            try {
                gitRepository.getCommits(skip = commits.size, limit = 100).collect { loadedCommits ->
                    commits = commits + loadedCommits
                    hasMoreCommits = loadedCommits.size == 100
                    isLoadingMoreCommits = false
                }
            } catch (e: Exception) {
                println("Error loading more commits: ${e.message}")
                isLoadingMoreCommits = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // TODO: Replace with actual logo
                        Box(modifier = Modifier.size(32.dp).padding(4.dp), contentAlignment = Alignment.Center) {
                            Text("GS")
                        }
                        
                        // Repository Selector
                        TextButton(onClick = { /* TODO: Show repo list */ }) {
                            Text(screenState.path.fileName.toString(), style = MaterialTheme.typography.titleMedium)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                },
                actions = {
                    // Git Actions
                    TextButton(onClick = { /* TODO */ }) { Text("Fetch") }
                    TextButton(onClick = { /* TODO */ }) { Text("Pull") }
                    TextButton(onClick = { /* TODO */ }) { Text("Push") }
                    TextButton(onClick = { /* TODO */ }) { Text("Stash") }
                    TextButton(onClick = { /* TODO */ }) { Text("Unstash") }
                    TextButton(onClick = { /* TODO */ }) { Text("New Branch") }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Add Repo & Settings
                    IconButton(onClick = { navController.navigateTo(Screen.AddRepository) }) {
                        Icon(Icons.Default.Add, contentDescription = "Ajouter un dépôt")
                    }
                    IconButton(onClick = { navController.navigateTo(Screen.Settings) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Paramètres")
                    }
                }
            )
        }
    ) { padding ->
        ThreeColumnResizablePanes(
            modifier = Modifier.fillMaxSize().padding(padding).padding(4.dp),
            initialLeftWidth = 0.2f,
            initialRightWidth = 0.25f,
            leftContent = {
                if (isRepoReady) {
                    LeftPanel(
                        localBranches = localBranches,
                        remoteBranches = remoteBranches,
                        tags = tags,
                        localBranchSearchQuery = localBranchSearchQuery,
                        remoteBranchSearchQuery = remoteBranchSearchQuery,
                        tagSearchQuery = tagSearchQuery,
                        hasMoreRemoteBranches = hasMoreRemoteBranches,
                        hasMoreTags = hasMoreTags,
                        onBranchClick = { /* ... */ },
                        onTagClick = { /* ... */ },
                        onLoadLocalBranches = ::loadLocalBranches,
                        onLocalBranchSearch = { query ->
                            localBranchSearchQuery = query
                            loadLocalBranches()
                        },
                        onLoadRemoteBranches = { loadRemoteBranches(reset = true) },
                        onLoadMoreRemoteBranches = { loadRemoteBranches(reset = false) },
                        onRemoteBranchSearch = { query ->
                            remoteBranchSearchQuery = query
                            loadRemoteBranches(reset = true)
                        },
                        onLoadTags = { loadTags(reset = true) },
                        onLoadMoreTags = { loadTags(reset = false) },
                        onTagSearch = { query ->
                            tagSearchQuery = query
                            loadTags(reset = true)
                        }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize())
                }
            },
            centerContent = {
                CenterPanel(
                    commits = commits,
                    workspaceStatus = workspaceState.status,
                    selectedItem = selectedItem,
                    onItemClick = { item -> selectedItem = item },
                    onLoadMore = ::loadMoreCommits,
                    hasMore = hasMoreCommits && !isLoadingMoreCommits
                )
            },
            rightContent = {
                RightPanel(
                    selectedItem = selectedItem,
                    workspaceStatus = workspaceState.status,
                    onStageFile = { path -> workspaceViewModel.stageFile(path) },
                    onUnstageFile = { path -> workspaceViewModel.unstageFile(path) },
                    onStageAll = { workspaceViewModel.stageAll() },
                    onUnstageAll = { workspaceViewModel.unstageAll() },
                    onDiscardChanges = { path, staged -> workspaceViewModel.discardChanges(path, staged) },
                    onStageFiles = { paths -> workspaceViewModel.stageFiles(paths) },
                    onUnstageFiles = { paths -> workspaceViewModel.unstageFiles(paths) },
                    onDiscardFilesChanges = { paths, staged -> workspaceViewModel.discardFilesChanges(paths, staged) }
                )
            }
        )
    }
}

@Composable
private fun LeftPanel(
    localBranches: List<Branch>,
    remoteBranches: List<Branch>,
    tags: List<String>,
    localBranchSearchQuery: String,
    remoteBranchSearchQuery: String,
    tagSearchQuery: String,
    hasMoreRemoteBranches: Boolean,
    hasMoreTags: Boolean,
    onBranchClick: (String) -> Unit,
    onTagClick: (String) -> Unit,
    onLoadLocalBranches: () -> Unit,
    onLocalBranchSearch: (String) -> Unit,
    onLoadRemoteBranches: () -> Unit,
    onLoadMoreRemoteBranches: () -> Unit,
    onRemoteBranchSearch: (String) -> Unit,
    onLoadTags: () -> Unit,
    onLoadMoreTags: () -> Unit,
    onTagSearch: (String) -> Unit
) {
    val localBranchesNames = localBranches.map { it.name }
    val remoteBranchesMap = remoteBranches
        .groupBy { it.name.substringBefore("/") }
        .mapValues { (_, branchesList) ->
            branchesList.map { it.name.substringAfter("/") }
        }
    val selectedBranch = localBranches.find { it.isHead }?.name ?: remoteBranches.find { it.isHead }?.name

    RepositoryLeftPanel(
        localBranches = localBranchesNames,
        remoteBranches = remoteBranchesMap,
        tags = tags,
        selectedBranch = selectedBranch,
        localBranchSearchQuery = localBranchSearchQuery,
        remoteBranchSearchQuery = remoteBranchSearchQuery,
        tagSearchQuery = tagSearchQuery,
        hasMoreRemoteBranches = hasMoreRemoteBranches,
        hasMoreTags = hasMoreTags,
        onBranchClick = onBranchClick,
        onTagClick = onTagClick,
        onLoadLocalBranches = onLoadLocalBranches,
        onLocalBranchSearch = onLocalBranchSearch,
        onLoadRemoteBranches = onLoadRemoteBranches,
        onLoadMoreRemoteBranches = onLoadMoreRemoteBranches,
        onRemoteBranchSearch = onRemoteBranchSearch,
        onLoadTags = onLoadTags,
        onLoadMoreTags = onLoadMoreTags,
        onTagSearch = onTagSearch
    )
}

@Composable
private fun CenterPanel(
    commits: List<Commit>,
    workspaceStatus: fr.accoradd.gitspine.domain.model.WorkspaceStatus,
    selectedItem: CommitOrWip?,
    onItemClick: (CommitOrWip) -> Unit,
    onLoadMore: () -> Unit,
    hasMore: Boolean
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm") }

    val commitDataList = buildList {
        if (workspaceStatus.hasChanges) {
            add(
                CommitData(
                    hash = "WIP",
                    shortHash = "WIP",
                    message = "Modifications en cours (${workspaceStatus.stagedCount + workspaceStatus.unstagedCount} fichiers)",
                    author = "",
                    date = "",
                    isSelected = selectedItem is CommitOrWip.Wip
                )
            )
        }
        commits.forEach { commit ->
            add(
                CommitData(
                    hash = commit.id,
                    shortHash = commit.shortId,
                    message = commit.message,
                    author = commit.author.name,
                    date = dateFormatter.format(
                        java.time.LocalDateTime.ofInstant(
                            commit.timestamp,
                            java.time.ZoneId.systemDefault()
                        )
                    ),
                    isSelected = selectedItem is CommitOrWip.CommitItem && selectedItem.commit.id == commit.id
                )
            )
        }
    }

    CommitList(
        commits = commitDataList,
        onCommitClick = { commitData ->
            if (commitData.hash == "WIP") {
                onItemClick(CommitOrWip.Wip)
            } else {
                commits.find { it.id == commitData.hash }?.let { commit ->
                    onItemClick(CommitOrWip.CommitItem(commit))
                }
            }
        },
        onLoadMore = onLoadMore,
        hasMore = hasMore
    )
}

@Composable
private fun RightPanel(
    selectedItem: CommitOrWip?,
    workspaceStatus: fr.accoradd.gitspine.domain.model.WorkspaceStatus,
    onStageFile: (String) -> Unit,
    onUnstageFile: (String) -> Unit,
    onStageAll: () -> Unit,
    onUnstageAll: () -> Unit,
    onDiscardChanges: (String, Boolean) -> Unit,
    onStageFiles: (List<String>) -> Unit,
    onUnstageFiles: (List<String>) -> Unit,
    onDiscardFilesChanges: (List<String>, Boolean) -> Unit
) {
    when (selectedItem) {
        is CommitOrWip.Wip -> {
            WorkspaceChangesPanel(
                status = workspaceStatus,
                onStageFile = onStageFile,
                onUnstageFile = onUnstageFile,
                onStageAll = onStageAll,
                onUnstageAll = onUnstageAll,
                onDiscardChanges = onDiscardChanges,
                onStageFiles = onStageFiles,
                onUnstageFiles = onUnstageFiles,
                onDiscardFilesChanges = onDiscardFilesChanges,
                modifier = Modifier.fillMaxSize()
            )
        }
        is CommitOrWip.CommitItem -> {
            CommitDetailsPanel(commit = selectedItem.commit)
        }
        null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Sélectionnez un commit",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CommitDetailsPanel(commit: Commit) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "Commit",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                commit.shortId,
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = JetBrainsMonoFamily),
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        HorizontalDivider()
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "Auteur",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "${commit.author.name} <${commit.author.email}>",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "Date",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                dateFormatter.format(
                    java.time.LocalDateTime.ofInstant(
                        commit.timestamp,
                        java.time.ZoneId.systemDefault()
                    )
                ),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        HorizontalDivider()
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "Message",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                commit.message,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
