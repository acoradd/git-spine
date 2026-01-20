package fr.accoradd.gitspine.ui.screens.add

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.accoradd.gitspine.domain.model.Notification
import fr.accoradd.gitspine.infrastructure.filesystem.FileDialogs
import fr.accoradd.gitspine.infrastructure.git.GitCloner
import fr.accoradd.gitspine.core.notifications.NotificationManager
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.nio.file.Path
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.ui.component.CircularProgressIndicator
import fr.accoradd.gitspine.ui.theme.jewelColors
import fr.accoradd.gitspine.ui.theme.jewelTextStyle

@Composable
fun AddRepositoryScreen(
    onBack: () -> Unit,
    onCloneSuccess: (Path) -> Unit
) {
    val notificationManager: NotificationManager = koinInject()
    val scope = rememberCoroutineScope()
    var url by remember { mutableStateOf("") }
    var destinationPath by remember { mutableStateOf<Path?>(null) }
    var isCloning by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Cloner un dépôt")
            DefaultButton(onClick = onBack) {
                Text("Retour")
            }
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("URL du dépôt")
                TextField(
                    value = url,
                    onValueChange = { url = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Dossier de destination")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = destinationPath?.toString() ?: "",
                        onValueChange = {},
                        modifier = Modifier.weight(1f),
                        readOnly = true
                    )
                    OutlinedButton(onClick = {
                        scope.launch {
                            destinationPath = FileDialogs.openDirectory("Sélectionner le répertoire de destination")
                        }
                    }) {
                        Text("Parcourir")
                    }
                }
            }

            DefaultButton(
                onClick = {
                    val dest = destinationPath
                    if (url.isNotBlank() && dest != null) {
                        isCloning = true
                        scope.launch {
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
                                onCloneSuccess(dest)
                            } else {
                                val errorMsg = result.exceptionOrNull()?.message ?: "Erreur inconnue"
                                notificationManager.failNotification(
                                    id = notificationId,
                                    errorMessage = errorMsg
                                )
                            }
                            isCloning = false
                        }
                    }
                },
                enabled = url.isNotBlank() && destinationPath != null && !isCloning,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isCloning) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clonage en cours...")
                } else {
                    Text("Cloner")
                }
            }
        }
    }
}
