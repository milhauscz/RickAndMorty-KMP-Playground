package cz.cernilovsky.android.rickandmorty.core.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getAppDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    return Room.databaseBuilder<AppDatabase>(context, AppDatabase.DB_NAME)
}