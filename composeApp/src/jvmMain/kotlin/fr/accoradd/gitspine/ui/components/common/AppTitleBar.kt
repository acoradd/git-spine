package fr.accoradd.gitspine.ui.components.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.accoradd.gitspine.domain.model.Project
import fr.accoradd.gitspine.infrastructure.filesystem.FileDialogs
import fr.accoradd.gitspine.ui.navigation.AppNavigator
import fr.accoradd.gitspine.ui.viewmodel.ProjectState
import fr.accoradd.gitspine.ui.viewmodel.ProjectViewModel
import fr.accoradd.gitspine.ui.viewmodel.TitlebarViewModel
import gitspine.composeapp.generated.resources.Res
import gitspine.composeapp.generated.resources.dropdown_clone_repository
import gitspine.composeapp.generated.resources.dropdown_open_repository
import gitspine.composeapp.generated.resources.ic_gitspine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.ui.component.Dropdown
import org.jetbrains.jewel.ui.component.IconActionButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.Tooltip
import org.jetbrains.jewel.ui.component.separator
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.window.DecoratedWindowScope
import org.jetbrains.jewel.window.TitleBar
import org.jetbrains.jewel.window.newFullscreenControls
import org.koin.compose.koinInject

@OptIn(ExperimentalJewelApi::class)
@Composable
fun DecoratedWindowScope.AppTitleBar(
    navigator: AppNavigator
) {
    val viewModel: TitlebarViewModel = koinInject()
    val projectViewModel: ProjectViewModel = koinInject()

    val state by viewModel.state.collectAsState()
    val projectState by projectViewModel.state.collectAsState()

    val scope = rememberCoroutineScope()

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
                AppTitleBarProject(scope, projectViewModel, projectState, navigator)
            }
        }
        Row(
            modifier = Modifier.align(Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (projectState.project != null) {
                IconActionButton(key = AllIconsKeys.General.Settings, contentDescription = "Settings", onClick = { navigator.navigateToSettings() })
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppTitleBarProject(
    scope: CoroutineScope,
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
                onClick = {
                    scope.launch {
                        val path = FileDialogs.openDirectory("Open Git Repository")
                        if (path != null) {
                            var newProject = Project(path = path)
                            newProject = projectViewModel.addAndSetProject(newProject).project!!
                            navigator.navigateToRepository(newProject)
                        }
                    }
                }
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

            if (projectState.recentsProject.isNotEmpty()) {
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
                        Column {
                            Text(it.name)
                            Text(it.path.toAbsolutePath().toString())
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
    IconActionButton(key = AllIconsKeys.Vcs.ShelveSilent, contentDescription = "Stash", onClick = { projectViewModel.stash() })
    IconActionButton(key = AllIconsKeys.Vcs.Unshelve, contentDescription = "Unstash", onClick = { projectViewModel.unstash() })
    IconActionButton(key = AllIconsKeys.Vcs.Branch, contentDescription = "New Branch", onClick = { projectViewModel.createBranch() })

}
