package fr.accoradd.gitspine.infrastructure.git

import fr.accoradd.gitspine.core.notifications.NotificationManager
import fr.accoradd.gitspine.domain.model.Notification
import org.eclipse.jgit.api.GitCommand
import java.util.UUID

enum class ProgressStatus {
    WAITING,
    STARTED,
    FINISHED,
    ERROR
}

data class ProgressTask(
    val id: String,
    val title: String,
    val completed: Int = 0,
    val total: Int = 0,
)

data class Progress(
    val status: ProgressStatus,
    val completed: Int = 0,
    val total: Int = 0,
    val currentTask: ProgressTask? = null,
    val previousTasks: List<ProgressTask> = emptyList(),
    val error: String? = null
)


open class ProgressMonitor<T>(
    val id: String = UUID.randomUUID().toString(),
    private val cmd: GitCommand<T>,
    private val onStart: ((Progress) -> Unit)? = null,
    private val onEnd: ((Progress) -> Unit)? = null,
    private val onBeginTask: ((ProgressTask, Progress) -> Unit)? = null,
    private val onEndTask: ((ProgressTask, Progress) -> Unit)? = null,
    private val onProgress: ((Progress) -> Unit)? = null
): org.eclipse.jgit.lib.ProgressMonitor {

    var canceled = false
    private var progress = Progress(ProgressStatus.WAITING)

    override fun start(totalTasks: Int) {
        progress = progress.copy(status = ProgressStatus.STARTED, total = totalTasks)
        onStart?.invoke(progress)
        onProgress?.invoke(progress)
    }

    override fun beginTask(title: String?, totalTasks: Int) {
        val currentTask = ProgressTask(UUID.randomUUID().toString(), title ?: "", 0, totalTasks)
        progress = progress.copy(
            status = ProgressStatus.STARTED,
            currentTask = currentTask
        )
        onBeginTask?.invoke(currentTask, progress)
        onProgress?.invoke(progress)
    }

    override fun update(completed: Int) {
        val currentTask = progress.currentTask ?: return
        progress = progress.copy(currentTask = currentTask.copy(completed = currentTask.completed + completed))
        onProgress?.invoke(progress)
    }

    override fun endTask() {
        val currentTask = progress.currentTask ?: return
        progress = progress.copy(
            currentTask = null,
            previousTasks = listOf(currentTask) + progress.previousTasks,
            completed = progress.completed + 1
        )
        onEndTask?.invoke(currentTask, progress)

        if (progress.completed == progress.total) {
            onEnd?.invoke(progress)
        }

        onProgress?.invoke(progress)
    }

    override fun isCancelled(): Boolean {
        return canceled
    }

    override fun showDuration(enabled: Boolean) {}

    fun call(): T {
        try {
            return cmd.call()
                .also {
                    val currentTask = progress.currentTask
                    progress = progress.copy(
                        status = ProgressStatus.FINISHED,
                        previousTasks = if(currentTask != null) listOf(currentTask) + progress.previousTasks else progress.previousTasks,
                        completed = progress.total,
                        currentTask = null
                    )
                    onEnd?.invoke(progress)
                }
        } catch (e: Exception) {
            progress = progress.copy(
                status = ProgressStatus.ERROR,
                error = e.message
            )
            onEnd?.invoke(progress)
            throw e
        }
    }
}

class NotificationProgressMonitor<T>(
    private val notificationManager: NotificationManager,
    cmd: GitCommand<T>,
    private val title: String,
    private val startMessage: String,
    private var notificationId: String = notificationManager.createNotification(
        title = title,
        message = startMessage,
        progress = Notification.Progress.Indeterminate
    ),
    private val successMessage: String? = null,
    onStart: ((Progress) -> Unit)? = null,
    private val onEnd: ((Progress) -> Unit)? = null,
    onBeginTask: ((ProgressTask, Progress) -> Unit)? = null,
    onEndTask: ((ProgressTask, Progress) -> Unit)? = null,
    private val onProgress: ((Progress) -> Unit)? = null
): ProgressMonitor<T>(
    cmd = cmd,
    onProgress = { progress ->
        val notifProgress = if ((progress.currentTask?.total ?: progress.total) > 0) {
            Notification.Progress.Determinate(
                progress.currentTask?.completed ?: progress.completed,
                progress.currentTask?.total ?: progress.total
            )
        } else {
            Notification.Progress.Indeterminate
        }
        notificationManager.updateProgress(
            id = notificationId,
            message = progress.currentTask?.title ?: "",
            progress = notifProgress
        )
        onProgress?.invoke(progress)
    },
    onEnd = { progress ->
        if (progress.status == ProgressStatus.FINISHED) {
            notificationManager.completeNotification(
                id = notificationId,
                message = successMessage ?: "Opération terminée avec succès"
            )
        } else if (progress.status == ProgressStatus.ERROR) {
            notificationManager.failNotification(
                id = notificationId,
                errorMessage = progress.error ?: "Erreur inconnue"
            )
        }
        onEnd?.invoke(progress)
    },
    onStart = onStart,
    onBeginTask = onBeginTask,
    onEndTask = onEndTask
) {
}
