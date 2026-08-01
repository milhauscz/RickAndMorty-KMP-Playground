package cz.cernilovsky.kmp.rickandmorty.core.di

import cz.cernilovsky.kmp.rickandmorty.core.annotation.InternalRickAndMortyApi
import org.koin.core.module.Module

/** Platform-specific app/build info such as the debuggable flag (see `BuildConfig`). */
@InternalRickAndMortyApi
public expect val commonPlatformModule: Module
