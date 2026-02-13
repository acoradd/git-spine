package fr.accoradd.gitspine.infrastructure.git

import fr.accoradd.gitspine.core.notifications.NotificationManager
import fr.accoradd.gitspine.domain.model.*
import fr.accoradd.gitspine.domain.repository.GitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevSort
import org.eclipse.jgit.revwalk.RevTag
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.revwalk.filter.CommitTimeRevFilter
import java.time.Instant

class JGitRepository(
    private val session: GitSession,
    private val notificationManager: NotificationManager
) : GitRepository {

    private val _statusUpdates = MutableSharedFlow<Unit>(replay = 1)
    val statusUpdates: SharedFlow<Unit> = _statusUpdates

    private val repoState
        get(): GitRepositoryState? {
            return session.activeRepository.value
        }

    override suspend fun getHeadCommitId(): String? = withContext(Dispatchers.IO) {
        val repository = repoState?.repository ?: return@withContext null
        repository.resolve("HEAD")?.name
    }

    override fun getCommits(
        beforeTimestamp: Instant?,
        excludeCommitId: String?,
        limit: Int
    ): Flow<List<Commit>> = flow {
        val repository = repoState?.repository ?: run {
            emit(emptyList())
            return@flow
        }

        val commits = mutableListOf<Commit>()

        RevWalk(repository).use { walk ->
            try {
                // Tri par date (plus récent d'abord)
                walk.sort(RevSort.COMMIT_TIME_DESC)

                // Filtre par timestamp pour sauter directement aux commits pertinents
                if (beforeTimestamp != null) {
                    // +1 seconde car le filtre est inclusif et on veut exclusif
                    walk.revFilter = CommitTimeRevFilter.before(beforeTimestamp.plusSeconds(1))
                }

                // Ajouter tous les refs comme points de départ
                repository.refDatabase.refs.forEach { ref ->
                    try {
                        val objectId = ref.objectId ?: ref.leaf?.objectId
                        if (objectId != null) {
                            walk.markStart(walk.parseCommit(objectId))
                        }
                    } catch (_: Exception) {
                        // Ignorer les refs qui ne pointent pas vers des commits
                    }
                }

                var count = 0
                for (revCommit in walk) {
                    // Exclure le commit spécifié (évite les doublons si même timestamp)
                    if (revCommit.name == excludeCommitId) continue

                    if (count >= limit) break

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
                    count++
                }
            } catch (e: Exception) {
                println("Error loading commits: ${e.message}")
            }
        }

        emit(commits)
    }.flowOn(Dispatchers.IO)

    override fun getLocalBranches(search: String?): Flow<List<Branch>> = flow {
        val repository = repoState?.repository ?: run {
            emit(emptyList())
            return@flow
        }

        val headRef = repository.exactRef("HEAD")
        val currentBranchName = repository.branch
        val filter = search?.lowercase()

        val localBranches = repository.refDatabase.getRefsByPrefix("refs/heads/").asSequence()
            .map { ref ->
                Branch(
                    name = ref.name.removePrefix("refs/heads/"),
                    isRemote = false,
                    isHead = ref.name.removePrefix("refs/heads/") == currentBranchName,
                    commitId = ref.objectId.name
                )
            }
            .filter { filter == null || it.name.lowercase().contains(filter) }
            .sortedBy { it.name }
            .toList()

        emit(localBranches)
    }.flowOn(Dispatchers.IO)

    override fun getRemoteBranches(): Flow<List<Branch>> = flow {
        val repository = repoState?.repository ?: run {
            emit(emptyList())
            return@flow
        }

        val remoteBranches = repository.refDatabase.getRefsByPrefix("refs/remotes/").asSequence()
            .filter { !it.name.endsWith("HEAD") }
            .map { ref ->
                Branch(
                    name = ref.name.removePrefix("refs/remotes/"),
                    isRemote = true,
                    isHead = false,
                    commitId = ref.objectId.name
                )
            }
            .sortedBy { it.name }
            .toList()

        emit(remoteBranches)
    }.flowOn(Dispatchers.IO)

    override fun getTags(): Flow<List<Tag>> = flow {
        val repository = repoState?.repository ?: run {
            emit(emptyList())
            return@flow
        }

        val walk = RevWalk(repository)

        try {
            val tags = repository.refDatabase.getRefsByPrefix("refs/tags/").asSequence()
                .map { ref ->
                    val tagName = ref.name.removePrefix("refs/tags/")
                    val objectId = ref.objectId
                    val commit: RevCommit? = try {
                        when (val revObject = walk.parseAny(objectId)) {
                            is RevTag -> {
                                // If it's an annotated tag, get the tagged object (usually a commit)
                                val target = walk.parseAny(revObject.`object`)
                                target as? RevCommit
                            }

                            is RevCommit -> revObject
                            else -> null
                        }
                    } catch (e: Exception) {
                        null
                    }
                    val tag = Tag(tagName, commit?.name ?: objectId.name)
                    Triple(tag, commit?.commitTime ?: 0, ref)
                }
                .sortedByDescending { (_, time, _) -> time }
                .map { (name, _, _) -> name }
                .toList()

            emit(tags)
        } finally {
            walk.close()
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getStatus(): WorkspaceStatus = withContext(Dispatchers.IO) {
        val repository = repoState?.repository ?: return@withContext WorkspaceStatus()
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
        val repository = repoState?.repository ?: return@withContext
        Git(repository).add().addFilepattern(path).call()
    }

    override suspend fun unstage(path: String) = withContext(Dispatchers.IO) {
        val repository = repoState?.repository ?: return@withContext
        Git(repository).reset().addPath(path).call()
    }

    override suspend fun stageAll() = withContext(Dispatchers.IO) {
        val repository = repoState?.repository ?: return@withContext
        Git(repository).add().addFilepattern(".").setUpdate(true).call()
    }

    override suspend fun unstageAll() = withContext(Dispatchers.IO) {
        val repository = repoState?.repository ?: return@withContext
        Git(repository).reset().call()
    }

    override suspend fun discardChanges(path: String, staged: Boolean) = withContext(Dispatchers.IO) {
        val repository = repoState?.repository ?: return@withContext
        val git = Git(repository)
        if (staged) {
            git.reset().addPath(path).call()
        }
        git.checkout().addPath(path).call()
    }

    override suspend fun commit(message: String) = withContext(Dispatchers.IO) {
        val repository = repoState?.repository ?: return@withContext
        Git(repository).commit()
            .setMessage(message)
            .call()
    }

    override suspend fun fetch() = withContext(Dispatchers.IO) {
        val repository = repoState?.repository ?: return@withContext
        val cmd = Git(repository).fetch()
        val monitor = NotificationProgressMonitor(
            notificationManager = notificationManager,
            cmd = cmd,
            title = "Fetch",
            startMessage = "Fetching",
            successMessage = "Fetch réussi"
        )
        cmd.setProgressMonitor(monitor)
        try {
            monitor.call()
        } catch (e: Exception) {
            println("Error fetching: ${e.message}")
        }
    }

    override suspend fun pull() = withContext(Dispatchers.IO) {
        val repository = repoState?.repository ?: return@withContext
        val cmd = Git(repository).pull()
        val monitor = NotificationProgressMonitor(
            notificationManager = notificationManager,
            cmd = cmd,
            title = "Pull",
            startMessage = "Pulling",
            successMessage = "Pull réussi"
        )
        cmd.setProgressMonitor(monitor)
        try {
            monitor.call()
        } catch (e: Exception) {
            println("Error pulling: ${e.message}")
        }
    }

    override suspend fun push(distantBranch: String?, force: Boolean, pushTags: Boolean) = withContext(Dispatchers.IO) {
        val repository = repoState?.repository ?: return@withContext
        val cmd = Git(repository).push().setForce(force)

        if (distantBranch != null) {
            cmd.setRemote(distantBranch)
        }
        if (pushTags) {
            cmd.setPushTags()
        }

        val monitor = NotificationProgressMonitor(
            notificationManager = notificationManager,
            cmd = cmd,
            title = "Push",
            startMessage = "Pushing",
            successMessage = "Push réussi"
        )
        cmd.setProgressMonitor(monitor)
        try {
            monitor.call()
        } catch (e: Exception) {
            println("Error pushing: ${e.message}")
        }
    }

    override suspend fun stash() = withContext(Dispatchers.IO) {
        val repository = repoState?.repository ?: return@withContext
        try {
            Git(repository).stashCreate().setIncludeUntracked(true).call()
            notificationManager.createNotification(
                title = "Stash",
                message = "Stash créé avec succès",
                status = Notification.Status.Success
            )
        } catch (e: Exception) {
            notificationManager.createNotification(
                title = "Stash",
                message = e.message ?: "Erreur inconnue",
                status = Notification.Status.Error
            )
        }
    }

    override suspend fun unstash(stashName: String) = withContext(Dispatchers.IO) {
        val repository = repoState?.repository ?: return@withContext
        try {
            Git(repository).stashApply().setStashRef(stashName).call()
            notificationManager.createNotification(
                title = "Unstash",
                message = "Stash appliqué avec succès",
                status = Notification.Status.Success
            )
        } catch (e: Exception) {
            notificationManager.createNotification(
                title = "Unstash",
                message = e.message ?: "Erreur inconnue",
                status = Notification.Status.Error
            )
        }
    }

    override suspend fun createBranch(name: String) = withContext(Dispatchers.IO) {
        val repository = repoState?.repository ?: return@withContext
        val git = Git(repository)
        try {
            git.branchCreate().setName(name).call()
            git.checkout().setName(name).call()
            notificationManager.createNotification(
                title = "Create branch",
                message = "Branche créée avec succès",
                status = Notification.Status.Success
            )
        } catch (e: Exception) {
            notificationManager.createNotification(
                title = "Create branch",
                message = e.message ?: "Erreur inconnue",
                status = Notification.Status.Error
            )
        }
    }

    override suspend fun checkout(commitId: String) = withContext(Dispatchers.IO) {
        val repository = repoState?.repository ?: return@withContext
        val git = Git(repository)
        try {
            git.checkout().setName(commitId).call()
            notificationManager.createNotification(
                title = "Checkout",
                message = "Checkout vers ${commitId.take(7)} réussi",
                status = Notification.Status.Success
            )
        } catch (e: Exception) {
            notificationManager.createNotification(
                title = "Checkout",
                message = e.message ?: "Erreur inconnue",
                status = Notification.Status.Error
            )
        }
    }

    override suspend fun createBranchFromCommit(name: String, commitId: String) = withContext(Dispatchers.IO) {
        val repository = repoState?.repository ?: return@withContext
        val git = Git(repository)
        try {
            git.branchCreate()
                .setName(name)
                .setStartPoint(commitId)
                .call()
            git.checkout().setName(name).call()
            notificationManager.createNotification(
                title = "Create branch",
                message = "Branche '$name' créée depuis ${commitId.take(7)}",
                status = Notification.Status.Success
            )
        } catch (e: Exception) {
            notificationManager.createNotification(
                title = "Create branch",
                message = e.message ?: "Erreur inconnue",
                status = Notification.Status.Error
            )
        }
    }

    override suspend fun reset(commitId: String, mode: ResetMode) = withContext(Dispatchers.IO) {
        val repository = repoState?.repository ?: return@withContext
        val git = Git(repository)
        try {
            val resetMode = when (mode) {
                ResetMode.SOFT -> ResetCommand.ResetType.SOFT
                ResetMode.MIXED -> ResetCommand.ResetType.MIXED
                ResetMode.HARD -> ResetCommand.ResetType.HARD
            }
            git.reset()
                .setMode(resetMode)
                .setRef(commitId)
                .call()
            notificationManager.createNotification(
                title = "Reset",
                message = "Reset ${mode.name.lowercase()} vers ${commitId.take(7)} réussi",
                status = Notification.Status.Success
            )
        } catch (e: Exception) {
            notificationManager.createNotification(
                title = "Reset",
                message = e.message ?: "Erreur inconnue",
                status = Notification.Status.Error
            )
        }
    }

    override suspend fun revert(commitId: String) = withContext(Dispatchers.IO) {
        val repository = repoState?.repository ?: return@withContext
        val git = Git(repository)
        try {
            val objectId = ObjectId.fromString(commitId)
            RevWalk(repository).use { walk ->
                val commit = walk.parseCommit(objectId)
                git.revert().include(commit).call()
            }
            notificationManager.createNotification(
                title = "Revert",
                message = "Commit ${commitId.take(7)} annulé avec succès",
                status = Notification.Status.Success
            )
        } catch (e: Exception) {
            notificationManager.createNotification(
                title = "Revert",
                message = e.message ?: "Erreur inconnue",
                status = Notification.Status.Error
            )
        }
    }

    override suspend fun createTag(name: String, commitId: String) = withContext(Dispatchers.IO) {
        val repository = repoState?.repository ?: return@withContext
        val git = Git(repository)
        try {
            val objectId = ObjectId.fromString(commitId)
            RevWalk(repository).use { walk ->
                val commit = walk.parseCommit(objectId)
                git.tag()
                    .setName(name)
                    .setObjectId(commit)
                    .call()
            }
            notificationManager.createNotification(
                title = "Create tag",
                message = "Tag '$name' créé sur ${commitId.take(7)}",
                status = Notification.Status.Success
            )
        } catch (e: Exception) {
            notificationManager.createNotification(
                title = "Create tag",
                message = e.message ?: "Erreur inconnue",
                status = Notification.Status.Error
            )
        }
    }

    override suspend fun checkoutBranch(branchName: String) = withContext(Dispatchers.IO) {
        val repository = repoState?.repository ?: return@withContext
        val git = Git(repository)
        try {
            git.checkout().setName(branchName).call()
            notificationManager.createNotification(
                title = "Checkout",
                message = "Checkout vers '$branchName' réussi",
                status = Notification.Status.Success
            )
        } catch (e: Exception) {
            notificationManager.createNotification(
                title = "Checkout",
                message = e.message ?: "Erreur inconnue",
                status = Notification.Status.Error
            )
        }
    }

    override suspend fun deleteBranch(branchName: String, force: Boolean) = withContext(Dispatchers.IO) {
        val repository = repoState?.repository ?: return@withContext
        val git = Git(repository)
        try {
            git.branchDelete()
                .setBranchNames(branchName)
                .setForce(force)
                .call()
            notificationManager.createNotification(
                title = "Delete branch",
                message = "Branche '$branchName' supprimée",
                status = Notification.Status.Success
            )
        } catch (e: Exception) {
            notificationManager.createNotification(
                title = "Delete branch",
                message = e.message ?: "Erreur inconnue",
                status = Notification.Status.Error
            )
        }
    }

    override suspend fun deleteTag(tagName: String) = withContext(Dispatchers.IO) {
        val repository = repoState?.repository ?: return@withContext
        val git = Git(repository)
        try {
            git.tagDelete().setTags(tagName).call()
            notificationManager.createNotification(
                title = "Delete tag",
                message = "Tag '$tagName' supprimé",
                status = Notification.Status.Success
            )
        } catch (e: Exception) {
            notificationManager.createNotification(
                title = "Delete tag",
                message = e.message ?: "Erreur inconnue",
                status = Notification.Status.Error
            )
        }
    }

    override suspend fun pushBranch(branchName: String) = withContext(Dispatchers.IO) {
        val repository = repoState?.repository ?: return@withContext
        val cmd = Git(repository).push()
            .add(branchName)

        val monitor = NotificationProgressMonitor(
            notificationManager = notificationManager,
            cmd = cmd,
            title = "Push",
            startMessage = "Pushing '$branchName'",
            successMessage = "Push de '$branchName' réussi"
        )
        cmd.setProgressMonitor(monitor)
        try {
            monitor.call()
        } catch (e: Exception) {
            println("Error pushing branch: ${e.message}")
        }
    }

    override suspend fun pullMergeBranch(branchName: String) = withContext(Dispatchers.IO) {
        val repository = repoState?.repository ?: return@withContext
        val git = Git(repository)

        try {
            // First checkout the branch if not already on it
            val currentBranch = repository.branch
            if (currentBranch != branchName) {
                git.checkout().setName(branchName).call()
            }

            // Then pull
            val cmd = git.pull()
            val monitor = NotificationProgressMonitor(
                notificationManager = notificationManager,
                cmd = cmd,
                title = "Pull",
                startMessage = "Pulling '$branchName'",
                successMessage = "Pull de '$branchName' réussi"
            )
            cmd.setProgressMonitor(monitor)
            monitor.call()
        } catch (e: Exception) {
            notificationManager.createNotification(
                title = "Pull",
                message = e.message ?: "Erreur inconnue",
                status = Notification.Status.Error
            )
        }
    }
}
