package com.example.weather_test

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.weather_test.models.RecentSearch
import com.example.weather_test.operations.RecentSearchOperations

@Database(entities = [RecentSearch::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recentSearchOperations(): RecentSearchOperations

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "recent_search_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
