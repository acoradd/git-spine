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
import fr.accoradd.gitspine.infrastructure.filesystem.FileDialogs
import kotlinx.coroutines.launch
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import java.nio.file.Path
import kotlin.io.path.exists

@Composable
fun CloneRepositoryDialog(
    onDismiss: () -> Unit,
    onClone: (Path, String) -> Unit
) {
    val scope = rememberCoroutineScope()

    val urlState = rememberTextFieldState()
    val pathState = rememberTextFieldState()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = { onDismiss() }) {
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
                    modifier = Modifier.fillMaxWidth()
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
                        modifier = Modifier.weight(1f)
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
                                val path =
                                    FileDialogs.openDirectory("Sélectionner le répertoire de destination", startPath)
                                if (path != null) {
                                    pathState.setTextAndPlaceCursorAtEnd(path.toAbsolutePath().toString())
                                    errorMessage = null
                                }
                            }
                        }
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
                    onClick = onDismiss
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
                        errorMessage = null

                        onClone(Path.of(dest), url)
                    }
                ) {
                    Text("Cloner")
                }
            }
        }
    }
}
