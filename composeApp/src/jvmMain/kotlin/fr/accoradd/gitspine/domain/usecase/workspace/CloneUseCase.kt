package fr.accoradd.gitspine.domain.usecase.workspace

import fr.accoradd.gitspine.core.notifications.NotificationManager
import fr.accoradd.gitspine.domain.model.Notification
import fr.accoradd.gitspine.infrastructure.git.GitCloner
import java.nio.file.Path

class CloneUseCase(private val notificationManager: NotificationManager) {
    suspend operator fun invoke(dest: Path, url: String): Path? {
        val notificationId = notificationManager.createNotification(
            title = "Clonage du dépôt",
            message = "Démarrage...",
            progress = Notification.Progress.Indeterminate
        )

        val result = GitCloner.clone(
            url = url,
            destinationPath = dest.toString(),
            onProgress = { progress ->
                val notifProgress = if (progress.total > 0) {
                    Notification.Progress.Determinate(progress.completed, progress.total)
                } else {
                    Notification.Progress.Indeterminate
                }
                notificationManager.updateProgress(
                    id = notificationId,
                    message = progress.message,
                    progress = notifProgress
                )
            }
        )

        if (result.isSuccess) {
            notificationManager.completeNotification(
                id = notificationId,
                message = "Dépôt cloné avec succès"
            )
            return dest
        } else {
            notificationManager.failNotification(
                id = notificationId,
                errorMessage = result.exceptionOrNull()?.message ?: "Erreur inconnue"
            )
            return null
        }
    }
}
