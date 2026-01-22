package fr.accoradd.gitspine.ui.components.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.accoradd.gitspine.core.notifications.NotificationManager
import fr.accoradd.gitspine.domain.model.Notification

@Composable
fun NotificationsContainer(
    notificationManager: NotificationManager,
    modifier: Modifier = Modifier
) {
    val notifications by notificationManager.notifications.collectAsState()
    // Only show completed notifications (success or error) as toasts
    val completedNotifications = notifications.filter {
        it.status == Notification.Status.Success || it.status == Notification.Status.Error
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .widthIn(max = 400.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom),
            horizontalAlignment = Alignment.End
        ) {
            completedNotifications.forEach { notification ->
                NotificationToast(
                    notification = notification,
                    onDismiss = {
                        notificationManager.removeNotification(notification.id)
                    }
                )
            }
        }
    }
}
