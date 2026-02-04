package fr.accoradd.gitspine.infrastructure.image

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import fr.accoradd.gitspine.infrastructure.persistence.AppDataPath
import okio.Path.Companion.toOkioPath

object ImageLoaderFactory {
    fun create(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(AppDataPath.getDataDir().resolve("cache/image").toOkioPath())
                    .maxSizeBytes(512L * 1024 * 1024)
                    .build()
            }
            .eventListener(LoggingEventListener())
            .build()
    }
}
