package cz.cernilovsky.kmp.rickandmorty.core.network

import io.ktor.client.plugins.cache.storage.CacheStorage
import io.ktor.client.plugins.cache.storage.CachedResponseData
import io.ktor.http.Url

/**
 * HTTP response cache that can be wiped in one call via [clear].
 */
// Used for user-initiated refresh: anything served from the HTTP-level cache (including pages
// fetched afterwards by pagination) would defeat asking for fresh data. clear() swaps in a new
// empty storage from delegateFactory rather than tracking and removing every URL individually.
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
