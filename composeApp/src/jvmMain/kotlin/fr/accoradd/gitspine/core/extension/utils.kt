package fr.accoradd.gitspine.core.extension

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlinx.coroutines.delay
import java.security.MessageDigest

fun String.toMD5(): String {
    val bytes = MessageDigest.getInstance("MD5").digest(this.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}


@Composable
fun Modifier.showRecompositions(): Modifier {
    val triggered = remember { mutableStateOf(0) }

    SideEffect {
        triggered.value++
    }

    val alpha by animateFloatAsState(
        targetValue = if (triggered.value > 0) 0f else 0.5f,
        animationSpec = tween(durationMillis = 500)
    )

    LaunchedEffect(triggered.value) {
        if (triggered.value > 0) {
            delay(100)
        }
    }

    return this.background(Color.Red.copy(alpha = alpha))
}

enum class CornerPosition {
    BOTTOM_RIGHT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    TOP_LEFT
}

fun DrawScope.drawRoundedCornerPath(
    start: Offset,
    end: Offset,
    cornerRadius: Float,
    color: Color,
    strokeWidth: Float,
    cap: StrokeCap
) {

    val drawDirection = when {
        start.x > end.x && start.y > end.y -> CornerPosition.BOTTOM_LEFT
        start.x > end.x && start.y < end.y -> CornerPosition.BOTTOM_RIGHT
        start.x < end.x && start.y > end.y -> CornerPosition.TOP_LEFT
        start.x < end.x && start.y < end.y -> CornerPosition.TOP_RIGHT
        else -> CornerPosition.TOP_RIGHT
    }

    val path = Path().apply {
        moveTo(start.x, start.y)

        when (drawDirection) {
            CornerPosition.TOP_RIGHT -> {
                // Horizontal puis vertical
                lineTo(end.x - cornerRadius, start.y)
                quadraticTo(end.x, start.y, end.x, start.y + cornerRadius)
                lineTo(end.x, end.y)
            }

            CornerPosition.TOP_LEFT -> {
                // Vertical puis horizontal (vers la gauche)
                lineTo(start.x, end.y + cornerRadius)
                quadraticTo(start.x, end.y, start.x + cornerRadius, end.y)
                lineTo(end.x, end.y)
            }

            CornerPosition.BOTTOM_RIGHT -> {
                // Vertical puis horizontal
                lineTo(start.x, end.y - cornerRadius)
                quadraticTo(start.x, end.y, start.x - cornerRadius, end.y)
                lineTo(end.x, end.y)
            }

            CornerPosition.BOTTOM_LEFT -> {
                // Horizontal puis vertical (vers la gauche)
                lineTo(end.x + cornerRadius, start.y)
                quadraticTo(end.x, start.y, end.x, end.y + cornerRadius)
                lineTo(end.x, end.y)
            }
        }
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = cap
        )
    )
}
