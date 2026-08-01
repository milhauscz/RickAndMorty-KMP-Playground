package cz.cernilovsky.kmp.rickandmorty.runtime

import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.FeatureFlag

/** Stable flag keys for [RickAndMortySdkConfig.Builder.overrideFeatureFlag]. */
public object RickAndMortyFeatureFlags {
    public val CHARACTER_DETAIL_AUTO_REFRESH: String = FeatureFlag.CharacterDetailAutoRefresh.key
}
