package cz.cernilovsky.kmp.rickandmorty.runtime

import org.koin.core.module.Module
import org.koin.dsl.module

/** Initializes the SDK on iOS. */
public fun RickAndMortySdk.initialize(
    config: RickAndMortySdkConfig = RickAndMortySdkConfig.default(),
    extraModules: List<Module> = emptyList(),
) {
    start(config, module { }, extraModules)
}
