package fr.accoradd.gitspine.ui.components.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import fr.accoradd.gitspine.core.config.AppConfig
import fr.accoradd.gitspine.ui.navigation.AppNavigator
import fr.accoradd.gitspine.ui.navigation.Screen
import fr.accoradd.gitspine.ui.viewmodel.ProjectState
import fr.accoradd.gitspine.ui.viewmodel.ProjectViewModel
import fr.accoradd.gitspine.ui.viewmodel.TitlebarViewModel
import gitspine.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Dropdown
import org.jetbrains.jewel.ui.component.IconActionButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.separator
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.window.DecoratedWindowScope
import org.jetbrains.jewel.window.TitleBar
import org.jetbrains.jewel.window.newFullscreenControls
import org.koin.compose.koinInject

@OptIn(ExperimentalJewelApi::class)
@Composable
fun DecoratedWindowScope.AppTitleBar(
    navigator: AppNavigator,
    navController: NavController
) {
    val viewModel: TitlebarViewModel = koinInject()
    val projectViewModel: ProjectViewModel = koinInject()

    val state by viewModel.state.collectAsState()
    val projectState by projectViewModel.state.collectAsState()
    val welcomeTitle = stringResource(Res.string.welcome_title, AppConfig.APP_NAME)

    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            when {
                destination.hasRoute<Screen.Welcome>() -> viewModel.setTitle(welcomeTitle)
                else -> viewModel.setTitle(null)
            }
        }
        navController.addOnDestinationChangedListener(listener)

        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }


    TitleBar(Modifier.newFullscreenControls()) {
        Row(
            modifier = Modifier.align(Alignment.Start)
                .padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_gitspine),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            if (state.title != null) {
                Text(state.title!!)
                Spacer(modifier = Modifier.width(16.dp))
            }
            if (projectState.project != null) {
                AppTitleBarProject(viewModel, projectViewModel, projectState, navigator)
            }
        }
        Row(
            modifier = Modifier.align(Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (projectState.project != null) {
                IconActionButton(
                    key = AllIconsKeys.General.Settings,
                    contentDescription = "Settings",
                    onClick = { navigator.navigateToSettings() })
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppTitleBarProject(
    viewModel: TitlebarViewModel,
    projectViewModel: ProjectViewModel,
    projectState: ProjectState,
    navigator: AppNavigator
) {
    Dropdown(
        Modifier.height(30.dp).width(200.dp),
        menuModifier = Modifier.width(400.dp),
        menuContent = {
            selectableItem(
                selected = false,
                onClick = { viewModel.openProject() }
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(Res.string.dropdown_open_repository))
                }
            }
            selectableItem(
                selected = false,
                onClick = { navigator.openCloneDialog() }
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(Res.string.dropdown_clone_repository))
                }
            }

            if (projectState.recentsProject.size > 1) {
                separator()
            }

            projectState.recentsProject.forEach {
                if (projectState.project != it) {
                    selectableItem(
                        selected = false,
                        onClick = {
                            navigator.navigateToRepository(it)
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(it.name)
                                Text(
                                    text = it.path.toAbsolutePath().toString(),
                                    color = JewelTheme.globalColors.text.info
                                )
                            }
                            IconActionButton(
                                key = AllIconsKeys.General.Close,
                                contentDescription = "Close",
                                onClick = { projectViewModel.removeFromRecent(it) }
                            )
                        }
                    }
                }
            }
        },
    ) {
        Text(projectState.project!!.name)
    }

    Spacer(modifier = Modifier.width(16.dp))

    IconActionButton(key = AllIconsKeys.Vcs.Fetch, contentDescription = "Fetch", onClick = { projectViewModel.fetch() })
    IconActionButton(key = AllIconsKeys.Vcs.Clone, contentDescription = "Pull", onClick = { projectViewModel.pull() })
    IconActionButton(key = AllIconsKeys.Vcs.Push, contentDescription = "Push", onClick = { projectViewModel.push() })
    IconActionButton(
        key = AllIconsKeys.Vcs.ShelveSilent,
        contentDescription = "Stash",
        onClick = { projectViewModel.stash() })
    IconActionButton(
        key = AllIconsKeys.Vcs.Unshelve,
        contentDescription = "Unstash",
        onClick = { projectViewModel.unstash() })
    IconActionButton(
        key = AllIconsKeys.Vcs.Branch,
        contentDescription = "New Branch",
        onClick = { projectViewModel.createBranch() })

}
