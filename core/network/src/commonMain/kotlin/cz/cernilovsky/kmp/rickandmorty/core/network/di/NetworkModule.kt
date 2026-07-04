package cz.cernilovsky.kmp.rickandmorty.core.network.di

import cz.cernilovsky.kmp.rickandmorty.core.BuildConfig
import cz.cernilovsky.kmp.rickandmorty.core.network.HttpClientFactory
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.dsl.module

/** Platform-specific HTTP client engine binding (Android / Darwin). */
expect val networkPlatformModule: Module

val networkModule =
    module {
        single<HttpClient> {
            HttpClientFactory.create(engine = get(), isDebug = get<BuildConfig>().isDebug)
        }
    }
