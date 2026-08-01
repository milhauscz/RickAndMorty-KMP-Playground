package cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain

/**
 * A remotely controllable behaviour identified by a stable [key].
 *
 * Pass a flag instance to [FeatureFlags.isEnabled]. The [key] is also what remote configuration
 * documents and host overrides use.
 */
public sealed class FeatureFlag {
    public abstract val key: String
    public abstract val defaultEnabled: Boolean

    /**
     * Refreshes a character's locations and episodes in the background when detail observation starts.
     * Off by default.
     */
    public data object CharacterDetailAutoRefresh : FeatureFlag() {
        override val key: String = "character_detail_auto_refresh"
        override val defaultEnabled: Boolean = false
    }

    public companion object {
        /** Every flag declared in this module. */
        public val entries: List<FeatureFlag> = listOf(CharacterDetailAutoRefresh)
    }
}
