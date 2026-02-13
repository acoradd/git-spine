package fr.accoradd.gitspine.ui.components.repository

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import fr.accoradd.gitspine.domain.model.Branch
import fr.accoradd.gitspine.domain.model.RefCommit
import fr.accoradd.gitspine.domain.model.Tag
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RefBadgeList(
    refs: List<RefCommit>,
    backgroundColor: Color,
    onRefRightClick: (RefCommit, Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    if (refs.isEmpty()) return

    val density = LocalDensity.current

    var layoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var popupMinWidth by remember { mutableStateOf<Dp?>(null) }
    var popupMinHeight by remember { mutableStateOf<Dp?>(null) }
    var popupOffsetY by remember { mutableStateOf<Int?>(null) }
    val interactionBadgeSource = remember { MutableInteractionSource() }
    val interactionPopupSource = remember { MutableInteractionSource() }
    val isBadgeHovered by interactionBadgeSource.collectIsHoveredAsState()
    val isPopupHovered by interactionPopupSource.collectIsHoveredAsState()
    val popupOffsetX = with(density) { 12.dp.toPx().toInt() }

    Box(
        modifier = modifier
            .onGloballyPositioned { layoutCoordinates = it }
            .hoverable(interactionBadgeSource)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(12.dp))

            RefBadgeItem(
                ref = refs.first(),
                backgroundColor = backgroundColor,
                extraCount = if (refs.size > 1) refs.size - 1 else 0,
                onRightClick = { offset ->
                    val rootOffset = layoutCoordinates?.localToRoot(offset) ?: offset
                    onRefRightClick(refs.first(), rootOffset)
                },
                modifier = Modifier.weight(0.8f).onGloballyPositioned {
                    popupMinWidth = with(density) {it.size.width.toDp()}
                    popupMinHeight = with(density) {it.size.height.toDp()}
                    popupOffsetY = (layoutCoordinates?.localPositionOf(it, Offset.Zero)?.y?.toInt())
                }
            )

            Box(
                modifier = Modifier
                    .weight(0.2f)
                    .height(1.dp)
                    .background(backgroundColor)
            )
        }

        if ((isBadgeHovered || isPopupHovered)) {
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(popupOffsetX, popupOffsetY ?: 0),
                properties = PopupProperties(focusable = false),
            ) {
                Column(
                    modifier = Modifier
                        .defaultMinSize(minWidth = popupMinWidth ?: 0.dp, minHeight = popupMinHeight ?: 0.dp)
                        .width(IntrinsicSize.Max)
                        .clip(RoundedCornerShape(2.dp))
                        .background(JewelTheme.globalColors.panelBackground, RoundedCornerShape(4.dp))
                        .hoverable(interactionPopupSource),
                ) {
                    refs.forEach { ref ->
                        ExpandedRefBadgeItem(
                            ref = ref,
                            backgroundColor = backgroundColor,
                            onRightClick = { offset ->
                                println(offset)
                                onRefRightClick(ref, offset)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun RefBadgeItem(
    ref: RefCommit,
    backgroundColor: Color,
    extraCount: Int = 0,
    onRightClick: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val isBranch = ref is Branch
    val isLocal = isBranch && !ref.isRemote
    val isTag = ref is Tag
    val displayName = when {
        isBranch && !isLocal -> ref.name.substringAfter("/", ref.name)
        else -> ref.name
    }


    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(2.dp))
                .background(if (isHovered) backgroundColor.copy(alpha = 0.6f) else backgroundColor)
                .onPointerEvent(PointerEventType.Press) { event ->
                    if (event.buttons.isSecondaryPressed) {
                        event.changes.forEach { it.consume() }
                        val pos = event.changes.firstOrNull()?.position ?: Offset.Zero
                        onRightClick(pos)
                    }
                }
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
                modifier = Modifier.weight(1f),
                style = JewelTheme.defaultTextStyle.copy(fontSize = 12.sp)
            )

            if (extraCount > 0) {
                Text(
                    text = "+$extraCount",
                    style = JewelTheme.defaultTextStyle.copy(fontSize = 10.sp),
                    color = JewelTheme.globalColors.text.disabled,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            RefIcon(ref = ref, isLocal = isLocal, isTag = isTag)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ExpandedRefBadgeItem(
    ref: RefCommit,
    backgroundColor: Color,
    onRightClick: (Offset) -> Unit
) {
    val isBranch = ref is Branch
    val isLocal = isBranch && !ref.isRemote
    val isTag = ref is Tag
    val displayName = when {
        isBranch && !isLocal -> ref.name.substringAfter("/", ref.name)
        else -> ref.name
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isHovered) backgroundColor.copy(alpha = 0.6f) else backgroundColor)
            .hoverable(interactionSource)
            .onPointerEvent(PointerEventType.Press) { event ->
                if (event.buttons.isSecondaryPressed) {
                    event.changes.forEach { it.consume() }
                    val pos = event.changes.firstOrNull()?.position ?: Offset.Zero
                    onRightClick(pos)
                }
            }
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = displayName,
            maxLines = 1,
            style = JewelTheme.defaultTextStyle.copy(fontSize = 12.sp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        RefIcon(ref = ref, isLocal = isLocal, isTag = isTag)
    }
}

@Composable
private fun RefIcon(
    ref: RefCommit,
    isLocal: Boolean,
    isTag: Boolean
) {
    val isBranch = ref is Branch

    if (isTag) {
        Icon(
            key = AllIconsKeys.Nodes.Tag,
            contentDescription = "Tag",
            modifier = Modifier.size(14.dp)
        )
    } else if (isBranch) {
        val hasRemoteTracking = isLocal // Simplified - in real code check if remote exists

        Row(
            modifier = Modifier.width(if (isLocal && hasRemoteTracking) 30.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Icon(
                key = if (isLocal) AllIconsKeys.Vcs.Branch else AllIconsKeys.Javaee.WebService,
                contentDescription = if (isLocal) "Local branch" else "Remote branch",
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
