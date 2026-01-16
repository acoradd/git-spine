package fr.accoradd.gitspine.ui.components.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.nio.file.Path

@Composable
fun CloneRepositoryDialog(
    onDismiss: () -> Unit,
    onClone: (url: String, destinationPath: Path) -> Unit,
    onBrowse: suspend (currentPath: String) -> Path?,
    modifier: Modifier = Modifier
) {
    var url by remember { mutableStateOf("") }
    var destinationPath by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cloner un dépôt distant") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // URL field
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL du dépôt") },
                    placeholder = { Text("https://github.com/user/repo.git") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Destination path field with browse button
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OutlinedTextField(
                        value = destinationPath,
                        onValueChange = { destinationPath = it },
                        label = { Text("Répertoire de destination") },
                        placeholder = { Text("C:/repos/mon-projet") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    val selected = onBrowse(destinationPath)
                                    if (selected != null) {
                                        destinationPath = selected.toString()
                                    }
                                }
                            }
                        ) {
                            Text("Parcourir...")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (url.isNotBlank() && destinationPath.isNotBlank()) {
                        onClone(url, Path.of(destinationPath))
                        onDismiss()
                    }
                },
                enabled = url.isNotBlank() && destinationPath.isNotBlank()
            ) {
                Text("Cloner")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        },
        modifier = modifier
    )
}
