package cz.cernilovsky.kmp.rickandmorty.episode.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import cz.cernilovsky.kmp.rickandmorty.episode.data.local.EpisodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeRoomDataSource {
    @Upsert
    suspend fun upsertAll(episodes: List<EpisodeEntity>)

    @Query("SELECT * FROM episodes WHERE url IN (:urls) ORDER BY id ASC")
    fun episodesByUrls(urls: List<String>): Flow<List<EpisodeEntity>>
}
