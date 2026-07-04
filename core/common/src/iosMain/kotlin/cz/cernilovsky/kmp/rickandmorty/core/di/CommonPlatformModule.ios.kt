package cz.cernilovsky.kmp.rickandmorty.core.di

import cz.cernilovsky.kmp.rickandmorty.core.BuildConfig
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

@OptIn(ExperimentalNativeApi::class)
actual val commonPlatformModule: Module =
    module {
        single {
            BuildConfig(isDebug = Platform.isDebugBinary)
        }
    }
