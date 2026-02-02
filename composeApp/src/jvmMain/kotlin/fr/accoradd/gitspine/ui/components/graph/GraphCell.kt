package fr.accoradd.gitspine.ui.components.graph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import fr.accoradd.gitspine.core.extension.drawRoundedCornerPath
import fr.accoradd.gitspine.domain.model.EdgeType
import fr.accoradd.gitspine.domain.model.GraphEdge
import fr.accoradd.gitspine.ui.components.repository.CommitData
import fr.accoradd.gitspine.ui.theme.graphColorsAlpha


val CELL_SIZE = 30.dp
private val CIRCLE_RADIUS = 12.dp
private val MERGE_CIRCLE_RADIUS = 6.dp
private val CORNER_RADIUS = 8.dp
private val LINE_WIDTH = 2.dp
private val SMALL_LINE_WIDTH = 1.dp

/**
 * Une cellule du graphe git (30x30 dp).
 * Dessine les segments d'edges qui passent par cette cellule et le noeud si present.
 *
 * @param row Index de ligne de cette cellule
 * @param column Index de colonne de cette cellule
 * @param hasNode true si un commit est positionne dans cette cellule
 * @param edges Liste des edges a considerer pour le dessin
 */
@Composable
fun GraphCell(
    commit: CommitData,
    column: Int,
    nodePosition: Int?,
    edges: List<GraphEdge>,
    modifier: Modifier = Modifier,
    gravatars: Map<String, ImageBitmap?>
) {
    val row = commit.row
    val author = commit.info.author
    val color = graphColorsAlpha.colors[column % graphColorsAlpha.size].copy(alpha = graphColorsAlpha.alphaBorder)

    val imageBitmap = gravatars[author.email]

    Canvas(modifier = modifier.size(CELL_SIZE)) {
        val cellWidth = size.width
        val cellHeight = size.height
        val centerX = cellWidth / 2
        val centerY = cellHeight / 2
        val lineWidthPx = LINE_WIDTH.toPx()
        val smallLineWidthPx = SMALL_LINE_WIDTH.toPx()
        val circleRadiusPx = CIRCLE_RADIUS.toPx()
        val mergeCircleRadiusPx = MERGE_CIRCLE_RADIUS.toPx()
        val cornerRadiusPx = CORNER_RADIUS.toPx();
        val bgWidth = mergeCircleRadiusPx * 4
        val offsetBgTop = (size.height - bgWidth) / 2
        val isMerge = commit.info.parents.size > 1

        if (nodePosition != null) {
            val colorBg = graphColorsAlpha.colors[nodePosition % graphColorsAlpha.size].copy(graphColorsAlpha.alphaBg)
            val colorBranchBg = graphColorsAlpha.colors[nodePosition % graphColorsAlpha.size].copy(graphColorsAlpha.alphaBranchBg)
            if (nodePosition < column) {
                drawRect(
                    color = colorBg,
                    topLeft = Offset(0f, offsetBgTop),
                    size = Size(size.width, bgWidth)
                )
            } else if (nodePosition == column) {
                drawRect(
                    color = colorBg,
                    topLeft = Offset(centerX, offsetBgTop),
                    size = Size(centerX, bgWidth)
                )
                if (commit.refs.isNotEmpty()) {
                    drawLine(
                        color = colorBranchBg,
                        start = Offset(0f, centerY),
                        end = Offset(centerX, centerY),
                        strokeWidth = smallLineWidthPx,
                        cap = StrokeCap.Butt
                    )
                }
            } else if (commit.refs.isNotEmpty()) {
                drawLine(
                    color = colorBranchBg,
                    start = Offset(0f, centerY),
                    end = Offset(cellWidth, centerY),
                    strokeWidth = smallLineWidthPx,
                    cap = StrokeCap.Butt
                )
            }
        }

        // Pour chaque edge, determiner ce qu'il faut dessiner dans cette cellule
        for (edge in edges) {
            val fromRow = edge.from.row
            val fromCol = edge.from.column
            val toRow = edge.to.row
            val toCol = edge.to.column
            val isEdgeMerge = edge.type == EdgeType.Merge
            val minRow = minOf(fromRow, toRow)
            val maxRow = maxOf(fromRow, toRow)
            val minCol = minOf(fromCol, toCol)
            val maxCol = maxOf(fromCol, toCol)

            val edgeColor = if (isEdgeMerge) {
                graphColorsAlpha.colors[toCol % graphColorsAlpha.size].copy(alpha = graphColorsAlpha.alphaBorder)
            } else graphColorsAlpha.colors[fromCol % graphColorsAlpha.size].copy(alpha = graphColorsAlpha.alphaBorder)

            val isPhantom = edge.type == EdgeType.Phantom

            if (isPhantom) {
                // Edge fantome: ligne verticale en pointilles
                if (column == fromCol && row in minRow..maxRow) {
                    val startY = if (row == fromRow) centerY else 0f

                    drawLine(
                        color = edgeColor,
                        start = Offset(centerX, startY),
                        end = Offset(centerX, cellHeight),
                        strokeWidth = lineWidthPx,
                        cap = StrokeCap.Butt
                    )
                }
            } else if (isEdgeMerge) {
                when {
                    // merge arrivant sur ce commit
                    row == fromRow && fromCol == column -> {
                        val endX = if (fromCol < toCol) cellWidth else 0f
                        drawLine(
                            color = edgeColor,
                            start = Offset(centerX, centerY),
                            end = Offset(endX, centerY),
                            strokeWidth = lineWidthPx,
                            cap = StrokeCap.Butt
                        )
                    }

                    // merge de cette colonne vers ce commit
                    row == fromRow && toCol == column -> {
                        if (fromCol < toCol) {
                            drawRoundedCornerPath(
                                color = edgeColor,
                                start = Offset(0f, centerY),
                                end = Offset(centerX, cellHeight),
                                strokeWidth = lineWidthPx,
                                cap = StrokeCap.Butt,
                                cornerRadius = cornerRadiusPx
                            )
                        } else {
                            drawRoundedCornerPath(
                                color = edgeColor,
                                start = Offset(centerX, cellHeight),
                                end = Offset(cellWidth, centerY),
                                strokeWidth = lineWidthPx,
                                cap = StrokeCap.Butt,
                                cornerRadius = cornerRadiusPx
                            )
                        }
                    }

                    // merge sur cette ligne
                    row == fromRow && column in minCol..maxCol -> {
                        drawLine(
                            color = edgeColor,
                            start = Offset(0f, centerY),
                            end = Offset(cellWidth, centerY),
                            strokeWidth = lineWidthPx,
                            cap = StrokeCap.Butt
                        )
                    }

                    column == toCol && row in minRow..maxRow -> {
                        val endY = if (row == toRow) centerY else cellHeight

                        drawLine(
                            color = edgeColor,
                            start = Offset(centerX, 0f),
                            end = Offset(centerX, endY),
                            strokeWidth = lineWidthPx,
                            cap = StrokeCap.Butt
                        )
                    }
                }
            } else {
                when {
                    // meme branche
                    toCol == column && fromCol == column && row in minRow..maxRow -> {
                        val startY = if (row == fromRow) centerY else 0f
                        val endY = if (row == toRow) centerY else cellHeight

                        drawLine(
                            color = edgeColor,
                            start = Offset(centerX, startY),
                            end = Offset(centerX, endY),
                            strokeWidth = lineWidthPx,
                            cap = StrokeCap.Butt
                        )
                    }

                    // merge de cette colonne vers ce commit
                    row == toRow && fromCol == column -> {
                        if (fromCol < toCol) {
                            drawRoundedCornerPath(
                                color = edgeColor,
                                start = Offset(cellWidth, centerY),
                                end = Offset(centerX, 0f),
                                strokeWidth = lineWidthPx,
                                cap = StrokeCap.Butt,
                                cornerRadius = cornerRadiusPx
                            )
                        } else {
                            drawRoundedCornerPath(
                                color = edgeColor,
                                start = Offset(centerX, 0f),
                                end = Offset(0f, centerY),
                                strokeWidth = lineWidthPx,
                                cap = StrokeCap.Butt,
                                cornerRadius = cornerRadiusPx
                            )
                        }
                    }

                    fromCol == column && row in minRow.. maxRow -> {
                        val startY = if (row == fromRow) centerY else 0f
                        val endY = if (row == toRow) centerY else cellHeight

                        drawLine(
                            color = edgeColor,
                            start = Offset(centerX, startY),
                            end = Offset(centerX, endY),
                            strokeWidth = lineWidthPx,
                            cap = StrokeCap.Butt
                        )
                    }

                    toRow == row && column == toCol -> {
                        val endX = if (column < fromCol) cellWidth else 0f
                        drawLine(
                            color = edgeColor,
                            start = Offset(centerX, centerY),
                            end = Offset(endX, centerY),
                            strokeWidth = lineWidthPx,
                            cap = StrokeCap.Butt
                        )
                    }

                    toRow == row && column in minCol.. maxCol -> {
                        drawLine(
                            color = edgeColor,
                            start = Offset(0f, centerY),
                            end = Offset(cellWidth, centerY),
                            strokeWidth = lineWidthPx,
                            cap = StrokeCap.Butt
                        )
                    }

                }
            }

            when {


                column == fromCol && row in minRow..maxRow -> {
                    val startY = if (row == fromRow) centerY else 0f
                    val endY = if (row == toRow) centerY else cellHeight

//                    drawLine(
//                        color = edgeColor,
//                        start = Offset(centerX, startY),
//                        end = Offset(centerX, endY),
//                        strokeWidth = lineWidthPx,
//                        cap = StrokeCap.Butt
//                    )
                }

                column == toCol && row in fromRow..toRow -> {
                    val startY = if (row == fromRow) centerY else 0f
                    val endY = if (row == toRow) centerY else cellHeight

//                    drawLine(
//                        color = edgeColor,
//                        start = Offset(centerX, startY),
//                        end = Offset(centerX, endY),
//                        strokeWidth = lineWidthPx,
//                        cap = StrokeCap.Butt
//                    )
                }

                else -> {
                    drawCircle(
                        color = edgeColor,
                        radius = smallLineWidthPx,
                        center = Offset(centerX, centerY)
                    )
                }
            }
        }

        // Dessiner le noeud (cercle) si present
        if (nodePosition == column) {
            if (isMerge) {
                drawCircle(
                    color = color,
                    radius = mergeCircleRadiusPx,
                    center = Offset(centerX, centerY)
                )
            } else {
                drawCircle(
                    color = color,
                    radius = circleRadiusPx,
                    center = Offset(centerX, centerY)
                )
                if (imageBitmap != null) {
                    val center = Offset(size.width / 2, size.height / 2)

                    // Créer un chemin circulaire
                    val path = Path().apply {
                        addOval(
                            androidx.compose.ui.geometry.Rect(
                                center.x - circleRadiusPx + lineWidthPx,
                                center.y - circleRadiusPx + lineWidthPx,
                                center.x + circleRadiusPx - lineWidthPx,
                                center.y + circleRadiusPx - lineWidthPx
                            )
                        )
                    }
                    clipPath(path) {
                        drawImage(
                            image = imageBitmap,
                            dstOffset = IntOffset((lineWidthPx * 2).toInt(), (lineWidthPx * 2).toInt()),
                            dstSize = IntSize(
                                ((circleRadiusPx) * 2 - lineWidthPx).toInt(),
                                ((circleRadiusPx - lineWidthPx) * 2).toInt()
                            )
                        )
                    }
                } else {
                    drawCircle(
                        color = graphColorsAlpha.colors[column % graphColorsAlpha.size].copy(alpha = graphColorsAlpha.alphaBg),
                        radius = circleRadiusPx - lineWidthPx,
                        center = Offset(centerX, centerY)
                    )
                }
            }
        }
    }
}

/**
 * Filtre les edges pertinents pour une cellule donnee.
 */
fun getEdgesForCell(row: Int, column: Int, allEdges: List<GraphEdge>): List<GraphEdge> {
    return allEdges.filter { edge ->
        val fromRow = edge.from.row
        val fromCol = edge.from.column
        val toRow = edge.to.row
        val toCol = edge.to.column

        val minCol = minOf(fromCol, toCol)
        val maxCol = maxOf(fromCol, toCol)
        val minRow = minOf(fromRow, toRow)
        val maxRow = maxOf(fromRow, toRow)

        if (edge.type == EdgeType.Phantom) {
            if (column == fromCol && row in minRow..maxRow) return@filter true
        } else if (edge.type == EdgeType.Merge) {
            if (row == fromRow && column in minCol..maxCol) return@filter true
            if (column == toCol && row in minRow..maxRow) return@filter true
        } else {
            if (row == toRow && column in minCol..maxCol) return@filter true
            if (column == fromCol && row in minRow..maxRow) return@filter true
        }

        return@filter false
    }
}
