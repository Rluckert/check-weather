package com.example.weather_test.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_search")
data class RecentSearch(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    val cityName: String,
    val temperature: Double,
    val description: String
)
