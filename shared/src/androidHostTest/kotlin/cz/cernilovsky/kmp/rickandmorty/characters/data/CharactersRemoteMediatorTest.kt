package cz.cernilovsky.kmp.rickandmorty.characters.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterLocationEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterRemoteKeyEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.remote.CharacterDto
import cz.cernilovsky.kmp.rickandmorty.characters.data.remote.CharacterLocationDto
import cz.cernilovsky.kmp.rickandmorty.characters.data.remote.CharactersResponseDto
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterGender
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterStatus
import cz.cernilovsky.kmp.rickandmorty.core.data.model.InfoDto
import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalPagingApi::class)
class CharactersRemoteMediatorTest {
    private companion object {
        const val NEXT_PAGE_URL = "https://rickandmortyapi.com/api/character?page=2"
    }

    private lateinit var fakeRemote: FakeCharactersDataSource
    private lateinit var fakeLocal: FakeCharactersRoomDataSource
    private lateinit var mediator: CharactersRemoteMediator

    @Before
    fun setUp() {
        fakeRemote = FakeCharactersDataSource()
        fakeLocal = FakeCharactersRoomDataSource()
        mediator = CharactersRemoteMediator(fakeRemote, fakeLocal)
    }

    // --- initialize() ---

    @Test
    fun initialize_whenCacheIsEmpty_returnsLaunchInitialRefresh() =
        runTest {
            val action = mediator.initialize()
            assertEquals(RemoteMediator.InitializeAction.LAUNCH_INITIAL_REFRESH, action)
        }

    @Test
    fun initialize_whenCacheIsFresh_returnsSkipInitialRefresh() =
        runTest {
            fakeLocal.setLastUpdated(Clock.System.now().toEpochMilliseconds())
            val action = mediator.initialize()
            assertEquals(RemoteMediator.InitializeAction.SKIP_INITIAL_REFRESH, action)
        }

    @Test
    fun initialize_whenCacheIsOlderThan15Minutes_returnsLaunchInitialRefresh() =
        runTest {
            val staleTimestamp = (Clock.System.now() - 16.minutes).toEpochMilliseconds()
            fakeLocal.setLastUpdated(staleTimestamp)
            val action = mediator.initialize()
            assertEquals(RemoteMediator.InitializeAction.LAUNCH_INITIAL_REFRESH, action)
        }

    // --- load(REFRESH) ---

    @Test
    fun load_refresh_onSuccess_callsRefreshAndReturnsSuccess() =
        runTest {
            fakeRemote.result = Result.Success(successResponse())

            val result = mediator.load(LoadType.REFRESH, emptyPagingState())

            assertIs<RemoteMediator.MediatorResult.Success>(result)
            assertEquals(1, fakeLocal.refreshCallCount)
        }

    @Test
    fun load_refresh_onLastPage_returnsMediatorSuccessWithEndOfPaginationReached() =
        runTest {
            fakeRemote.result = Result.Success(successResponse(hasNextPage = false))

            val result = mediator.load(LoadType.REFRESH, emptyPagingState())

            val success = assertIs<RemoteMediator.MediatorResult.Success>(result)
            assertTrue(success.endOfPaginationReached)
        }

    @Test
    fun load_refresh_onError_returnsMediatorError() =
        runTest {
            fakeRemote.result = Result.Error(DataError.Remote.NO_INTERNET)

            val result = mediator.load(LoadType.REFRESH, emptyPagingState())

            assertIs<RemoteMediator.MediatorResult.Error>(result)
        }

    // --- load(PREPEND) ---

    @Test
    fun load_prepend_whenPagesAreEmpty_returnsSuccessEndOfPaginationNotReached() =
        runTest {
            val result = mediator.load(LoadType.PREPEND, emptyPagingState())

            val success = assertIs<RemoteMediator.MediatorResult.Success>(result)
            assertTrue(!success.endOfPaginationReached)
        }

