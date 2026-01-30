package fr.accoradd.gitspine.core.extension

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
