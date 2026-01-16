package fr.accoradd.gitspine.ui.components.common

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AddRepositoryMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onOpenExisting: () -> Unit,
    onCloneRemote: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        DropdownMenuItem(
            text = { Text("Ouvrir un dépôt existant") },
            onClick = {
                onDismiss()
                onOpenExisting()
            }
        )
        DropdownMenuItem(
            text = { Text("Cloner un dépôt distant") },
            onClick = {
                onDismiss()
                onCloneRemote()
            }
        )
    }
}