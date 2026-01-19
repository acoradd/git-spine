package fr.accoradd.gitspine.ui.components.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.accoradd.gitspine.domain.model.FileStatusType

@Composable
fun FileTreeView(
    nodes: List<FileTreeNode>,
    isStaged: Boolean,
    onFileAction: (String, FileTreeAction) -> Unit,
    onFolderAction: (List<String>, FileTreeAction) -> Unit,
    modifier: Modifier = Modifier,
    indent: Int = 0
) {
    Column(modifier = modifier) {
        nodes.forEach { node ->
            when (node) {
                is FileTreeNode.Folder -> {
                    FolderItem(
                        folder = node,
                        isStaged = isStaged,
                        onFileAction = onFileAction,
                        onFolderAction = onFolderAction,
                        indent = indent
                    )
                }
                is FileTreeNode.File -> {
                    FileItem(
                        file = node,
                        isStaged = isStaged,
                        onAction = onFileAction,
                        indent = indent
                    )
                }
            }
        }
    }
}

@Composable
private fun FolderItem(
    folder: FileTreeNode.Folder,
    isStaged: Boolean,
    onFileAction: (String, FileTreeAction) -> Unit,
    onFolderAction: (List<String>, FileTreeAction) -> Unit,
    indent: Int
) {
    var expanded by remember { mutableStateOf(true) }
    val allFilePaths = remember(folder) { folder.allFiles().map { it.path } }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Folder header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (indent == 0) MaterialTheme.colorScheme.surfaceContainerLow
                    else MaterialTheme.colorScheme.surface
                )
                .height(36.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState())
                    .padding(start = (indent * 16).dp)
                    .clickable { expanded = !expanded }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (expanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                    contentDescription = "Folder",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Visible
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "(${allFilePaths.size})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Folder actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                if (isStaged) {
                    IconButton(
                        onClick = { onFolderAction(allFilePaths, FileTreeAction.Unstage) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Désindexer le dossier",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = { onFolderAction(allFilePaths, FileTreeAction.Stage) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Indexer le dossier",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                IconButton(
                    onClick = { onFolderAction(allFilePaths, FileTreeAction.Discard) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer les modifications du dossier",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Children (files and subfolders)
        if (expanded) {
            FileTreeView(
                nodes = folder.children,
                isStaged = isStaged,
                onFileAction = onFileAction,
                onFolderAction = onFolderAction,
                indent = indent + 1
            )
        }
    }
}

@Composable
private fun FileItem(
    file: FileTreeNode.File,
    isStaged: Boolean,
    onAction: (String, FileTreeAction) -> Unit,
    indent: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState())
                .padding(start = (indent * 16 + 28).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = file.status.statusType.icon(),
                contentDescription = file.status.statusType.name,
                tint = file.status.statusType.color(),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = file.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Visible
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(end = 8.dp)
        ) {
            if (isStaged) {
                IconButton(
                    onClick = { onAction(file.path, FileTreeAction.Unstage) },
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
                    onClick = { onAction(file.path, FileTreeAction.Stage) },
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
                onClick = { onAction(file.path, FileTreeAction.Discard) },
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

enum class FileTreeAction {
    Stage,
    Unstage,
    Discard
}

// Extension functions for file status icons and colors
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
