package fr.accoradd.gitspine

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import fr.accoradd.gitspine.core.settings.Theme
import fr.accoradd.gitspine.core.tabs.TabsManager
import fr.accoradd.gitspine.infrastructure.filesystem.FileDialogs
import fr.accoradd.gitspine.ui.components.tabs.TabBar
import fr.accoradd.gitspine.ui.screens.repository.RepositoryScreen
import fr.accoradd.gitspine.ui.screens.welcome.WelcomeScreen
import fr.accoradd.gitspine.ui.theme.GitSpineTheme
import fr.accoradd.gitspine.ui.viewmodel.GraphViewModel
import org.koin.compose.koinInject

@Composable
fun App() {
    val tabsManager: TabsManager = koinInject()
    val graphViewModel: GraphViewModel = koinInject()

    val tabs by tabsManager.tabs.collectAsState()
    val activeTabId by tabsManager.activeTabId.collectAsState()

    var showOpenDialog by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    GitSpineTheme(appTheme = Theme.SYSTEM) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Tab bar (only show if there are tabs)
            if (tabs.isNotEmpty()) {
                TabBar(
                    tabs = tabs,
                    activeTabId = activeTabId,
                    onTabSelect = { tabsManager.selectTab(it) },
                    onTabClose = { tabsManager.closeTab(it) },
                    onAddClick = { showOpenDialog = true },
                    onSettingsClick = { showSettings = true }
                )
            }

            // Content
            if (tabs.isEmpty()) {
                WelcomeScreen(
                    onOpenRepository = { showOpenDialog = true },
                    onCloneRepository = { /* TODO: Clone dialog */ },
                    modifier = Modifier.weight(1f)
                )
            } else {
                RepositoryScreen(
                    viewModel = graphViewModel,
                    modifier = Modifier.weight(1f)
                )
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
        }
    }

    // TODO: Settings dialog
    if (showSettings) {
        // Will be implemented later
        showSettings = false
    }
}
