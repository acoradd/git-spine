package fr.accoradd.gitspine.ui.components.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.accoradd.gitspine.domain.model.Tab
import fr.accoradd.gitspine.ui.components.common.ToolbarIconButton

@Composable
fun TabBar(
    tabs: List<Tab>,
    activeTabId: String?,
    onTabSelect: (String) -> Unit,
    onTabClose: (String) -> Unit,
    onAddClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(MaterialTheme.colorScheme.surfaceContainer),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Scrollable tabs area
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState())
                .padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                TabItem(
                    tab = tab,
                    isActive = tab.id == activeTabId,
                    onClick = { onTabSelect(tab.id) },
                    onClose = { onTabClose(tab.id) }
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
        }

        // Fixed icons on the right
        Row(verticalAlignment = Alignment.CenterVertically) {
            ToolbarIconButton(
                onClick = onAddClick,
                icon = Icons.Default.Add,
                contentDescription = "Add repository"
            )

            ToolbarIconButton(
                onClick = onSettingsClick,
                icon = Icons.Default.Settings,
                contentDescription = "Settings"
            )
        }
    }
}
