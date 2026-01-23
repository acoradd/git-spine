package fr.accoradd.gitspine.core.settings

import kotlinx.coroutines.flow.Flow

interface Settings {
    val theme: Flow<Theme>
    suspend fun setTheme(theme: Theme)

    // Generic key-value storage
    fun getString(key: String, default: String): String
    suspend fun setString(key: String, value: String)

    fun getInt(key: String, default: Int): Int
    suspend fun setInt(key: String, value: Int)

    fun getBoolean(key: String, default: Boolean): Boolean
    suspend fun setBoolean(key: String, value: Boolean)
}

enum class Theme {
    LIGHT,
    DARK,
    SYSTEM
}