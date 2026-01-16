package fr.accoradd.gitspine.ui.components.repository

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.accoradd.gitspine.ui.components.common.ExpandableSection

@Composable
fun RepositoryLeftPanel(
    localBranches: List<String> = emptyList(),
    remoteBranches: Map<String, List<String>> = emptyMap(),
    tags: List<String> = emptyList(),
    selectedBranch: String? = null,
    onBranchClick: (String) -> Unit = {},
    onTagClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Section: Branches locales
            ExpandableSection(
                title = "Branches locales (${localBranches.size})",
                initialExpanded = true
            ) {
                if (localBranches.isEmpty()) {
                    EmptyState("Aucune branche locale")
                } else {
                    BranchTreeView(
                        branches = localBranches,
                        selectedBranch = selectedBranch,
                        onBranchClick = onBranchClick
                    )
                }
            }

            // Section: Branches distantes
            ExpandableSection(
                title = "Branches distantes (${remoteBranches.values.sumOf { it.size }})",
                initialExpanded = false
            ) {
                if (remoteBranches.isEmpty()) {
                    EmptyState("Aucune branche distante")
                } else {
                    Column {
                        remoteBranches.forEach { (remote, branches) ->
                            RemoteSection(
                                remoteName = remote,
                                branches = branches,
                                selectedBranch = selectedBranch,
                                onBranchClick = onBranchClick
                            )
                        }
                    }
                }
            }

            // Section: Tags
            ExpandableSection(
                title = "Tags (${tags.size})",
                initialExpanded = false
            ) {
                if (tags.isEmpty()) {
                    EmptyState("Aucun tag")
                } else {
                    TagsList(
                        tags = tags,
                        onTagClick = onTagClick
                    )
                }
            }
        }
    }
}

@Composable
private fun RemoteSection(
    remoteName: String,
    branches: List<String>,
    selectedBranch: String?,
    onBranchClick: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    Column {
        // Remote header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = remoteName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "(${branches.size})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Branches du remote
        BranchTreeView(
            branches = branches,
            selectedBranch = selectedBranch,
            onBranchClick = onBranchClick,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun TagsList(
    tags: List<String>,
    onTagClick: (String) -> Unit
) {
    Column {
        tags.forEach { tag ->
            TagItem(
                tag = tag,
                onClick = { onTagClick(tag) }
            )
        }
    }
}

@Composable
private fun TagItem(
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 6.dp)
        ) {
            // Tag icon
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        MaterialTheme.colorScheme.tertiary,
                        shape = MaterialTheme.shapes.small
                    )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = tag,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
