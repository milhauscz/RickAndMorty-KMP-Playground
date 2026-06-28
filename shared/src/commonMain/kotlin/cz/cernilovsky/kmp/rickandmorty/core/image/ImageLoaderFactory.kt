package cz.cernilovsky.kmp.rickandmorty.core.image

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.annotation.ExperimentalCoilApi
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import okio.Path

private const val DISK_CACHE_MAX_BYTES = 64L * 1024 * 1024 // 64 MB
private const val MEMORY_CACHE_PERCENT = 0.25

// Dedicated client for image loading. CIO (instead of the HttpURLConnection-based
// Android engine) avoids the ~5 connections-per-host limit that stalls many
// concurrent image requests during fast scrolling on real devices.
private val imageHttpClient: HttpClient =
    HttpClient(CIO) {
        engine {
            maxConnectionsCount = 1000
            endpoint.maxConnectionsPerRoute = 100
        }
    }

@OptIn(ExperimentalCoilApi::class)
fun createImageLoader(context: PlatformContext): ImageLoader =
    ImageLoader
        .Builder(context)
        .components {
            add(KtorNetworkFetcherFactory(httpClient = imageHttpClient))
        }.memoryCache {
            MemoryCache
                .Builder()
                .maxSizePercent(context, MEMORY_CACHE_PERCENT)
                .build()
        }.diskCache {
            DiskCache
                .Builder()
                .directory(imageCacheDirectory(context))
                .maxSizeBytes(DISK_CACHE_MAX_BYTES)
                .build()
        }.crossfade(true)
        .build()

internal expect fun imageCacheDirectory(context: PlatformContext): Path
