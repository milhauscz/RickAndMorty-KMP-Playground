package cz.cernilovsky.kmp.rickandmorty.runtime

import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.RickAndMortyFlags

/** Keys for flags a host may override via [RickAndMortySdkConfig.Builder.overrideFeatureFlag]. */
public object RickAndMortyFeatureFlags {
    public val CHARACTER_DETAIL_AUTO_REFRESH: String = RickAndMortyFlags.characterDetailAutoRefresh.key
}
