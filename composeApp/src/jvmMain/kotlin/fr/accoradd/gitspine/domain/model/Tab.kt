package fr.accoradd.gitspine.domain.model

import java.nio.file.Path
import java.util.UUID

data class Tab(
    val id: String = UUID.randomUUID().toString(),
    val path: Path,
    val name: String = path.fileName.toString()
)
