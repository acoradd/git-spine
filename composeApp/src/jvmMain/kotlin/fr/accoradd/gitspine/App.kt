package fr.accoradd.gitspine

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import fr.accoradd.gitspine.core.notifications.NotificationManager
import fr.accoradd.gitspine.core.settings.Theme
import fr.accoradd.gitspine.core.tabs.TabsManager
import fr.accoradd.gitspine.domain.model.Notification
import fr.accoradd.gitspine.infrastructure.filesystem.FileDialogs
import fr.accoradd.gitspine.infrastructure.git.GitCloner
import fr.accoradd.gitspine.ui.components.dialogs.CloneRepositoryDialog
import fr.accoradd.gitspine.ui.components.notifications.NotificationsContainer
import fr.accoradd.gitspine.ui.components.tabs.TabBar
import fr.accoradd.gitspine.ui.screens.repository.RepositoryScreen
import fr.accoradd.gitspine.ui.screens.welcome.WelcomeScreen
import fr.accoradd.gitspine.ui.theme.GitSpineTheme
import fr.accoradd.gitspine.ui.viewmodel.GraphViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun App() {
    val tabsManager: TabsManager = koinInject()
    val graphViewModel: GraphViewModel = koinInject()
    val notificationManager: NotificationManager = koinInject()

    val tabs by tabsManager.tabs.collectAsState()
    val activeTabId by tabsManager.activeTabId.collectAsState()

    var showOpenDialog by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showCloneDialog by remember { mutableStateOf(false) }
    var isDialogBusy by remember { mutableStateOf(false) }
    
    var currentTheme by remember { mutableStateOf(Theme.SYSTEM) }

    val scope = rememberCoroutineScope()

    GitSpineTheme(appTheme = currentTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Tab bar (only show if there are tabs)
                    if (tabs.isNotEmpty()) {
                        Column {
                            TabBar(
                                tabs = tabs,
                                activeTabId = activeTabId,
                                onTabSelect = { tabsManager.selectTab(it) },
                                onTabClose = { tabsManager.closeTab(it) },
                                onSettingsClick = {
                                    if (!isDialogBusy) {
                                        showSettings = true
                                    }
                                },
                                onOpenExisting = {
                                    if (!isDialogBusy) {
                                        isDialogBusy = true
                                        showOpenDialog = true
                                    }
                                },
                                onCloneRemote = {
                                    if (!isDialogBusy) {
                                        showCloneDialog = true
                                    }
                                },
                                enabled = !isDialogBusy
                            )
                            // Theme switcher for debug
                            Row {
                                Button(onClick = { currentTheme = Theme.SYSTEM }) { Text("System") }
                                Button(onClick = { currentTheme = Theme.LIGHT }) { Text("Light") }
                                Button(onClick = { currentTheme = Theme.DARK }) { Text("Dark") }
                            }
                        }
                    }

                    // Content
                    if (tabs.isEmpty()) {
                        WelcomeScreen(
                            onOpenRepository = {
                                if (!isDialogBusy) {
                                    isDialogBusy = true
                                    showOpenDialog = true
                                }
                            },
                            onCloneRepository = {
                                if (!isDialogBusy) {
                                    showCloneDialog = true
                                }
                            },
                            enabled = !isDialogBusy,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        RepositoryScreen(
                            viewModel = graphViewModel,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Notifications container
                NotificationsContainer(notificationManager = notificationManager)
            }
        }
    }

    // File chooser dialog
    if (showOpenDialog) {
        LaunchedEffect(Unit) {
            val path = FileDialogs.openDirectory("Open Git Repository")
            if (path != null) {
                tabsManager.openTab(path)
            }
            showOpenDialog = false
            isDialogBusy = false
        }
    }

    // TODO: Settings dialog
    if (showSettings) {
        // Will be implemented later
        showSettings = false
    }

    // Clone repository dialog
    if (showCloneDialog) {
        CloneRepositoryDialog(
            onDismiss = { showCloneDialog = false },
            onClone = { url, destinationPath ->
                scope.launch {
                    // Créer une notification de clonage
                    val notificationId = notificationManager.createNotification(
                        title = "Clonage du dépôt",
                        message = "Démarrage...",
                        progress = Notification.Progress.Indeterminate
                    )

                    val result = GitCloner.clone(
                        url = url,
                        destinationPath = destinationPath.toString(),
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
                        tabsManager.openTab(destinationPath)
                    } else {
                        val errorMsg = result.exceptionOrNull()?.message ?: "Erreur inconnue"
                        notificationManager.failNotification(
                            id = notificationId,
                            errorMessage = errorMsg
                        )
                    }
                }
            },
            onBrowse = { _ ->
                FileDialogs.openDirectory("Sélectionner le répertoire de destination")
            }
        )
    }
}
