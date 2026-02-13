package fr.accoradd.gitspine.domain.repository

import fr.accoradd.gitspine.domain.model.Branch
import fr.accoradd.gitspine.domain.model.Commit
import fr.accoradd.gitspine.domain.model.ResetMode
import fr.accoradd.gitspine.domain.model.Tag
import fr.accoradd.gitspine.domain.model.WorkspaceStatus
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface GitRepository {
    /**
     * Charge les commits triés par date décroissante.
     * @param beforeTimestamp Si non null, ne charge que les commits avant ce timestamp (exclusif)
     * @param excludeCommitId Si non null, exclut ce commit (utile si plusieurs commits ont le même timestamp)
     * @param limit Nombre max de commits à charger
     */
    fun getCommits(
        beforeTimestamp: Instant? = null,
        excludeCommitId: String? = null,
        limit: Int = 100
    ): Flow<List<Commit>>

    // Get HEAD commit ID
    suspend fun getHeadCommitId(): String?

    // Load all local branches, with optional search
    fun getLocalBranches(search: String? = null): Flow<List<Branch>>

    // Load remote branches with pagination and search
    fun getRemoteBranches(): Flow<List<Branch>>

    fun getTags(): Flow<List<Tag>>

    // Workspace status
    suspend fun getStatus(): WorkspaceStatus

    // Staging operations
    suspend fun stage(path: String)
    suspend fun unstage(path: String)
    suspend fun stageAll()
    suspend fun unstageAll()
    suspend fun discardChanges(path: String, staged: Boolean)

    // Commit
    suspend fun commit(message: String)

    suspend fun fetch()
    suspend fun pull()
    suspend fun push(distantBranch: String?, force: Boolean, pushTags: Boolean)
    suspend fun stash()
    suspend fun unstash(stashName: String)
    suspend fun createBranch(name: String)

    // Context menu operations
    suspend fun checkout(commitId: String)
    suspend fun createBranchFromCommit(name: String, commitId: String)
    suspend fun reset(commitId: String, mode: ResetMode)
    suspend fun revert(commitId: String)
    suspend fun createTag(name: String, commitId: String)

    // Ref operations
    suspend fun checkoutBranch(branchName: String)
    suspend fun deleteBranch(branchName: String, force: Boolean = false)
    suspend fun deleteTag(tagName: String)
    suspend fun pushBranch(branchName: String)
    suspend fun pullMergeBranch(branchName: String)
}
