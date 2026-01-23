package fr.accoradd.gitspine.domain.repository

import fr.accoradd.gitspine.domain.model.RecentRepository
import kotlinx.coroutines.flow.Flow

interface RecentRepositoryStore {
    val recentRepositories: Flow<List<RecentRepository>>

    suspend fun addOrUpdate(repo: RecentRepository)
    suspend fun remove(path: String)
    suspend fun clear()
}
