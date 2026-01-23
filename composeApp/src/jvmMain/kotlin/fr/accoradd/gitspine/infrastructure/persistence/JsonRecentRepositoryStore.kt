package fr.accoradd.gitspine.infrastructure.persistence

import fr.accoradd.gitspine.domain.model.RecentRepository
import fr.accoradd.gitspine.domain.repository.RecentRepositoryStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

class JsonRecentRepositoryStore : RecentRepositoryStore {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val mutex = Mutex()
    private val filePath: Path = AppDataPath.getConfigDir().resolve(FILE_NAME)

    private val _recentRepositories = MutableStateFlow<List<RecentRepository>>(emptyList())
    override val recentRepositories: Flow<List<RecentRepository>> = _recentRepositories.asStateFlow()

    init {
        // Load repositories synchronously on init
        _recentRepositories.value = loadFromDisk()
    }

    override suspend fun addOrUpdate(repo: RecentRepository) = mutex.withLock {
        val current = _recentRepositories.value.toMutableList()

        // Remove existing entry with same path
        current.removeAll { it.path == repo.path }

        // Add at the beginning (most recent first)
        current.add(0, repo)

        // Limit to MAX_ENTRIES
        val trimmed = current.take(MAX_ENTRIES)

        _recentRepositories.value = trimmed
        saveToDisk(trimmed)
    }

    override suspend fun remove(path: String) = mutex.withLock {
        val current = _recentRepositories.value.filter { it.path != path }
        _recentRepositories.value = current
        saveToDisk(current)
    }

    override suspend fun clear() = mutex.withLock {
        _recentRepositories.value = emptyList()
        saveToDisk(emptyList())
    }

    private fun loadFromDisk(): List<RecentRepository> {
        return try {
            if (!filePath.exists()) {
                return emptyList()
            }
            val content = filePath.readText()
            val data = json.decodeFromString<RecentRepositoriesData>(content)
            data.repositories
        } catch (e: Exception) {
            // If file is corrupted, start fresh
            emptyList()
        }
    }

    private suspend fun saveToDisk(repositories: List<RecentRepository>) = withContext(Dispatchers.IO) {
        try {
            // Ensure parent directory exists
            val parent = filePath.parent
            if (!parent.exists()) {
                Files.createDirectories(parent)
            }

            val data = RecentRepositoriesData(repositories)
            val content = json.encodeToString(RecentRepositoriesData.serializer(), data)
            filePath.writeText(content)
        } catch (e: Exception) {
            // Log error but don't crash the app
            e.printStackTrace()
        }
    }

    @Serializable
    private data class RecentRepositoriesData(
        val repositories: List<RecentRepository>
    )

    companion object {
        private const val FILE_NAME = "repositories.json"
        private const val MAX_ENTRIES = 50
    }
}
