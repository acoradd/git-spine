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
import fr.accoradd.gitspine.domain.model.Branch
import fr.accoradd.gitspine.domain.model.Commit
import fr.accoradd.gitspine.domain.model.RefCommit
import fr.accoradd.gitspine.domain.model.ResetMode
import fr.accoradd.gitspine.domain.model.Tag
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text

data class RefContextMenuState(
    val isVisible: Boolean = false,
    val ref: RefCommit? = null,
    val commit: Commit? = null,
    val position: Offset = Offset.Zero,
    val hasRemoteTracking: Boolean = false
)

sealed interface RefContextMenuAction {
    // Ref-specific actions
    data class CheckoutRef(val ref: RefCommit) : RefContextMenuAction
    data class DeleteRef(val ref: RefCommit) : RefContextMenuAction
    data class PushBranch(val branch: Branch) : RefContextMenuAction
    data class PullMergeBranch(val branch: Branch) : RefContextMenuAction

    // Commit actions (inherited from CommitContextMenu)
    data class CheckoutCommit(val commit: Commit) : RefContextMenuAction
    data class CreateBranch(val commit: Commit, val branchName: String) : RefContextMenuAction
    data class Reset(val commit: Commit, val mode: ResetMode) : RefContextMenuAction
    data class Revert(val commit: Commit) : RefContextMenuAction
    data class CreateTag(val commit: Commit, val tagName: String) : RefContextMenuAction
}

@Composable
fun RefContextMenu(
    state: RefContextMenuState,
    onDismiss: () -> Unit,
    onAction: (RefContextMenuAction) -> Unit,
    onShowInputDialog: (title: String, placeholder: String, onConfirm: (String) -> Unit) -> Unit
) {
    if (state.isVisible && state.ref != null) {
        val ref = state.ref
        val commit = state.commit
        val density = LocalDensity.current
        var showResetSubmenu by remember { mutableStateOf(false) }

        val isBranch = ref is Branch
        val isLocalBranch = isBranch && !(ref as Branch).isRemote
        val isTag = ref is Tag

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
                // Section: Ref actions
                RefMenuItem(
                    text = if (isBranch) "Checkout cette branche" else "Checkout ce tag",
                    onClick = {
                        onAction(RefContextMenuAction.CheckoutRef(ref))
                        onDismiss()
                    },
                    onHover = { showResetSubmenu = false }
                )

                if (isLocalBranch) {
                    RefMenuItem(
                        text = "Push",
                        onClick = {
                            onAction(RefContextMenuAction.PushBranch(ref as Branch))
                            onDismiss()
                        },
                        onHover = { showResetSubmenu = false }
                    )

                    if (state.hasRemoteTracking) {
                        RefMenuItem(
                            text = "Pull et merge",
                            onClick = {
                                onAction(RefContextMenuAction.PullMergeBranch(ref as Branch))
                                onDismiss()
                            },
                            onHover = { showResetSubmenu = false }
                        )
                    }
                }

                RefMenuItem(
                    text = if (isBranch) "Supprimer la branche" else "Supprimer le tag",
                    onClick = {
                        onAction(RefContextMenuAction.DeleteRef(ref))
                        onDismiss()
                    },
                    onHover = { showResetSubmenu = false }
                )

                // Separator
                if (commit != null) {
                    RefMenuDivider()

                    // Section: Commit actions
                    Text(
                        text = "Commit",
                        color = JewelTheme.globalColors.text.disabled,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )

                    RefMenuItem(
                        text = "Checkout le commit",
                        onClick = {
                            onAction(RefContextMenuAction.CheckoutCommit(commit))
                            onDismiss()
                        },
                        onHover = { showResetSubmenu = false }
                    )

                    RefMenuItem(
                        text = "Créer une branche...",
                        onClick = {
                            onDismiss()
                            onShowInputDialog("Nouvelle branche", "Nom de la branche") { branchName ->
                                if (branchName.isNotBlank()) {
                                    onAction(RefContextMenuAction.CreateBranch(commit, branchName))
                                }
                            }
                        },
                        onHover = { showResetSubmenu = false }
                    )

                    RefMenuItemWithSubmenu(
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
                                RefMenuItem(
                                    text = "Soft",
                                    onClick = {
                                        onAction(RefContextMenuAction.Reset(commit, ResetMode.SOFT))
                                        showResetSubmenu = false
                                        onDismiss()
                                    }
                                )
                                RefMenuItem(
                                    text = "Mixed",
                                    onClick = {
                                        onAction(RefContextMenuAction.Reset(commit, ResetMode.MIXED))
                                        showResetSubmenu = false
                                        onDismiss()
                                    }
                                )
                                RefMenuItem(
                                    text = "Hard",
                                    onClick = {
                                        onAction(RefContextMenuAction.Reset(commit, ResetMode.HARD))
                                        showResetSubmenu = false
                                        onDismiss()
                                    }
                                )
                            }
                        }
                    )

                    RefMenuItem(
                        text = "Revert ce commit",
                        onClick = {
                            onAction(RefContextMenuAction.Revert(commit))
                            onDismiss()
                        },
                        onHover = { showResetSubmenu = false }
                    )

                    RefMenuItem(
                        text = "Créer un tag...",
                        onClick = {
                            onDismiss()
                            onShowInputDialog("Nouveau tag", "Nom du tag") { tagName ->
                                if (tagName.isNotBlank()) {
                                    onAction(RefContextMenuAction.CreateTag(commit, tagName))
                                }
                            }
                        },
                        onHover = { showResetSubmenu = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun RefMenuItem(
    text: String,
    onClick: () -> Unit,
    onHover: () -> Unit = {}
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
private fun RefMenuItemWithSubmenu(
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
private fun RefMenuDivider() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(vertical = 4.dp)
            .background(JewelTheme.globalColors.borders.normal)
    )
}
