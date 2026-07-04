package cz.cernilovsky.kmp.rickandmorty.core.di

import org.koin.core.module.Module

/** Platform-specific app/build info such as the debuggable flag (see `BuildConfig`). */
expect val commonPlatformModule: Module
