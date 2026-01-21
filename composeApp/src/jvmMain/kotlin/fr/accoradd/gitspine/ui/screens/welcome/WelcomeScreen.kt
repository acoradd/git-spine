package fr.accoradd.gitspine.ui.screens.welcome

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.accoradd.gitspine.core.config.AppConfig
import fr.accoradd.gitspine.ui.viewmodel.TitlebarViewModel
import org.jetbrains.compose.resources.stringResource
import gitspine.composeapp.generated.resources.Res
import gitspine.composeapp.generated.resources.welcome_title
import gitspine.composeapp.generated.resources.welcome_subtitle
import gitspine.composeapp.generated.resources.welcome_open_repository
import gitspine.composeapp.generated.resources.welcome_clone_repository
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text
import org.koin.compose.koinInject

@Composable
fun WelcomeScreen(
    onOpenRepository: () -> Unit,
    onCloneRepository: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    koinInject<TitlebarViewModel>()
        .setTitle(stringResource(Res.string.welcome_title, AppConfig.APP_NAME))
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Title
            Text(
                text = stringResource(Res.string.welcome_title, AppConfig.APP_NAME),
            )

            Text(
                text = stringResource(Res.string.welcome_subtitle)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DefaultButton(
                    onClick = onOpenRepository,
                    enabled = enabled
                ) {
                    Text(stringResource(Res.string.welcome_open_repository))
                }

                OutlinedButton(
                    onClick = onCloneRepository,
                    enabled = enabled
                ) {
                    Text(stringResource(Res.string.welcome_clone_repository))
                }
            }
        }
    }
}
