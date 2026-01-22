package fr.accoradd.gitspine.ui.components.common

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.jewel.ui.component.TextField

@Composable
fun SimpleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val state = rememberTextFieldState(value)

    TextField(
        enabled = enabled,
        state = state,
        modifier = modifier,
        readOnly = readOnly,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon
    )
}
