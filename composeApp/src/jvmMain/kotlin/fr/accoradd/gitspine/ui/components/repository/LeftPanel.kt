package fr.accoradd.gitspine.ui.components.repository

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import fr.accoradd.gitspine.ui.components.common.SearchField
import fr.accoradd.gitspine.ui.components.common.SectionHeader
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.VerticalScrollbar
import org.jetbrains.jewel.ui.component.Link
import fr.accoradd.gitspine.ui.theme.jewelColors

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
    var expandedFolders by remember { mutableStateOf(emptySet<String>()) }

    LaunchedEffect(selectedBranch) {
        selectedBranch?.let {
            val parts = it.split('/')
            if (parts.size > 1) {
                val pathsToExpand = (1 until parts.size).map { i ->
                    parts.take(i).joinToString("/")
                }
                expandedFolders = expandedFolders + pathsToExpand
            }
        }
    }

    val onToggleFolder = { path: String ->
        expandedFolders = if (expandedFolders.contains(path)) {
            expandedFolders - path
        } else {
            expandedFolders + path
        }
    }

    // Jewel gère le fond par défaut, pas besoin de Surface
    Column(
        modifier = modifier.fillMaxSize()
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
                SearchField(
                    value = localBranchSearchQuery,
                    onValueChange = onLocalBranchSearch,
                    onDebouncedValueChange = onLocalBranchSearch,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    placeholder = "Chercher..."
                )

                LazyScrollableContent {
                    if (localBranches.isEmpty()) {
                        item { EmptyState("Aucune branche locale") }
                    } else {
                        branchTreeView(
                            branches = localBranches,
                            selectedBranch = selectedBranch,
                            onBranchClick = onBranchClick,
                            expandedFolders = expandedFolders,
                            onToggleFolder = onToggleFolder,
                            keyPrefix = "local"
                        )
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
                SearchField(
                    value = remoteBranchSearchQuery,
                    onValueChange = onRemoteBranchSearch,
                    onDebouncedValueChange = onRemoteBranchSearch,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    placeholder = "Chercher..."
                )

                LazyScrollableContent {
                    if (remoteBranches.isEmpty()) {
                        item { EmptyState("Aucune branche distante") }
                    } else {
                        remoteBranches.forEach { (remote, branches) ->
                            item(key = "remote-header-$remote") {
                                RemoteHeader(remoteName = remote, branchesCount = branches.size)
                            }
                            branchTreeView(
                                branches = branches,
                                selectedBranch = selectedBranch,
                                onBranchClick = onBranchClick,
                                expandedFolders = expandedFolders,
                                onToggleFolder = onToggleFolder,
                                baseLevel = 1,
                                keyPrefix = "remote-$remote"
                            )
                        }
                        if (hasMoreRemoteBranches) {
                            item { LoadMoreButton(onClick = onLoadMoreRemoteBranches) }
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
                SearchField(
                    value = tagSearchQuery,
                    onValueChange = onTagSearch,
                    onDebouncedValueChange = onTagSearch,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    placeholder = "Chercher..."
                )

                LazyScrollableContent {
                    if (tags.isEmpty()) {
                        item { EmptyState("Aucun tag") }
                    } else {
                        items(tags, key = { "tag-$it" }) { tag ->
                            TagItem(
                                tag = tag,
                                onClick = { onTagClick(tag) }
                            )
                        }
                        if (hasMoreTags) {
                            item { LoadMoreButton(onClick = onLoadMoreTags) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LazyScrollableContent(modifier: Modifier = Modifier, content: LazyListScope.() -> Unit) {
    val scrollState = rememberLazyListState()

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(0.dp),
            state = scrollState
        ) {
            content()
        }

        VerticalScrollbar(
            scrollState = scrollState,
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
        )
    }
}

@Composable
private fun LoadMoreButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Link(
            text = "Charger plus...",
            onClick = onClick
        )
    }
}

@Composable
private fun RemoteHeader(remoteName: String, branchesCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(jewelColors.grey(3)) // Fond légèrement plus foncé
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text(
            text = remoteName,
            color = jewelColors.blue(4)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "($branchesCount)",
            color = jewelColors.grey(8)
        )
    }
}

@Composable
private fun TagItem(
    tag: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isHovered) jewelColors.grey(3) else jewelColors.grey(1)
            )
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(onClick = onClick)
            .hoverable(interactionSource)
            .padding(horizontal = 24.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tag icon (simple box for now)
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(jewelColors.yellow(4))
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = tag,
            color = jewelColors.grey(12)
        )
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
            color = jewelColors.grey(8)
        )
    }
}
