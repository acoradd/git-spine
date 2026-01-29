package fr.accoradd.gitspine.ui.viewmodel

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.accoradd.gitspine.core.extension.toMD5
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image
import java.net.URI

class GravatarViewModel(): ViewModel() {


    private val _state = MutableStateFlow(mapOf<String, ImageBitmap?>())
    val state: StateFlow<Map<String, ImageBitmap?>> = _state.asStateFlow()

    fun loadGravatarOfAuthor(email: String) {
        viewModelScope.launch {
            if (!_state.value.containsKey(email)) {
                _state.update { it + (email to null) }
                loadGravatarOfAuthorInternal(email)
            }
        }
    }

    private suspend fun loadGravatarOfAuthorInternal(email: String) = withContext(Dispatchers.IO) {
        try {
            val connection = URI("https://www.gravatar.com/avatar/${email.toMD5()}").toURL().openConnection()
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.getInputStream().use { inputStream ->
                val bytes = inputStream.readBytes()
                Image.makeFromEncoded(bytes).toComposeImageBitmap().let { image ->
                    _state.update { it + (email to image) }
                }
            }
        } catch (e: Exception) {}
    }
}
