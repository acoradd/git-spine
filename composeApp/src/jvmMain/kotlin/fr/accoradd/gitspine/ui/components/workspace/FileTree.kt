package fr.accoradd.gitspine.ui.components.workspace

import fr.accoradd.gitspine.domain.model.FileStatus
import fr.accoradd.gitspine.domain.model.FileStatusType

/**
 * Represents a node in the file tree (either a folder or a file)
 */
sealed class FileTreeNode {
    abstract val name: String
    abstract val path: String

    data class Folder(
        override val name: String,
        override val path: String,
        val children: MutableList<FileTreeNode> = mutableListOf()
    ) : FileTreeNode() {
        fun allFiles(): List<File> {
            return children.flatMap {
                when (it) {
                    is File -> listOf(it)
                    is Folder -> it.allFiles()
                }
            }
        }
    }

    data class File(
        override val name: String,
        override val path: String,
        val status: FileStatus
    ) : FileTreeNode()
}


/**
 * Simplified tree builder - creates a clean hierarchical structure
 */
fun buildFileTreeSimple(files: List<FileStatus>): List<FileTreeNode> {
    val root = FileTreeNode.Folder("", "", mutableListOf())

    files.forEach { fileStatus ->
        val parts = fileStatus.path.replace("\\", "/").split("/")
        var currentFolder = root

        // Navigate through folders, creating them if needed
        for (i in 0 until parts.size - 1) {
            val folderName = parts[i]
            val folderPath = parts.subList(0, i + 1).joinToString("/")

            var childFolder = currentFolder.children
                .filterIsInstance<FileTreeNode.Folder>()
                .find { it.name == folderName }

            if (childFolder == null) {
                childFolder = FileTreeNode.Folder(folderName, folderPath, mutableListOf())
                currentFolder.children.add(childFolder)
            }

            currentFolder = childFolder
        }

        // Add the file
        currentFolder.children.add(
            FileTreeNode.File(
                name = parts.last(),
                path = fileStatus.path,
                status = fileStatus
            )
        )
    }

    // Sort all levels: folders first, then alphabetically
    fun sortNode(node: FileTreeNode) {
        if (node is FileTreeNode.Folder) {
            node.children.sortWith(compareBy(
                { it !is FileTreeNode.Folder },
                { it.name.lowercase() }
            ))
            node.children.forEach { sortNode(it) }
        }
    }
    sortNode(root)

    // Compact the tree: merge folders with single folder child
    val compactedChildren = root.children.map { compactNode(it) }

    return compactedChildren
}

/**
 * Compacts a tree node by merging folders that have only one folder child
 * Example: src -> main -> java becomes src/main/java
 */
private fun compactNode(node: FileTreeNode): FileTreeNode {
    return when (node) {
        is FileTreeNode.File -> node
        is FileTreeNode.Folder -> {
            // First, compact all children recursively
            val compactedChildren = node.children.map { compactNode(it) }.toMutableList()

            // If this folder has exactly one child and that child is a folder, merge them
            if (compactedChildren.size == 1 && compactedChildren[0] is FileTreeNode.Folder) {
                val childFolder = compactedChildren[0] as FileTreeNode.Folder
                // Merge: combine names with "/"
                FileTreeNode.Folder(
                    name = if (node.name.isEmpty()) childFolder.name else "${node.name}/${childFolder.name}",
                    path = childFolder.path,
                    children = childFolder.children
                )
            } else {
                // Keep the folder as is, but with compacted children
                FileTreeNode.Folder(
                    name = node.name,
                    path = node.path,
                    children = compactedChildren
                )
            }
        }
    }
}
