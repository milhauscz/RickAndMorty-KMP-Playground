package cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain

/**
 * One flag entry from the remote configuration document.
 *
 * @property enabled When `false`, the flag is off for every installation (kill switch).
 * @property rolloutPercent Share of installations (0–100) that receive the flag when [enabled]
 * is `true`. Omit or use [FULL_ROLLOUT] for everyone.
 */
// enabled and rolloutPercent are separate so a kill switch takes effect everywhere at once,
// while lowering rolloutPercent only stops new installations from joining.
internal data class RemoteFlagConfig(
    val enabled: Boolean,
    val rolloutPercent: Int,
) {
    companion object {
        const val FULL_ROLLOUT: Int = 100
    }
}
