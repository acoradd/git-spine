package fr.accoradd.gitspine.domain.usecase.workspace

import fr.accoradd.gitspine.domain.repository.GitRepository

class CreateBranchUseCase(private val gitRepository: GitRepository) {
    suspend operator fun invoke(name: String) {
        gitRepository.createBranch(name)
    }
}
