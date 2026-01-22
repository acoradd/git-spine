package fr.accoradd.gitspine.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fr.accoradd.gitspine.core.settings.Theme
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.RadioButtonRow
import org.jetbrains.jewel.ui.component.Text

@Composable
fun SettingsDialog(
    currentTheme: Theme,
    onThemeChange: (Theme) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .shadow(8.dp, RoundedCornerShape(8.dp))
                .background(JewelTheme.globalColors.panelBackground, RoundedCornerShape(8.dp))
                .padding(24.dp)
                .width(300.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Paramètres", style = JewelTheme.defaultTextStyle)

            // Section Thème
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Thème de l'application")
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Theme.entries.forEach { theme ->
                        RadioButtonRow(
                            text = theme.name.lowercase().replaceFirstChar { it.uppercase() },
                            selected = currentTheme == theme,
                            onClick = { onThemeChange(theme) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                DefaultButton(onClick = onDismiss) {
                    Text("Fermer")
                }
            }
        }
    }
}
