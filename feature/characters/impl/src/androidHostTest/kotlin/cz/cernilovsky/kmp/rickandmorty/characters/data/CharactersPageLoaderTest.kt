package cz.cernilovsky.kmp.rickandmorty.characters.data

import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterGenderEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterLocationEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterRemoteKeyEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterStatusEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.remote.CharacterDto
import cz.cernilovsky.kmp.rickandmorty.characters.data.remote.CharacterLocationDto
import cz.cernilovsky.kmp.rickandmorty.characters.data.remote.CharactersResponseDto
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterFilters
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharactersLoadType
import cz.cernilovsky.kmp.rickandmorty.core.data.model.InfoDto
import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import cz.cernilovsky.kmp.rickandmorty.core.network.ClearableCacheStorage
import cz.cernilovsky.kmp.rickandmorty.core.network.NetworkConfig
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock

class CharactersPageLoaderTest {
    private val remote = FakeCharactersDataSource()
    private val local = FakeCharactersRoomDataSource()
    private val repository =
        CharactersRepositoryImpl(
            remoteDataSource = remote,
            localDataSource = local,
            cacheStorage = ClearableCacheStorage(),
            networkConfig = NetworkConfig(baseUrl = NetworkConfig.DEFAULT_BASE_URL, loggingEnabled = false),
        )

    @Test
    fun init_whenCacheEmpty_fetchesFirstPageFromRemote() =
        runTest {
            remote.result = Result.Success(charactersResponse(ids = listOf(1, 2), next = NEXT_PAGE_URL))

            val result = repository.loadCharacters(CharactersLoadType.Init, CharacterFilters.EMPTY)

            assertTrue(result is Result.Success)
            assertEquals(listOf(1, 2), result.data.characters.map { it.id })
            assertTrue(result.data.hasMore)
            assertEquals(1, local.refreshCallCount)
            assertEquals(1, remote.requestCount)
        }

    @Test
    fun init_whenCacheFresh_returnsLocalFirstPageWithoutNetwork() =
        runTest {
            seedWarmCache(characterIds = (1..40).toList(), nextKeyForLast = PAGE_3_URL)
            remote.result = Result.Error(DataError.Remote.UNKNOWN)

            val result = repository.loadCharacters(CharactersLoadType.Init, CharacterFilters.EMPTY)

            assertTrue(result is Result.Success)
            assertEquals((1..20).toList(), result.data.characters.map { it.id })
            assertTrue(result.data.hasMore)
            assertEquals(0, remote.requestCount)
            assertEquals(0, local.refreshCallCount)
        }

    @Test
    fun init_mapsNotFoundToEmptyCache() =
        runTest {
            remote.result = Result.Error(DataError.Remote.NOT_FOUND)

            val result = repository.loadCharacters(CharactersLoadType.Init, CharacterFilters.EMPTY)

            assertTrue(result is Result.Success)
            assertTrue(result.data.characters.isEmpty())
            assertTrue(local.characters.isEmpty())
        }

    @Test
    fun append_withWarmCache_returnsLocalWindowWithoutNetwork() =
        runTest {
            seedWarmCache(characterIds = (1..40).toList(), nextKeyForLast = PAGE_3_URL)
            remote.result = Result.Error(DataError.Remote.UNKNOWN)

            val result =
                repository.loadCharacters(
                    CharactersLoadType.Append,
                    CharacterFilters.EMPTY,
                    anchorCharacterId = 20,
                )

            assertTrue(result is Result.Success)
            assertEquals((21..40).toList(), result.data.characters.map { it.id })
            assertEquals(0, remote.requestCount)
        }

