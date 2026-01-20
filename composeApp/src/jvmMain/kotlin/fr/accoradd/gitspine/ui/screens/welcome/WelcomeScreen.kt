package fr.accoradd.gitspine.ui.screens.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.accoradd.gitspine.core.config.AppConfig
import org.jetbrains.compose.resources.stringResource
import gitspine.composeapp.generated.resources.Res
import gitspine.composeapp.generated.resources.welcome_clone_repository
import gitspine.composeapp.generated.resources.welcome_open_repository
import gitspine.composeapp.generated.resources.welcome_subtitle
import gitspine.composeapp.generated.resources.welcome_title

@Composable
fun WelcomeScreen(
    onOpenRepository: () -> Unit,
    onCloneRepository: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = stringResource(Res.string.welcome_title, AppConfig.APP_NAME),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = stringResource(Res.string.welcome_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Box {
                    Button(
                        onClick = onOpenRepository,
                        enabled = enabled
                    ) {
                        Text(stringResource(Res.string.welcome_open_repository))
                    }
                }
                Box {
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
}
