package fr.accoradd.gitspine.ui.components.repository

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import fr.accoradd.gitspine.domain.model.Commit
import fr.accoradd.gitspine.domain.model.ResetMode
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text

data class CommitContextMenuState(
    val isVisible: Boolean = false,
    val commit: Commit? = null,
    val position: Offset = Offset.Zero
)

sealed interface CommitContextMenuAction {
    data class Checkout(val commit: Commit) : CommitContextMenuAction
    data class CreateBranch(val commit: Commit, val branchName: String) : CommitContextMenuAction
    data class Reset(val commit: Commit, val mode: ResetMode) : CommitContextMenuAction
    data class Revert(val commit: Commit) : CommitContextMenuAction
    data class CreateTag(val commit: Commit, val tagName: String) : CommitContextMenuAction
}

@Composable
fun CommitContextMenu(
    state: CommitContextMenuState,
    onDismiss: () -> Unit,
    onAction: (CommitContextMenuAction) -> Unit,
    onShowInputDialog: (title: String, placeholder: String, onConfirm: (String) -> Unit) -> Unit
) {
    if (state.isVisible && state.commit != null) {
        val commit = state.commit
        var showResetSubmenu by remember { mutableStateOf(false) }
        val density = LocalDensity.current

        // Compensation pour le padding horizontal (28.dp) et autres décalages
        val offsetX = with(density) { state.position.x - 28.dp.toPx() }
        val offsetY = with(density) { state.position.y - 28.dp.toPx() }

        Popup(
            offset = IntOffset(offsetX.toInt(), offsetY.toInt()),
            onDismissRequest = {
                showResetSubmenu = false
                onDismiss()
            },
            properties = PopupProperties(focusable = true)
        ) {
            Column(
                modifier = Modifier
                    .shadow(8.dp, RoundedCornerShape(4.dp))
                    .background(JewelTheme.globalColors.panelBackground, RoundedCornerShape(4.dp))
                    .padding(4.dp)
                    .width(IntrinsicSize.Max)
            ) {
                ContextMenuItem(
                    text = "Checkout",
                    onClick = {
                        onAction(CommitContextMenuAction.Checkout(commit))
                        onDismiss()
                    },
                    onHover = { showResetSubmenu = false }
                )

                ContextMenuItem(
                    text = "Créer une branche...",
                    onClick = {
                        onDismiss()
                        onShowInputDialog("Nouvelle branche", "Nom de la branche") { branchName ->
                            if (branchName.isNotBlank()) {
                                onAction(CommitContextMenuAction.CreateBranch(commit, branchName))
                            }
                        }
                    },
                    onHover = { showResetSubmenu = false }
                )

                ContextMenuDivider()

                ContextMenuItemWithSubmenu(
                    text = "Reset vers ce commit",
                    expanded = showResetSubmenu,
                    onHover = { showResetSubmenu = true },
                    submenuContent = {
                        Column(
                            modifier = Modifier
                                .width(IntrinsicSize.Max)
                                .shadow(8.dp, RoundedCornerShape(4.dp))
                                .background(JewelTheme.globalColors.panelBackground, RoundedCornerShape(4.dp))
                                .padding(4.dp)
                        ) {
                            ContextMenuItem(
                                text = "Soft (conserver les changements indexés)",
                                onClick = {
                                    onAction(CommitContextMenuAction.Reset(commit, ResetMode.SOFT))
                                    showResetSubmenu = false
                                    onDismiss()
                                }
                            )
                            ContextMenuItem(
                                text = "Mixed (conserver les changements non indexés)",
                                onClick = {
                                    onAction(CommitContextMenuAction.Reset(commit, ResetMode.MIXED))
                                    showResetSubmenu = false
                                    onDismiss()
                                }
                            )
                            ContextMenuItem(
                                text = "Hard (supprimer tous les changements)",
                                onClick = {
                                    onAction(CommitContextMenuAction.Reset(commit, ResetMode.HARD))
                                    showResetSubmenu = false
                                    onDismiss()
                                }
                            )
                        }
                    }
                )

                ContextMenuItem(
                    text = "Revert ce commit",
                    onClick = {
                        onAction(CommitContextMenuAction.Revert(commit))
                        onDismiss()
                    },
                    onHover = { showResetSubmenu = false }
                )

                ContextMenuDivider()

                ContextMenuItem(
                    text = "Créer un tag...",
                    onClick = {
                        onDismiss()
                        onShowInputDialog("Nouveau tag", "Nom du tag") { tagName ->
                            if (tagName.isNotBlank()) {
                                onAction(CommitContextMenuAction.CreateTag(commit, tagName))
                            }
                        }
                    },
                    onHover = { showResetSubmenu = false }
                )
            }
        }
    }
}

@Composable
private fun ContextMenuItem(
    text: String,
    onClick: () -> Unit,
    onHover: () -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    LaunchedEffect(isHovered) {
        if (isHovered) onHover()
    }

    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(interactionSource)
            .background(
                if (isHovered) JewelTheme.globalColors.outlines.focused.copy(alpha = 0.2f)
                else JewelTheme.globalColors.panelBackground
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

@Composable
private fun ContextMenuItemWithSubmenu(
    text: String,
    expanded: Boolean,
    onHover: () -> Unit = {},
    submenuContent: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val density = LocalDensity.current
    var itemWidth by remember { mutableStateOf(0) }

    LaunchedEffect(isHovered) {
        if (isHovered) onHover()
    }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    itemWidth = coordinates.size.width
                }
                .hoverable(interactionSource)
                .background(
                    if (isHovered || expanded) JewelTheme.globalColors.outlines.focused.copy(alpha = 0.2f)
                    else JewelTheme.globalColors.panelBackground
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = text)
            Text(text = "▶", modifier = Modifier.padding(start = 8.dp))
        }

        if (expanded) {
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(itemWidth + with(density) { 4.dp.roundToPx() }, 0)
            ) {
                submenuContent()
            }
        }
    }
}

@Composable
private fun ContextMenuDivider() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(vertical = 4.dp)
            .background(JewelTheme.globalColors.borders.normal)
    )
}
