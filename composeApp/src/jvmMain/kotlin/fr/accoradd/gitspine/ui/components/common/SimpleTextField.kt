package fr.accoradd.gitspine.ui.components.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.theme.JewelTheme

@Composable
fun SimpleTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    placeholder: (@Composable () -> Unit)? = null
) {
    BasicTextField(
        value = state.text.toString(),
        onValueChange = { state.setTextAndPlaceCursorAtEnd(it) },
        textStyle = JewelTheme.defaultTextStyle.copy(
            color = JewelTheme.globalColors.text.normal
        ),
        cursorBrush = SolidColor(JewelTheme.globalColors.text.normal),
        modifier = modifier.padding(8.dp),
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                leadingContent?.let {
                    it()
                    Spacer(Modifier.width(8.dp))
                }

                Box(Modifier.weight(1f)) {
                    if (state.text.isEmpty() && placeholder != null) {
                        placeholder()
                    }
                    innerTextField()
                }

                trailingContent?.invoke()
            }
        })
}
