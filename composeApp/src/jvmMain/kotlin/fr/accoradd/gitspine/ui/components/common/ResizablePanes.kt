package fr.accoradd.gitspine.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
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

    BoxWithConstraints(modifier = modifier) {
        val totalWidth = constraints.maxWidth.toFloat()

        Layout(
            content = {
                // Left pane
                Box(modifier = Modifier) {
                    leftContent()
                }

                // Left divider
                VerticalDivider(
                    onDrag = { delta ->
                        val newLeftWidth = (leftWidth + delta / totalWidth).coerceIn(
                            minPaneWidth,
                            1f - rightWidth - minPaneWidth
                        )
                        leftWidth = newLeftWidth
                    }
                )

                // Center pane
                Box(modifier = Modifier) {
                    centerContent()
                }

                // Right divider
                VerticalDivider(
                    onDrag = { delta ->
                        val newRightWidth = (rightWidth - delta / totalWidth).coerceIn(
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
    onDrag: (delta: Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(4.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
            .pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x)
                }
            }
    )
}
