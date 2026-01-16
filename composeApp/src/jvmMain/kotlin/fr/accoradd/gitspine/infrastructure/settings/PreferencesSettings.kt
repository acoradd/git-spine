package fr.accoradd.gitspine.infrastructure.settings

import fr.accoradd.gitspine.core.settings.Settings
import fr.accoradd.gitspine.core.settings.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.prefs.Preferences

class PreferencesSettings : Settings {

    private val prefs = Preferences.userNodeForPackage(PreferencesSettings::class.java)

    private val _theme = MutableStateFlow(loadTheme())
    override val theme: Flow<Theme> = _theme.asStateFlow()

    override suspend fun setTheme(theme: Theme) {
        prefs.put(KEY_THEME, theme.name)
        _theme.value = theme
    }

    private fun loadTheme(): Theme {
        val themeName = prefs.get(KEY_THEME, Theme.SYSTEM.name)
        return Theme.valueOf(themeName)
    }

    companion object {
        private const val KEY_THEME = "theme"
    }
}
