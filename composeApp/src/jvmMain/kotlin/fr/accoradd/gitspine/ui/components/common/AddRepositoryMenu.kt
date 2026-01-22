package fr.accoradd.gitspine.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun AddRepositoryMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onOpenExisting: () -> Unit,
    onCloneRemote: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (expanded) {
        Popup(onDismissRequest = onDismiss) {
            Column(
                modifier = modifier
                    .shadow(8.dp, RoundedCornerShape(4.dp))
                    .background(JewelTheme.globalColors.panelBackground, RoundedCornerShape(4.dp))
                    .padding(4.dp)
            ) {
                MenuItem(
                    text = "Ouvrir un dépôt existant",
                    onClick = {
                        onDismiss()
                        onOpenExisting()
                    }
                )
                MenuItem(
                    text = "Cloner un dépôt distant",
                    onClick = {
                        onDismiss()
                        onCloneRemote()
                    }
                )
            }
        }
    }
}

@Composable
private fun MenuItem(
    text: String,
    onClick: () -> Unit
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}