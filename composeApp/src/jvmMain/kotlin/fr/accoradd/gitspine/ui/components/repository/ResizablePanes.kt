package fr.accoradd.gitspine.ui.components.repository

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import jdk.internal.org.jline.utils.Colors.h
import org.jetbrains.jewel.foundation.theme.JewelTheme
import java.awt.Cursor

@Composable
fun RepositoryResizablePanes(
    leftContent: @Composable () -> Unit,
    centerContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    initialLeftWidth: Float = 0.2f,
    minPaneWidth: Float = 0.15f
) {
    var leftWidth by remember { mutableStateOf(initialLeftWidth) }
    var leftWidthAtStartOfDrag by remember { mutableStateOf(initialLeftWidth) }

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
                            1f - minPaneWidth
                        )
                        leftWidth = newLeftWidth
                    }
                )

                // Center pane
                Pane(cornerRadius = cornerRadius) { centerContent() }
            }
        ) { measurables, constraints ->
            val width = constraints.maxWidth
            val height = constraints.maxHeight

            val dividerWidth = spacerWidth.roundToPx()

            val leftPaneWidth = (availableWidth * leftWidth).toInt()
            val centerPaneWidth = width - leftPaneWidth - dividerWidth

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

            layout(width, height) {
                var x = 0

                leftPlaceable.placeRelative(x, 0)
                x += leftPaneWidth

                leftDividerPlaceable.placeRelative(x, 0)
                x += dividerWidth

                centerPlaceable.placeRelative(x, 0)
                x += centerPaneWidth
            }
        }
    }
}

@Composable
private fun Pane(
    cornerRadius: Dp,
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
fun HorizontalSpacer(
    width: Dp,
    enableDrag: Boolean = true,
    modifier: Modifier = Modifier,
    onDragStart: () -> Unit,
    onPositionChange: (absoluteX: Float) -> Unit
) {
    Box(
        modifier = modifier
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
