package fr.accoradd.gitspine.core.config

import fr.accoradd.gitspine.core.extension.toMD5

object AppConfig {
    const val APP_NAME = "GitSpine"
    const val VERSION = "1.0.0"
    const val GRAVATAR_URL = "https://www.gravatar.com/avatar/%s?d=retro"

    fun getGravatarUrl(email: String) = GRAVATAR_URL.format(email.toMD5())
}
