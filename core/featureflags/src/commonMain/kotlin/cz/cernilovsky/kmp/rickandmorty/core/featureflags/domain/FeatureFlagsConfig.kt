package cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain

/** Configuration for remote feature flags and host overrides. */
public data class FeatureFlagsConfig(
    val remoteConfigUrl: String? = null,
    val overrides: Map<String, Boolean> = emptyMap(),
)

// Overrides win over remote config, which wins over compile-time defaults. See docs/feature-flags.md.
