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
import fr.accoradd.gitspine.domain.model.Tag
import fr.accoradd.gitspine.ui.components.common.HorizontalDivider
import fr.accoradd.gitspine.ui.components.common.SectionHeader
import fr.accoradd.gitspine.ui.components.common.SimpleTextField
import fr.accoradd.gitspine.ui.theme.jewelColors
import fr.accoradd.gitspine.ui.viewmodel.RepositoryScreenViewModel
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.window.defaultTitleBarStyle

@Composable
fun RepositoryLeftPanel(
    repositoryScreenViewModel: RepositoryScreenViewModel,

    localBranches: List<Branch>,
    remoteBranches: List<Branch>,
    tags: List<Tag> = emptyList(),

    // Callbacks
    onBranchClick: (String) -> Unit = {},
    onTagClick: (Tag) -> Unit = {},

    onSearch: (String, Boolean, Boolean, Boolean) -> Unit,

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
    var localExpanded by remember { mutableStateOf(true) }
    var remoteExpanded by remember { mutableStateOf(false) }
    var tagsExpanded by remember { mutableStateOf(false) }

    val hasMoreRemoteBranches = repositoryScreenViewModel.hasMoreRemoteBranch.collectAsState()
    val hasMoreTags = repositoryScreenViewModel.hasMoreTags.collectAsState()

    Column(
        modifier = modifier.fillMaxSize()
    ) {

        SearchRef(
            onSearch = {
                onSearch(it, localExpanded, remoteExpanded, tagsExpanded)
            }
        )

        SectionBranch(
            "Local",
            expanded = localExpanded,
            toggleExpanded = {
                localExpanded = !localExpanded

                if (localExpanded) {
                    repositoryScreenViewModel.loadLocalBranches()
                } else {
                    repositoryScreenViewModel.clearLocalBranches()
                }
            },
            branches = localBranches,
            branchesNamesByOrigin = localBranchesNames,
            selectedBranch = selectedBranch,
            onBranchClick = onBranchClick
        )

        SectionBranch(
            "Remote",
            expanded = remoteExpanded,
            toggleExpanded = {
                remoteExpanded = !remoteExpanded

                if (remoteExpanded) {
                    repositoryScreenViewModel.loadRemoteBranches(true)
                } else {
                    repositoryScreenViewModel.clearRemoteBranches()
                }
            },
            branches = remoteBranches,
            branchesNamesByOrigin = remoteBranchesMap,
            selectedBranch = selectedBranch,
            onBranchClick = onBranchClick,
            hasMoreBranches = hasMoreRemoteBranches.value,
            onLoadMoreBranches = { repositoryScreenViewModel.loadRemoteBranches() }
        )

        SectionTag(
            expanded = tagsExpanded,
            toggleExpanded = {
                tagsExpanded = !tagsExpanded

                if (tagsExpanded) {
                    repositoryScreenViewModel.loadTags(true)
                } else {
                    repositoryScreenViewModel.clearTags()
                }
            },
            tags = tags,
            onTagClick = onTagClick,
            hasMoreTags = hasMoreTags.value,
            onLoadMoreTags = { repositoryScreenViewModel.loadTags() }
        )
    }
}

@Composable
private fun ColumnScope.SectionBranch(
    title: String,
    expanded: Boolean,
    toggleExpanded: () -> Unit,
    branches: List<Branch>,
    branchesNamesByOrigin: Map<String, List<String>>,
    selectedBranch: String?,
    onBranchClick: (String) -> Unit,
    hasMoreBranches: Boolean = false,
    onLoadMoreBranches: () -> Unit = {},
) {
    var expandedFolders by remember { mutableStateOf(emptySet<String>()) }

    val onToggleFolder = { path: String ->
        expandedFolders = if (expandedFolders.contains(path)) {
            expandedFolders - path
        } else {
            expandedFolders + path
        }
    }

    LaunchedEffect(selectedBranch) {
        if (expanded) {
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
        onToggle = { toggleExpanded() }
    )

    if (expanded) {
        Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
            LazyScrollableContent {
                if (branches.isEmpty()) {
                    item { EmptyState("Aucune branche") }
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
    expanded: Boolean,
    toggleExpanded: () -> Unit,
    tags: List<Tag>,
    onTagClick: (Tag) -> Unit,
    hasMoreTags: Boolean,
    onLoadMoreTags: () -> Unit
) {
    SectionHeader(
        title = "Tags",
        expanded = expanded,
        onToggle = { toggleExpanded() }
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
    onSearch: (String) -> Unit
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
        onSearch(searchState.text.toString())
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
        Text(text = remoteName, color = JewelTheme.globalColors.text.info)
        Spacer(modifier = Modifier.width(8.dp))
    }
}

@Composable
private fun TagItem(
    tag: Tag,
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
                contentDescription = tag.name
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(text = tag.name)
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
            color = JewelTheme.globalColors.text.info
        )
    }
}
