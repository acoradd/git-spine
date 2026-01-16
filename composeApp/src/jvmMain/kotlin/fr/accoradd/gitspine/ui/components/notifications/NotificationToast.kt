package fr.accoradd.gitspine.ui.components.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.accoradd.gitspine.domain.model.Notification
import fr.accoradd.gitspine.ui.components.common.ToolbarIconButton
import kotlinx.coroutines.launch

@Composable
fun NotificationToast(
    notification: Notification,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // Auto-dismiss sur succès après 3 secondes
    LaunchedEffect(notification.status) {
        if (notification.status == Notification.Status.Success) {
            kotlinx.coroutines.delay(3000)
            visible = false
            kotlinx.coroutines.delay(300) // Animation duration
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
    ) {
        Card(
            modifier = modifier
                .width(350.dp)
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (notification.status) {
                    Notification.Status.Error -> MaterialTheme.colorScheme.errorContainer
                    Notification.Status.Success -> MaterialTheme.colorScheme.primaryContainer
                    Notification.Status.Running -> MaterialTheme.colorScheme.surfaceContainerHigh
                }
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header: Titre + bouton fermer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 4.dp, top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Icon de statut
                        when (notification.status) {
                            Notification.Status.Success -> Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Notification.Status.Error -> Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            else -> {}
                        }

                        if (notification.status != Notification.Status.Running) {
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = when (notification.status) {
                                Notification.Status.Error -> MaterialTheme.colorScheme.onErrorContainer
                                Notification.Status.Success -> MaterialTheme.colorScheme.onPrimaryContainer
                                Notification.Status.Running -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }

                    IconButton(
                        onClick = {
                            visible = false
                            // Délai pour l'animation
                            scope.launch {
                                kotlinx.coroutines.delay(300)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Fermer",
                            modifier = Modifier.size(18.dp),
                            tint = when (notification.status) {
                                Notification.Status.Error -> MaterialTheme.colorScheme.onErrorContainer
                                Notification.Status.Success -> MaterialTheme.colorScheme.onPrimaryContainer
                                Notification.Status.Running -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }

                // Message
                if (notification.message.isNotBlank()) {
                    Text(
                        text = notification.message,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = when (notification.status) {
                            Notification.Status.Error -> MaterialTheme.colorScheme.onErrorContainer
                            Notification.Status.Success -> MaterialTheme.colorScheme.onPrimaryContainer
                            Notification.Status.Running -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    when (val progress = notification.progress) {
                        is Notification.Progress.Indeterminate -> {
                            if (notification.status == Notification.Status.Running) {
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        is Notification.Progress.Determinate -> {
                            if (notification.status == Notification.Status.Running || progress.percentage < 1f) {
                                LinearProgressIndicator(
                                    progress = { progress.percentage },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
