package fr.accoradd.gitspine.ui.components.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.accoradd.gitspine.domain.model.Tab
import fr.accoradd.gitspine.ui.components.common.CloseButton

@Composable
fun TabItem(
    tab: Tab,
    isActive: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isActive) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    Row(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = tab.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 150.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        CloseButton(onClose = onClose)
    }
}
