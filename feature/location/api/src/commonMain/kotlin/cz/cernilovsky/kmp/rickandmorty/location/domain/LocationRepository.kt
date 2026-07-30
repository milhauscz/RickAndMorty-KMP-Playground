package cz.cernilovsky.kmp.rickandmorty.location.domain

import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.EmptyResult
import cz.cernilovsky.kmp.rickandmorty.location.domain.model.Location
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun observeByUrls(urls: List<String>): Flow<List<Location>>

    suspend fun refreshByUrls(urls: List<String>): EmptyResult<DataError.Remote>
}
