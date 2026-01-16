package fr.accoradd.gitspine.domain.model

data class GraphNode(
    val commit: Commit,
    val column: Int,
    val connections: List<Connection>
)

data class Connection(
    val fromColumn: Int,
    val toColumn: Int,
    val type: ConnectionType
)

enum class ConnectionType {
    PARENT,
    MERGE
}