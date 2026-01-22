package fr.accoradd.gitspine.ui.components.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.accoradd.gitspine.domain.model.FileStatusType
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icon.IconKey
import org.jetbrains.jewel.ui.icons.AllIconsKeys

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
                .background(JewelTheme.globalColors.panelBackground)
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
                    key = if (expanded) AllIconsKeys.General.ChevronDown else AllIconsKeys.General.ChevronRight,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    key = if (expanded) AllIconsKeys.Nodes.Folder else AllIconsKeys.Nodes.Folder,
                    contentDescription = "Folder",
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF90CAF9)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = folder.name,
                    maxLines = 1,
                    overflow = TextOverflow.Visible
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "(${allFilePaths.size})",
                    color = JewelTheme.globalColors.text.disabled
                )
            }

            // Folder actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                if (isStaged) {
                    IconButton(
                        onClick = { onFolderAction(allFilePaths, FileTreeAction.Unstage) }
                    ) {
                        Icon(
                            key = AllIconsKeys.Actions.Cancel,
                            contentDescription = "Désindexer le dossier",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = { onFolderAction(allFilePaths, FileTreeAction.Stage) }
                    ) {
                        Icon(
                            key = AllIconsKeys.General.Add,
                            contentDescription = "Indexer le dossier",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                IconButton(
                    onClick = { onFolderAction(allFilePaths, FileTreeAction.Discard) }
                ) {
                    Icon(
                        key = AllIconsKeys.Actions.GC,
                        contentDescription = "Supprimer les modifications du dossier",
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFF44336)
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
                key = file.status.statusType.iconKey(),
                contentDescription = file.status.statusType.name,
                tint = file.status.statusType.color(),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = file.name,
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
                    onClick = { onAction(file.path, FileTreeAction.Unstage) }
                ) {
                    Icon(
                        key = AllIconsKeys.Actions.Cancel,
                        contentDescription = "Désindexer",
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = { onAction(file.path, FileTreeAction.Stage) }
                ) {
                    Icon(
                        key = AllIconsKeys.General.Add,
                        contentDescription = "Indexer",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            IconButton(
                onClick = { onAction(file.path, FileTreeAction.Discard) }
            ) {
                Icon(
                    key = AllIconsKeys.Actions.GC,
                    contentDescription = "Annuler",
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFFF44336)
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
private fun FileStatusType.iconKey(): IconKey = when (this) {
    FileStatusType.ADDED -> AllIconsKeys.General.Add
    FileStatusType.MODIFIED -> AllIconsKeys.Actions.Edit
    FileStatusType.DELETED -> AllIconsKeys.Actions.GC
    FileStatusType.UNTRACKED -> AllIconsKeys.FileTypes.Unknown
    FileStatusType.CONFLICTING -> AllIconsKeys.General.Warning
    FileStatusType.RENAMED -> AllIconsKeys.Actions.Forward
}

private fun FileStatusType.color() = when (this) {
    FileStatusType.ADDED -> Color(0xFF4CAF50)      // Green
    FileStatusType.MODIFIED -> Color(0xFF2196F3)   // Blue
    FileStatusType.DELETED -> Color(0xFFF44336)    // Red
    FileStatusType.UNTRACKED -> Color(0xFF9E9E9E)  // Gray
    FileStatusType.CONFLICTING -> Color(0xFFFF9800) // Orange
    FileStatusType.RENAMED -> Color(0xFF9C27B0)    // Purple
}