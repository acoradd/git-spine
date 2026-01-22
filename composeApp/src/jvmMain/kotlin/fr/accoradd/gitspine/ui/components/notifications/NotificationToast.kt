package fr.accoradd.gitspine.ui.components.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import fr.accoradd.gitspine.domain.model.Notification
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.jewel.foundation.theme.JewelTheme
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
            delay(3000)
            visible = false
            delay(300)
            onDismiss()
        }
    }

    val iconKey = when (notification.status) {
        Notification.Status.Success -> AllIconsKeys.General.InspectionsOK
        Notification.Status.Error -> AllIconsKeys.General.Error
        else -> AllIconsKeys.General.Information
    }

    val iconTint = when (notification.status) {
        Notification.Status.Success -> JewelTheme.globalColors.text.info
        Notification.Status.Error -> JewelTheme.globalColors.text.error
        else -> JewelTheme.globalColors.text.normal
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
    ) {
        Column(
            modifier = modifier
                .width(320.dp)
                .padding(4.dp)
                .shadow(4.dp, RoundedCornerShape(6.dp))
                .background(JewelTheme.globalColors.panelBackground, RoundedCornerShape(6.dp))
                .border(1.dp, JewelTheme.globalColors.borders.normal, RoundedCornerShape(6.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        key = iconKey,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = notification.title,
                        style = JewelTheme.defaultTextStyle
                    )
                }

                IconButton(
                    onClick = {
                        visible = false
                        scope.launch {
                            delay(300)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        key = AllIconsKeys.Actions.Close,
                        contentDescription = "Fermer",
                        modifier = Modifier.size(12.dp),
                        tint = JewelTheme.globalColors.text.disabled
                    )
                }
            }

            if (notification.message.isNotBlank()) {
                Text(
                    text = notification.message,
                    modifier = Modifier.padding(top = 6.dp, start = 24.dp),
                    style = JewelTheme.defaultTextStyle,
                    color = JewelTheme.globalColors.text.info
                )
            }
        }
    }
}
