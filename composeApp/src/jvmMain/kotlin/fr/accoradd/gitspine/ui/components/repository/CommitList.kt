package fr.accoradd.gitspine.ui.components.repository

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.accoradd.gitspine.domain.model.GraphResult
import fr.accoradd.gitspine.ui.components.graph.GraphCell
import fr.accoradd.gitspine.ui.components.graph.getEdgesForCell
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.CircularProgressIndicator
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.VerticalScrollbar
import org.jetbrains.jewel.window.defaultTitleBarStyle
import java.awt.Cursor

data class TableColumn(
    val id: String,
    val title: String,
    val defaultWidth: Float,
    val minWidth: Float = 0.05f,
    val resizable: Boolean = true
)

data class CommitData(
    val hash: String,
    val shortHash: String,
    val message: String,
    val author: String,
    val date: String,
    val isSelected: Boolean = false,
    val row: Int = 0
)

@Composable
fun CommitList(
    commits: List<CommitData> = emptyList(),
    graphResult: GraphResult? = null,
    onCommitClick: (CommitData) -> Unit = {},
    onLoadMore: () -> Unit = {},
    hasMore: Boolean = false,
    modifier: Modifier = Modifier
) {
    val columns = remember {
        listOf(
            TableColumn(id = "branch", title = "Ref", defaultWidth = 0.1f, resizable = false),
            TableColumn(id = "graph", title = "Graph", defaultWidth = 0.3f),
            TableColumn(id = "message", title = "Message", defaultWidth = 0.4f),
            TableColumn(id = "date", title = "Date", defaultWidth = 0.2f)
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

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        ResizableColumnsHeader(
            columns = columns,
            columnWidths = columnWidths,
            onWidthChanged = { columnId, newWidth ->
                columnWidths[columnId] = newWidth
            }
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
                        graphResult = graphResult,
                        onClick = { onCommitClick(commit) },
                        onWidthChanged = { columnId, newWidth ->
                            columnWidths[columnId] = newWidth
                        }
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
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            VerticalScrollbar(
                scrollState = listState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun ResizableColumnsHeader(
    columns: List<TableColumn>,
    columnWidths: Map<String, Float>,
    onWidthChanged: (String, Float) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .border(1.dp, JewelTheme.defaultTitleBarStyle.colors.background)
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
                    contentAlignment = if (index == columns.size - 1) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Text(
                        text = column.title,
                        color = JewelTheme.globalColors.text.disabled,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                ResizeColumnsDivider(index, columns, columnWidths, totalWidth, column, onWidthChanged)
            }
        }
    }
}

@Composable
private fun CommitRow(
    commit: CommitData,
    columns: List<TableColumn>,
    columnWidths: Map<String, Float>,
    graphResult: GraphResult?,
    onClick: () -> Unit,
    onWidthChanged: (String, Float) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .background(
                when {
                    commit.isSelected -> JewelTheme.defaultTitleBarStyle.colors.titlePaneButtonHoveredBackground
                    else -> Color.Transparent
                }
            )
            .clickable(onClick = onClick)
            .hoverable(interactionSource)
    ) {
        val totalWidth = constraints.maxWidth.toFloat()

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            columns.forEachIndexed { index, column ->
                val width = columnWidths[column.id] ?: column.defaultWidth

                Box(
                    modifier = Modifier
                        .weight(width)
                        .fillMaxHeight()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp),
                    contentAlignment = if (index == columns.size - 1) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    when (column.id) {
                        "branch" -> {
                            Text(text = "BRANCH")
                        }

                        "graph" -> {
                            if (graphResult != null && graphResult.width > 0) {
                                val position = graphResult.positions[commit.hash]
                                Row {
                                    for (col in 0 until graphResult.width) {
                                        val hasNode = position?.column == col
                                        val cellEdges = getEdgesForCell(commit.row, col, graphResult.edges)
                                        GraphCell(
                                            row = commit.row,
                                            column = col,
                                            hasNode = hasNode,
                                            edges = cellEdges
                                        )
                                    }
                                }
                            } else {
                                Text(text = "●")
                            }
                        }

                        "hash" -> {
                            Text(
                                text = commit.shortHash,
                                maxLines = 1,
                                overflow = TextOverflow.Visible
                            )
                        }

                        "message" -> {
                            Text(
                                text = commit.message,
                                maxLines = 1,
                                overflow = TextOverflow.Visible
                            )
                        }

                        "date" -> {
                            Text(
                                text = commit.date,
                                maxLines = 1,
                                overflow = TextOverflow.Visible
                            )
                        }
                    }
                }

                ResizeColumnsDivider(index, columns, columnWidths, totalWidth, column, onWidthChanged)
            }
        }
    }


}


@Composable
private fun ResizeColumnsDivider(
    index: Int,
    columns: List<TableColumn>,
    columnWidths: Map<String, Float>,
    totalWidth: Float,
    column: TableColumn,
    onWidthChanged: (String, Float) -> Unit
) {
    var columnWidthsAtStartOfDrag = columnWidths.toMap()
    val currentColumn = columns[index]
    val nextColumn = columns.getOrNull(index + 1)
    if (index < columns.size - 1) {
        ColumnDivider(
            resizable = column.resizable,
            onDragStart = { columnWidthsAtStartOfDrag = columnWidths.toMap() },
            onPositionChange = { deltaX ->

                val dragRatio = deltaX / totalWidth

                val widthAtStartOfDrag = columnWidthsAtStartOfDrag[column.id] ?: column.defaultWidth
                val ratio = widthAtStartOfDrag + dragRatio
                val newCurrentWidth = ratio.coerceAtLeast(currentColumn.minWidth)
                val dragRatioFinal = newCurrentWidth - widthAtStartOfDrag

                if (nextColumn != null) {
                    val nextColumnWidthAtStartOfDrag = columnWidthsAtStartOfDrag[nextColumn.id] ?: column.defaultWidth
                    val nextColumnRatio = nextColumnWidthAtStartOfDrag - dragRatioFinal
                    val newNextWidth = nextColumnRatio.coerceAtLeast(nextColumn.minWidth)

                    if (newCurrentWidth >= currentColumn.minWidth && nextColumnRatio >= nextColumn.minWidth) {
                        onWidthChanged(currentColumn.id, newCurrentWidth)
                        onWidthChanged(nextColumn.id, newNextWidth)
                    }
                } else if (newCurrentWidth >= currentColumn.minWidth) {
                    onWidthChanged(currentColumn.id, newCurrentWidth)
                }
            }
        )
    }
}

@Composable
private fun ColumnDivider(
    onDragStart: () -> Unit,
    onPositionChange: (absoluteX: Float) -> Unit,
    resizable: Boolean = true
) {

    val modifier = if (resizable) Modifier
        .pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))
        .pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown()
                onDragStart()
                var totalAccumulatedDelta = 0f
                drag(down.id) { change ->
                    val dragAmount = change.position.x - change.previousPosition.x
                    totalAccumulatedDelta += dragAmount
                    onPositionChange(totalAccumulatedDelta)
                    change.consume()
                }
            }
        } else Modifier

    Row(
        modifier = modifier.width(4.dp).fillMaxHeight(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .background(JewelTheme.defaultTitleBarStyle.colors.background)
                .fillMaxHeight()
                .width(1.dp)
        )
    }
}
