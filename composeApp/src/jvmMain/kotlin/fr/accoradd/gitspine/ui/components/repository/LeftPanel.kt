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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import fr.accoradd.gitspine.domain.model.Branch
import fr.accoradd.gitspine.ui.components.common.HorizontalDivider
import fr.accoradd.gitspine.ui.components.common.SectionHeader
import fr.accoradd.gitspine.ui.components.common.SimpleTextField
import fr.accoradd.gitspine.ui.theme.jewelColors
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.window.defaultTitleBarStyle

@Composable
fun RepositoryLeftPanel(

    localBranches: List<Branch>,
    remoteBranches: List<Branch>,
    tags: List<String> = emptyList(),

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

    val localBranchesNames = mapOf(Pair("", localBranches.map { it.name }))
    val remoteBranchesMap = remoteBranches
        .groupBy { it.name.substringBefore("/") }
        .mapValues { (_, branchesList) ->
            branchesList.map { it.name.substringAfter("/") }
        }
    val selectedBranch = localBranches.find { it.isHead }?.name ?: remoteBranches.find { it.isHead }?.name

    // Expansion states
    var remoteExpanded by remember { mutableStateOf(false) }
    var tagsExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize()
    ) {

        SearchRef(
            onLocalBranchSearch = onLocalBranchSearch,
            onRemoteBranchSearch = onRemoteBranchSearch,
            onTagSearch = onTagSearch
        )

        SectionBranch(
            "Local",
            true,
            localBranches,
            onLoadLocalBranches,
            localBranchesNames,
            selectedBranch,
            onBranchClick
        )

        SectionBranch(
            "Remote",
            false,
            localBranches,
            onLoadRemoteBranches,
            remoteBranchesMap,
            selectedBranch,
            onBranchClick,
            hasMoreRemoteBranches,
            onLoadMoreRemoteBranches
        )

        SectionTag(
            tagsExpanded,
            tags,
            onLoadTags,
            onTagClick,
            hasMoreTags,
            onLoadMoreTags
        )
    }
}

@Composable
private fun ColumnScope.SectionBranch(
    title: String,
    expandedByDefault: Boolean,
    branches: List<Branch>,
    onLoadBranches: () -> Unit,
    branchesNamesByOrigin: Map<String, List<String>>,
    selectedBranch: String?,
    onBranchClick: (String) -> Unit,
    hasMoreBranches: Boolean = false,
    onLoadMoreBranches: () -> Unit = {},
) {
    var expanded by remember { mutableStateOf(expandedByDefault) }
    var expandedFolders by remember { mutableStateOf(emptySet<String>()) }

    val onToggleFolder = { path: String ->
        expandedFolders = if (expandedFolders.contains(path)) {
            expandedFolders - path
        } else {
            expandedFolders + path
        }
    }

    LaunchedEffect(selectedBranch) {
        if (expandedByDefault) {
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
    }

    SectionHeader(
        title = title,
        expanded = expanded,
        onToggle = {
            expanded = !expanded
            if (expanded && branches.isEmpty()) {
                onLoadBranches()
            }
        }
    )

    if (expanded) {
        Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
            LazyScrollableContent {
                if (branches.isEmpty()) {
                    item { EmptyState("Aucune branche locale") }
                } else {
                    branchesNamesByOrigin.forEach { (origin, branches) ->
                        if (origin.isNotEmpty()) {
                            item(key = "origin-header-$origin") {
                                RemoteHeader(remoteName = origin)
                            }
                        }
                        branchTreeView(
                            branches = branches,
                            selectedBranch = selectedBranch,
                            onBranchClick = onBranchClick,
                            expandedFolders = expandedFolders,
                            onToggleFolder = onToggleFolder,
                            keyPrefix = "local",
                            baseLevel = 0,
                        )
                    }
                }
                if (hasMoreBranches) {
                    item { LoadMoreButton(onClick = onLoadMoreBranches) }
                }
            }
        }

        HorizontalDivider()
    }
}

@Composable
private fun ColumnScope.SectionTag(
    expandedByDefault: Boolean,
    tags: List<String>,
    onLoadTags: () -> Unit,
    onTagClick: (String) -> Unit,
    hasMoreTags: Boolean,
    onLoadMoreTags: () -> Unit
) {
    var expanded by remember { mutableStateOf(expandedByDefault) }
    SectionHeader(
        title = "Tags",
        expanded = expanded,
        onToggle = {
            expanded = !expanded
            if (expanded && tags.isEmpty()) {
                onLoadTags()
            }
        }
    )

    if (expanded) {
        Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
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

@Composable
private fun SearchRef(
    onLocalBranchSearch: (String) -> Unit,
    onRemoteBranchSearch: (String) -> Unit,
    onTagSearch: (String) -> Unit
) {
    val searchState = rememberTextFieldState("")

    SimpleTextField(
        state = searchState,
        modifier = Modifier.fillMaxWidth(),
        leadingContent = {
            Icon(
                key = AllIconsKeys.Actions.Find,
                contentDescription = "Search",
                modifier = Modifier,
                tint = JewelTheme.globalColors.text.info
            )
        },
        placeholder = {
            Text("Rechercher...", color = Color.Gray)
        },
        trailingContent = {
            if (searchState.text.isNotEmpty()) {
                IconActionButton(
                    key = AllIconsKeys.Actions.Close,
                    contentDescription = "Clear",
                    onClick = { searchState.clearText() },
                    modifier = Modifier.size(16.dp)
                        .pointerHoverIcon(PointerIcon.Hand)
                )
            }
        }
    )

    HorizontalDivider()

    LaunchedEffect(searchState.text) {
        onLocalBranchSearch(searchState.text.toString())
        onRemoteBranchSearch(searchState.text.toString())
        onTagSearch(searchState.text.toString())
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
private fun RemoteHeader(remoteName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text(text = remoteName)
        Spacer(modifier = Modifier.width(8.dp))
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
        modifier = Modifier.padding(start = 8.dp, end = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(
                    when {
                        isHovered -> JewelTheme.defaultTitleBarStyle.colors.background
                        else -> Color.Transparent
                    }
                )
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable(onClick = onClick)
                .hoverable(interactionSource)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                key = AllIconsKeys.General.Pin,
                contentDescription = tag
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(text = tag)
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
            color = jewelColors.grey(8)
        )
    }
}
