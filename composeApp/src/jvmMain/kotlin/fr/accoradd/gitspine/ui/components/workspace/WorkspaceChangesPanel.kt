package fr.accoradd.gitspine.ui.components.workspace

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    modifier: Modifier = Modifier
) {
    var stagedExpanded by remember { mutableStateOf(true) }
    var unstagedExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
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
            // Staged files section
            FileSection(
                title = "Fichiers indexés (${status.stagedCount})",
                files = status.staged,
                expanded = stagedExpanded,
                onExpandToggle = { stagedExpanded = !stagedExpanded },
                onFileAction = { file, action ->
                    when (action) {
                        FileAction.Unstage -> onUnstageFile(file.path)
                        FileAction.Discard -> onDiscardChanges(file.path, true)
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
            FileSection(
                title = "Modifications non indexées (${status.unstagedCount})",
                files = status.unstaged,
                expanded = unstagedExpanded,
                onExpandToggle = { unstagedExpanded = !unstagedExpanded },
                onFileAction = { file, action ->
                    when (action) {
                        FileAction.Stage -> onStageFile(file.path)
                        FileAction.Discard -> onDiscardChanges(file.path, false)
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
private fun FileSection(
    title: String,
    files: List<FileStatus>,
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    onFileAction: (FileStatus, FileAction) -> Unit,
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

        // File list
        AnimatedVisibility(visible = expanded) {
            Column {
                if (files.isEmpty()) {
                    Text(
                        text = "Aucun fichier",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    files.forEach { file ->
                        FileStatusItem(
                            file = file,
                            isStaged = isStaged,
                            onAction = { action -> onFileAction(file, action) }
                        )
                    }
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
private fun FileStatusItem(
    file: FileStatus,
    isStaged: Boolean,
    onAction: (FileAction) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = file.statusType.icon(),
                contentDescription = file.statusType.name,
                tint = file.statusType.color(),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = file.path,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (isStaged) {
                IconButton(
                    onClick = { onAction(FileAction.Unstage) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Désindexer",
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = { onAction(FileAction.Stage) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Indexer",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            IconButton(
                onClick = { onAction(FileAction.Discard) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Annuler",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.error
                )
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

private enum class FileAction {
    Stage,
    Unstage,
    Discard
}

private fun FileStatusType.icon() = when (this) {
    FileStatusType.ADDED -> Icons.Default.Add
    FileStatusType.MODIFIED -> Icons.Default.Edit
    FileStatusType.DELETED -> Icons.Default.Delete
    FileStatusType.UNTRACKED -> Icons.Default.Star
    FileStatusType.CONFLICTING -> Icons.Default.Warning
    FileStatusType.RENAMED -> Icons.Default.Info
}

private fun FileStatusType.color() = when (this) {
    FileStatusType.ADDED -> Color(0xFF4CAF50)      // Green
    FileStatusType.MODIFIED -> Color(0xFF2196F3)   // Blue
    FileStatusType.DELETED -> Color(0xFFF44336)    // Red
    FileStatusType.UNTRACKED -> Color(0xFF9E9E9E)  // Gray
    FileStatusType.CONFLICTING -> Color(0xFFFF9800) // Orange
    FileStatusType.RENAMED -> Color(0xFF9C27B0)    // Purple
}
