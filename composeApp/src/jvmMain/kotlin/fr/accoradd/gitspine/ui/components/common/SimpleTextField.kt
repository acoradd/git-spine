package fr.accoradd.gitspine.ui.components.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Wrapper simple pour TextField compatible avec l'ancienne API
 * Utilise Material3 OutlinedTextField au lieu de Jewel TextField
 * car l'API de Jewel 0.33 est trop complexe pour nos besoins
 */
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
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = if (placeholder.isNotEmpty()) {
            { Text(placeholder) }
        } else null,
        enabled = enabled,
        readOnly = readOnly,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        singleLine = true
    )
}
