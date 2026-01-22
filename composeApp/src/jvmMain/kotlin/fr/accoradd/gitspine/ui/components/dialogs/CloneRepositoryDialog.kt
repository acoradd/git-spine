package fr.accoradd.gitspine.ui.components.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fr.accoradd.gitspine.domain.model.Notification
import fr.accoradd.gitspine.infrastructure.filesystem.FileDialogs
import fr.accoradd.gitspine.infrastructure.git.GitCloner
import fr.accoradd.gitspine.core.notifications.NotificationManager
import kotlinx.coroutines.launch
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.*
import org.koin.compose.koinInject
import java.nio.file.Path
import kotlin.io.path.exists

@Composable
fun CloneRepositoryDialog(
    onDismiss: () -> Unit,
    onCloneSuccess: (Path) -> Unit
) {
    val notificationManager: NotificationManager = koinInject()
    val scope = rememberCoroutineScope()

    val urlState = rememberTextFieldState()
    val pathState = rememberTextFieldState()
    var isCloning by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = { if (!isCloning) onDismiss() }) {
        Column(
            modifier = Modifier
                .shadow(8.dp, RoundedCornerShape(8.dp))
                .background(JewelTheme.globalColors.panelBackground, RoundedCornerShape(8.dp))
                .padding(24.dp)
                .width(450.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Cloner un dépôt", style = JewelTheme.defaultTextStyle)

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("URL du dépôt")
                TextField(
                    state = urlState,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCloning
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Dossier de destination")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        state = pathState,
                        modifier = Modifier.weight(1f),
                        enabled = !isCloning
                    )
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val currentPath = pathState.text
                                val startPath = if (currentPath.isNotBlank()) {
                                    try {
                                        var currentStartPath = Path.of(currentPath.trim().toString())
                                        if (!currentStartPath.exists()) {
                                            currentStartPath = currentStartPath.parent
                                        }
                                        currentStartPath
                                    } catch (e: Exception) {
                                        null
                                    }
                                } else {
                                    null
                                }
                                val path = FileDialogs.openDirectory("Sélectionner le répertoire de destination", startPath)
                                if (path != null) {
                                    pathState.setTextAndPlaceCursorAtEnd(path.toAbsolutePath().toString())
                                    errorMessage = null
                                }
                            }
                        },
                        enabled = !isCloning
                    ) {
                        Text("Parcourir")
                    }
                }
            }

            AnimatedVisibility(visible = errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(JewelTheme.globalColors.outlines.error)
                        .padding(8.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = JewelTheme.globalColors.text.error
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isCloning
                ) {
                    Text("Annuler")
                }
                DefaultButton(
                    onClick = {
                        val dest = pathState.text.toString()
                        val url = urlState.text.toString()
                        if (url.isBlank()) {
                            errorMessage = "Veuillez entrer une URL"
                            return@DefaultButton
                        }
                        if (dest.isBlank()) {
                            errorMessage = "Veuillez sélectionner un dossier de destination"
                            return@DefaultButton
                        }

                        isCloning = true
                        errorMessage = null

                        scope.launch {
                            val notificationId = notificationManager.createNotification(
                                title = "Clonage du dépôt",
                                message = "Démarrage...",
                                progress = Notification.Progress.Indeterminate
                            )

                            val result = GitCloner.clone(
                                url = url,
                                destinationPath = dest,
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
                                onCloneSuccess(Path.of(dest))
                            } else {
                                val errorMsg = result.exceptionOrNull()?.message ?: "Erreur inconnue"
                                notificationManager.failNotification(
                                    id = notificationId,
                                    errorMessage = errorMsg
                                )
                                errorMessage = errorMsg
                                isCloning = false
                            }
                        }
                    },
                    enabled = !isCloning
                ) {
                    if (isCloning) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clonage...")
                    } else {
                        Text("Cloner")
                    }
                }
            }
        }
    }
}
