package fr.accoradd.gitspine.ui.components.common

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.icon.IconKey

@Composable
fun ToolbarIconButton(
    onClick: () -> Unit,
    iconKey: IconKey,
    contentDescription: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    ) {
        Icon(
            key = iconKey,
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp)
        )
    }
}
