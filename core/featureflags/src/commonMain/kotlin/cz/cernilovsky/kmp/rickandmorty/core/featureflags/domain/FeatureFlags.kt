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
     * Loads any cached remote configuration, then fetches a fresh document when a remote URL was
     * supplied at SDK initialization.
     *
     * The SDK calls this once during startup. Until cache load / refresh completes, [isEnabled] may
     * still see compile-time defaults (or host overrides). A failed network refresh leaves the
     * last successful config in memory and on disk.
     */
    public suspend fun refresh(): EmptyResult<DataError.Remote>
}

// isEnabled is synchronous so ordinary code paths can branch on a flag without suspending.
// Network work belongs in refresh(), called explicitly at startup.
