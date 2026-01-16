package fr.accoradd.gitspine.domain.usecase.graph

import fr.accoradd.gitspine.domain.model.GraphNode
import fr.accoradd.gitspine.domain.repository.GitRepository
import kotlinx.coroutines.flow.Flow

class GetGraphUseCase(
    private val gitRepository: GitRepository
) {
    operator fun invoke(): Flow<List<GraphNode>> = gitRepository.getGraph()
}