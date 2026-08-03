package cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain

import cz.cernilovsky.kmp.rickandmorty.core.annotation.InternalRickAndMortyApi

/**
 * Feature-flag settings for the SDK graph.
 *
 * @property remoteConfigUrl URL of the remote flags document, or `null` to use defaults only.
 * @property overrides Absolute on/off overrides keyed by flag key; win over remote config.
 */
@InternalRickAndMortyApi
public data class FeatureFlagsConfig(
    val remoteConfigUrl: String? = null,
    val overrides: Map<String, Boolean> = emptyMap(),
)

// Overrides win over remote config, which wins over compile-time defaults. See docs/feature-flags.md.
