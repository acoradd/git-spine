package fr.accoradd.gitspine.ui.components.repository

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
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

private val TreeNode.key: String
    get() = when (this) {
        is TreeNode.Folder -> "folder_${this.path}"
        is TreeNode.Leaf -> "leaf_${this.fullName}"
    }

fun LazyListScope.branchTreeView(
    branches: List<String>,
    selectedBranch: String?,
    onBranchClick: (String) -> Unit,
    expandedFolders: Set<String>,
    onToggleFolder: (String) -> Unit,
    baseLevel: Int = 0,
    keyPrefix: String = ""
) {
    val tree = buildTree(branches)
    
    val flatTree = mutableListOf<Pair<TreeNode, Int>>()
    fun addNodes(nodes: List<TreeNode>, level: Int) {
        nodes.forEach { node ->
            flatTree.add(node to level)
            if (node is TreeNode.Folder && expandedFolders.contains(node.path)) {
                addNodes(node.children, level + 1)
            }
        }
    }
    addNodes(tree, baseLevel)

    items(flatTree, key = { (node, _) -> "$keyPrefix-${node.key}" }) { (node, level) ->
        when (node) {
            is TreeNode.Folder -> {
                FolderItem(
                    folder = node,
                    level = level,
                    isExpanded = expandedFolders.contains(node.path),
                    onToggleExpand = { onToggleFolder(node.path) }
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
}

@Composable
private fun FolderItem(
    folder: TreeNode.Folder,
    level: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isHovered) MaterialTheme.colorScheme.surfaceContainerHighest
                else MaterialTheme.colorScheme.surface
            )
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(onClick = onToggleExpand)
            .hoverable(interactionSource)
            .horizontalScroll(rememberScrollState())
            .padding(start = (level * 16 + 8).dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(4.dp))

        Icon(
            imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
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
            overflow = TextOverflow.Visible
        )
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
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(onClick = onClick)
            .hoverable(interactionSource)
            .horizontalScroll(rememberScrollState())
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
            overflow = TextOverflow.Visible
        )
    }
}

private fun buildTree(branches: List<String>): List<TreeNode> {
    val root = mutableMapOf<String, MutableList<TreeNode>>()

    branches.forEach { branch ->
        val parts = branch.split("/")

        if (parts.size == 1) {
            root.getOrPut("") { mutableListOf() }.add(
                TreeNode.Leaf(name = parts[0], fullName = branch)
            )
        } else {
            val currentLevel = root.getOrPut(parts[0]) { mutableListOf() }
            addToTree(currentLevel, parts.drop(1), branch, parts[0])
        }
    }

    val priorityBranches = listOf("master", "main", "develop")

    fun getCategory(node: TreeNode): Int {
        return when {
            node is TreeNode.Leaf && node.name in priorityBranches -> 0
            node is TreeNode.Folder -> 1
            else -> 2
        }
    }

    val nodeComparator = compareBy(::getCategory)
        .thenBy {
            if (it is TreeNode.Leaf && it.name in priorityBranches) {
                priorityBranches.indexOf(it.name)
            } else {
                null
            }
        }
        .thenBy {
            when (it) {
                is TreeNode.Folder -> it.name
                is TreeNode.Leaf -> it.name
            }
        }

    fun sortRecursively(nodes: List<TreeNode>): List<TreeNode> {
        return nodes.map { node ->
            if (node is TreeNode.Folder) {
                node.copy(children = sortRecursively(node.children))
            } else {
                node
            }
        }.sortedWith(nodeComparator)
    }

    val unsortedTree = mutableListOf<TreeNode>()
    root[""]?.let { unsortedTree.addAll(it) }
    root.entries
        .filter { it.key.isNotEmpty() }
        .forEach { (folderName, children) ->
            unsortedTree.add(
                TreeNode.Folder(
                    name = folderName,
                    children = children,
                    path = folderName
                )
            )
        }

    return sortRecursively(unsortedTree)
}

private fun addToTree(
    currentLevel: MutableList<TreeNode>,
    remainingParts: List<String>,
    fullName: String,
    currentPath: String
) {
    if (remainingParts.size == 1) {
        currentLevel.add(TreeNode.Leaf(name = remainingParts[0], fullName = fullName))
    } else {
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
