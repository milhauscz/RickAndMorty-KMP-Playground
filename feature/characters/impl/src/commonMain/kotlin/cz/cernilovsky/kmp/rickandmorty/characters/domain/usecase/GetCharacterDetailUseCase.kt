package cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase

import cz.cernilovsky.kmp.rickandmorty.characters.domain.CharactersRepository
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.Character
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterDetail
import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.EmptyResult
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.FeatureFlag
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.FeatureFlags
import cz.cernilovsky.kmp.rickandmorty.episode.domain.EpisodeRepository
import cz.cernilovsky.kmp.rickandmorty.location.domain.LocationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

public class GetCharacterDetailUseCase(
    private val charactersRepository: CharactersRepository,
    private val locationRepository: LocationRepository,
    private val episodeRepository: EpisodeRepository,
    private val featureFlags: FeatureFlags,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    public fun observe(id: Int): Flow<CharacterDetail?> {
        val cached =
            charactersRepository.observeCharacter(id).flatMapLatest { character ->
                if (character == null) {
                    flowOf(null)
                } else {
                    combine(
                        locationRepository.observeByUrls(character.locationUrls()),
                        episodeRepository.observeByUrls(character.episode),
                    ) { locations, episodes ->
                        CharacterDetail(
                            character = character,
                            origin = locations.firstOrNull { it.url == character.origin.url },
                            location = locations.firstOrNull { it.url == character.location.url },
                            episodes = episodes,
                        )
                    }
                }
            }

        if (!featureFlags.isEnabled(FeatureFlag.CharacterDetailAutoRefresh)) return cached

        return channelFlow {
            launch { refresh(id) }
            cached.collect { detail -> send(detail) }
        }
    }

    public suspend fun refresh(id: Int): EmptyResult<DataError.Remote> {
        val character = charactersRepository.observeCharacter(id).first() ?: return Result.Success(Unit)
        val locationResult = locationRepository.refreshByUrls(character.locationUrls())
        if (locationResult is Result.Error) return locationResult
        return episodeRepository.refreshByUrls(character.episode)
    }

    private fun Character.locationUrls(): List<String> =
        listOf(origin.url, location.url).filter { it.isNotBlank() }.distinct()
}
