package cz.cernilovsky.kmp.rickandmorty.runtime

import android.content.Context
import org.koin.core.module.Module
import org.koin.dsl.module

/** Initializes the SDK on Android. */
public fun RickAndMortySdk.initialize(
    context: Context,
    config: RickAndMortySdkConfig = RickAndMortySdkConfig.default(),
    extraModules: List<Module> = emptyList(),
) {
    val applicationContext = context.applicationContext
    start(
        config = config,
        platformModule = module { single<Context> { applicationContext } },
        extraModules = extraModules,
    )
}
