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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import fr.accoradd.gitspine.domain.model.Commit
import fr.accoradd.gitspine.domain.model.GraphResult
import fr.accoradd.gitspine.ui.components.graph.CELL_SIZE
import fr.accoradd.gitspine.ui.components.graph.GraphCell
import fr.accoradd.gitspine.ui.components.graph.getEdgesForCell
import fr.accoradd.gitspine.ui.theme.graphColors
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
    val maxWidth: Float? = null,
    val resizable: Boolean = true,
)

data class CommitData(
    val info: Commit,
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
    gravatars: Map<String, ImageBitmap?>
) {
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val totalWidth = constraints.maxWidth.toFloat()

        var columns by remember {
            mutableStateOf(
                getTableColumns(density, graphResult, totalWidth)
            )
        }

        val columnWidths = remember {
            mutableStateMapOf<String, Float>().apply {
                columns.forEach { put(it.id, it.defaultWidth) }
            }
        }

        val listState = rememberLazyListState()

        LaunchedEffect(graphResult?.width, density, totalWidth) {
            columns = getTableColumns(density, graphResult, totalWidth)

            columns.forEachIndexed { index, column ->
                if (column.maxWidth != null && column.maxWidth < columnWidths[column.id]!!) {
                    val diff = columnWidths[column.id]!! - column.maxWidth
                    columnWidths[column.id] = column.maxWidth
                    columnWidths[columns[index + 1].id] = columnWidths[columns[index + 1].id]!! + diff
                }
            }
        }

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
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            ResizableColumnsHeader(
                columns = columns,
                columnWidths = columnWidths,
                onWidthChanged = { columnId, newWidth ->
                    columnWidths[columnId] = newWidth
                },
                totalWidth = totalWidth
            )

            Spacer(modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(JewelTheme.defaultTitleBarStyle.colors.background)
            )

            val horizontalGraphScrollState = rememberScrollState()
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
                            },
                            gravatars = gravatars,
                            totalWidth = totalWidth,
                            horizontalGraphScrollState = horizontalGraphScrollState
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
}

private fun getTableColumns(
    density: Density,
    graphResult: GraphResult?,
    totalWidth: Float
): List<TableColumn> = listOf(
    TableColumn(id = "branch", title = "Ref", defaultWidth = 0.1f, resizable = false),
    TableColumn(
        id = "graph",
        title = "Graph",
        defaultWidth = 0.3f,
        maxWidth = with(density) { graphResult?.width?.takeIf { it > 0 }?.let { CELL_SIZE * it }?.toPx()?.let { (it / totalWidth).coerceAtLeast(0.01f) } }),
    TableColumn(id = "message", title = "Message", defaultWidth = 0.4f),
    TableColumn(id = "date", title = "Date", defaultWidth = 0.2f)
)

@Composable
private fun ResizableColumnsHeader(
    columns: List<TableColumn>,
    columnWidths: Map<String, Float>,
    onWidthChanged: (String, Float) -> Unit,
    totalWidth: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            columns.forEachIndexed { index, column ->
                val width = (columnWidths[column.id] ?: column.defaultWidth).coerceAtLeast(0.01f)

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
    onWidthChanged: (String, Float) -> Unit,
    gravatars: Map<String, ImageBitmap?>,
    totalWidth: Float,
    horizontalGraphScrollState: ScrollState
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
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val position = graphResult?.positions[commit.info.id]?.column
            columns.forEachIndexed { index, column ->
                val width = (columnWidths[column.id] ?: column.defaultWidth).coerceAtLeast(0.01f)
                val padding = if (column.id == "graph") 0.dp else 12.dp


                Box(
                    modifier = Modifier
                        .weight(width)
                        .fillMaxHeight()
                        .horizontalScroll(if (column.id === "graph") horizontalGraphScrollState else rememberScrollState())
                        .padding(horizontal = padding),
                    contentAlignment = if (index == columns.size - 1) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    when (column.id) {
                        "branch" -> {
                            Text(text = "")
                        }

                        "graph" -> {
                            if (graphResult != null && graphResult.width > 0) {
                                Row {
                                    for (col in 1 until graphResult.width + 1) {
                                        val cellEdges = getEdgesForCell(commit.row, col, graphResult.edges)
                                        GraphCell(
                                            commit = commit,
                                            column = col,
                                            nodePosition = position,
                                            edges = cellEdges,
                                            gravatars = gravatars
                                        )
                                    }
                                }
                            } else {
                                Text(text = "●")
                            }
                        }

                        "hash" -> {
                            Text(
                                text = commit.info.shortId,
                                maxLines = 1,
                                overflow = TextOverflow.Visible,
                                style = JewelTheme.editorTextStyle
                            )
                        }

                        "message" -> {
                            Text(
                                text = commit.info.message,
                                maxLines = 1,
                                overflow = TextOverflow.Visible
                            )
                        }

                        "date" -> {
                            Text(
                                text = commit.date,
                                maxLines = 1,
                                overflow = TextOverflow.Visible,
                                style = JewelTheme.editorTextStyle
                            )
                        }
                    }
                }

                ResizeColumnsDivider(
                    index,
                    columns,
                    columnWidths,
                    totalWidth,
                    column,
                    onWidthChanged,
                    color = if (column.id === "graph") position?.let { graphColors[it % graphColors.size].border } else null,
                    sliderModifier = if (column.id === "graph") Modifier.width(2.dp).padding(vertical = 2.dp) else null
                )
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
    onWidthChanged: (String, Float) -> Unit,
    color: Color? = null,
    sliderModifier: Modifier? = null
) {
    var columnWidthsAtStartOfDrag = columnWidths.toMap()
    val currentColumn = columns[index]
    val nextColumn = columns.getOrNull(index + 1)
    if (index < columns.size - 1) {
        ColumnDivider(
            color = color,
            sliderModifier = sliderModifier,
            resizable = column.resizable,
            onDragStart = { columnWidthsAtStartOfDrag = columnWidths.toMap() },
            onPositionChange = { deltaX ->

                val dragRatio = deltaX / totalWidth

                val widthAtStartOfDrag = columnWidthsAtStartOfDrag[column.id] ?: column.defaultWidth
                val ratio = widthAtStartOfDrag + dragRatio
                val newCurrentWidth = ratio.coerceIn(currentColumn.minWidth, currentColumn.maxWidth)
                val dragRatioFinal = newCurrentWidth - widthAtStartOfDrag

                if (nextColumn != null) {
                    val nextColumnWidthAtStartOfDrag = columnWidthsAtStartOfDrag[nextColumn.id] ?: column.defaultWidth
                    val nextColumnRatio = nextColumnWidthAtStartOfDrag - dragRatioFinal
                    val newNextWidth = nextColumnRatio.coerceIn(nextColumn.minWidth, nextColumn.maxWidth)

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
    resizable: Boolean = true,
    color: Color? = null,
    sliderModifier: Modifier? = null
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
            modifier = (sliderModifier ?: Modifier.width(1.dp))
                .background(color ?: JewelTheme.defaultTitleBarStyle.colors.background)
                .fillMaxHeight()
        )
    }
}
