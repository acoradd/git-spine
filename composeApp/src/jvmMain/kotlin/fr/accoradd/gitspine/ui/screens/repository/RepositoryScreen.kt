package fr.accoradd.gitspine.ui.screens.repository

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import fr.accoradd.gitspine.ui.components.graph.GitGraph
import fr.accoradd.gitspine.ui.viewmodel.GraphViewModel

@Composable
fun RepositoryScreen(
    viewModel: GraphViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        GitGraph(
            nodes = state.nodes,
            modifier = Modifier.weight(1f)
        )
    }
}
