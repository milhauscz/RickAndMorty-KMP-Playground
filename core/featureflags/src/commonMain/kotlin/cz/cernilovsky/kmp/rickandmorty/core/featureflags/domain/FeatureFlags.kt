package cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain

import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.EmptyResult

/**
 * Reads whether a [FeatureFlag] is enabled for this installation.
 *
 * Resolution order: host override → remote/cached configuration (with rollout) → compile-time
 * default on the flag. On startup the SDK loads the last successful remote config from local
 * storage, then refreshes from the network when configured.
 */
public interface FeatureFlags {
    /** Returns whether [flag] is on right now. */
    public fun isEnabled(flag: FeatureFlag): Boolean

    /**
     * Fetches remote configuration and writes it into the local cache.
     *
     * The SDK starts a refresh when feature flags are constructed. Call again to force a network
     * refresh; a failure leaves the last successful cache (memory and disk) unchanged.
     */
    public suspend fun refresh(): EmptyResult<DataError.Remote>
}

// isEnabled is synchronous so ordinary code paths can branch on a flag without suspending.
// Network work belongs in refresh() (auto-started at construction; callable again on demand).
