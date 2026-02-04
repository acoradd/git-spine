package fr.accoradd.gitspine.infrastructure.persistence

import java.nio.file.Path

object AppDataPath {
    private const val APP_NAME = "GitSpine"

    fun getDataDir(): Path {
        val os = System.getProperty("os.name").lowercase()
        return when {
            os.contains("win") -> {
                val appData = System.getenv("APPDATA")
                    ?: Path.of(System.getProperty("user.home"), "AppData", "Roaming").toString()
                Path.of(appData, APP_NAME)
            }
            os.contains("mac") -> {
                Path.of(System.getProperty("user.home"), "Library", "Application Support", APP_NAME)
            }
            else -> {
                // Linux - follow XDG Base Directory spec
                val configHome = System.getenv("XDG_CONFIG_HOME")
                    ?: Path.of(System.getProperty("user.home"), ".config").toString()
                Path.of(configHome, APP_NAME.lowercase())
            }
        }
    }
}
