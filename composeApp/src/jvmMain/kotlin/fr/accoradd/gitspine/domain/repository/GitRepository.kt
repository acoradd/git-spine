package fr.accoradd.gitspine.domain.repository

import fr.accoradd.gitspine.domain.model.Branch
import fr.accoradd.gitspine.domain.model.Commit
import fr.accoradd.gitspine.domain.model.GraphNode
import fr.accoradd.gitspine.domain.model.WorkspaceStatus
import kotlinx.coroutines.flow.Flow

interface GitRepository {
    fun getCommits(skip: Int = 0, limit: Int = 1000): Flow<List<Commit>>

    // Load all local branches, with optional search
    fun getLocalBranches(search: String? = null): Flow<List<Branch>>

    // Load remote branches with pagination and search
    fun getRemoteBranches(skip: Int = 0, limit: Int = 100, search: String? = null): Flow<List<Branch>>

    fun getTags(skip: Int = 0, limit: Int = 100, search: String? = null): Flow<List<String>>

    fun getGraph(): Flow<List<GraphNode>>

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
}
