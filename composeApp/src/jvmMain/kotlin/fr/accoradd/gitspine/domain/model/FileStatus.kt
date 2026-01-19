package fr.accoradd.gitspine.domain.model

data class FileStatus(
    val path: String,
    val statusType: FileStatusType
)

enum class FileStatusType {
    ADDED,        // Nouveau fichier staged
    MODIFIED,     // Fichier modifié
    DELETED,      // Fichier supprimé
    UNTRACKED,    // Nouveau fichier non staged
    CONFLICTING,  // Conflit de merge
    RENAMED       // Fichier renommé
}

data class WorkspaceStatus(
    val staged: List<FileStatus> = emptyList(),
    val unstaged: List<FileStatus> = emptyList(),
    val isLoading: Boolean = false
) {
    val stagedCount: Int get() = staged.size
    val unstagedCount: Int get() = unstaged.size
    val hasChanges: Boolean get() = stagedCount + unstagedCount > 0
}
