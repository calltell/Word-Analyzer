package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StopWordDao {
    @Query("SELECT * FROM stop_words")
    fun getAllStopWords(): Flow<List<StopWordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStopWord(stopWord: StopWordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStopWords(stopWords: List<StopWordEntity>)

    @Query("DELETE FROM stop_words WHERE word = :word")
    suspend fun deleteStopWord(word: String)
}
