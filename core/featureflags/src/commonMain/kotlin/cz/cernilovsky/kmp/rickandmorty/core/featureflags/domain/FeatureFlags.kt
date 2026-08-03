package cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain

import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.EmptyResult

/**
 * Reads whether a [FeatureFlag] is enabled for this installation.
 *
 * Resolution order: host override → remote configuration (with rollout) → compile-time default
 * on the flag. Before the first successful [refresh], and whenever remote config is unavailable,
 * the compile-time default applies.
 */
public interface FeatureFlags {
    /** Returns whether [flag] is on right now. */
    public fun isEnabled(flag: FeatureFlag): Boolean

    /**
     * Fetches remote configuration from the URL supplied at SDK initialization.
     *
     * The SDK calls this once during startup. Until it succeeds, [isEnabled] uses compile-time
     * defaults (or host overrides). A failed refresh leaves the last successful config in memory;
     * there is no disk cache of remote flags across process restarts.
     */
    public suspend fun refresh(): EmptyResult<DataError.Remote>
}

// isEnabled is synchronous so ordinary code paths can branch on a flag without suspending.
// Network work belongs in refresh(), called explicitly at startup.
