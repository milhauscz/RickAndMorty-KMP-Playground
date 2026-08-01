package cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain

import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.EmptyResult

/**
 * Reads whether a [FeatureFlag] is enabled for this installation.
 */
public interface FeatureFlags {
    /** Returns whether [flag] is on right now. */
    public fun isEnabled(flag: FeatureFlag): Boolean

    /**
     * Fetches remote configuration. Call once after startup; flag values already read are unchanged
     * until the next launch.
     */
    public suspend fun refresh(): EmptyResult<DataError.Remote>
}

// isEnabled is synchronous so ordinary code paths can branch on a flag without suspending.
// Network work belongs in refresh(), called explicitly at startup.
