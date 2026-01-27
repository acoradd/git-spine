package fr.accoradd.gitspine.ui.components.graph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import fr.accoradd.gitspine.domain.model.GraphEdge
import fr.accoradd.gitspine.domain.model.GraphPosition
import fr.accoradd.gitspine.ui.theme.graphColors

private val CELL_SIZE = 30.dp
private val CIRCLE_RADIUS = 5.dp
private val LINE_WIDTH = 2.dp

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
    hasNode: Boolean,
    edges: List<GraphEdge>,
    modifier: Modifier = Modifier
) {
    val color = graphColors[column % graphColors.size].border

    Canvas(modifier = modifier.size(CELL_SIZE)) {
        val cellWidth = size.width
        val cellHeight = size.height
        val centerX = cellWidth / 2
        val centerY = cellHeight / 2
        val lineWidthPx = LINE_WIDTH.toPx()
        val circleRadiusPx = CIRCLE_RADIUS.toPx()

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

                // Cas 2: Diagonale - la cellule est le point de depart (from)
                row == fromRow && column == fromCol && fromCol != toCol -> {
                    // Dessiner du centre vers le bas, en direction de toCol
                    val targetX = if (toCol > fromCol) cellWidth else 0f
                    drawLine(
                        color = edgeColor,
                        start = Offset(centerX, centerY),
                        end = Offset(targetX, cellHeight),
                        strokeWidth = lineWidthPx,
                        cap = StrokeCap.Round
                    )
                }

                // Cas 3: Diagonale - la cellule est le point d'arrivee (to)
                row == toRow && column == toCol && fromCol != toCol -> {
                    // Dessiner du haut en direction de fromCol vers le centre
                    val sourceX = if (fromCol > toCol) cellWidth else 0f
                    drawLine(
                        color = edgeColor,
                        start = Offset(sourceX, 0f),
                        end = Offset(centerX, centerY),
                        strokeWidth = lineWidthPx,
                        cap = StrokeCap.Round
                    )
                }

                // Cas 4: Diagonale traversante - la cellule est entre from et to
                fromCol != toCol && row in (fromRow + 1) until toRow -> {
                    // Verifier si cette colonne est sur le chemin diagonal
                    val minCol = minOf(fromCol, toCol)
                    val maxCol = maxOf(fromCol, toCol)

                    if (column in minCol..maxCol) {
                        // Calculer si cette cellule est sur la diagonale
                        val progress = (row - fromRow).toFloat() / (toRow - fromRow).toFloat()
                        val expectedCol = fromCol + ((toCol - fromCol) * progress).toInt()

                        if (column == expectedCol || column == expectedCol + 1 || column == expectedCol - 1) {
                            // Cette cellule est sur ou proche de la diagonale
                            val enterX = if (toCol > fromCol) 0f else cellWidth
                            val exitX = if (toCol > fromCol) cellWidth else 0f

                            drawLine(
                                color = edgeColor,
                                start = Offset(enterX, 0f),
                                end = Offset(exitX, cellHeight),
                                strokeWidth = lineWidthPx,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }

                // Cas 5: Ligne verticale passante (passing lane) - meme colonne que from ou to
                // quand l'edge traverse plusieurs lignes
                (column == fromCol || column == toCol) && row in (fromRow + 1) until toRow -> {
                    // Ligne verticale passante
                    val passingColor = graphColors[column % graphColors.size].border
                    drawLine(
                        color = passingColor,
                        start = Offset(centerX, 0f),
                        end = Offset(centerX, cellHeight),
                        strokeWidth = lineWidthPx,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // Dessiner le noeud (cercle) si present
        if (hasNode) {
            // Cercle exterieur (bordure)
            drawCircle(
                color = color,
                radius = circleRadiusPx,
                center = Offset(centerX, centerY)
            )
            // Cercle interieur (remplissage)
            drawCircle(
                color = graphColors[column % graphColors.size].bg,
                radius = circleRadiusPx - lineWidthPx,
                center = Offset(centerX, centerY)
            )
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
