package cz.cernilovsky.kmp.rickandmorty.core.featureflags.di

import cz.cernilovsky.kmp.rickandmorty.core.annotation.InternalRickAndMortyApi
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.data.FeatureFlagsDataSourceKtorImpl
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.data.FeatureFlagsRepository
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.data.FeatureFlagsRoomDataSource
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.data.InstallIdStore
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.FeatureFlags
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.FeatureFlagsConfig
import org.koin.core.module.Module
import org.koin.dsl.module

/** Platform-specific [InstallIdStore] binding. */
@InternalRickAndMortyApi
public expect val featureFlagsPlatformModule: Module

@InternalRickAndMortyApi
public fun featureFlagsModule(config: FeatureFlagsConfig = FeatureFlagsConfig()): Module =
    module {
        single { config }
        // Remote source is optional: no remoteConfigUrl means defaults + Room cache only.
        single<FeatureFlags> {
            FeatureFlagsRepository(
                localDataSource = get<FeatureFlagsRoomDataSource>(),
                remoteDataSource =
                    config.remoteConfigUrl?.let { url ->
                        FeatureFlagsDataSourceKtorImpl(httpClient = get(), remoteConfigUrl = url)
                    },
                installId = get<InstallIdStore>().installId(),
                overrides = config.overrides,
                scope = get(),
            )
        }
    }
