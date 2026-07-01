package cz.cernilovsky.kmp.rickandmorty.core.db

import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseCreator : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

data class DatabaseConfig(
    val allowDestructiveMigration: Boolean,
)

fun getAppDatabase(
    builder: RoomDatabase.Builder<AppDatabase>,
    allowDestructiveMigration: Boolean,
): AppDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .apply {
            // Only wipe the DB on a schema change in debug builds; release builds must migrate.
            if (allowDestructiveMigration) {
                fallbackToDestructiveMigration(dropAllTables = true)
            }
        }.build()