    @Test
    fun append_atEndOfLocalCache_fetchesRemoteNextPage() =
        runTest {
            seedWarmCache(characterIds = (1..20).toList(), nextKeyForLast = NEXT_PAGE_URL)
            remote.result =
                Result.Success(charactersResponse(ids = listOf(21, 22), prev = FIRST_PAGE_URL, next = null))

            val result =
                repository.loadCharacters(
                    CharactersLoadType.Append,
                    CharacterFilters.EMPTY,
                    anchorCharacterId = 20,
                )

            assertTrue(result is Result.Success)
            assertEquals(listOf(21, 22), result.data.characters.map { it.id })
            assertEquals(NEXT_PAGE_URL, remote.lastRequestedUrl)
            assertEquals(22, local.characters.size)
        }

    @Test
    fun append_withoutAnchor_returnsEmptyPage() =
        runTest {
            val result = repository.loadCharacters(CharactersLoadType.Append, CharacterFilters.EMPTY)

            assertTrue(result is Result.Success)
            assertTrue(result.data.characters.isEmpty())
            assertEquals(false, result.data.hasMore)
        }

    @Test
    fun prepend_withLocalRows_returnsLocalWindowWithoutNetwork() =
        runTest {
            seedWarmCache(characterIds = (1..40).toList(), nextKeyForLast = null)
            remote.result = Result.Error(DataError.Remote.UNKNOWN)

            val result =
                repository.loadCharacters(
                    CharactersLoadType.Prepend,
                    CharacterFilters.EMPTY,
                    anchorCharacterId = 21,
                )

            assertTrue(result is Result.Success)
            assertEquals((1..20).toList(), result.data.characters.map { it.id })
            assertEquals(0, remote.requestCount)
        }

    private suspend fun seedWarmCache(
        characterIds: List<Int>,
        nextKeyForLast: String?,
    ) {
        val entities = characterIds.map { characterEntity(it) }
        val remoteKeys =
            characterIds.map { id ->
                CharacterRemoteKeyEntity(
                    characterId = id,
                    prevKey = if (id == characterIds.first()) null else FIRST_PAGE_URL,
                    nextKey = if (id == characterIds.last()) nextKeyForLast else NEXT_PAGE_URL,
                )
            }
        local.refresh(entities, remoteKeys, UNFILTERED_URL)
        local.setLastUpdated(Clock.System.now().toEpochMilliseconds())
        remote.requestCount = 0
        local.refreshCallCount = 0
    }

    private fun charactersResponse(
        ids: List<Int>,
        prev: String? = null,
        next: String? = null,
    ): CharactersResponseDto =
        CharactersResponseDto(
            info = InfoDto(count = ids.size, pages = 2, next = next, prev = prev),
            results =
                ids.map { id ->
                    CharacterDto(
                        id = id,
                        name = "Character $id",
                        status = "Alive",
                        species = "Human",
                        type = "",
                        gender = "Male",
                        origin = CharacterLocationDto(name = "Earth", url = "https://origin/$id"),
                        location = CharacterLocationDto(name = "Citadel", url = "https://location/$id"),
                        image = "https://image/$id.jpeg",
                        episode = listOf("https://episode/1"),
                        url = "https://character/$id",
                        created = "2017-11-04T18:48:46.250Z",
                    )
                },
        )

    private fun characterEntity(id: Int) =
        CharacterEntity(
            id = id,
            name = "Character $id",
            status = CharacterStatusEntity.Alive,
            species = "Human",
            type = "",
            gender = CharacterGenderEntity.Male,
            origin = CharacterLocationEntity(name = "Earth", url = "https://origin/$id"),
            location = CharacterLocationEntity(name = "Citadel", url = "https://location/$id"),
            image = "https://image/$id.jpeg",
            episode = listOf("https://episode/1"),
            url = "https://character/$id",
            created = "2017-11-04T18:48:46.250Z",
        )

    private companion object {
        const val UNFILTERED_URL = "${NetworkConfig.DEFAULT_BASE_URL}/character"
        const val FIRST_PAGE_URL = "https://rickandmortyapi.com/api/character?page=1"
        const val NEXT_PAGE_URL = "https://rickandmortyapi.com/api/character?page=2"
        const val PAGE_3_URL = "https://rickandmortyapi.com/api/character?page=3"
    }
}
