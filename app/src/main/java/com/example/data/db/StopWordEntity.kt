package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stop_words")
data class StopWordEntity(
    @PrimaryKey val word: String,
    val isCustom: Boolean = true
)
