package fr.accoradd.gitspine.ui.screens.repository

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.accoradd.gitspine.domain.model.Commit
import fr.accoradd.gitspine.domain.model.CommitOrWip
import fr.accoradd.gitspine.domain.model.GraphResult
import fr.accoradd.gitspine.domain.model.WorkspaceStatus
import fr.accoradd.gitspine.ui.components.common.ThreeColumnResizablePanes
import fr.accoradd.gitspine.ui.components.repository.CommitData
import fr.accoradd.gitspine.ui.components.repository.CommitDataAuthor
import fr.accoradd.gitspine.ui.components.repository.CommitList
import fr.accoradd.gitspine.ui.components.repository.RepositoryLeftPanel
import fr.accoradd.gitspine.ui.components.workspace.WorkspaceChangesPanel
import fr.accoradd.gitspine.ui.theme.jewelColors
import fr.accoradd.gitspine.ui.viewmodel.GravatarViewModel
import fr.accoradd.gitspine.ui.viewmodel.RepositoryScreenViewModel
import fr.accoradd.gitspine.ui.viewmodel.WorkspaceViewModel
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.window.defaultTitleBarStyle
import org.koin.compose.koinInject
import java.nio.file.Path
import java.time.format.DateTimeFormatter


@Composable
fun RepositoryScreen(
    path: String
) {
    val workspaceViewModel: WorkspaceViewModel = koinInject()
    val repositoryScreenViewModel: RepositoryScreenViewModel = koinInject()
    val gravatarViewModel: GravatarViewModel = koinInject()

    val workspaceState by workspaceViewModel.state.collectAsState()


    val localBranches = repositoryScreenViewModel.localBranches.collectAsState()
    val remoteBranches = repositoryScreenViewModel.remoteBranches.collectAsState()
    val commits = repositoryScreenViewModel.commits.collectAsState()
    val tags = repositoryScreenViewModel.tags.collectAsState()

    val selectedItem = repositoryScreenViewModel.commit.collectAsState()
    val hasMoreCommits = repositoryScreenViewModel.hasMoreCommits.collectAsState()
    val graphResult = repositoryScreenViewModel.graphResult.collectAsState()

    LaunchedEffect(path) {
        repositoryScreenViewModel.open(Path.of(path))
        workspaceViewModel.loadStatus()
    }

    DisposableEffect(path) {
        onDispose {
            repositoryScreenViewModel.close(Path.of(path))
            workspaceViewModel.unloadStatus()
        }
    }

    // Auto-select WIP when changes appear and refresh graph
    LaunchedEffect(workspaceState.status.hasChanges) {
        if (workspaceState.status.hasChanges && selectedItem.value == null) {
            repositoryScreenViewModel.setCommit(CommitOrWip.Wip)
        } else if (!workspaceState.status.hasChanges && selectedItem.value is CommitOrWip.Wip) {
            repositoryScreenViewModel.setCommit(
                if (commits.value.isNotEmpty()) {
                    CommitOrWip.CommitItem(commits.value.first())
                } else {
                    null
                }
            )
        }
        // Refresh graph when WIP status changes
        repositoryScreenViewModel.refreshGraph(workspaceState.status.hasChanges)
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(JewelTheme.defaultTitleBarStyle.colors.background)
    ) {
        ThreeColumnResizablePanes(
            modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
            initialLeftWidth = 0.2f,
            initialRightWidth = 0.25f,
            leftContent = {
                RepositoryLeftPanel(
                    repositoryScreenViewModel,
                    localBranches = localBranches.value,
                    remoteBranches = remoteBranches.value,
                    tags = tags.value,
                    onBranchClick = { /* ... */ },
                    onTagClick = { /* ... */ },
                    onSearch = { query, localExpanded, remoteExpanded, tagsExpanded ->
                        repositoryScreenViewModel.setSearchQuery(query)
                        if (localExpanded) {
                            repositoryScreenViewModel.loadLocalBranches()
                        }
                        if (remoteExpanded) {
                            repositoryScreenViewModel.loadRemoteBranches(true)
                        }
                        if (tagsExpanded) {
                            repositoryScreenViewModel.loadTags(true)
                        }
                    },
                )
            },
            centerContent = {
                CenterPanel(
                    commits = commits.value,
                    graphResult = graphResult.value,
                    workspaceStatus = workspaceState.status,
                    selectedItem = selectedItem.value,
                    onItemClick = { repositoryScreenViewModel.onClickCommit(it) },
                    onLoadMore = { repositoryScreenViewModel.loadCommits() },
                    hasMore = hasMoreCommits.value,
                    gravatarViewModel = gravatarViewModel
                )
            },
            rightContent = {
                RightPanel(
                    selectedItem = selectedItem.value,
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
private fun CenterPanel(
    commits: List<Commit>,
    graphResult: GraphResult?,
    workspaceStatus: WorkspaceStatus,
    selectedItem: CommitOrWip?,
    onItemClick: (CommitOrWip) -> Unit,
    onLoadMore: () -> Unit,
    hasMore: Boolean,
    gravatarViewModel: GravatarViewModel
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm") }
    val hasWip = workspaceStatus.hasChanges

    val commitDataList = buildList {
        if (hasWip) {
            add(
                CommitData(
                    hash = "WIP",
                    shortHash = "WIP",
                    message = "Modifications en cours (${workspaceStatus.stagedCount + workspaceStatus.unstagedCount} fichiers)",
                    author = null,
                    date = "",
                    isSelected = selectedItem is CommitOrWip.Wip,
                    row = 0
                )
            )
        }
        commits.forEachIndexed { index, commit ->
            val row = if (hasWip) index + 1 else index
            add(
                CommitData(
                    hash = commit.id,
                    shortHash = commit.shortId,
                    message = commit.message,
                    author = CommitDataAuthor(name = commit.author.name, commit.author.email),
                    date = dateFormatter.format(
                        java.time.LocalDateTime.ofInstant(
                            commit.timestamp,
                            java.time.ZoneId.systemDefault()
                        )
                    ),
                    isSelected = selectedItem is CommitOrWip.CommitItem && selectedItem.commit.id == commit.id,
                    row = row
                )
            )
        }
    }

    CommitList(
        commits = commitDataList,
        graphResult = graphResult,
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
        hasMore = hasMore,
        gravatarViewModel = gravatarViewModel
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
                    color = jewelColors.grey(8)
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
                color = jewelColors.grey(8)
            )
            Text(
                commit.shortId,
                color = jewelColors.blue(4)
            )
        }
        Divider(orientation = Orientation.Horizontal)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "Auteur",
                color = jewelColors.grey(8)
            )
            Text(
                "${commit.author.name} <${commit.author.email}>",
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "Date",
                color = jewelColors.grey(8)
            )
            Text(
                dateFormatter.format(
                    java.time.LocalDateTime.ofInstant(
                        commit.timestamp,
                        java.time.ZoneId.systemDefault()
                    )
                ),
            )
        }
        Divider(orientation = Orientation.Horizontal)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "Message",
                color = jewelColors.grey(8)
            )
            Text(
                commit.message,
            )
        }
    }
}
