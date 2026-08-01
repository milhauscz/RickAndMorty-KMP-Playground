package cz.cernilovsky.kmp.rickandmorty.runtime

import cz.cernilovsky.kmp.rickandmorty.core.annotation.InternalRickAndMortyApi
import cz.cernilovsky.kmp.rickandmorty.runtime.internals.RickAndMortyContainer
import org.koin.core.KoinApplication
import org.koin.core.module.Module

/**
 * Entry point to the Rick and Morty SDK.
 *
 * Call [initialize] once during application startup. Use [RickAndMortySdkScope] or the character
 * screens from `:feature:characters:ui` to show the widget.
 */
@OptIn(InternalRickAndMortyApi::class, InternalRickAndMortyRuntimeApi::class)
public object RickAndMortySdk {
    private var container: RickAndMortyContainer? = null

    public val isInitialized: Boolean
        get() = container != null

    /** Isolated Koin container used by the character UI screens. */
    @InternalRickAndMortyRuntimeApi
    public val internalContainer: KoinApplication
        get() = requireContainer().koinApplication

    /** Releases SDK resources. Safe when not initialized. */
    public fun shutdown() {
        container?.close()
        container = null
    }

    internal val containerOrNull: RickAndMortyContainer?
        get() = container

    @OptIn(InternalRickAndMortyApi::class)
    internal fun start(
        config: RickAndMortySdkConfig,
        platformModule: Module,
        extraModules: List<Module> = emptyList(),
    ) {
        check(container == null) {
            "RickAndMortySdk is already initialized. Call shutdown() before initializing again."
        }
        container = RickAndMortyContainer(config, platformModule, extraModules)
    }

    private fun requireContainer(): RickAndMortyContainer =
        container ?: error("RickAndMortySdk is not initialized. Call RickAndMortySdk.initialize(...) first.")
}
