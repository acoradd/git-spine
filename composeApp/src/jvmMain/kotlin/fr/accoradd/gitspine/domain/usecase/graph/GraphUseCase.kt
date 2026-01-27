package fr.accoradd.gitspine.domain.usecase.graph

import fr.accoradd.gitspine.domain.model.Commit
import fr.accoradd.gitspine.domain.model.CommitOrWip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GraphUseCase {
    suspend operator fun invoke(wip: CommitOrWip.Wip?, commits: List<Commit>): Unit = withContext(Dispatchers.IO) {

    }
}
