package fr.accoradd.gitspine.domain.repository

import fr.accoradd.gitspine.domain.model.Branch
import fr.accoradd.gitspine.domain.model.Commit
import fr.accoradd.gitspine.domain.model.GraphNode
import kotlinx.coroutines.flow.Flow

interface GitRepository {
    fun getCommits(): Flow<List<Commit>>
    fun getBranches(): Flow<List<Branch>>
    fun getTags(): Flow<List<String>>
    fun getGraph(): Flow<List<GraphNode>>
    suspend fun stage(path: String)
    suspend fun unstage(path: String)
    suspend fun commit(message: String)
}