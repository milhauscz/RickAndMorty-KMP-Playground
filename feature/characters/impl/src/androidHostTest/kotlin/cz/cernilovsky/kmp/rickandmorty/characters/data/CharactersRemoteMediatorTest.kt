package cz.cernilovsky.kmp.rickandmorty.characters.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterGenderEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterLocationEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterRemoteKeyEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterStatusEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.remote.CharacterDto
import cz.cernilovsky.kmp.rickandmorty.characters.data.remote.CharacterLocationDto
import cz.cernilovsky.kmp.rickandmorty.characters.data.remote.CharactersResponseDto
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterFilters
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterGender
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterStatus
import cz.cernilovsky.kmp.rickandmorty.core.data.model.InfoDto
import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import cz.cernilovsky.kmp.rickandmorty.core.network.ClearableCacheStorage
import cz.cernilovsky.kmp.rickandmorty.core.network.NetworkConfig
import io.ktor.client.plugins.cache.storage.CachedResponseData
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import io.ktor.util.date.GMTDate
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
        const val UNFILTERED_URL = "${NetworkConfig.BASE_URL}/character"
    }

    private lateinit var fakeRemote: FakeCharactersDataSource
    private lateinit var fakeLocal: FakeCharactersRoomDataSource
    private lateinit var cacheStorage: ClearableCacheStorage
    private lateinit var mediator: CharactersRemoteMediator

    @Before
    fun setUp() {
        fakeRemote = FakeCharactersDataSource()
        fakeLocal = FakeCharactersRoomDataSource()
        cacheStorage = ClearableCacheStorage()
        mediator = CharactersRemoteMediator(fakeRemote, fakeLocal, cacheStorage)
    }

    private fun mediatorWithFilters(filters: CharacterFilters) =
        CharactersRemoteMediator(fakeRemote, fakeLocal, cacheStorage, filters)

    private fun cachedResponseData(url: String) =
        CachedResponseData(
            url = Url(url),
            statusCode = HttpStatusCode.OK,
            requestTime = GMTDate(),
            responseTime = GMTDate(),
            version = HttpProtocolVersion.HTTP_1_1,
            expires = GMTDate(Long.MAX_VALUE),
            headers = headersOf(),
            varyKeys = emptyMap(),
            body = ByteArray(0),
        )

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
            fakeLocal.setAppliedFiltersKey(UNFILTERED_URL)
            val action = mediator.initialize()
            assertEquals(RemoteMediator.InitializeAction.SKIP_INITIAL_REFRESH, action)
        }

    @Test
    fun initialize_whenCacheIsOlderThan15Minutes_returnsLaunchInitialRefresh() =
        runTest {
            val staleTimestamp = (Clock.System.now() - 16.minutes).toEpochMilliseconds()
            fakeLocal.setLastUpdated(staleTimestamp)
            fakeLocal.setAppliedFiltersKey(UNFILTERED_URL)
            val action = mediator.initialize()
            assertEquals(RemoteMediator.InitializeAction.LAUNCH_INITIAL_REFRESH, action)
        }

    @Test
    fun initialize_whenCacheFreshButFiltersDiffer_returnsLaunchInitialRefresh() =
        runTest {
            fakeLocal.setLastUpdated(Clock.System.now().toEpochMilliseconds())
            fakeLocal.setAppliedFiltersKey(UNFILTERED_URL)
            val filteredMediator = mediatorWithFilters(CharacterFilters(status = CharacterStatus.Alive))

            val action = filteredMediator.initialize()

            assertEquals(RemoteMediator.InitializeAction.LAUNCH_INITIAL_REFRESH, action)
        }

    @Test
    fun initialize_whenCacheFreshAndFiltersMatch_returnsSkipInitialRefresh() =
        runTest {
            val filteredMediator = mediatorWithFilters(CharacterFilters(status = CharacterStatus.Alive))
            fakeLocal.setLastUpdated(Clock.System.now().toEpochMilliseconds())
            fakeLocal.setAppliedFiltersKey("$UNFILTERED_URL?status=alive")

            val action = filteredMediator.initialize()

            assertEquals(RemoteMediator.InitializeAction.SKIP_INITIAL_REFRESH, action)
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
    fun load_refresh_clearsHttpCache() =
        runTest {
            cacheStorage.store(Url(UNFILTERED_URL), cachedResponseData(UNFILTERED_URL))
            fakeRemote.result = Result.Success(successResponse())

            mediator.load(LoadType.REFRESH, emptyPagingState())

            assertTrue(cacheStorage.findAll(Url(UNFILTERED_URL)).isEmpty())
        }

    @Test
    fun load_append_doesNotClearHttpCache() =
        runTest {
            cacheStorage.store(Url(UNFILTERED_URL), cachedResponseData(UNFILTERED_URL))
            fakeLocal.setRemoteKey(CharacterRemoteKeyEntity(characterId = 1, prevKey = null, nextKey = NEXT_PAGE_URL))
            fakeRemote.result = Result.Success(successResponse(hasNextPage = false))
            val state = pagingStateWithItems(listOf(characterEntity(id = 1)))

            mediator.load(LoadType.APPEND, state)

            assertTrue(cacheStorage.findAll(Url(UNFILTERED_URL)).isNotEmpty())
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

    @Test
    fun load_refresh_withFilters_requestsUrlWithLowercaseQueryParams() =
        runTest {
            fakeRemote.result = Result.Success(successResponse())
            val filteredMediator =
                mediatorWithFilters(
                    CharacterFilters(
                        name = "rick sanchez",
                        status = CharacterStatus.Alive,
                        gender = CharacterGender.Male,
                    ),
                )

            filteredMediator.load(LoadType.REFRESH, emptyPagingState())

            val requestedUrl = fakeRemote.lastRequestedUrl
            assertTrue(requestedUrl != null && requestedUrl.contains("name=rick+sanchez"))
            assertTrue(requestedUrl.contains("status=alive"))
            assertTrue(requestedUrl.contains("gender=male"))
        }

    @Test
    fun load_refresh_on404_clearsCacheAndReturnsSuccessEndOfPagination() =
        runTest {
            fakeLocal.insertAll(listOf(characterEntity(id = 1)))
            fakeRemote.result = Result.Error(DataError.Remote.NOT_FOUND)

            val result = mediator.load(LoadType.REFRESH, emptyPagingState())

            val success = assertIs<RemoteMediator.MediatorResult.Success>(result)
            assertTrue(success.endOfPaginationReached)
            assertEquals(1, fakeLocal.refreshCallCount)
            assertTrue(fakeLocal.characters.isEmpty())
            assertEquals(UNFILTERED_URL, fakeLocal.metadata?.appliedFiltersKey)
        }

    @Test
    fun load_append_on404_returnsSuccessWithoutClearing() =
        runTest {
            fakeLocal.setRemoteKey(CharacterRemoteKeyEntity(characterId = 1, prevKey = null, nextKey = NEXT_PAGE_URL))
            fakeLocal.insertAll(listOf(characterEntity(id = 1)))
            fakeRemote.result = Result.Error(DataError.Remote.NOT_FOUND)
            val state = pagingStateWithItems(listOf(characterEntity(id = 1)))

            val result = mediator.load(LoadType.APPEND, state)

            val success = assertIs<RemoteMediator.MediatorResult.Success>(result)
            assertTrue(success.endOfPaginationReached)
            assertEquals(0, fakeLocal.refreshCallCount)
            assertTrue(fakeLocal.characters.isNotEmpty())
        }

    @Test
    fun load_refresh_onSuccess_persistsAppliedFiltersKeyAndAdvancesLastUpdated() =
        runTest {
            fakeRemote.result = Result.Success(successResponse())

            mediator.load(LoadType.REFRESH, emptyPagingState())
            val firstLastUpdated = fakeLocal.metadata?.lastUpdated
            assertEquals(UNFILTERED_URL, fakeLocal.metadata?.appliedFiltersKey)
            assertTrue(firstLastUpdated != null && firstLastUpdated > 0)

            mediator.load(LoadType.REFRESH, emptyPagingState())
            val secondLastUpdated = fakeLocal.metadata?.lastUpdated

            assertTrue(secondLastUpdated != null && secondLastUpdated >= firstLastUpdated)
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
            status = CharacterStatusEntity.Alive,
            species = "Human",
            type = "",
            gender = CharacterGenderEntity.Male,
            origin = CharacterLocationEntity(name = "Earth", url = ""),
            location = CharacterLocationEntity(name = "Citadel of Ricks", url = ""),
            image = "https://example.com/image.jpg",
            episode = listOf("https://api.example.com/episode/1"),
            url = "https://api.example.com/character/1",
            created = "2017-11-04T18:48:46.250Z",
        )
}
