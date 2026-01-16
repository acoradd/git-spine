package fr.accoradd.gitspine.infrastructure.git

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
        onProgress: ((Progress) -> Unit)? = null
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

            onProgress?.invoke(Progress("Démarrage du clonage..."))

            var currentTaskName = ""
            var currentTaskTotal = 0
            var currentTaskCompleted = 0

            // Cloner le dépôt
            val cloneCommand = Git.cloneRepository()
                .setURI(url)
                .setDirectory(directory)
                .setProgressMonitor(object : org.eclipse.jgit.lib.ProgressMonitor {
                    override fun start(totalTasks: Int) {
                        onProgress?.invoke(Progress("Démarrage du clonage...", 0, totalTasks))
                    }

                    override fun beginTask(title: String?, totalWork: Int) {
                        currentTaskName = title ?: "Clonage en cours..."
                        currentTaskTotal = totalWork
                        currentTaskCompleted = 0
                        onProgress?.invoke(Progress(currentTaskName, 0, totalWork))
                    }

                    override fun update(completed: Int) {
                        currentTaskCompleted += completed
                        onProgress?.invoke(
                            Progress(
                                currentTaskName,
                                currentTaskCompleted,
                                currentTaskTotal
                            )
                        )
                    }

                    override fun endTask() {
                        onProgress?.invoke(Progress(currentTaskName, currentTaskTotal, currentTaskTotal))
                    }

                    override fun isCancelled(): Boolean = false

                    override fun showDuration(enabled: Boolean) {
                        // Show duration feature
                    }
                })

            // Configure SSH transport pour les URLs git@...
            if (url.startsWith("git@") || url.startsWith("ssh://")) {
                cloneCommand.setTransportConfigCallback { transport ->
                    if (transport is SshTransport) {
                        transport.sshSessionFactory = SshdSessionFactory()
                    }
                }
            }

            cloneCommand.call()
                .use { git ->
                    onProgress?.invoke(Progress("Clonage terminé", 1, 1))
                    return@withContext Result.success(destinationPath)
                }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
