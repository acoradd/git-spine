package fr.accoradd.gitspine.ui.components.common

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField

@OptIn(FlowPreview::class)
@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onDebouncedValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Rechercher..."
) {
    // State flow for debouncing
    val textState = remember { MutableStateFlow(value) }
    
    LaunchedEffect(value) {
        textState.value = value
    }

    LaunchedEffect(Unit) {
        textState
            .debounce(300) // 300ms debounce
            .onEach { onDebouncedValueChange(it) }
            .launchIn(this)
    }

    TextField(
        value = value,
        onValueChange = { 
            onValueChange(it)
            textState.value = it
        },
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = {
            // TODO: Use Jewel search icon resource
            Text("🔍") 
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = {
                    onValueChange("")
                    textState.value = ""
                }) {
                    // TODO: Use Jewel close icon resource
                    Text("✕")
                }
            }
        }
    )
}
