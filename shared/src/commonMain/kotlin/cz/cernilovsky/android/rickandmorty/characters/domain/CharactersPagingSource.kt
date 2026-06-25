package cz.cernilovsky.android.rickandmorty.characters.domain

import app.cash.paging.PagingSource
import app.cash.paging.PagingSourceLoadParams
import app.cash.paging.PagingSourceLoadResult
import app.cash.paging.PagingSourceLoadResultError
import app.cash.paging.PagingSourceLoadResultInvalid
import app.cash.paging.PagingSourceLoadResultPage
import app.cash.paging.PagingState
import cz.cernilovsky.android.rickandmorty.characters.domain.model.Character
import cz.cernilovsky.android.rickandmorty.characters.domain.usecase.GetCharactersUseCase
import cz.cernilovsky.android.rickandmorty.core.domain.onError
import cz.cernilovsky.android.rickandmorty.core.domain.onSuccess
import cz.cernilovsky.android.rickandmorty.core.network.HttpClientException

class CharactersPagingSource(private val getCharactersUseCase: GetCharactersUseCase) :
    PagingSource<Int, Character>() {
    override fun getRefreshKey(state: PagingState<Int, Character>): Int? {
        // Try to find the page key of the closest page to anchorPosition from
        // either the prevKey or the nextKey; you need to handle nullability
        // here.
        //  * prevKey == null -> anchorPage is the first page.
        //  * nextKey == null -> anchorPage is the last page.
        //  * both prevKey and nextKey are null -> anchorPage is the
        //    initial page, so return null.
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: PagingSourceLoadParams<Int>): PagingSourceLoadResult<Int, Character> {
        val nextPageNumber = params.key ?: 1
        val response = getCharactersUseCase(nextPageNumber)
        response.onSuccess {
            return PagingSourceLoadResultPage(
                data = it.characters,
                prevKey = null,
                nextKey = nextPageNumber + 1
            )
        }.onError {
            return PagingSourceLoadResultError(HttpClientException(it))
        }
        return PagingSourceLoadResultInvalid()
    }
}