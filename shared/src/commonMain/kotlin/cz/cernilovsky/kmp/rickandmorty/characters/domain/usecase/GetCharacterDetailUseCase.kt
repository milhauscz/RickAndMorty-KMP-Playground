package cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase

import cz.cernilovsky.kmp.rickandmorty.characters.domain.ICharactersRepository
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.Character
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterDetail
import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.EmptyResult
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import cz.cernilovsky.kmp.rickandmorty.episode.domain.IEpisodeRepository
import cz.cernilovsky.kmp.rickandmorty.location.domain.ILocationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class GetCharacterDetailUseCase(
    private val charactersRepository: ICharactersRepository,
    private val locationRepository: ILocationRepository,
    private val episodeRepository: IEpisodeRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observe(id: Int): Flow<CharacterDetail?> =
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

    suspend fun refresh(id: Int): EmptyResult<DataError.Remote> {
        val character = charactersRepository.observeCharacter(id).first() ?: return Result.Success(Unit)
        val locationResult = locationRepository.refreshByUrls(character.locationUrls())
        if (locationResult is Result.Error) return locationResult
        return episodeRepository.refreshByUrls(character.episode)
    }

    private fun Character.locationUrls(): List<String> =
        listOf(origin.url, location.url).filter { it.isNotBlank() }.distinct()
}