    @Test
    fun load_prepend_whenFirstItemHasNoPrevKey_returnsSuccessEndOfPaginationReached() =
        runTest {
            fakeLocal.setRemoteKey(CharacterRemoteKeyEntity(characterId = 1, prevKey = null, nextKey = NEXT_PAGE_URL))
            val state = pagingStateWithItems(listOf(characterEntity(id = 1)))

            val result = mediator.load(LoadType.PREPEND, state)

            val success = assertIs<RemoteMediator.MediatorResult.Success>(result)
            assertTrue(success.endOfPaginationReached)
        }

    // --- load(APPEND) ---

    @Test
    fun load_append_whenPagesAreEmpty_returnsSuccessEndOfPaginationNotReached() =
        runTest {
            val result = mediator.load(LoadType.APPEND, emptyPagingState())

            val success = assertIs<RemoteMediator.MediatorResult.Success>(result)
            assertTrue(!success.endOfPaginationReached)
        }

    @Test
    fun load_append_whenLastItemHasNoNextKey_returnsSuccessEndOfPaginationReached() =
        runTest {
            fakeLocal.setRemoteKey(CharacterRemoteKeyEntity(characterId = 1, prevKey = null, nextKey = null))
            val state = pagingStateWithItems(listOf(characterEntity(id = 1)))

            val result = mediator.load(LoadType.APPEND, state)

            val success = assertIs<RemoteMediator.MediatorResult.Success>(result)
            assertTrue(success.endOfPaginationReached)
        }

    @Test
    fun load_append_whenLastItemHasNextKey_fetchesNextPageAndInsertsData() =
        runTest {
            fakeLocal.setRemoteKey(CharacterRemoteKeyEntity(characterId = 1, prevKey = null, nextKey = NEXT_PAGE_URL))
            fakeRemote.result = Result.Success(successResponse(hasNextPage = false))
            val state = pagingStateWithItems(listOf(characterEntity(id = 1)))

            val result = mediator.load(LoadType.APPEND, state)

            assertIs<RemoteMediator.MediatorResult.Success>(result)
            assertEquals(NEXT_PAGE_URL, fakeRemote.lastRequestedUrl)
            assertTrue(fakeLocal.characters.isNotEmpty())
        }

    // --- helpers ---

    private fun emptyPagingState() =
        PagingState<Int, CharacterEntity>(
            pages = emptyList(),
            anchorPosition = null,
            config = PagingConfig(pageSize = 20),
            leadingPlaceholderCount = 0,
        )

    private fun pagingStateWithItems(items: List<CharacterEntity>) =
        PagingState(
            pages =
                listOf(
                    PagingSource.LoadResult.Page<Int, CharacterEntity>(data = items, prevKey = null, nextKey = null),
                ),
            anchorPosition = null,
            config = PagingConfig(pageSize = 20),
            leadingPlaceholderCount = 0,
        )

    private fun successResponse(hasNextPage: Boolean = true) =
        CharactersResponseDto(
            info =
                InfoDto(
                    count = 20,
                    pages = if (hasNextPage) 2 else 1,
                    next = if (hasNextPage) NEXT_PAGE_URL else null,
                    prev = null,
                ),
            results = listOf(characterDto()),
        )

    private fun characterDto() =
        CharacterDto(
            id = 1,
            name = "Rick Sanchez",
            status = "Alive",
            species = "Human",
            type = "",
            gender = "Male",
            origin = CharacterLocationDto(name = "Earth", url = ""),
            location = CharacterLocationDto(name = "Citadel of Ricks", url = ""),
            image = "https://example.com/image.jpg",
            episode = listOf("https://api.example.com/episode/1"),
            url = "https://api.example.com/character/1",
            created = "2017-11-04T18:48:46.250Z",
        )

    private fun characterEntity(id: Int = 1) =
        CharacterEntity(
            id = id,
            name = "Rick Sanchez",
            status = CharacterStatus.Alive,
            species = "Human",
            type = "",
            gender = CharacterGender.Male,
            origin = CharacterLocationEntity(name = "Earth", url = ""),
            location = CharacterLocationEntity(name = "Citadel of Ricks", url = ""),
            image = "https://example.com/image.jpg",
            episode = listOf("https://api.example.com/episode/1"),
            url = "https://api.example.com/character/1",
            created = "2017-11-04T18:48:46.250Z",
        )
}
