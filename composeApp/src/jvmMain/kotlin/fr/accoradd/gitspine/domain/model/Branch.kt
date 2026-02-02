package fr.accoradd.gitspine.domain.model

data class Branch(
    override val name: String,
    val isRemote: Boolean,
    val isHead: Boolean,
    override val commitId: String
): RefCommit(name, commitId)

abstract class RefCommit(
    open val name: String,
    open val commitId: String
)
