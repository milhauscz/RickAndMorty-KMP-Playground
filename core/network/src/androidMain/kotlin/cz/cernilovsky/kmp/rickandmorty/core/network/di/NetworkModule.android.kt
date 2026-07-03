package cz.cernilovsky.kmp.rickandmorty.core.network.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
import org.koin.core.module.Module
import org.koin.dsl.module

actual val networkPlatformModule: Module =
    module {
        single<HttpClientEngine> {
            Android.create()
        }
    }
