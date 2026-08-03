package cz.cernilovsky.kmp.rickandmorty.core.featureflags.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feature_flag_configs")
data class FeatureFlagConfigEntity(
    @PrimaryKey
    val key: String,
    val enabled: Boolean,
    val rolloutPercent: Int,
)
