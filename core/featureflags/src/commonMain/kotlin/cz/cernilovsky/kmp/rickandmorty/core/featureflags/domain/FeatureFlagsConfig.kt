package cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain

/**
 * Feature-flag settings for the SDK graph.
 *
 * @property remoteConfigUrl URL of the remote flags document, or `null` to use defaults only.
 * @property overrides Absolute on/off overrides keyed by [FeatureFlag.key]; win over remote config.
 */
public data class FeatureFlagsConfig(
    val remoteConfigUrl: String? = null,
    val overrides: Map<String, Boolean> = emptyMap(),
)

// Overrides win over remote config, which wins over compile-time defaults. See docs/feature-flags.md.
