package fr.accoradd.gitspine.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import java.awt.Cursor

@Composable
fun ThreeColumnResizablePanes(
    leftContent: @Composable () -> Unit,
    centerContent: @Composable () -> Unit,
    rightContent: @Composable () -> Unit,
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

    BoxWithConstraints(modifier = modifier) {
        val totalWidth = constraints.maxWidth.toFloat()
        val dividerWidthDp = 4.dp
        val dividerWidthPx = with(density) { dividerWidthDp.toPx() }
        // Calculate available width for panes (excluding dividers)
        val availableWidth = totalWidth - (dividerWidthPx * 2)

        Layout(
            content = {
                // Left pane
                Box(modifier = Modifier) {
                    leftContent()
                }

                // Left divider
                VerticalDivider(
                    onDragStart = { leftWidthAtStartOfDrag = leftWidth },
                    onPositionChange = { deltaX ->
                        val ratio = leftWidthAtStartOfDrag + (deltaX / availableWidth)
                        val newLeftWidth = ratio.coerceIn(
                            minPaneWidth,
                            1f - rightWidth - minPaneWidth
                        )
                        leftWidth = newLeftWidth
                        println("Left width: $newLeftWidth, $deltaX, $ratio")
                    }
                )

                // Center pane
                Box(modifier = Modifier) {
                    centerContent()
                }

                // Right divider
                VerticalDivider(
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
                Box(modifier = Modifier) {
                    rightContent()
                }
            }
        ) { measurables, constraints ->
        val width = constraints.maxWidth
        val height = constraints.maxHeight

        val dividerWidth = 4.dp.roundToPx()

        val leftPaneWidth = (width * leftWidth).toInt()
        val rightPaneWidth = (width * rightWidth).toInt()
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
private fun VerticalDivider(
    onDragStart: () -> Unit,
    onPositionChange: (absoluteX: Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(4.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
            .pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    onDragStart()
                    var totalAccumulatedDelta = 0f
                    drag(down.id) { change ->
                        val dragAmount = change.position.x - change.previousPosition.x
                        totalAccumulatedDelta += dragAmount
                        onPositionChange(totalAccumulatedDelta)
                        change.consume()
                    }
                }
            }
    )
}
