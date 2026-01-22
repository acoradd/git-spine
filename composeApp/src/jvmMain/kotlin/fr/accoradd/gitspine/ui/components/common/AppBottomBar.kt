package fr.accoradd.gitspine.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.accoradd.gitspine.core.config.AppConfig
import fr.accoradd.gitspine.core.notifications.NotificationManager
import fr.accoradd.gitspine.domain.model.Notification
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.HorizontalProgressBar
import org.jetbrains.jewel.ui.component.IndeterminateHorizontalProgressBar
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.window.defaultTitleBarStyle

@Composable
fun AppBottomBar(
    notificationManager: NotificationManager
) {
    val notifications by notificationManager.notifications.collectAsState()
    val runningNotifications = notifications.filter { it.status == Notification.Status.Running }
    val currentProgress = runningNotifications.firstOrNull()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(JewelTheme.defaultTitleBarStyle.colors.background)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.fillMaxHeight(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(AppConfig.APP_NAME.lowercase())
        }
        Row(
            modifier = Modifier.fillMaxHeight(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            currentProgress?.let { notification ->
                Text(
                    text = buildString {
                        append(notification.title)
                        if (notification.message.isNotBlank()) {
                            append(": ")
                            append(notification.message)
                        }
                    },
                    style = JewelTheme.defaultTextStyle,
                    color = JewelTheme.globalColors.text.info
                )

                Spacer(modifier = Modifier.width(12.dp))

                Box(modifier = Modifier.width(150.dp)) {
                    when (val progress = notification.progress) {
                        is Notification.Progress.Determinate -> {
                            HorizontalProgressBar(
                                progress = progress.percentage,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        is Notification.Progress.Indeterminate -> {
                            IndeterminateHorizontalProgressBar(
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))
            }
            Text(AppConfig.APP_NAME.lowercase())
        }
    }
}
