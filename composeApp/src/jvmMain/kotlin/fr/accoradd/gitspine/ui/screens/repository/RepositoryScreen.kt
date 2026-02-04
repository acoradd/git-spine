package fr.accoradd.gitspine.ui.screens.repository

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import fr.accoradd.gitspine.domain.model.*
import fr.accoradd.gitspine.ui.components.repository.RepositoryResizablePanes
import fr.accoradd.gitspine.ui.components.repository.CommitData
import fr.accoradd.gitspine.ui.components.repository.CommitList
import fr.accoradd.gitspine.ui.components.repository.HorizontalSpacer
import fr.accoradd.gitspine.ui.components.repository.RepositoryLeftPanel
import fr.accoradd.gitspine.ui.components.workspace.WorkspaceChangesPanel
import fr.accoradd.gitspine.ui.theme.jewelColors
import fr.accoradd.gitspine.ui.viewmodel.RepositoryScreenViewModel
import fr.accoradd.gitspine.ui.viewmodel.WorkspaceViewModel
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.window.defaultTitleBarStyle
import org.koin.compose.koinInject
import java.nio.file.Path
import java.time.Instant
import java.time.format.DateTimeFormatter


@Composable
fun RepositoryScreen(
    path: String
) {
    val workspaceViewModel: WorkspaceViewModel = koinInject()
    val repositoryScreenViewModel: RepositoryScreenViewModel = koinInject()

    val workspaceState by workspaceViewModel.state.collectAsState()


    val localBranches = repositoryScreenViewModel.localBranches.collectAsState()
    val remoteBranches = repositoryScreenViewModel.remoteBranches.collectAsState()
    val tags = repositoryScreenViewModel.tags.collectAsState()

    val localBranchesFilter = repositoryScreenViewModel.localBranchesFilter.collectAsState()
    val remoteBranchesFilter = repositoryScreenViewModel.remoteBranchesFilter.collectAsState()
    val tagsFilter = repositoryScreenViewModel.tagsFilter.collectAsState()

    val commits = repositoryScreenViewModel.commits.collectAsState()

    val selectedItem = repositoryScreenViewModel.commit.collectAsState()
    val hasMoreCommits = repositoryScreenViewModel.hasMoreCommits.collectAsState()
    val graphResult = repositoryScreenViewModel.graphResult.collectAsState()

    val density = LocalDensity.current

    var rightWidth by remember { mutableStateOf(400.dp) }
    var rightWidthAtStartOfDrag by remember { mutableStateOf(400.dp) }

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

    Box(
        modifier = Modifier.fillMaxSize()
            .background(JewelTheme.defaultTitleBarStyle.colors.background).padding(horizontal = 28.dp)
    ) {
        RepositoryResizablePanes(
            modifier = Modifier.fillMaxSize(),
            initialLeftWidth = 0.2f,
            leftContent = {
                RepositoryLeftPanel(
                    localBranches = localBranchesFilter.value,
                    remoteBranches = remoteBranchesFilter.value,
                    tags = tagsFilter.value,
                    onBranchClick = { /* ... */ },
                    onTagClick = { /* ... */ },
                    onSearch = { query -> repositoryScreenViewModel.setSearchQuery(query) },
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
                    localBranches = localBranches.value,
                    remoteBranches = remoteBranches.value,
                    tags = tags.value
                )
            }
        )
        if (selectedItem.value != null) {
            // Panneau latéral droit
            AnimatedVisibility(
                visible = selectedItem.value != null,
                enter = slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ),
                exit = slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(rightWidth + 4.dp)
                        .background(JewelTheme.defaultTitleBarStyle.colors.background),
                    horizontalArrangement = Arrangement.End
                ) {
                    HorizontalSpacer(
                        width = 4.dp,
                        onDragStart = { rightWidthAtStartOfDrag = rightWidth },
                        onPositionChange = { deltaX ->
                            with(density) {
                                rightWidth = (rightWidthAtStartOfDrag - deltaX.toDp()).coerceAtLeast(200.dp)
                            }
                        }
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(rightWidth)
                            .clip(RoundedCornerShape(8.dp))
                            .background(JewelTheme.globalColors.panelBackground)
                    ) {
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
                            onDiscardFilesChanges = { paths, staged ->
                                workspaceViewModel.discardFilesChanges(
                                    paths,
                                    staged
                                )
                            }
                        )
                    }
                }
            }
        }
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
    localBranches: List<Branch>,
    remoteBranches: List<Branch>,
    tags: List<Tag>
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm") }
    val hasWip = workspaceStatus.hasChanges

    val commitDataList = buildList {
        if (hasWip) {
            add(
                CommitData(
                    info = Commit(
                        id = "WIP",
                        shortId = "WIP",
                        message = "WIP",
                        author = Author(name = "", email = ""),
                        timestamp = Instant.now(),
                        parents = emptyList()
                    ),
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
                    info = commit,
                    date = dateFormatter.format(
                        java.time.LocalDateTime.ofInstant(
                            commit.timestamp,
                            java.time.ZoneId.systemDefault()
                        )
                    ),
                    isSelected = selectedItem is CommitOrWip.CommitItem && selectedItem.commit.id == commit.id,
                    row = row,
                    refs = localBranches.filter { it.commitId == commit.id }
                        + remoteBranches.filter { it.commitId == commit.id && !it.name.endsWith("/HEAD") }
                        + tags.filter { it.commitId == commit.id }
                )
            )
        }
    }

    CommitList(
        commits = commitDataList,
        graphResult = graphResult,
        onCommitClick = { commitData ->
            if (commitData.info.id == "WIP") {
                onItemClick(CommitOrWip.Wip)
            } else {
                onItemClick(CommitOrWip.CommitItem(commitData.info))
            }
        },
        onLoadMore = onLoadMore,
        hasMore = hasMore
    )
}

@Composable
private fun RightPanel(
    selectedItem: CommitOrWip?,
    workspaceStatus: WorkspaceStatus,
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
                    color = JewelTheme.globalColors.text.info
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
