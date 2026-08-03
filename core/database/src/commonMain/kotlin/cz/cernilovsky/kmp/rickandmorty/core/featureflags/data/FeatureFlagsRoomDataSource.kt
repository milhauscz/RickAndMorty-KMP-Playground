package cz.cernilovsky.kmp.rickandmorty.core.featureflags.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.data.local.FeatureFlagConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeatureFlagsRoomDataSource {
    @Query("SELECT * FROM feature_flag_configs")
    fun observeAll(): Flow<List<FeatureFlagConfigEntity>>

    @Query("SELECT * FROM feature_flag_configs")
    suspend fun getAll(): List<FeatureFlagConfigEntity>

    @Upsert
    suspend fun upsertAll(configs: List<FeatureFlagConfigEntity>)

    @Query("DELETE FROM feature_flag_configs")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(configs: List<FeatureFlagConfigEntity>) {
        deleteAll()
        if (configs.isNotEmpty()) {
            upsertAll(configs)
        }
    }
}
