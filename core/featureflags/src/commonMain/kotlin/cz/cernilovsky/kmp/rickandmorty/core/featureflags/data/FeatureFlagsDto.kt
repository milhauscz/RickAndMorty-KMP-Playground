package cz.cernilovsky.kmp.rickandmorty.core.featureflags.data

import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.RemoteFlagConfig
import kotlinx.serialization.Serializable

/**
 * The remote configuration document:
 *
 * ```json
 * { "flags": { "character_detail_auto_refresh": { "enabled": true, "rolloutPercent": 25 } } }
 * ```
 *
 * Every field has a default, so a config written by a service that knows about newer fields than
 * this build does still parses. A flag system that fails closed on an unexpected key is a flag
 * system that turns a config typo into an outage.
 */
@Serializable
internal data class FeatureFlagsDto(
    val flags: Map<String, FeatureFlagDto> = emptyMap(),
)

@Serializable
internal data class FeatureFlagDto(
    val enabled: Boolean = false,
    val rolloutPercent: Int = RemoteFlagConfig.FULL_ROLLOUT,
)

internal fun FeatureFlagsDto.toRemoteConfig(): Map<String, RemoteFlagConfig> =
    flags.mapValues { (_, dto) ->
        RemoteFlagConfig(
            enabled = dto.enabled,
            // A percentage outside 0..100 is a mistake somewhere upstream, and the sensible reading
            // of it is the nearest thing that makes sense rather than a crash in a partner's app.
            rolloutPercent = dto.rolloutPercent.coerceIn(0, RemoteFlagConfig.FULL_ROLLOUT),
        )
    }
