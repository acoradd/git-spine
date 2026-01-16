package fr.accoradd.gitspine.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.accoradd.gitspine.domain.model.GraphNode
import fr.accoradd.gitspine.domain.usecase.graph.GetGraphUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GraphState(
    val nodes: List<GraphNode> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class GraphViewModel(
    private val getGraphUseCase: GetGraphUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(GraphState())
    val state: StateFlow<GraphState> = _state.asStateFlow()

    fun loadGraph() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                getGraphUseCase().collect { nodes ->
                    _state.update { it.copy(nodes = nodes, isLoading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}
