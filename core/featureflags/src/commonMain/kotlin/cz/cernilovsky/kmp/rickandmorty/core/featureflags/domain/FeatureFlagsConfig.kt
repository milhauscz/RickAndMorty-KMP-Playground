package cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain

/**
 * How flags are resolved for one process.
 *
 * [overrides] wins over everything, including a kill switch, and exists so that a host integrating
 * the SDK can write a test for a behaviour without waiting for someone to flip it in a console. The
 * cost of not offering this is that partners fake the whole SDK instead, and then their tests stop
 * telling either of us anything.
 */
public data class FeatureFlagsConfig(
    val remoteConfigUrl: String? = null,
    val overrides: Map<String, Boolean> = emptyMap(),
)
