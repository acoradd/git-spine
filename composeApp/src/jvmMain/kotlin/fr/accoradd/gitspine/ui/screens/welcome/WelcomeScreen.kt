package fr.accoradd.gitspine.ui.screens.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.accoradd.gitspine.core.config.AppConfig
import fr.accoradd.gitspine.ui.viewmodel.WelcomeScreenViewModel
import gitspine.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icon.IconKey
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.koin.compose.koinInject

@Composable
fun WelcomeScreen(
    welcomeViewModel: WelcomeScreenViewModel = koinInject(),
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Main content centered
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(128.dp))
            // Title
            Text(
                text = stringResource(Res.string.welcome_title, AppConfig.APP_NAME),
                style = JewelTheme.defaultTextStyle.copy(fontSize = 32.sp)
            )

            Text(
                text = stringResource(Res.string.welcome_subtitle)
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(64.dp)
            ) {
                WelcomeActionButton(
                    iconKey = AllIconsKeys.Actions.MenuOpen,
                    label = stringResource(Res.string.welcome_open_repository),
                    onClick = welcomeViewModel::openRepository
                )

                WelcomeActionButton(
                    iconKey = AllIconsKeys.Vcs.FromVCSDialog,
                    label = stringResource(Res.string.welcome_clone_repository),
                    onClick = welcomeViewModel::goToClone
                )
            }
        }

        // Settings button in bottom left
        IconButton(
            onClick = { welcomeViewModel.openSettings() },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Icon(
                key = AllIconsKeys.General.Settings,
                contentDescription = "Settings"
            )
        }
    }
}

@Composable
private fun WelcomeActionButton(
    iconKey: IconKey,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = modifier
                .size(56.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(JewelTheme.globalColors.toolwindowBackground)
        ) {
            Icon(
                key = iconKey,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = JewelTheme.globalColors.outlines.focused
            )
        }
        Text(
            text = label,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
