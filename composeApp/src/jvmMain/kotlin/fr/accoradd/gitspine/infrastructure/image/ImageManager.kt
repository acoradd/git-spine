package fr.accoradd.gitspine.infrastructure.image

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.size.Size
import coil3.toBitmap
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class ImageManager {
    private val loadingJobs = mutableMapOf<String, Deferred<ImageBitmap?>>()

    suspend fun loadImage(
        pathOrUrl: String,
        imageLoader: ImageLoader,
        context: PlatformContext,
        size: Size = Size.ORIGINAL,
        cacheKey: String? = null,
        fallbackType: FallbackType? = null,
        fallbackKey: String? = null,
        fallbackCache: Boolean = fallbackType != null
    ): ImageBitmap? = coroutineScope {
        // Si une requête est déjà en cours pour cette URL, on attend son résultat
        loadingJobs.getOrPut(pathOrUrl) {
            async {
                try {
                    val request = ImageRequest.Builder(context)
                        .data(pathOrUrl)
                        .diskCacheKey(cacheKey)
                        .memoryCacheKey(cacheKey)
                        .size(size)
                        .apply {
                            if (fallbackType != null) {
                                extras[FallbackHeaders.FALLBACK_TYPE] = fallbackType.name
                                extras[FallbackHeaders.FALLBACK_CACHE] = fallbackCache
                            }
                            if (fallbackKey != null) {
                                extras[FallbackHeaders.FALLBACK_KEY] = fallbackKey
                            }
                        }
                        .build()

                    val result = imageLoader.execute(request)
                    if (result is SuccessResult) {
                        result.image.toBitmap().asComposeImageBitmap()
                    } else if (result is ErrorResult && result.image != null) {
                        result.image!!.toBitmap().asComposeImageBitmap()
                    } else {
                        null
                    }
                } finally {
                    loadingJobs.remove(pathOrUrl)
                }
            }
        }.await()
    }
}
