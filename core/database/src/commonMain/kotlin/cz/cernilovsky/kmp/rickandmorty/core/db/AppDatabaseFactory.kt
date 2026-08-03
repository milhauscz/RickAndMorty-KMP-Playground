package cz.cernilovsky.kmp.rickandmorty.core.db

import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlin.coroutines.CoroutineContext

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseCreator : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

data class DatabaseConfig(
    val allowDestructiveMigration: Boolean,
)

val MIGRATION_4_5 =
    object : Migration(AppDatabase.DB_VERSION - 1, AppDatabase.DB_VERSION) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS `feature_flag_configs` " +
                    "(`key` TEXT NOT NULL, `enabled` INTEGER NOT NULL, `rolloutPercent` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`key`))",
            )
        }
    }

fun getAppDatabase(
    builder: RoomDatabase.Builder<AppDatabase>,
    allowDestructiveMigration: Boolean,
    queryCoroutineContext: CoroutineContext,
): AppDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(queryCoroutineContext)
        .addMigrations(MIGRATION_4_5)
        .apply {
            // Only wipe the DB on a schema change in debug builds; release builds must migrate.
            if (allowDestructiveMigration) {
                fallbackToDestructiveMigration(dropAllTables = true)
            }
        }.build()
