package fr.accoradd.gitspine.domain.model

data class Tag(
    override val name: String,
    override val commitId: String
): RefCommit(name, commitId)
