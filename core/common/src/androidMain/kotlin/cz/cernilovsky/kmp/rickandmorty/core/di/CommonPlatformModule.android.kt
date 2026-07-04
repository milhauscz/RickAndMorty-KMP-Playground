package cz.cernilovsky.kmp.rickandmorty.core.di

import android.content.Context
import android.content.pm.ApplicationInfo
import cz.cernilovsky.kmp.rickandmorty.core.BuildConfig
import org.koin.core.module.Module
import org.koin.dsl.module

actual val commonPlatformModule: Module =
    module {
        single {
            val debuggable = (get<Context>().applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
            BuildConfig(isDebug = debuggable)
        }
    }
