package fr.accoradd.gitspine.domain.model

/**
 * Position d'un commit dans le graphe.
 * @param row Index vertical (ligne dans la liste de commits, 0 = WIP ou premier commit)
 * @param column Index horizontal (colonne/lane du graphe)
 */
data class GraphPosition(
    val row: Int,
    val column: Int
)

/**
 * Type d'arete dans le graphe.
 */
enum class EdgeType {
    /** Arete normale (premier parent) */
    Normal,
    /** Arete de merge (parent secondaire) */
    Merge
}

/**
 * Arete entre deux commits dans le graphe.
 */
data class GraphEdge(
    val from: GraphPosition,
    val to: GraphPosition,
    val type: EdgeType
)

/**
 * Resultat du calcul du graphe.
 * @param positions Map commitId -> position dans le graphe
 * @param edges Liste de toutes les aretes
 * @param width Nombre de colonnes utilisees
 */
data class GraphResult(
    val positions: Map<String, GraphPosition>,
    val edges: List<GraphEdge>,
    val width: Int
)
