package cz.cernilovsky.kmp.rickandmorty.core.db

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSHomeDirectory

fun getAppDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = NSHomeDirectory() + "/${AppDatabase.DB_NAME}.db"
    return Room.databaseBuilder<AppDatabase>(name = dbFile)
}
