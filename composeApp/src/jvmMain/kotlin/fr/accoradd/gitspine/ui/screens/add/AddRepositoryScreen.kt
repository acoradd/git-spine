package fr.accoradd.gitspine.ui.screens.add

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.accoradd.gitspine.domain.model.Notification
import fr.accoradd.gitspine.infrastructure.filesystem.FileDialogs
import fr.accoradd.gitspine.infrastructure.git.GitCloner
import fr.accoradd.gitspine.core.notifications.NotificationManager
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.nio.file.Path

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cloner un dépôt") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("URL du dépôt") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = destinationPath?.toString() ?: "",
                    onValueChange = {},
                    label = { Text("Dossier de destination") },
                    modifier = Modifier.weight(1f),
                    readOnly = true
                )
                Button(onClick = {
                    scope.launch {
                        destinationPath = FileDialogs.openDirectory("Sélectionner le répertoire de destination")
                    }
                }) {
                    Text("Parcourir")
                }
            }

            Button(
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
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Cloner")
                }
            }
        }
    }
}
