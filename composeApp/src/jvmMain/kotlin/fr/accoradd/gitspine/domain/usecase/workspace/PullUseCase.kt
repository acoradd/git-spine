package fr.accoradd.gitspine.domain.usecase.workspace

import fr.accoradd.gitspine.domain.repository.GitRepository

class PullUseCase(private val gitRepository: GitRepository) {
    suspend operator fun invoke() {
        gitRepository.pull()
    }
}
