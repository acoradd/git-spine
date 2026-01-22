package fr.accoradd.gitspine.infrastructure.git

import fr.accoradd.gitspine.core.notifications.NotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.SshTransport
import org.eclipse.jgit.transport.sshd.SshdSessionFactory
import java.io.File

object GitCloner {

    data class Progress(
        val message: String,
        val completed: Int = 0,
        val total: Int = 0
    )

    suspend fun clone(
        url: String,
        destinationPath: String,
        notificationManager: NotificationManager
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val directory = File(destinationPath)

            // Vérifier si le répertoire existe déjà
            if (directory.exists() && directory.listFiles()?.isNotEmpty() == true) {
                return@withContext Result.failure(
                    IllegalArgumentException("Le répertoire n'est pas vide")
                )
            }

            // Créer le répertoire parent si nécessaire
            directory.parentFile?.mkdirs()

            // Cloner le dépôt
            val cloneCommand = Git.cloneRepository()
                .setURI(url)
                .setDirectory(directory)

            val monitor = NotificationProgressMonitor(
                notificationManager = notificationManager,
                cmd = cloneCommand,
                title = "Clone",
                startMessage = "Clonage du dépôt $url vers $destinationPath",
                successMessage = "Dépôt cloné avec succès"
            )

            cloneCommand.setProgressMonitor(monitor)

            // Configure SSH transport pour les URLs git@...
            if (url.startsWith("git@") || url.startsWith("ssh://")) {
                cloneCommand.setTransportConfigCallback { transport ->
                    if (transport is SshTransport) {
                        transport.sshSessionFactory = SshdSessionFactory()
                    }
                }
            }

            monitor.call()
                .use { _ ->
                    return@withContext Result.success(destinationPath)
                }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
