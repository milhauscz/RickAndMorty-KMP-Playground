package cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase

import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.EmptyResult
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.FeatureFlag
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.FeatureFlags

class FakeFeatureFlags(
    private val overrides: Map<String, Boolean> = emptyMap(),
) : FeatureFlags {
    override fun isEnabled(flag: FeatureFlag): Boolean = overrides[flag.key] ?: flag.defaultEnabled

    override suspend fun refresh(): EmptyResult<DataError.Remote> = Result.Success(Unit)
}
