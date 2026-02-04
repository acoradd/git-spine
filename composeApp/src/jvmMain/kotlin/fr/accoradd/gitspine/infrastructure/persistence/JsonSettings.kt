package fr.accoradd.gitspine.infrastructure.persistence

import fr.accoradd.gitspine.core.settings.Settings
import fr.accoradd.gitspine.core.settings.Theme
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

class JsonSettings : Settings {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val mutex = Mutex()
    private val filePath: Path = AppDataPath.getDataDir().resolve(FILE_NAME)

    private var data: SettingsData = loadFromDisk()

    private val _theme = MutableStateFlow(Theme.valueOf(data.values[KEY_THEME] ?: Theme.SYSTEM.name))
    override val theme: Flow<Theme> = _theme.asStateFlow()

    override suspend fun setTheme(theme: Theme) {
        setString(KEY_THEME, theme.name)
        _theme.value = theme
    }

    override fun getString(key: String, default: String): String {
        return data.values[key] ?: default
    }

    override suspend fun setString(key: String, value: String) = mutex.withLock {
        data = data.copy(values = data.values + (key to value))
        saveToDisk()
    }

    override fun getInt(key: String, default: Int): Int {
        return data.values[key]?.toIntOrNull() ?: default
    }

    override suspend fun setInt(key: String, value: Int) {
        setString(key, value.toString())
    }

    override fun getBoolean(key: String, default: Boolean): Boolean {
        return data.values[key]?.toBooleanStrictOrNull() ?: default
    }

    override suspend fun setBoolean(key: String, value: Boolean) {
        setString(key, value.toString())
    }

    private fun loadFromDisk(): SettingsData {
        return try {
            if (!filePath.exists()) {
                return SettingsData()
            }
            val content = filePath.readText()
            json.decodeFromString<SettingsData>(content)
        } catch (e: Exception) {
            SettingsData()
        }
    }

    private suspend fun saveToDisk() = withContext(Dispatchers.IO) {
        try {
            val parent = filePath.parent
            if (!parent.exists()) {
                Files.createDirectories(parent)
            }
            val content = json.encodeToString(SettingsData.serializer(), data)
            filePath.writeText(content)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Serializable
    private data class SettingsData(
        val values: Map<String, String> = emptyMap()
    )

    companion object {
        private const val FILE_NAME = "settings.json"
        private const val KEY_THEME = "theme"
    }
}
