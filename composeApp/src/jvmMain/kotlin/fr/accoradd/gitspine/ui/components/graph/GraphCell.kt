package fr.accoradd.gitspine.ui.components.graph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import fr.accoradd.gitspine.domain.model.Author
import fr.accoradd.gitspine.domain.model.EdgeType
import fr.accoradd.gitspine.domain.model.GraphEdge
import fr.accoradd.gitspine.ui.theme.graphColors


val CELL_SIZE = 30.dp
private val CIRCLE_RADIUS = 12.dp
private val MERGE_CIRCLE_RADIUS = 6.dp
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
    row: Int,
    column: Int,
    nodePosition: Int?,
    edges: List<GraphEdge>,
    modifier: Modifier = Modifier,
    author: Author,
    gravatars: Map<String, ImageBitmap?>
) {
    val color = graphColors[column % graphColors.size].border

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
        val bgWidth = mergeCircleRadiusPx * 4
        val offsetBgTop = (size.height - bgWidth) / 2

        if (nodePosition != null) {
            if (nodePosition < column) {
                drawRect(
                    color = graphColors[nodePosition % graphColors.size].bg,
                    topLeft = Offset(0f, offsetBgTop),
                    size = Size(size.width, bgWidth)
                )
            } else if (nodePosition == column) {
                val halfWidth = size.width / 2
                drawRect(
                    color = graphColors[nodePosition % graphColors.size].bg,
                    topLeft = Offset(halfWidth, offsetBgTop),
                    size = Size(halfWidth, bgWidth)
                )
            }
        }

        // Pour chaque edge, determiner ce qu'il faut dessiner dans cette cellule
        for (edge in edges) {
            val fromRow = edge.from.row
            val fromCol = edge.from.column
            val toRow = edge.to.row
            val toCol = edge.to.column

            val edgeColor = graphColors[fromCol % graphColors.size].border

            // L'edge va de fromRow vers toRow (toRow > fromRow car parent est plus ancien)
            // Cette cellule est a (row, column)

            when {
                // Cas 1: Ligne verticale - meme colonne, la cellule est entre from et to
                fromCol == column && toCol == column && row in fromRow..toRow -> {
                    // Dessiner une ligne verticale complete
                    val startY = if (row == fromRow) centerY else 0f
                    val endY = if (row == toRow) centerY else cellHeight

                    drawLine(
                        color = edgeColor,
                        start = Offset(centerX, startY),
                        end = Offset(centerX, endY),
                        strokeWidth = lineWidthPx,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // Dessiner le noeud (cercle) si present
        if (nodePosition == column) {
            val isMerge = edges.any { edge -> edge.type == EdgeType.Merge }
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
                        color = graphColors[column % graphColors.size].bg,
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

        // L'edge est pertinent si:
        // 1. Cette cellule est le point de depart
        if (row == fromRow && column == fromCol) return@filter true

        // 2. Cette cellule est le point d'arrivee
        if (row == toRow && column == toCol) return@filter true

        // 3. Cette cellule est sur une ligne verticale de l'edge
        if (fromCol == toCol && column == fromCol && row in fromRow..toRow) return@filter true

        // 4. Cette cellule est sur la diagonale de l'edge
        if (fromCol != toCol && row in fromRow..toRow) {
            val minCol = minOf(fromCol, toCol)
            val maxCol = maxOf(fromCol, toCol)
            if (column in minCol..maxCol) {
                return@filter true
            }
        }

        false
    }
}
