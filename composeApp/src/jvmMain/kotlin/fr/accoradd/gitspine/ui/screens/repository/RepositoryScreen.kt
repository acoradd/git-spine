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
import fr.accoradd.gitspine.ui.viewmodel.GraphViewModel
import org.koin.compose.koinInject
import java.time.format.DateTimeFormatter

@Composable
fun RepositoryScreen(
    viewModel: GraphViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val tabsManager: TabsManager = koinInject()
    val gitRepository: GitRepository = koinInject()

    val activeTabId by tabsManager.activeTabId.collectAsState()
    val activeTab = tabsManager.activeTab

    var branches by remember { mutableStateOf<List<Branch>>(emptyList()) }
    var commits by remember { mutableStateOf<List<Commit>>(emptyList()) }
    var tags by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedCommit by remember { mutableStateOf<Commit?>(null) }

    // Load repository data when active tab changes
    LaunchedEffect(activeTabId) {
        println("RepositoryScreen: Active tab ID changed to: $activeTabId")
        val jgitRepo = gitRepository as? JGitRepository

        // Reset state first
        branches = emptyList()
        commits = emptyList()
        tags = emptyList()
        selectedCommit = null

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
            println("RepositoryScreen: Loading commits for ${activeTab.path}")
            try {
                // Load commits
                gitRepository.getCommits().collect { loadedCommits ->
                    println("RepositoryScreen: Loaded ${loadedCommits.size} commits")
                    commits = loadedCommits
                    if (selectedCommit == null && loadedCommits.isNotEmpty()) {
                        selectedCommit = loadedCommits.first()
                    } else if (loadedCommits.isNotEmpty()) {
                        // Update selected commit if it still exists in the new list
                        selectedCommit = loadedCommits.find { it.id == selectedCommit?.id }
                            ?: loadedCommits.first()
                    } else {
                        selectedCommit = null
                    }
                }
            } catch (e: Exception) {
                println("Error loading commits: ${e.message}")
                e.printStackTrace()
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
                selectedCommit = selectedCommit,
                onCommitClick = { commit ->
                    selectedCommit = commit
                }
            )
        },
        rightContent = {
            RightPanel(commit = selectedCommit)
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
    selectedCommit: Commit?,
    onCommitClick: (Commit) -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm") }

    val commitDataList = commits.map { commit ->
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
            isSelected = commit.id == selectedCommit?.id
        )
    }

    CommitList(
        commits = commitDataList,
        onCommitClick = { commitData ->
            commits.find { it.id == commitData.hash }?.let { commit ->
                onCommitClick(commit)
            }
        }
    )
}

@Composable
private fun RightPanel(commit: Commit?) {
    Card(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        if (commit == null) {
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
        } else {
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
}
