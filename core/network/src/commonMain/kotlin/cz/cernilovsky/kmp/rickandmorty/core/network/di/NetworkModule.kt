package cz.cernilovsky.kmp.rickandmorty.core.network.di

import cz.cernilovsky.kmp.rickandmorty.core.BuildConfig
import cz.cernilovsky.kmp.rickandmorty.core.annotation.InternalRickAndMortyApi
import cz.cernilovsky.kmp.rickandmorty.core.network.ClearableCacheStorage
import cz.cernilovsky.kmp.rickandmorty.core.network.HttpClientFactory
import cz.cernilovsky.kmp.rickandmorty.core.network.NetworkConfig
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.dsl.module

/** Platform-specific HTTP client engine binding (Android / Darwin). */
expect val networkPlatformModule: Module

/**
 * Takes its configuration as a parameter rather than defining a default binding that callers
 * override: Koin's last-module-wins override would make the effective base URL depend on module
 * ordering, which is exactly the kind of thing that goes unnoticed until a partner's traffic lands
 * on the wrong host.
 */
@OptIn(InternalRickAndMortyApi::class)
fun networkModule(config: NetworkConfig = NetworkConfig()): Module =
    module {
        single { config }
        single { ClearableCacheStorage() }
        single<HttpClient> {
            HttpClientFactory.create(
                engine = get(),
                isDebug = config.loggingEnabled ?: get<BuildConfig>().isDebug,
                cacheStorage = get(),
            )
        }
    }
