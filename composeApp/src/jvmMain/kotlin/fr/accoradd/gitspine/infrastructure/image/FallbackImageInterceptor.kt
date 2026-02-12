package fr.accoradd.gitspine.infrastructure.image

import coil3.Extras
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DataSource
import coil3.intercept.Interceptor
import coil3.memory.MemoryCache
import coil3.network.HttpException
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.ImageResult
import coil3.request.SuccessResult
import okio.FileSystem
import okio.buffer
import org.jetbrains.skia.*

object FallbackHeaders {
    val FALLBACK_TYPE = Extras.Key(default = "")
    val FALLBACK_CACHE = Extras.Key(default = false)
    val FALLBACK_KEY = Extras.Key(default = "")
}

// Types de fallback possibles
enum class FallbackType {
    GRAVATAR
}

class FallbackImageInterceptor(private val imageLoader: () -> ImageLoader) : Interceptor {
    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val result = chain.proceed()


        if (result is ErrorResult && isHttpError(result.throwable, 404)) {
            val fallbackType = chain.request.extras[FallbackHeaders.FALLBACK_TYPE]
            val fallbackCache = chain.request.extras[FallbackHeaders.FALLBACK_CACHE]
            if (fallbackType.isNullOrEmpty()) return result

            val fallbackKey = chain.request.extras[FallbackHeaders.FALLBACK_KEY]

            val fallbackImage = generateFallbackImage(fallbackType, fallbackKey) ?: return result
            val coilImage = fallbackImage.asImage(shareable = true)

            if (fallbackCache != null && fallbackCache) {
                val memoryCache = imageLoader().memoryCache
                val diskCache = imageLoader().diskCache
                val memoryCacheKey = generateMemoryCacheKey(chain.request)
                val diskCacheKey = generateDiskCacheKey(chain.request)

                memoryCache?.set(MemoryCache.Key(memoryCacheKey), MemoryCache.Value(coilImage))
                diskCache?.openEditor(diskCacheKey)?.let { editor ->
                    try {
                        // Encoder l'image en PNG
                        val encodedData = encodeBitmapToPNG(fallbackImage)

                        if (encodedData != null) {
                            FileSystem.SYSTEM.sink(editor.data).buffer().use { sink ->
                                sink.write(encodedData)
                            }
                            editor.commit()
                        } else {
                            editor.abort()
                        }
                    } catch (e: Exception) {
                        editor.abort()
                        throw e
                    }
                }
            }

            return SuccessResult(
                image = coilImage,
                request = chain.request,
                dataSource = DataSource.MEMORY
            )
        }

        return result
    }

    private fun encodeBitmapToPNG(bitmap: Bitmap): ByteArray? {
        val pixelData = bitmap.readPixels(
            dstInfo = bitmap.imageInfo,
            dstRowBytes = bitmap.width * 4,
            srcX = 0,
            srcY = 0
        ) ?: return null

        val image = org.jetbrains.skia.Image.makeRaster(
            imageInfo = ImageInfo(
                width = bitmap.width,
                height = bitmap.height,
                colorType = ColorType.BGRA_8888,
                alphaType = ColorAlphaType.PREMUL
            ),
            bytes = pixelData,
            rowBytes = bitmap.width * 4
        )

        // Encoder en PNG
        return image.encodeToData(EncodedImageFormat.PNG)?.bytes
    }

    private fun generateMemoryCacheKey(request: ImageRequest): String {
        return request.memoryCacheKey ?: request.data.toString()
    }

    private fun generateDiskCacheKey(request: ImageRequest): String {
        return request.diskCacheKey ?: request.data.toString()
    }

    private fun isHttpError(throwable: Throwable, code: Int): Boolean {
        return throwable is HttpException && throwable.response.code == code
    }

    private fun generateFallbackImage(fallbackType: String, fallbackKey: String?): Bitmap? {
        return when (fallbackType) {
            FallbackType.GRAVATAR.name -> {
                IdenticonGenerator().generateSkiaBitmap(fallbackKey ?: "")
            }

            else -> null
        }
    }
}
