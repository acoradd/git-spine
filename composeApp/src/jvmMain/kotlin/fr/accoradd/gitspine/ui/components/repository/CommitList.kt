package fr.accoradd.gitspine.ui.components.repository

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.awt.Cursor

data class CommitColumn(
    val id: String,
    val title: String,
    val defaultWidth: Float,
    val minWidth: Float = 0.05f
)

data class CommitData(
    val hash: String,
    val shortHash: String,
    val message: String,
    val author: String,
    val date: String,
    val isSelected: Boolean = false
)

@Composable
fun CommitList(
    commits: List<CommitData> = emptyList(),
    onCommitClick: (CommitData) -> Unit = {},
    onLoadMore: () -> Unit = {},
    hasMore: Boolean = false,
    modifier: Modifier = Modifier
) {
    val columns = remember {
        listOf(
            CommitColumn(id = "graph", title = "Graph", defaultWidth = 0.1f),
            CommitColumn(id = "hash", title = "Hash", defaultWidth = 0.1f),
            CommitColumn(id = "message", title = "Message", defaultWidth = 0.4f),
            CommitColumn(id = "author", title = "Author", defaultWidth = 0.2f),
            CommitColumn(id = "date", title = "Date", defaultWidth = 0.2f)
        )
    }

    val columnWidths = remember {
        mutableStateMapOf<String, Float>().apply {
            columns.forEach { put(it.id, it.defaultWidth) }
        }
    }

    val listState = rememberLazyListState()

    // Détecter quand on arrive vers la fin de la liste
    LaunchedEffect(listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index) {
        val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        val totalItems = listState.layoutInfo.totalItemsCount

        // Charger plus quand on est à 10 éléments de la fin
        if (hasMore && totalItems > 0 && lastVisibleIndex >= totalItems - 10) {
            onLoadMore()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            // Header
            ResizableColumnsHeader(
                columns = columns,
                columnWidths = columnWidths,
                onWidthChanged = { columnId, newWidth ->
                    columnWidths[columnId] = newWidth
                }
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )

            // Commit list with scrollbar
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState
                ) {
                    items(commits) { commit ->
                        CommitRow(
                            commit = commit,
                            columns = columns,
                            columnWidths = columnWidths,
                            onClick = { onCommitClick(commit) }
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            thickness = 0.5.dp
                        )
                    }

                    // Loading indicator at the end if there's more
                    if (hasMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                }

                VerticalScrollbar(
                    adapter = rememberScrollbarAdapter(listState),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun ResizableColumnsHeader(
    columns: List<CommitColumn>,
    columnWidths: Map<String, Float>,
    onWidthChanged: (String, Float) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        val totalWidth = constraints.maxWidth.toFloat()

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            columns.forEachIndexed { index, column ->
                val width = columnWidths[column.id] ?: column.defaultWidth

                // Column header
                Box(
                    modifier = Modifier
                        .weight(width)
                        .fillMaxHeight()
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = column.title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Divider (except after last column)
                if (index < columns.size - 1) {
                    ColumnDivider(
                        onDrag = { delta ->
                            val currentColumn = columns[index]
                            val nextColumn = columns[index + 1]
                            val currentWidth = columnWidths[currentColumn.id] ?: currentColumn.defaultWidth
                            val nextWidth = columnWidths[nextColumn.id] ?: nextColumn.defaultWidth

                            val deltaRatio = delta / totalWidth
                            val newCurrentWidth = (currentWidth + deltaRatio).coerceAtLeast(currentColumn.minWidth)
                            val newNextWidth = (nextWidth - deltaRatio).coerceAtLeast(nextColumn.minWidth)

                            // Only update if both constraints are satisfied
                            if (newCurrentWidth >= currentColumn.minWidth && newNextWidth >= nextColumn.minWidth) {
                                onWidthChanged(currentColumn.id, newCurrentWidth)
                                onWidthChanged(nextColumn.id, newNextWidth)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnDivider(
    onDrag: (delta: Float) -> Unit
) {
    Box(
        modifier = Modifier
            .width(4.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.outlineVariant)
            .pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x)
                }
            }
    )
}

@Composable
private fun CommitRow(
    commit: CommitData,
    columns: List<CommitColumn>,
    columnWidths: Map<String, Float>,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = if (commit.isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            columns.forEach { column ->
                val width = columnWidths[column.id] ?: column.defaultWidth

                Box(
                    modifier = Modifier
                        .weight(width)
                        .fillMaxHeight()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                        when (column.id) {
                            "graph" -> {
                                // Placeholder for graph visualization
                                Text(
                                    text = "●",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            "hash" -> {
                                Text(
                                    text = commit.shortHash,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Visible
                                )
                            }
                            "message" -> {
                                Text(
                                    text = commit.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (commit.isSelected) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Visible
                                )
                            }
                            "author" -> {
                                Text(
                                    text = commit.author,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (commit.isSelected) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Visible
                                )
                            }
                            "date" -> {
                                Text(
                                    text = commit.date,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (commit.isSelected) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Visible
                                )
                            }
                        }
                    }
                }
            }
        }
}
