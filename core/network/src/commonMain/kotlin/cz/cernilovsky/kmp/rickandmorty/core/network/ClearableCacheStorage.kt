package cz.cernilovsky.kmp.rickandmorty.core.network

import io.ktor.client.plugins.cache.storage.CacheStorage
import io.ktor.client.plugins.cache.storage.CachedResponseData
import io.ktor.http.Url
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Wraps a [CacheStorage], tracking every URL a response has been stored for, so the whole cache
 * can be cleared on demand - e.g. a user-initiated refresh, where anything served from the
 * HTTP-level cache (including pages fetched afterwards by pagination) would defeat the point of
 * asking for fresh data. [CacheStorage] itself only supports removing entries per URL.
 */
class ClearableCacheStorage(
    private val delegate: CacheStorage = CacheStorage.Unlimited(),
) : CacheStorage by delegate {
    private val mutex = Mutex()
    private val trackedUrls = mutableSetOf<Url>()

    override suspend fun store(
        url: Url,
        data: CachedResponseData,
    ) {
        mutex.withLock { trackedUrls.add(url) }
        delegate.store(url, data)
    }

    suspend fun clear() {
        val urls = mutex.withLock { trackedUrls.toList().also { trackedUrls.clear() } }
        urls.forEach { delegate.removeAll(it) }
    }
}
