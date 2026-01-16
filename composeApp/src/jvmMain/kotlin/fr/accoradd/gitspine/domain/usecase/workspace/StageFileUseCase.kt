package fr.accoradd.gitspine.domain.usecase.workspace

import fr.accoradd.gitspine.domain.repository.GitRepository

class StageFileUseCase(
    private val gitRepository: GitRepository
) {
    suspend operator fun invoke(path: String) = gitRepository.stage(path)
}