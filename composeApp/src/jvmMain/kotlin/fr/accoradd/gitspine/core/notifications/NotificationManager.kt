package fr.accoradd.gitspine.core.notifications

import fr.accoradd.gitspine.domain.model.Notification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NotificationManager {
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    fun addNotification(notification: Notification) {
        _notifications.update { current ->
            current + notification
        }
    }

    fun updateNotification(id: String, update: (Notification) -> Notification) {
        _notifications.update { current ->
            current.map { notification ->
                if (notification.id == id) update(notification) else notification
            }
        }
    }

    fun removeNotification(id: String) {
        _notifications.update { current ->
            current.filterNot { it.id == id }
        }
    }

    fun createNotification(
        title: String,
        message: String = "",
        progress: Notification.Progress = Notification.Progress.Indeterminate
    ): String {
        val notification = Notification(
            title = title,
            message = message,
            progress = progress
        )
        addNotification(notification)
        return notification.id
    }

    fun updateProgress(
        id: String,
        message: String? = null,
        progress: Notification.Progress? = null
    ) {
        updateNotification(id) { notification ->
            notification.copy(
                message = message ?: notification.message,
                progress = progress ?: notification.progress
            )
        }
    }

    fun completeNotification(id: String, message: String? = null) {
        updateNotification(id) { notification ->
            notification.copy(
                message = message ?: notification.message,
                status = Notification.Status.Success,
                progress = Notification.Progress.Determinate(1, 1)
            )
        }
    }

    fun failNotification(id: String, errorMessage: String) {
        updateNotification(id) { notification ->
            notification.copy(
                message = errorMessage,
                status = Notification.Status.Error
            )
        }
    }
}
