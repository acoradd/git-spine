package fr.accoradd.gitspine.domain.repository

import fr.accoradd.gitspine.domain.model.Branch
import fr.accoradd.gitspine.domain.model.Commit
import fr.accoradd.gitspine.domain.model.WorkspaceStatus
import kotlinx.coroutines.flow.Flow

interface GitRepository {
    fun getCommits(skip: Int = 0, limit: Int = 1000): Flow<List<Commit>>

    // Get HEAD commit ID
    suspend fun getHeadCommitId(): String?

    // Load all local branches, with optional search
    fun getLocalBranches(search: String? = null): Flow<List<Branch>>

    // Load remote branches with pagination and search
    fun getRemoteBranches(skip: Int = 0, limit: Int = 100, search: String? = null): Flow<List<Branch>>

    fun getTags(skip: Int = 0, limit: Int = 100, search: String? = null): Flow<List<String>>

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
}
