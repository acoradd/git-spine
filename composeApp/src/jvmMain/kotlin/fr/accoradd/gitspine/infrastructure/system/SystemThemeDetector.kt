package fr.accoradd.gitspine.infrastructure.system

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.BufferedReader
import java.io.InputStreamReader

class SystemThemeDetector {
    private val _isDarkTheme = MutableStateFlow(detectSystemDarkTheme())
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private var pollingJob: Job? = null

    fun startListening(scope: CoroutineScope) {
        pollingJob?.cancel()
        pollingJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                val newValue = detectSystemDarkTheme()
                if (_isDarkTheme.value != newValue) {
                    _isDarkTheme.value = newValue
                }
                delay(1000) // Check every second
            }
        }
    }

    fun stopListening() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private fun detectSystemDarkTheme(): Boolean {
        val os = System.getProperty("os.name").lowercase()
        return when {
            os.contains("win") -> detectWindowsDarkTheme()
            os.contains("mac") -> detectMacOSDarkTheme()
            else -> detectLinuxDarkTheme()
        }
    }

    private fun detectWindowsDarkTheme(): Boolean {
        return try {
            val process = ProcessBuilder(
                "reg", "query",
                "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                "/v", "AppsUseLightTheme"
            ).start()

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()
            reader.close()
            process.waitFor()

            // If AppsUseLightTheme is 0, it's dark mode
            output.contains("0x0")
        } catch (e: Exception) {
            false
        }
    }

    private fun detectMacOSDarkTheme(): Boolean {
        return try {
            val process = ProcessBuilder(
                "defaults", "read", "-g", "AppleInterfaceStyle"
            ).start()

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText().trim()
            reader.close()
            process.waitFor()

            output.equals("Dark", ignoreCase = true)
        } catch (e: Exception) {
            // If the key doesn't exist, it's light mode
            false
        }
    }

    private fun detectLinuxDarkTheme(): Boolean {
        return try {
            // Try GNOME/GTK setting
            val process = ProcessBuilder(
                "gsettings", "get", "org.gnome.desktop.interface", "color-scheme"
            ).start()

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText().trim()
            reader.close()
            process.waitFor()

            output.contains("dark", ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }
}
