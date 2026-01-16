package fr.accoradd.gitspine.ui.components.repository

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

sealed class TreeNode {
    data class Folder(
        val name: String,
        val children: List<TreeNode>,
        val path: String = ""
    ) : TreeNode()

    data class Leaf(
        val name: String,
        val fullName: String,
        val isSelected: Boolean = false
    ) : TreeNode()
}

@Composable
fun BranchTreeView(
    branches: List<String>,
    selectedBranch: String? = null,
    onBranchClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val tree = remember(branches) {
        buildTree(branches)
    }

    Column(modifier = modifier) {
        tree.forEach { node ->
            TreeNodeItem(
                node = node,
                selectedBranch = selectedBranch,
                onBranchClick = onBranchClick,
                level = 0
            )
        }
    }
}

@Composable
private fun TreeNodeItem(
    node: TreeNode,
    selectedBranch: String?,
    onBranchClick: (String) -> Unit,
    level: Int
) {
    when (node) {
        is TreeNode.Folder -> {
            FolderItem(
                folder = node,
                selectedBranch = selectedBranch,
                onBranchClick = onBranchClick,
                level = level
            )
        }
        is TreeNode.Leaf -> {
            LeafItem(
                leaf = node,
                isSelected = node.fullName == selectedBranch,
                onClick = { onBranchClick(node.fullName) },
                level = level
            )
        }
    }
}

@Composable
private fun FolderItem(
    folder: TreeNode.Folder,
    selectedBranch: String?,
    onBranchClick: (String) -> Unit,
    level: Int
) {
    var expanded by remember { mutableStateOf(true) }
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isHovered) MaterialTheme.colorScheme.surfaceContainerHighest
                    else MaterialTheme.colorScheme.surface
                )
                .clickable { expanded = !expanded }
                .hoverable(interactionSource)
                .padding(start = (level * 16 + 8).dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = if (expanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = folder.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                folder.children.forEach { child ->
                    TreeNodeItem(
                        node = child,
                        selectedBranch = selectedBranch,
                        onBranchClick = onBranchClick,
                        level = level + 1
                    )
                }
            }
        }
    }
}

@Composable
private fun LeafItem(
    leaf: TreeNode.Leaf,
    isSelected: Boolean,
    onClick: () -> Unit,
    level: Int
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    isHovered -> MaterialTheme.colorScheme.surfaceContainerHighest
                    else -> MaterialTheme.colorScheme.surface
                }
            )
            .clickable(onClick = onClick)
            .hoverable(interactionSource)
            .padding(start = (level * 16 + 32).dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Branch icon (simple circle)
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = MaterialTheme.shapes.small
                )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = leaf.name,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun buildTree(branches: List<String>): List<TreeNode> {
    val root = mutableMapOf<String, MutableList<TreeNode>>()

    branches.forEach { branch ->
        val parts = branch.split("/")

        if (parts.size == 1) {
            // Branch sans dossier
            root.getOrPut("") { mutableListOf() }.add(
                TreeNode.Leaf(name = parts[0], fullName = branch)
            )
        } else {
            // Branch dans un dossier
            val currentLevel = root.getOrPut(parts[0]) { mutableListOf() }
            addToTree(currentLevel, parts.drop(1), branch, parts[0])
        }
    }

    // Convertir la map en liste de TreeNode
    val result = mutableListOf<TreeNode>()

    // Ajouter les branches racine
    root[""]?.let { result.addAll(it) }

    // Ajouter les dossiers
    root.entries
        .filter { it.key.isNotEmpty() }
        .sortedBy { it.key }
        .forEach { (folderName, children) ->
            result.add(
                TreeNode.Folder(
                    name = folderName,
                    children = children.sortedBy {
                        when (it) {
                            is TreeNode.Folder -> it.name
                            is TreeNode.Leaf -> it.name
                        }
                    },
                    path = folderName
                )
            )
        }

    return result
}

private fun addToTree(
    currentLevel: MutableList<TreeNode>,
    remainingParts: List<String>,
    fullName: String,
    currentPath: String
) {
    if (remainingParts.size == 1) {
        // Dernier segment = leaf
        currentLevel.add(TreeNode.Leaf(name = remainingParts[0], fullName = fullName))
    } else {
        // Segment intermédiaire = folder
        val folderName = remainingParts[0]
        val newPath = "$currentPath/$folderName"

        val folder = currentLevel.find {
            it is TreeNode.Folder && it.name == folderName
        } as? TreeNode.Folder

        if (folder != null) {
            addToTree(
                (folder.children as MutableList<TreeNode>),
                remainingParts.drop(1),
                fullName,
                newPath
            )
        } else {
            val newChildren = mutableListOf<TreeNode>()
            addToTree(newChildren, remainingParts.drop(1), fullName, newPath)
            currentLevel.add(
                TreeNode.Folder(
                    name = folderName,
                    children = newChildren,
                    path = newPath
                )
            )
        }
    }
}
