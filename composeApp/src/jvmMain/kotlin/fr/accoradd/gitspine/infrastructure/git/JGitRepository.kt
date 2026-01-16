package fr.accoradd.gitspine.infrastructure.git

import fr.accoradd.gitspine.domain.model.*
import fr.accoradd.gitspine.domain.repository.GitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.nio.file.Path

class JGitRepository : GitRepository {

    private var repo: Repository? = null
    private val _isOpen = MutableStateFlow(false)

    fun open(repoPath: Path) {
        repo = FileRepositoryBuilder()
            .setGitDir(repoPath.resolve(".git").toFile())
            .build()
        _isOpen.value = true
    }

    fun close() {
        repo?.close()
        repo = null
        _isOpen.value = false
    }

    override fun getCommits(): Flow<List<Commit>> = flow {
        // TODO: Implémenter avec JGit
        emit(emptyList())
    }

    override fun getBranches(): Flow<List<Branch>> = flow {
        // TODO: Implémenter avec JGit
        emit(emptyList())
    }

    override fun getGraph(): Flow<List<GraphNode>> = flow {
        // TODO: Implémenter l'algo de construction du graph
        emit(emptyList())
    }

    override suspend fun stage(path: String) {
        // TODO: Implémenter avec JGit
    }

    override suspend fun unstage(path: String) {
        // TODO: Implémenter avec JGit
    }

    override suspend fun commit(message: String) {
        // TODO: Implémenter avec JGit
    }
}
