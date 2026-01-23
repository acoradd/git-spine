package fr.accoradd.gitspine.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.accoradd.gitspine.core.settings.Settings
import fr.accoradd.gitspine.core.settings.Theme
import fr.accoradd.gitspine.ui.viewmodel.AppViewModel
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.RadioButtonRow
import org.jetbrains.jewel.ui.component.Text
import org.koin.compose.koinInject

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    settings: Settings = koinInject(),
    appViewModel: AppViewModel = koinInject()
) {

    val currentTheme = settings.theme.collectAsState(Theme.SYSTEM)

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
            Text("Paramètres")
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
            // Section Thème
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Thème de l'application")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Theme.entries.forEach { theme ->
                        RadioButtonRow(
                            text = theme.name.lowercase().replaceFirstChar { it.uppercase() },
                            selected = currentTheme.value == theme,
                            onClick = { appViewModel.setTheme(theme) }
                        )
                    }
                }
            }
        }
    }
}
