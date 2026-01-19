package fr.accoradd.gitspine.domain.usecase.workspace

import fr.accoradd.gitspine.domain.model.WorkspaceStatus
import fr.accoradd.gitspine.domain.repository.GitRepository

class GetStatusUseCase(private val gitRepository: GitRepository) {
    suspend operator fun invoke(): WorkspaceStatus = gitRepository.getStatus()
}
