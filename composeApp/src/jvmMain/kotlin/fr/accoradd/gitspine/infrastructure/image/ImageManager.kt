package fr.accoradd.gitspine.infrastructure.image

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class ImageManager {
    private val loadingJobs = mutableMapOf<String, Deferred<ImageBitmap?>>()

    suspend fun loadImage(
        url: String,
        imageLoader: ImageLoader,
        context: PlatformContext
    ): ImageBitmap? = coroutineScope {
        // Si une requête est déjà en cours pour cette URL, on attend son résultat
        loadingJobs.getOrPut(url) {
            async {
                try {
                    val request = ImageRequest.Builder(context)
                        .data(url)
                        .build()

                    val result = imageLoader.execute(request)
                    if (result is SuccessResult) {
                        result.image.toBitmap().asComposeImageBitmap()
                    } else null
                } finally {
                    loadingJobs.remove(url)
                }
            }
        }.await()
    }
}
