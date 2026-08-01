package cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain

import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.EmptyResult

/**
 * Answers whether a behaviour is on, right now, for this installation.
 *
 * [isEnabled] is deliberately not suspending. A flag check sits inside ordinary code paths, and a
 * suspending check would either colour half the codebase or invite someone to block on the network
 * to decide whether to show a button. Fetching is a separate, explicit [refresh].
 */
public interface FeatureFlags {
    public fun isEnabled(flag: FeatureFlag): Boolean

    /**
     * Pulls the remote configuration and replaces the cached copy. Values already read stay as they
     * were; a flag flipping mid-session is a worse experience than one that changes on next launch.
     */
    public suspend fun refresh(): EmptyResult<DataError.Remote>
}
