package fr.accoradd.gitspine.ui.components.workspace

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.accoradd.gitspine.domain.model.FileStatus
import fr.accoradd.gitspine.domain.model.FileStatusType
import fr.accoradd.gitspine.domain.model.WorkspaceStatus

@Composable
fun WorkspaceChangesPanel(
    status: WorkspaceStatus,
    onStageFile: (String) -> Unit,
    onUnstageFile: (String) -> Unit,
    onStageAll: () -> Unit,
    onUnstageAll: () -> Unit,
    onDiscardChanges: (String, Boolean) -> Unit,
    onStageFiles: (List<String>) -> Unit,
    onUnstageFiles: (List<String>) -> Unit,
    onDiscardFilesChanges: (List<String>, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var stagedExpanded by remember { mutableStateOf(true) }
    var unstagedExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        if (status.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (!status.hasChanges) {
            EmptyState()
        } else {
            // Build trees for staged and unstaged files
            val stagedTree = remember(status.staged) { buildFileTreeSimple(status.staged) }
            val unstagedTree = remember(status.unstaged) { buildFileTreeSimple(status.unstaged) }

            // Staged files section
            FileTreeSection(
                title = "Fichiers indexés (${status.stagedCount})",
                tree = stagedTree,
                expanded = stagedExpanded,
                onExpandToggle = { stagedExpanded = !stagedExpanded },
                onFileAction = { path, action ->
                    when (action) {
                        FileTreeAction.Unstage -> onUnstageFile(path)
                        FileTreeAction.Discard -> onDiscardChanges(path, true)
                        else -> {}
                    }
                },
                onFolderAction = { paths, action ->
                    when (action) {
                        FileTreeAction.Unstage -> onUnstageFiles(paths)
                        FileTreeAction.Discard -> onDiscardFilesChanges(paths, true)
                        else -> {}
                    }
                },
                headerActions = {
                    if (status.unstagedCount > 0) {
                        TextButton(onClick = onStageAll) {
                            Text("Tout indexer")
                        }
                    }
                },
                footerActions = {
                    if (status.stagedCount > 0) {
                        TextButton(onClick = onUnstageAll) {
                            Text("Tout désindexer")
                        }
                    }
                },
                isStaged = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Unstaged files section
            FileTreeSection(
                title = "Modifications non indexées (${status.unstagedCount})",
                tree = unstagedTree,
                expanded = unstagedExpanded,
                onExpandToggle = { unstagedExpanded = !unstagedExpanded },
                onFileAction = { path, action ->
                    when (action) {
                        FileTreeAction.Stage -> onStageFile(path)
                        FileTreeAction.Discard -> onDiscardChanges(path, false)
                        else -> {}
                    }
                },
                onFolderAction = { paths, action ->
                    when (action) {
                        FileTreeAction.Stage -> onStageFiles(paths)
                        FileTreeAction.Discard -> onDiscardFilesChanges(paths, false)
                        else -> {}
                    }
                },
                headerActions = null,
                footerActions = null,
                isStaged = false
            )
        }
    }
}

@Composable
private fun FileTreeSection(
    title: String,
    tree: List<FileTreeNode>,
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    onFileAction: (String, FileTreeAction) -> Unit,
    onFolderAction: (List<String>, FileTreeAction) -> Unit,
    headerActions: (@Composable () -> Unit)?,
    footerActions: (@Composable () -> Unit)?,
    isStaged: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandToggle() }
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            headerActions?.invoke()
        }

        // File tree
        AnimatedVisibility(visible = expanded) {
            Column {
                if (tree.isEmpty()) {
                    Text(
                        text = "Aucun fichier",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    FileTreeView(
                        nodes = tree,
                        isStaged = isStaged,
                        onFileAction = onFileAction,
                        onFolderAction = onFolderAction
                    )
                }

                // Footer actions
                footerActions?.let {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        it()
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Aucun changement",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
