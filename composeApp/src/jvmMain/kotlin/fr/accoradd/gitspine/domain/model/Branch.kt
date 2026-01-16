package fr.accoradd.gitspine.domain.model

data class Branch(
    val name: String,
    val isRemote: Boolean,
    val isHead: Boolean,
    val commitId: String
)