package cz.cernilovsky.kmp.rickandmorty.runtime.bridge

import com.rickclephas.kmp.nativecoroutines.NativeCoroutineScope
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterDetail
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterFilters
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharactersLoadType
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharactersPageLoadResult
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.GetCharacterDetailUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.LoadCharactersUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.ObserveCharacterFiltersUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.ObserveSelectedCharacterIdUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.SetCharacterFiltersUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.SetSelectedCharacterIdUseCase
import cz.cernilovsky.kmp.rickandmorty.core.di.MainDispatcher
import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.EmptyResult
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import cz.cernilovsky.kmp.rickandmorty.runtime.RickAndMortySdk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow

/**
 * Swift-facing headless API for character data.
 *
 * Requires [RickAndMortySdk.initialize] first. Pair with the
 * [KMPNativeCoroutinesAsync](https://github.com/rickclephas/KMP-NativeCoroutines) Swift package
 * (`asyncSequence(for:)` / `asyncFunction(for:)`) to consume [Flow] and suspend APIs from SwiftUI.
 */
public class CharactersIosBridge private constructor(
    @NativeCoroutineScope internal val coroutineScope: CoroutineScope,
) {
    private val loadCharactersUseCase: LoadCharactersUseCase = RickAndMortySdk.get()
    private val getCharacterDetailUseCase: GetCharacterDetailUseCase = RickAndMortySdk.get()
    private val observeCharacterFiltersUseCase: ObserveCharacterFiltersUseCase = RickAndMortySdk.get()
    private val setCharacterFiltersUseCase: SetCharacterFiltersUseCase = RickAndMortySdk.get()
    private val observeSelectedCharacterIdUseCase: ObserveSelectedCharacterIdUseCase = RickAndMortySdk.get()
    private val setSelectedCharacterIdUseCase: SetSelectedCharacterIdUseCase = RickAndMortySdk.get()

    @NativeCoroutines
    public suspend fun loadCharacters(
        loadType: CharactersLoadType,
        filters: CharacterFilters,
        anchorCharacterId: Int?,
    ): Result<CharactersPageLoadResult, DataError.Remote> = loadCharactersUseCase(loadType, filters, anchorCharacterId)

    @NativeCoroutines
    public fun observeCharacterDetail(id: Int): Flow<CharacterDetail?> = getCharacterDetailUseCase.observe(id)

    @NativeCoroutines
    public suspend fun refreshCharacterDetail(id: Int): EmptyResult<DataError.Remote> =
        getCharacterDetailUseCase.refresh(id)

    @NativeCoroutines
    public fun observeFilters(): Flow<CharacterFilters> = observeCharacterFiltersUseCase()

    @NativeCoroutines
    public suspend fun setFilters(filters: CharacterFilters) {
        setCharacterFiltersUseCase(filters)
    }

    @NativeCoroutines
    public fun observeSelectedCharacterId(): Flow<Int?> = observeSelectedCharacterIdUseCase()

    @NativeCoroutines
    public suspend fun setSelectedCharacterId(id: Int?) {
        setSelectedCharacterIdUseCase(id)
    }

    /** Cancels in-flight bridge coroutines. Call before [RickAndMortySdk.shutdown]. */
    public fun close() {
        coroutineScope.cancel()
    }

    public companion object {
        /** Creates a bridge backed by the initialized SDK graph. */
        public fun create(): CharactersIosBridge {
            check(RickAndMortySdk.isInitialized) {
                "RickAndMortySdk is not initialized. Call RickAndMortySdk.initialize(...) first."
            }
            val mainDispatcher =
                RickAndMortySdk.internalContainer.koin.get<CoroutineDispatcher>(MainDispatcher)
            return CharactersIosBridge(CoroutineScope(SupervisorJob() + mainDispatcher))
        }
    }
}
