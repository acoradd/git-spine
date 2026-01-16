package fr.accoradd.gitspine.core.settings

import kotlinx.coroutines.flow.Flow

interface Settings {
    val theme: Flow<Theme>
    suspend fun setTheme(theme: Theme)
}

enum class Theme {
    LIGHT,
    DARK,
    SYSTEM
}