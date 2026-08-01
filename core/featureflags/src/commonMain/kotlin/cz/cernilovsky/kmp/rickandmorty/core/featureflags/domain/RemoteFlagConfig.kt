package cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain

/**
 * What the remote configuration says about one flag.
 *
 * [enabled] and [rolloutPercent] are separate on purpose. Setting `enabled = false` is a kill
 * switch that takes effect everywhere at once; lowering [rolloutPercent] only stops new
 * installations from joining, and installations already inside the bucket keep the behaviour.
 */
public data class RemoteFlagConfig(
    val enabled: Boolean,
    val rolloutPercent: Int,
) {
    public companion object {
        public const val FULL_ROLLOUT: Int = 100
    }
}
