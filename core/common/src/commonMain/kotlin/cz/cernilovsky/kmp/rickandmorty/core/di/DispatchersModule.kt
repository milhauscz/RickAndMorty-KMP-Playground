package cz.cernilovsky.kmp.rickandmorty.core.di

import cz.cernilovsky.kmp.rickandmorty.core.annotation.InternalRickAndMortyApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.module.Module
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Qualifier for [Dispatchers.Default]. */
@InternalRickAndMortyApi
public val DefaultDispatcher: Qualifier = named("DefaultDispatcher")

/** Qualifier for [Dispatchers.IO]. */
@InternalRickAndMortyApi
public val IoDispatcher: Qualifier = named("IoDispatcher")

/** Qualifier for [Dispatchers.Main]. */
@InternalRickAndMortyApi
public val MainDispatcher: Qualifier = named("MainDispatcher")

/**
 * Qualifier for [Dispatchers.Main.immediate] — runs inline when already on the main thread.
 */
@InternalRickAndMortyApi
public val MainImmediateDispatcher: Qualifier = named("MainImmediateDispatcher")

/**
 * Binds the standard [CoroutineDispatcher] set used across the SDK.
 *
 * [Dispatchers.Unconfined] is intentionally omitted — it is rarely appropriate in production and
 * is better left as an explicit test-time choice.
 */
@InternalRickAndMortyApi
public val dispatchersModule: Module =
    module {
        single<CoroutineDispatcher>(DefaultDispatcher) { Dispatchers.Default }
        single<CoroutineDispatcher>(IoDispatcher) { Dispatchers.IO }
        single<CoroutineDispatcher>(MainDispatcher) { Dispatchers.Main }
        single<CoroutineDispatcher>(MainImmediateDispatcher) { Dispatchers.Main.immediate }
    }
