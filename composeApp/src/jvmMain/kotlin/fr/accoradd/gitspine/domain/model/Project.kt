package fr.accoradd.gitspine.domain.model

import java.nio.file.Path
import java.util.UUID

data class Project(
    val path: Path,
    val name: String = path.fileName.toString()
)
