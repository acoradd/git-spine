package fr.accoradd.gitspine.infrastructure.git

import fr.accoradd.gitspine.domain.model.*
import fr.accoradd.gitspine.domain.repository.GitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.nio.file.Path
import java.time.Instant

class JGitRepository : GitRepository {

    private var repo: Repository? = null
    private val _isOpen = MutableStateFlow(false)

    fun open(repoPath: Path) {
        println("JGitRepository: Opening repository at $repoPath")
        repo = FileRepositoryBuilder()
            .setGitDir(repoPath.resolve(".git").toFile())
            .build()
        _isOpen.value = true
        println("JGitRepository: Repository opened successfully")
    }

    fun close() {
        println("JGitRepository: Closing repository")
        repo?.close()
        repo = null
        _isOpen.value = false
        println("JGitRepository: Repository closed")
    }

    override fun getCommits(): Flow<List<Commit>> = flow {
        val repository = repo ?: run {
            emit(emptyList())
            return@flow
        }

        val commits = mutableListOf<Commit>()
        val git = Git(repository)

        try {
            val logs = git.log().setMaxCount(1000).call()

            for (revCommit in logs) {
                commits.add(
                    Commit(
                        id = revCommit.name,
                        shortId = revCommit.name.take(7),
                        message = revCommit.shortMessage,
                        author = Author(
                            name = revCommit.authorIdent.name,
                            email = revCommit.authorIdent.emailAddress
                        ),
                        timestamp = Instant.ofEpochSecond(revCommit.commitTime.toLong()),
                        parents = revCommit.parents.map { it.name }
                    )
                )
            }
        } catch (e: Exception) {
            // Log error but return empty list
            println("Error loading commits: ${e.message}")
        }

        emit(commits)
    }

    override fun getBranches(): Flow<List<Branch>> = flow {
        val repository = repo ?: run {
            emit(emptyList())
            return@flow
        }

        val branches = mutableListOf<Branch>()
        val headRef = repository.exactRef("HEAD")
        val currentBranchName = repository.branch

        // Local branches
        repository.refDatabase.getRefsByPrefix("refs/heads/").forEach { ref ->
            branches.add(
                Branch(
                    name = ref.name.removePrefix("refs/heads/"),
                    isRemote = false,
                    isHead = ref.name.removePrefix("refs/heads/") == currentBranchName,
                    commitId = ref.objectId.name
                )
            )
        }

        // Remote branches
        repository.refDatabase.getRefsByPrefix("refs/remotes/").forEach { ref ->
            branches.add(
                Branch(
                    name = ref.name.removePrefix("refs/remotes/"),
                    isRemote = true,
                    isHead = false,
                    commitId = ref.objectId.name
                )
            )
        }

        emit(branches)
    }

    override fun getTags(): Flow<List<String>> = flow {
        val repository = repo ?: run {
            emit(emptyList())
            return@flow
        }

        val tags = mutableListOf<String>()

        // Get all tags
        repository.refDatabase.getRefsByPrefix("refs/tags/").forEach { ref ->
            tags.add(ref.name.removePrefix("refs/tags/"))
        }

        emit(tags.sorted())
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
