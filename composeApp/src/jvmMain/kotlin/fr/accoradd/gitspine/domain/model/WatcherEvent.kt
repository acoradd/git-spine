package fr.accoradd.gitspine.domain.model

sealed interface WatcherEvent {
    data class WorkspaceChanged(val hasGitDirChanged: Boolean) : WatcherEvent
    data class WatcherError(val message: String) : WatcherEvent
}
