package cz.cernilovsky.kmp.rickandmorty.core.featureflags.di

import cz.cernilovsky.kmp.rickandmorty.core.annotation.InternalRickAndMortyApi
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.data.DefaultFeatureFlags
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.data.FeatureFlagsDataSourceKtorImpl
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
        // Built here rather than injected so a missing remoteConfigUrl never leaves an unused
        // HTTP data source in the graph.
        single<FeatureFlags> {
            DefaultFeatureFlags(
                installId = get<InstallIdStore>().installId(),
                overrides = config.overrides,
                dataSource =
                    config.remoteConfigUrl?.let { url ->
                        FeatureFlagsDataSourceKtorImpl(httpClient = get(), remoteConfigUrl = url)
                    },
            )
        }
    }
