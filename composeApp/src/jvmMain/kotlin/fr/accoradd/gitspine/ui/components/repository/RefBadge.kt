package fr.accoradd.gitspine.ui.components.repository

import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    refContextMenuState: RefContextMenuState?,
    modifier: Modifier = Modifier
) {
    if (refs.isEmpty()) return

    // Sort refs to put HEAD first
    val sortedRefs = refs.sortedByDescending { ref ->
        when {
            ref is Branch && ref.isHead -> 3  // HEAD first
            ref is Branch && !ref.isRemote -> 2  // Then local branches
            ref is Branch -> 1  // Then remote branches
            else -> 0  // Then tags
        }
    }

    val density = LocalDensity.current

    var layoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var popupMinWidth by remember { mutableStateOf<Dp?>(null) }
    var popupMinHeight by remember { mutableStateOf<Dp?>(null) }
    var popupOffsetY by remember { mutableStateOf<Int?>(null) }
    var isMenuVisible by remember { mutableStateOf(false) }
    val interactionBadgeSource = remember { MutableInteractionSource() }
    val interactionPopupSource = remember { MutableInteractionSource() }
    val isBadgeHovered by interactionBadgeSource.collectIsHoveredAsState()
    val isPopupHovered by interactionPopupSource.collectIsHoveredAsState()
    val popupOffsetX = with(density) { 12.dp.toPx().toInt() }
    val branchToGroup = mutableMapOf<String, String>()
    val isHead = sortedRefs.first() is Branch && (sortedRefs.first() as Branch).isHead
    for (commit in sortedRefs) {
        branchToGroup[commit.name] = when (commit) {
            is Branch if commit.isRemote -> commit.name.substringAfter("/")
            else -> commit.name
        }
    }
    val refsGroupedByBranch = sortedRefs.groupBy { branchToGroup[it.name]!! }

    val othersRefs = refsGroupedByBranch.filter { it.key != branchToGroup[sortedRefs.first().name]!! }

    LaunchedEffect(refContextMenuState) {
        isMenuVisible = refContextMenuState?.let { menu ->
            menu.isVisible && menu.ref != null && refs.any { it.name == menu.ref.name }
        } ?: false
    }

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
                ref = sortedRefs.first(),
                othersBranchs = refsGroupedByBranch[sortedRefs.first().name],
                backgroundColor = backgroundColor,
                onRightClick = { offset ->
                    val rootOffset = layoutCoordinates?.localToRoot(offset) ?: offset
                    onRefRightClick(sortedRefs.first(), rootOffset)
                },
                modifier = Modifier.width(IntrinsicSize.Max).onGloballyPositioned {
                    popupMinWidth = with(density) { it.size.width.toDp() }
                    popupMinHeight = with(density) { it.size.height.toDp() }
                    popupOffsetY = (layoutCoordinates?.localPositionOf(it, Offset.Zero)?.y?.toInt())
                }
            )

            if (othersRefs.isNotEmpty() && !isBadgeHovered && !isPopupHovered && !isMenuVisible) {

                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(if (isHead) 2.dp else 1.dp)
                        .background(if (isHead) backgroundColor.copy(alpha = 0.6f) else backgroundColor)
                )
                Row(
                    modifier = Modifier
                        .width(IntrinsicSize.Min)
                        .background(backgroundColor)
                        .clip(RoundedCornerShape(2.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "+${othersRefs.size}",
                        style = JewelTheme.defaultTextStyle.copy(fontSize = 12.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isHead) 2.dp else 1.dp)
                    .background(if (isHead) backgroundColor.copy(alpha = 0.6f) else backgroundColor)
            )
        }

        if (isBadgeHovered || isPopupHovered || isMenuVisible) {
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

                    ExpandedRefBadgeItem(
                        ref = sortedRefs.first(),
                        othersRefs = refsGroupedByBranch[sortedRefs.first().name],
                        forceHover = !isPopupHovered,
                        backgroundColor = backgroundColor,
                        onRightClick = { offset ->
                            onRefRightClick(sortedRefs.first(), offset)
                        }
                    )

                    othersRefs.forEach { (_, refs) ->
                        ExpandedRefBadgeItem(
                            ref = refs.first(),
                            othersRefs = refsGroupedByBranch[refs.first().name],
                            forceHover = false,
                            backgroundColor = backgroundColor,
                            onRightClick = { offset ->
                                onRefRightClick(refs.first(), offset)
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
    onRightClick: (Offset) -> Unit,
    modifier: Modifier = Modifier,
    othersBranchs: List<RefCommit>?
) {
    val isBranch = ref is Branch
    val isLocal = isBranch && !ref.isRemote
    val isHead = isBranch && ref.isHead
    val isTag = ref is Tag
    val displayName = when {
        isBranch && !isLocal -> ref.name.substringAfter("/", ref.name)
        else -> ref.name
    }

    // HEAD gets a more vivid color (full alpha)
    val badgeColor = if (isHead) backgroundColor.copy(alpha = 1f) else backgroundColor

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .clip(RoundedCornerShape(2.dp))
                .background(if (isHovered || isHead) backgroundColor.copy(alpha = 0.6f) else backgroundColor)
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
                style = JewelTheme.defaultTextStyle.copy(fontSize = 12.sp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            if (isLocal && othersBranchs?.any { it is Branch && it.isRemote } ?: false) {
                RefIcon(ref = ref, isLocal = false, isTag = false)
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
    onRightClick: (Offset) -> Unit,
    forceHover: Boolean,
    othersRefs: List<RefCommit>?
) {
    val isBranch = ref is Branch
    val isLocal = isBranch && !ref.isRemote
    val isHead = isBranch && ref.isHead
    val isTag = ref is Tag
    val displayName = when {
        isBranch && !isLocal -> ref.name.substringAfter("/", ref.name)
        else -> ref.name
    }

    val interactionSource = remember { MutableInteractionSource() }
    var layoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isHovered || forceHover || isHead) backgroundColor.copy(alpha = 0.6f) else backgroundColor)
            .hoverable(interactionSource)
            .onGloballyPositioned { layoutCoordinates = it }
            .onPointerEvent(PointerEventType.Press) { event ->
                if (event.buttons.isSecondaryPressed) {
                    event.changes.forEach { it.consume() }
                    val pos = event.changes.firstOrNull()?.position ?: Offset.Zero
                    onRightClick(layoutCoordinates?.localToRoot(pos) ?: pos)
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

        if (isLocal && othersRefs?.any { it is Branch && it.isRemote } ?: false) {
            RefIcon(ref = ref, isLocal = false, isTag = false)
        }

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
        Icon(
            key = if (isLocal) AllIconsKeys.Vcs.Branch else AllIconsKeys.Javaee.WebService,
            contentDescription = if (isLocal) "Local branch" else "Remote branch",
            modifier = Modifier.size(14.dp)
        )
    }
}
