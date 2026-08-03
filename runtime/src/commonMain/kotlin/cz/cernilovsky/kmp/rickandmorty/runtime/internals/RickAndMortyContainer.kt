package cz.cernilovsky.kmp.rickandmorty.runtime.internals

import cz.cernilovsky.kmp.rickandmorty.characters.di.charactersModule
import cz.cernilovsky.kmp.rickandmorty.core.db.di.databaseModule
import cz.cernilovsky.kmp.rickandmorty.core.db.di.databasePlatformModule
import cz.cernilovsky.kmp.rickandmorty.core.di.DefaultDispatcher
import cz.cernilovsky.kmp.rickandmorty.core.di.commonPlatformModule
import cz.cernilovsky.kmp.rickandmorty.core.di.dispatchersModule
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.di.featureFlagsModule
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.di.featureFlagsPlatformModule
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.FeatureFlagsConfig
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.FeatureFlagsRepository
import cz.cernilovsky.kmp.rickandmorty.core.network.NetworkConfig
import cz.cernilovsky.kmp.rickandmorty.core.network.di.networkModule
import cz.cernilovsky.kmp.rickandmorty.core.network.di.networkPlatformModule
import cz.cernilovsky.kmp.rickandmorty.episode.di.episodeModule
import cz.cernilovsky.kmp.rickandmorty.location.di.locationModule
import cz.cernilovsky.kmp.rickandmorty.runtime.RickAndMortySdkConfig
import cz.cernilovsky.kmp.rickandmorty.runtime.SdkMode
import cz.cernilovsky.kmp.rickandmorty.runtime.SdkWidgetMarker
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import org.koin.dsl.module

internal class RickAndMortyContainer(
    config: RickAndMortySdkConfig,
    platformModule: Module,
    extraModules: List<Module>,
) {
    internal val koinApplication: KoinApplication =
        koinApplication {
            modules(
                listOf(
                    platformModule,
                    dispatchersModule,
                    module {
                        single { config }
                        // Application scope cancelled in [close]; FeatureFlagsRepository shares it.
                        single<CoroutineScope> {
                            CoroutineScope(SupervisorJob() + get<CoroutineDispatcher>(DefaultDispatcher))
                        }
                    },
                    commonPlatformModule,
                    networkModule(
                        NetworkConfig(
                            baseUrl = config.baseUrl,
                            loggingEnabled = config.loggingEnabled,
                        ),
                    ),
                    networkPlatformModule,
                    databaseModule,
                    databasePlatformModule,
                    featureFlagsModule(
                        FeatureFlagsConfig(
                            remoteConfigUrl = config.remoteConfigUrl,
                            overrides = config.featureFlagOverrides,
                        ),
                    ),
                    featureFlagsPlatformModule,
                    episodeModule,
                    locationModule,
                    charactersModule,
                ) + extraModules,
            )
        }

    internal val koin: Koin
        get() = koinApplication.koin

    private val applicationScope: CoroutineScope = koin.get()

    init {
        if (config.mode == SdkMode.Widget) {
            check(koin.getOrNull<SdkWidgetMarker>() != null) {
                "SdkMode.Widget requires the character UI graph. Call " +
                    "RickAndMortySdk.initializeWidget(...) from :feature:characters:ui " +
                    "(or pass charactersUiModule via extraModules)."
            }
        }
        // Construct FeatureFlagsRepository so Eagerly stateIn + init refresh run at SDK startup.
        koin.get<FeatureFlagsRepository>()
    }

    fun close() {
        applicationScope.cancel()
        koinApplication.close()
    }
}
