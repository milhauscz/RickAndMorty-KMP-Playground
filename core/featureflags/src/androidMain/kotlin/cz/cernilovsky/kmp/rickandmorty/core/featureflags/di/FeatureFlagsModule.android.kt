package cz.cernilovsky.kmp.rickandmorty.core.featureflags.di

import cz.cernilovsky.kmp.rickandmorty.core.annotation.InternalRickAndMortyApi
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.data.InstallIdStore
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.data.SharedPreferencesInstallIdStore
import org.koin.core.module.Module
import org.koin.dsl.module

@InternalRickAndMortyApi
public actual val featureFlagsPlatformModule: Module =
    module {
        single<InstallIdStore> { SharedPreferencesInstallIdStore(get()) }
    }
