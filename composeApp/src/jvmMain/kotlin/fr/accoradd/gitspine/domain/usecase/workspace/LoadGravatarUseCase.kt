package fr.accoradd.gitspine.domain.usecase.workspace

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import fr.accoradd.gitspine.core.extension.toMD5
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image
import java.net.URI

class LoadGravatarUseCase() {
    suspend operator fun invoke(email: String): ImageBitmap? = withContext(Dispatchers.IO) {
        try {
            val url = "https://www.gravatar.com/avatar/${email.toMD5()}"
            val connection = URI(url).toURL().openConnection()
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.getInputStream().use { inputStream ->
                val bytes = inputStream.readBytes()
                println("GET: $url")
                return@withContext Image.makeFromEncoded(bytes).toComposeImageBitmap()
            }
        } catch (e: Exception) {
            return@withContext null
        }
    }
}
