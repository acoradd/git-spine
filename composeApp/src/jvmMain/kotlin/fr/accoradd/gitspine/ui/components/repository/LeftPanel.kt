package fr.accoradd.gitspine.ui.components.repository

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import fr.accoradd.gitspine.ui.components.common.SectionHeader
import fr.accoradd.gitspine.ui.components.common.SearchField

@Composable
fun RepositoryLeftPanel(
    localBranches: List<String> = emptyList(),
    remoteBranches: Map<String, List<String>> = emptyMap(),
    tags: List<String> = emptyList(),
    selectedBranch: String? = null,
    
    // Search states
    localBranchSearchQuery: String = "",
    remoteBranchSearchQuery: String = "",
    tagSearchQuery: String = "",
    
    // Pagination states
    hasMoreRemoteBranches: Boolean = false,
    hasMoreTags: Boolean = false,
    
    // Callbacks
    onBranchClick: (String) -> Unit = {},
    onTagClick: (String) -> Unit = {},
    
    onLoadLocalBranches: () -> Unit = {},
    onLocalBranchSearch: (String) -> Unit = {},
    
    onLoadRemoteBranches: () -> Unit = {},
    onLoadMoreRemoteBranches: () -> Unit = {},
    onRemoteBranchSearch: (String) -> Unit = {},
    
    onLoadTags: () -> Unit = {},
    onLoadMoreTags: () -> Unit = {},
    onTagSearch: (String) -> Unit = {},
    
    modifier: Modifier = Modifier
) {
    // Expansion states
    var localExpanded by remember { mutableStateOf(true) }
    var remoteExpanded by remember { mutableStateOf(false) }
    var tagsExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Section: Branches locales
            val localBranchesCount = if (localBranches.size >= 100) "99+" else localBranches.size.toString()
            
            SectionHeader(
                title = "Branches locales ($localBranchesCount)",
                expanded = localExpanded,
                onToggle = { 
                    localExpanded = !localExpanded
                    if (localExpanded && localBranches.isEmpty()) {
                        onLoadLocalBranches()
                    }
                }
            )
            
            if (localExpanded) {
                Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    // Pinned Search field
                    SearchField(
                        value = localBranchSearchQuery,
                        onValueChange = onLocalBranchSearch,
                        onDebouncedValueChange = onLocalBranchSearch,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        placeholder = "Chercher..."
                    )
                    
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        ScrollableContent {
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
                    }
                }
            }

            // Section: Branches distantes
            val remoteBranchesCount = remoteBranches.values.sumOf { it.size }
            val remoteBranchesDisplay = if (remoteBranchesCount >= 100) "99+" else remoteBranchesCount.toString()
            
            SectionHeader(
                title = "Branches distantes ($remoteBranchesDisplay)",
                expanded = remoteExpanded,
                onToggle = { 
                    remoteExpanded = !remoteExpanded
                    if (remoteExpanded && remoteBranches.isEmpty()) {
                        onLoadRemoteBranches()
                    }
                }
            )
            
            if (remoteExpanded) {
                Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    // Pinned Search field
                    SearchField(
                        value = remoteBranchSearchQuery,
                        onValueChange = onRemoteBranchSearch,
                        onDebouncedValueChange = onRemoteBranchSearch,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        placeholder = "Chercher..."
                    )
                    
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        ScrollableContent {
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
                                    
                                    if (hasMoreRemoteBranches) {
                                        LoadMoreButton(onClick = onLoadMoreRemoteBranches)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section: Tags
            val tagsCount = if (tags.size >= 100) "99+" else tags.size.toString()
            
            SectionHeader(
                title = "Tags ($tagsCount)",
                expanded = tagsExpanded,
                onToggle = { 
                    tagsExpanded = !tagsExpanded
                    if (tagsExpanded && tags.isEmpty()) {
                        onLoadTags()
                    }
                }
            )
            
            if (tagsExpanded) {
                Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    // Pinned Search field
                    SearchField(
                        value = tagSearchQuery,
                        onValueChange = onTagSearch,
                        onDebouncedValueChange = onTagSearch,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        placeholder = "Chercher..."
                    )
                    
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        ScrollableContent {
                            if (tags.isEmpty()) {
                                EmptyState("Aucun tag")
                            } else {
                                TagsList(
                                    tags = tags,
                                    onTagClick = onTagClick
                                )
                                
                                if (hasMoreTags) {
                                    LoadMoreButton(onClick = onLoadMoreTags)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScrollableContent(content: @Composable ColumnScope.() -> Unit) {
    val scrollState = rememberScrollState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            content()
        }
        
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState)
        )
    }
}

@Composable
private fun LoadMoreButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        TextButton(
            onClick = onClick,
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
        ) {
            Text("Charger plus...")
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
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 6.dp)
                .pointerHoverIcon(PointerIcon.Hand),
            verticalAlignment = Alignment.CenterVertically
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
