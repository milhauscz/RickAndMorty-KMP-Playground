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

/** Binds [CoroutineDispatcher] instances used across the SDK. */
@InternalRickAndMortyApi
public val dispatchersModule: Module =
    module {
        single<CoroutineDispatcher>(DefaultDispatcher) { Dispatchers.Default }
        single<CoroutineDispatcher>(IoDispatcher) { Dispatchers.IO }
    }
