package fr.accoradd.gitspine.ui.components.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.accoradd.gitspine.domain.model.Notification
import kotlinx.coroutines.launch
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.CircularProgressIndicator
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys

@Composable
fun NotificationToast(
    notification: Notification,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // Auto-dismiss sur succès après 3 secondes
    LaunchedEffect(notification.status) {
        if (notification.status == Notification.Status.Success) {
            kotlinx.coroutines.delay(3000)
            visible = false
            kotlinx.coroutines.delay(300) // Animation duration
            onDismiss()
        }
    }

    val backgroundColor = when (notification.status) {
        Notification.Status.Error -> Color(0xFFFFCDD2)
        Notification.Status.Success -> Color(0xFFC8E6C9)
        Notification.Status.Running -> JewelTheme.globalColors.panelBackground
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
    ) {
        Column(
            modifier = modifier
                .width(350.dp)
                .padding(8.dp)
                .shadow(6.dp, RoundedCornerShape(8.dp))
                .background(backgroundColor, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            // Header: Titre + bouton fermer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Icon de statut
                    when (notification.status) {
                        Notification.Status.Success -> Icon(
                            key = AllIconsKeys.General.InspectionsOK,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(20.dp)
                        )
                        Notification.Status.Error -> Icon(
                            key = AllIconsKeys.General.Error,
                            contentDescription = null,
                            tint = Color(0xFFC62828),
                            modifier = Modifier.size(20.dp)
                        )
                        else -> {}
                    }

                    if (notification.status != Notification.Status.Running) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(text = notification.title)
                }

                IconButton(
                    onClick = {
                        visible = false
                        scope.launch {
                            kotlinx.coroutines.delay(300)
                            onDismiss()
                        }
                    }
                ) {
                    Icon(
                        key = AllIconsKeys.Actions.Close,
                        contentDescription = "Fermer",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Message
            if (notification.message.isNotBlank()) {
                Text(
                    text = notification.message,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Progress bar
            if (notification.status == Notification.Status.Running) {
                Spacer(modifier = Modifier.height(12.dp))
                CircularProgressIndicator()
            }
        }
    }
}
