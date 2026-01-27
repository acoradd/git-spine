package fr.accoradd.gitspine.domain.model

import java.time.Instant

data class Commit(
    val id: String,
    val shortId: String,
    val message: String,
    val author: Author,
    val timestamp: Instant,
    val parents: List<String>
)

data class Author(
    val name: String,
    val email: String
)

sealed class CommitOrWip {
    data object Wip : CommitOrWip()
    data class CommitItem(val commit: Commit) : CommitOrWip()
}
