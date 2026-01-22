package fr.accoradd.gitspine.domain.usecase.workspace

import fr.accoradd.gitspine.domain.repository.GitRepository

class StashUseCase(private val gitRepository: GitRepository) {
    suspend operator fun invoke() {
        gitRepository.stash()
    }
}
