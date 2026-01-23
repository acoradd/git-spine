package fr.accoradd.gitspine.ui.components.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.Divider

@Composable
fun HorizontalDivider(
    modifier: Modifier = Modifier
) {
    Divider(
        orientation = Orientation.Horizontal,
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp),
        color = JewelTheme.globalColors.borders.normal,
        thickness = 1.dp
    )
}
