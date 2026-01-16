package fr.accoradd.gitspine.infrastructure.git

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.Git
import java.io.File

object GitCloner {

    suspend fun clone(
        url: String,
        destinationPath: String,
        onProgress: ((String) -> Unit)? = null
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

            onProgress?.invoke("Clonage en cours...")

            // Cloner le dépôt
            Git.cloneRepository()
                .setURI(url)
                .setDirectory(directory)
                .setProgressMonitor(object : org.eclipse.jgit.lib.ProgressMonitor {
                    override fun start(totalTasks: Int) {
                        onProgress?.invoke("Démarrage du clonage...")
                    }

                    override fun beginTask(title: String?, totalWork: Int) {
                        onProgress?.invoke(title ?: "Clonage en cours...")
                    }

                    override fun update(completed: Int) {
                        // Update progress
                    }

                    override fun endTask() {
                        // Task ended
                    }

                    override fun isCancelled(): Boolean = false

                    override fun showDuration(enabled: Boolean) {
                        // Show duration feature
                    }
                })
                .call()
                .use { git ->
                    onProgress?.invoke("Clonage terminé")
                    return@withContext Result.success(destinationPath)
                }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
