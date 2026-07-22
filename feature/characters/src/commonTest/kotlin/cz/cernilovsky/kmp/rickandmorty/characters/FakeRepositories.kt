package cz.cernilovsky.kmp.rickandmorty.characters

import androidx.paging.PagingData
import cz.cernilovsky.kmp.rickandmorty.characters.domain.ICharactersRepository
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.Character
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterFilters
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterGender
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterLocation
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterStatus
import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.EmptyResult
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import cz.cernilovsky.kmp.rickandmorty.episode.domain.IEpisodeRepository
import cz.cernilovsky.kmp.rickandmorty.episode.domain.model.Episode
import cz.cernilovsky.kmp.rickandmorty.location.domain.ILocationRepository
import cz.cernilovsky.kmp.rickandmorty.location.domain.model.Location
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf

/** Builds a domain [Character] for tests. */
fun character(
    id: Int = 1,
    name: String = "Rick Sanchez",
): Character =
    Character(
        id = id,
        name = name,
        status = CharacterStatus.Alive,
        species = "Human",
        type = "",
        gender = CharacterGender.Male,
        origin = CharacterLocation(name = "Earth (C-137)", url = "https://origin/$id"),
        location = CharacterLocation(name = "Citadel of Ricks", url = "https://location/$id"),
        image = "https://image/$id.jpeg",
        episode = listOf("https://episode/1"),
        url = "https://character/$id",
        created = "2017-11-04T18:48:46.250Z",
    )

class FakeCharactersRepository(
    initialFilters: CharacterFilters = CharacterFilters.EMPTY,
    initialSelectedId: Int? = null,
    private val characters: List<Character> = emptyList(),
) : ICharactersRepository {
    private val filtersFlow = MutableStateFlow(initialFilters)
    private val selectedCharacterIdFlow = MutableStateFlow(initialSelectedId)

    var lastSetFilters: CharacterFilters? = null
        private set

    /** Test-only synchronous read of the current selection, since [selectedCharacterId] is a plain [Flow]. */
    val currentSelectedCharacterId: Int?
        get() = selectedCharacterIdFlow.value

    override val charactersPagingData: Flow<PagingData<Character>> = flowOf(PagingData.from(characters))

    override fun observeCharacter(id: Int): Flow<Character?> = flowOf(characters.firstOrNull { it.id == id })

    override val filters: Flow<CharacterFilters> = filtersFlow

    override suspend fun setFilters(filters: CharacterFilters) {
        lastSetFilters = filters
        filtersFlow.value = filters
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val selectedCharacterId: Flow<Int?> = selectedCharacterIdFlow.asFlow()

    override suspend fun setSelectedCharacterId(id: Int?) {
        selectedCharacterIdFlow.value = id
    }
}

class FakeEpisodeRepository(
    private val refreshResult: EmptyResult<DataError.Remote> = Result.Success(Unit),
) : IEpisodeRepository {
    override fun observeByUrls(urls: List<String>): Flow<List<Episode>> = flowOf(emptyList())

    override suspend fun refreshByUrls(urls: List<String>): EmptyResult<DataError.Remote> = refreshResult
}

class FakeLocationRepository(
    private val refreshResult: EmptyResult<DataError.Remote> = Result.Success(Unit),
) : ILocationRepository {
    override fun observeByUrls(urls: List<String>): Flow<List<Location>> = flowOf(emptyList())

    override suspend fun refreshByUrls(urls: List<String>): EmptyResult<DataError.Remote> = refreshResult
}
