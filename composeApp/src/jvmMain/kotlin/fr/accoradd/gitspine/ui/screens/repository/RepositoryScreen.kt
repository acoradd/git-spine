package fr.accoradd.gitspine.ui.screens.repository

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.accoradd.gitspine.core.tabs.TabsManager
import fr.accoradd.gitspine.domain.model.Branch
import fr.accoradd.gitspine.domain.model.Commit
import fr.accoradd.gitspine.domain.repository.GitRepository
import fr.accoradd.gitspine.infrastructure.git.JGitRepository
import fr.accoradd.gitspine.ui.components.common.ThreeColumnResizablePanes
import fr.accoradd.gitspine.ui.components.repository.CommitData
import fr.accoradd.gitspine.ui.components.repository.CommitList
import fr.accoradd.gitspine.ui.components.repository.RepositoryLeftPanel
import fr.accoradd.gitspine.ui.components.workspace.WorkspaceChangesPanel
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

@Composable
fun RepositoryScreen(
    viewModel: GraphViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val tabsManager: TabsManager = koinInject()
    val gitRepository: GitRepository = koinInject()
    val workspaceViewModel: WorkspaceViewModel = koinInject()

    val activeTabId by tabsManager.activeTabId.collectAsState()
    val activeTab = tabsManager.activeTab
    val workspaceState by workspaceViewModel.state.collectAsState()

    var branches by remember { mutableStateOf<List<Branch>>(emptyList()) }
    var commits by remember { mutableStateOf<List<Commit>>(emptyList()) }
    var tags by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedItem by remember { mutableStateOf<CommitOrWip?>(null) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var hasMoreCommits by remember { mutableStateOf(true) }

    // Load workspace status when active tab changes
    LaunchedEffect(activeTabId) {
        if (activeTab != null) {
            workspaceViewModel.loadStatus()
        }
    }

    // Auto-select WIP when changes appear
    LaunchedEffect(workspaceState.status.hasChanges) {
        if (workspaceState.status.hasChanges && selectedItem == null) {
            // Changes appeared and nothing is selected -> select WIP
            selectedItem = CommitOrWip.Wip
        } else if (!workspaceState.status.hasChanges && selectedItem is CommitOrWip.Wip) {
            // Changes disappeared and WIP was selected -> select first commit or null
            selectedItem = if (commits.isNotEmpty()) {
                CommitOrWip.CommitItem(commits.first())
            } else {
                null
            }
        }
    }

    // Load repository data when active tab changes
    LaunchedEffect(activeTabId) {
        println("RepositoryScreen: Active tab ID changed to: $activeTabId")
        val jgitRepo = gitRepository as? JGitRepository

        // Reset state first
        branches = emptyList()
        commits = emptyList()
        tags = emptyList()
        selectedItem = null
        hasMoreCommits = true
        isLoadingMore = false

        if (activeTab == null) {
            println("RepositoryScreen: No active tab, closing repository")
            // No active tab, close repository
            jgitRepo?.close()
        } else {
            println("RepositoryScreen: Switching to tab: ${activeTab.path}")
            // Close previous repository and open new one
            jgitRepo?.close()
            jgitRepo?.open(activeTab.path)

            // Load all data in parallel
            try {
                // Load branches
                gitRepository.getBranches().collect { loadedBranches ->
                    println("RepositoryScreen: Loaded ${loadedBranches.size} branches")
                    branches = loadedBranches
                }
            } catch (e: Exception) {
                println("Error loading branches: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(activeTabId) {
        activeTab?.let {
            println("RepositoryScreen: Loading initial commits for ${activeTab.path}")
            try {
                // Load initial commits (first 100)
                gitRepository.getCommits(skip = 0, limit = 100).collect { loadedCommits ->
                    println("RepositoryScreen: Loaded ${loadedCommits.size} initial commits")
                    commits = loadedCommits
                    hasMoreCommits = loadedCommits.size == 100

                    // Select WIP if there are changes, otherwise first commit
                    selectedItem = if (workspaceState.status.hasChanges) {
                        CommitOrWip.Wip
                    } else if (loadedCommits.isNotEmpty()) {
                        CommitOrWip.CommitItem(loadedCommits.first())
                    } else {
                        null
                    }
                }
            } catch (e: Exception) {
                println("Error loading commits: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    // Function to load more commits
    fun loadMoreCommits() {
        if (isLoadingMore || !hasMoreCommits || activeTab == null) return

        isLoadingMore = true
        println("RepositoryScreen: Loading more commits, current count: ${commits.size}")

        CoroutineScope(Dispatchers.Main).launch {
            try {
                gitRepository.getCommits(skip = commits.size, limit = 100).collect { loadedCommits ->
                    println("RepositoryScreen: Loaded ${loadedCommits.size} more commits")
                    commits = commits + loadedCommits
                    hasMoreCommits = loadedCommits.size == 100
                    isLoadingMore = false
                }
            } catch (e: Exception) {
                println("Error loading more commits: ${e.message}")
                e.printStackTrace()
                isLoadingMore = false
            }
        }
    }

    LaunchedEffect(activeTabId) {
        activeTab?.let {
            println("RepositoryScreen: Loading tags for ${activeTab.path}")
            try {
                // Load tags
                gitRepository.getTags().collect { loadedTags ->
                    println("RepositoryScreen: Loaded ${loadedTags.size} tags")
                    tags = loadedTags
                }
            } catch (e: Exception) {
                println("Error loading tags: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    ThreeColumnResizablePanes(
        modifier = modifier.fillMaxSize(),
        initialLeftWidth = 0.2f,
        initialRightWidth = 0.25f,
        leftContent = {
            LeftPanel(
                branches = branches,
                tags = tags,
                onBranchClick = { branchName ->
                    println("Branch clicked: $branchName")
                },
                onTagClick = { tagName ->
                    println("Tag clicked: $tagName")
                }
            )
        },
        centerContent = {
            CenterPanel(
                commits = commits,
                workspaceStatus = workspaceState.status,
                selectedItem = selectedItem,
                onItemClick = { item ->
                    selectedItem = item
                },
                onLoadMore = { loadMoreCommits() },
                hasMore = hasMoreCommits && !isLoadingMore
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

@Composable
private fun LeftPanel(
    branches: List<Branch>,
    tags: List<String>,
    onBranchClick: (String) -> Unit,
    onTagClick: (String) -> Unit
) {
    // Separate local and remote branches
    val localBranches = branches.filter { !it.isRemote }.map { it.name }
    val remoteBranches = branches.filter { it.isRemote }
        .groupBy { it.name.substringBefore("/") }
        .mapValues { (_, branchesList) ->
            branchesList.map { it.name.substringAfter("/") }
        }
    val selectedBranch = branches.find { it.isHead }?.name

    RepositoryLeftPanel(
        localBranches = localBranches,
        remoteBranches = remoteBranches,
        tags = tags,
        selectedBranch = selectedBranch,
        onBranchClick = onBranchClick,
        onTagClick = onTagClick
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
        // Add WIP row if there are changes
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

        // Add regular commits
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
            // Show workspace changes panel
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
            // Show commit details
            CommitDetailsPanel(commit = selectedItem.commit)
        }
        null -> {
            // No selection
            Card(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
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
}

@Composable
private fun CommitDetailsPanel(commit: Commit) {
    Card(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Commit hash
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Commit",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    commit.shortId,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            HorizontalDivider()

            // Author
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

            // Date
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

            // Message
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
}
