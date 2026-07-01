package cz.cernilovsky.kmp.rickandmorty.location.data

import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import cz.cernilovsky.kmp.rickandmorty.location.data.remote.LocationDto

interface ILocationDataSource {
    suspend fun getLocation(url: String): Result<LocationDto, DataError.Remote>
}
