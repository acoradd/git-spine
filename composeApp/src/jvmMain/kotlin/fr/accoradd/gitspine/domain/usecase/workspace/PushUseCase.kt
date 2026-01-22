package fr.accoradd.gitspine.domain.usecase.workspace

import fr.accoradd.gitspine.domain.repository.GitRepository

class PushUseCase(private val gitRepository: GitRepository) {
    suspend operator fun invoke(distantBranch: String? = null, force: Boolean = false, pushTags: Boolean = false) {
        gitRepository.push(distantBranch, force, pushTags)
    }
}
