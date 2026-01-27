package fr.accoradd.gitspine.domain.usecase.workspace

import fr.accoradd.gitspine.core.notifications.NotificationManager
import fr.accoradd.gitspine.infrastructure.git.GitCloner
import java.nio.file.Path

class CloneUseCase(private val notificationManager: NotificationManager) {
    suspend operator fun invoke(dest: Path, url: String): Path? {
        val result = GitCloner.clone(
            url = url,
            destinationPath = dest.toString(),
            notificationManager
        )

        return if (result.isSuccess) {
            dest
        } else {
            null
        }
    }
}
