package fr.accoradd.gitspine.infrastructure.git

import fr.accoradd.gitspine.domain.model.*
import fr.accoradd.gitspine.domain.repository.GitRepository
import fr.accoradd.gitspine.infrastructure.filesystem.FileWatcher
import fr.accoradd.gitspine.infrastructure.filesystem.GitIgnoreLoader
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.nio.file.Path
import java.time.Instant

class JGitRepository : GitRepository {

    private var repo: Repository? = null
    private val _isOpen = MutableStateFlow(false)

    // File watching
    private var fileWatcher: FileWatcher? = null
    private var watcherScope: CoroutineScope? = null
    private val gitIgnoreLoader = GitIgnoreLoader()
    private val _statusUpdates = MutableSharedFlow<Unit>(replay = 1)
    val statusUpdates: SharedFlow<Unit> = _statusUpdates

    fun open(repoPath: Path) {
        println("JGitRepository: Opening repository at $repoPath")
        repo = FileRepositoryBuilder()
            .setGitDir(repoPath.resolve(".git").toFile())
            .build()
        _isOpen.value = true

        // Start file watcher
        watcherScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        startFileWatcher()

        println("JGitRepository: Repository opened successfully")
    }

    fun close() {
        println("JGitRepository: Closing repository")

        // Stop file watcher
        fileWatcher?.close()
        watcherScope?.cancel()
        fileWatcher = null
        watcherScope = null

        repo?.close()
        repo = null
        _isOpen.value = false
        println("JGitRepository: Repository closed")
    }

    override fun getCommits(skip: Int, limit: Int): Flow<List<Commit>> = flow {
        val repository = repo ?: run {
            emit(emptyList())
            return@flow
        }

        val commits = mutableListOf<Commit>()
        val git = Git(repository)

        try {
            val logs = git.log().setMaxCount(skip + limit).call()

            // Skip the first 'skip' commits and take 'limit' commits
            logs.asSequence()
                .drop(skip)
                .take(limit)
                .forEach { revCommit ->
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

    override suspend fun getStatus(): WorkspaceStatus = withContext(Dispatchers.IO) {
        val repository = repo ?: return@withContext WorkspaceStatus()
        val git = Git(repository)

        try {
            val status = git.status().call()

            val staged = buildList {
                status.added.forEach { add(FileStatus(it, FileStatusType.ADDED)) }
                status.changed.forEach { add(FileStatus(it, FileStatusType.MODIFIED)) }
                status.removed.forEach { add(FileStatus(it, FileStatusType.DELETED)) }
            }

            val unstaged = buildList {
                status.untracked.forEach { add(FileStatus(it, FileStatusType.UNTRACKED)) }
                status.modified.forEach { add(FileStatus(it, FileStatusType.MODIFIED)) }
                status.missing.forEach { add(FileStatus(it, FileStatusType.DELETED)) }
                status.conflicting.forEach { add(FileStatus(it, FileStatusType.CONFLICTING)) }
            }

            WorkspaceStatus(staged = staged, unstaged = unstaged)
        } catch (e: Exception) {
            println("Error getting status: ${e.message}")
            WorkspaceStatus()
        }
    }

    override suspend fun stage(path: String) = withContext(Dispatchers.IO) {
        val repository = repo ?: return@withContext
        Git(repository).add().addFilepattern(path).call()
    }

    override suspend fun unstage(path: String) = withContext(Dispatchers.IO) {
        val repository = repo ?: return@withContext
        Git(repository).reset().addPath(path).call()
    }

    override suspend fun stageAll() = withContext(Dispatchers.IO) {
        val repository = repo ?: return@withContext
        Git(repository).add().addFilepattern(".").setUpdate(true).call()
    }

    override suspend fun unstageAll() = withContext(Dispatchers.IO) {
        val repository = repo ?: return@withContext
        Git(repository).reset().call()
    }

    override suspend fun discardChanges(path: String, staged: Boolean) = withContext(Dispatchers.IO) {
        val repository = repo ?: return@withContext
        val git = Git(repository)
        if (staged) {
            git.reset().addPath(path).call()
        }
        git.checkout().addPath(path).call()
    }

    override suspend fun commit(message: String) = withContext(Dispatchers.IO) {
        val repository = repo ?: return@withContext
        Git(repository).commit().setMessage(message).call()
    }

    private fun startFileWatcher() {
        val repository = repo ?: return
        watcherScope?.launch {
            try {
                val ignoreRules = gitIgnoreLoader.loadIgnoreRules(repository)
                val watcher = FileWatcher(this)
                fileWatcher = watcher

                watcher.watch(
                    workspacePath = repository.workTree.toPath(),
                    gitDirPath = repository.directory.toPath(),
                    gitIgnoreRules = ignoreRules
                )

                watcher.events.collect { event ->
                    when (event) {
                        is WatcherEvent.WorkspaceChanged -> _statusUpdates.emit(Unit)
                        is WatcherEvent.WatcherError -> println("Watcher error: ${event.message}")
                    }
                }
            } catch (e: Exception) {
                println("Error starting file watcher: ${e.message}")
            }
        }
    }
}
