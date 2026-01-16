package fr.accoradd.gitspine.ui.components.graph

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.accoradd.gitspine.domain.model.GraphNode

@Composable
fun GitGraph(
    nodes: List<GraphNode>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        // TODO: Implémenter le rendu du graph
        // - Dessiner les nœuds (commits)
        // - Dessiner les connexions (lignes entre commits)
        // - Gérer les couleurs par branche
    }
}
