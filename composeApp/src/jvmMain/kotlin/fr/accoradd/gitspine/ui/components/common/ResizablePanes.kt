package fr.accoradd.gitspine.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import java.awt.Cursor

@Composable
fun ThreeColumnResizablePanes(
    leftContent: @Composable () -> Unit,
    centerContent: @Composable () -> Unit,
    rightContent: @Composable () -> Unit,
    hideRightContent: Boolean = false,
    modifier: Modifier = Modifier,
    initialLeftWidth: Float = 0.2f,
    initialRightWidth: Float = 0.25f,
    minPaneWidth: Float = 0.15f
) {
    var leftWidth by remember { mutableStateOf(initialLeftWidth) }
    var rightWidth by remember { mutableStateOf(initialRightWidth) }
    var leftWidthAtStartOfDrag by remember { mutableStateOf(initialLeftWidth) }
    var rightWidthAtStartOfDrag by remember { mutableStateOf(initialRightWidth) }

    val density = LocalDensity.current
    val cornerRadius = 8.dp
    val spacerWidth = 4.dp

    BoxWithConstraints(modifier = modifier) {
        val totalWidth = constraints.maxWidth.toFloat()
        val dividerWidthPx = with(density) { spacerWidth.toPx() }
        val availableWidth = totalWidth - (dividerWidthPx * 2)

        Layout(
            content = {
                // Left pane
                Pane(cornerRadius = cornerRadius) { leftContent() }

                // Left divider
                HorizontalSpacer(
                    width = spacerWidth,
                    onDragStart = { leftWidthAtStartOfDrag = leftWidth },
                    onPositionChange = { deltaX ->
                        val ratio = leftWidthAtStartOfDrag + (deltaX / availableWidth)
                        val newLeftWidth = ratio.coerceIn(
                            minPaneWidth,
                            1f - rightWidth - minPaneWidth
                        )
                        leftWidth = newLeftWidth
                    }
                )

                // Center pane
                Pane(cornerRadius = cornerRadius) { centerContent() }

                // Right divider
                HorizontalSpacer(
                    width = spacerWidth,
                    enableDrag = !hideRightContent,
                    onDragStart = { rightWidthAtStartOfDrag = rightWidth },
                    onPositionChange = { deltaX ->
                        val ratio = rightWidthAtStartOfDrag - (deltaX / availableWidth)
                        val newRightWidth = ratio.coerceIn(
                            minPaneWidth,
                            1f - leftWidth - minPaneWidth
                        )
                        rightWidth = newRightWidth
                    }
                )

                // Right pane
                Pane(cornerRadius = cornerRadius) { rightContent() }
            }
        ) { measurables, constraints ->
            val width = constraints.maxWidth
            val height = constraints.maxHeight

            val dividerWidth = spacerWidth.roundToPx()

            val leftPaneWidth = (availableWidth * leftWidth).toInt()
            val rightPaneWidth = if (hideRightContent) 0 else (availableWidth * rightWidth).toInt()
            val centerPaneWidth = width - leftPaneWidth - rightPaneWidth - (dividerWidth * 2)

            // Measure children
            val leftPlaceable = measurables[0].measure(
                Constraints.fixed(leftPaneWidth, height)
            )
            val leftDividerPlaceable = measurables[1].measure(
                Constraints.fixed(dividerWidth, height)
            )
            val centerPlaceable = measurables[2].measure(
                Constraints.fixed(centerPaneWidth, height)
            )
            val rightDividerPlaceable = measurables[3].measure(
                Constraints.fixed(dividerWidth, height)
            )
            val rightPlaceable = measurables[4].measure(
                Constraints.fixed(rightPaneWidth, height)
            )

            layout(width, height) {
                var x = 0

                leftPlaceable.placeRelative(x, 0)
                x += leftPaneWidth

                leftDividerPlaceable.placeRelative(x, 0)
                x += dividerWidth

                centerPlaceable.placeRelative(x, 0)
                x += centerPaneWidth

                rightDividerPlaceable.placeRelative(x, 0)
                x += dividerWidth

                rightPlaceable.placeRelative(x, 0)
            }
        }
    }
}

@Composable
private fun Pane(
    cornerRadius: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit
) {
    // Remplacement de Surface par Box avec background Jewel
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(cornerRadius))
            .background(JewelTheme.globalColors.panelBackground), // Couleur de surface légère
        content = { content() }
    )
}

@Composable
private fun HorizontalSpacer(
    width: androidx.compose.ui.unit.Dp,
    enableDrag: Boolean = true,
    onDragStart: () -> Unit,
    onPositionChange: (absoluteX: Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(width)
            .pointerHoverIcon(if (enableDrag) PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)) else PointerIcon.Default)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    onDragStart()
                    var totalAccumulatedDelta = 0f
                    drag(down.id) { change ->
                        if (enableDrag) {
                            val dragAmount = change.position.x - change.previousPosition.x
                            totalAccumulatedDelta += dragAmount
                            onPositionChange(totalAccumulatedDelta)
                            change.consume()
                        }
                    }
                }
            }
    )
}
