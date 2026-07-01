package cz.cernilovsky.kmp.rickandmorty.location.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import cz.cernilovsky.kmp.rickandmorty.location.data.local.LocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationRoomDataSource {
    @Upsert
    suspend fun upsertAll(locations: List<LocationEntity>)

    @Query("SELECT * FROM locations WHERE url IN (:urls)")
    fun locationsByUrls(urls: List<String>): Flow<List<LocationEntity>>
}
