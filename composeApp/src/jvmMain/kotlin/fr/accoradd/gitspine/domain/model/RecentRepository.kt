package fr.accoradd.gitspine.domain.model

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class RecentRepository(
    val path: String,
    val name: String,
    val lastOpenedAt: Long // Epoch millis for serialization
) {
    val lastOpenedInstant: Instant
        get() = Instant.ofEpochMilli(lastOpenedAt)

    companion object {
        fun create(path: String, name: String): RecentRepository {
            return RecentRepository(
                path = path,
                name = name,
                lastOpenedAt = Instant.now().toEpochMilli()
            )
        }
    }
}
