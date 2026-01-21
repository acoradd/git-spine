package fr.accoradd.gitspine.ui.components.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.accoradd.gitspine.ui.viewmodel.TitlebarViewModel
import gitspine.composeapp.generated.resources.Res
import gitspine.composeapp.generated.resources.ic_gitspine
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.jewel.window.DecoratedWindowScope
import org.jetbrains.jewel.window.TitleBar
import org.jetbrains.jewel.window.newFullscreenControls
import org.koin.compose.koinInject

@Composable
fun DecoratedWindowScope.AppTitleBar() {
    val viewModel: TitlebarViewModel = koinInject()
    val state by viewModel.state.collectAsState()
    TitleBar(Modifier.newFullscreenControls()) {
        Row(modifier = Modifier.align(Alignment.Start)
            .padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_gitspine),
                contentDescription = "Logo GitSpine",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (state.title != null) {
                Text(state.title!!)
            }
        }
    }
}
