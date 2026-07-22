package cz.cernilovsky.kmp.rickandmorty.core.network

import io.ktor.client.plugins.cache.storage.CacheStorage
import io.ktor.client.plugins.cache.storage.CachedResponseData
import io.ktor.http.Url

/**
 * Wraps a [CacheStorage] so the whole cache can be thrown away on demand - e.g. a user-initiated
 * refresh, where anything served from the HTTP-level cache (including pages fetched afterwards by
 * pagination) would defeat the point of asking for fresh data. [clear] swaps in a brand new,
 * empty storage from [delegateFactory] rather than tracking and removing every URL individually.
 */
class ClearableCacheStorage(
    private var delegateFactory: () -> CacheStorage = CacheStorage.Unlimited,
) : CacheStorage {
    private var delegate: CacheStorage = delegateFactory()

    fun clear() {
        delegate = delegateFactory()
    }

    override suspend fun store(
        url: Url,
        data: CachedResponseData,
    ) {
        delegate.store(url, data)
    }

    override suspend fun find(
        url: Url,
        varyKeys: Map<String, String>,
    ): CachedResponseData? = delegate.find(url, varyKeys)

    override suspend fun findAll(url: Url): Set<CachedResponseData> = delegate.findAll(url)

    override suspend fun remove(
        url: Url,
        varyKeys: Map<String, String>,
    ) {
        delegate.remove(url, varyKeys)
    }

    override suspend fun removeAll(url: Url) {
        delegate.removeAll(url)
    }
}
