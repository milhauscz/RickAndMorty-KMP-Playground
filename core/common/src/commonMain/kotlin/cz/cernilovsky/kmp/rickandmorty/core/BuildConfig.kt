package cz.cernilovsky.kmp.rickandmorty.core

import cz.cernilovsky.kmp.rickandmorty.core.annotation.InternalRickAndMortyApi

/**
 * App-wide build information, provided per platform via [cz.cernilovsky.kmp.rickandmorty.core.di.commonPlatformModule].
 *
 * @property isDebug whether this is a debuggable build (Android: `FLAG_DEBUGGABLE`; iOS: debug binary).
 */
@InternalRickAndMortyApi
public data class BuildConfig(
    val isDebug: Boolean,
)
