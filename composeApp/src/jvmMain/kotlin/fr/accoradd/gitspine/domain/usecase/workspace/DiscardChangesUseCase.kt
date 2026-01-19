package fr.accoradd.gitspine.domain.usecase.workspace

import fr.accoradd.gitspine.domain.repository.GitRepository

class DiscardChangesUseCase(private val gitRepository: GitRepository) {
    suspend operator fun invoke(path: String, staged: Boolean) {
        gitRepository.discardChanges(path, staged)
    }
}
