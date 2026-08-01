package cz.cernilovsky.kmp.rickandmorty.core.di

import cz.cernilovsky.kmp.rickandmorty.core.BuildConfig
import cz.cernilovsky.kmp.rickandmorty.core.annotation.InternalRickAndMortyApi
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

@OptIn(ExperimentalNativeApi::class)
@InternalRickAndMortyApi
public actual val commonPlatformModule: Module =
    module {
        single {
            BuildConfig(isDebug = Platform.isDebugBinary)
        }
    }
