package com.example.weather_test.operations

import androidx.room.*
import com.example.weather_test.models.RecentSearch

@Dao
interface RecentSearchOperations {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(recentSearch: RecentSearch)

    @Query("SELECT cityName FROM recent_search")
    fun getAll(): List<String>

    @Query("DELETE FROM recent_search")
    fun clearAll()
}