package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("SELECT * FROM saved_words ORDER BY dateAdded DESC")
    fun getAllSavedWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM saved_words WHERE isFavorite = 1 ORDER BY dateAdded DESC")
    fun getFavoriteWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM saved_words WHERE word = :word LIMIT 1")
    suspend fun getWord(word: String): WordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWord(word: WordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<WordEntity>)

    @Query("DELETE FROM saved_words WHERE word = :word")
    suspend fun deleteWord(word: String)

    @Query("UPDATE saved_words SET isLearned = :isLearned WHERE word = :word")
    suspend fun updateLearnedStatus(word: String, isLearned: Boolean)

    @Query("UPDATE saved_words SET isFavorite = :isFavorite WHERE word = :word")
    suspend fun updateFavoriteStatus(word: String, isFavorite: Boolean)

    @Query("DELETE FROM saved_words")
    suspend fun clearAll()
}
