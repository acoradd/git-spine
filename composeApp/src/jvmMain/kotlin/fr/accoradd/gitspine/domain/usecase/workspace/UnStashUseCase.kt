package fr.accoradd.gitspine.domain.usecase.workspace

import fr.accoradd.gitspine.domain.repository.GitRepository

class UnStashUseCase(private val gitRepository: GitRepository) {
    suspend operator fun invoke(stashName: String) {
        gitRepository.unstash(stashName)
    }
}
