package com.example.data.repository

import com.example.data.db.DocumentDao
import com.example.data.db.DocumentEntity
import com.example.data.db.StopWordDao
import com.example.data.db.StopWordEntity
import com.example.data.db.WordDao
import com.example.data.db.WordEntity
import kotlinx.coroutines.flow.Flow

class WordRepository(
    private val wordDao: WordDao,
    private val documentDao: DocumentDao,
    private val stopWordDao: StopWordDao
) {
    val allSavedWords: Flow<List<WordEntity>> = wordDao.getAllSavedWords()
    val favoriteWords: Flow<List<WordEntity>> = wordDao.getFavoriteWords()
    val allDocuments: Flow<List<DocumentEntity>> = documentDao.getAllDocuments()
    val customStopWords: Flow<List<StopWordEntity>> = stopWordDao.getAllStopWords()

    suspend fun saveWord(wordEntity: WordEntity) {
        wordDao.insertOrUpdateWord(wordEntity)
    }

    suspend fun saveWords(words: List<WordEntity>) {
        wordDao.insertWords(words)
    }

    suspend fun toggleLearned(word: String, isLearned: Boolean) {
        wordDao.updateLearnedStatus(word, isLearned)
    }

    suspend fun toggleFavorite(word: String, isFavorite: Boolean) {
        wordDao.updateFavoriteStatus(word, isFavorite)
    }

    suspend fun deleteWord(word: String) {
        wordDao.deleteWord(word)
    }

    suspend fun saveDocument(doc: DocumentEntity): Long {
        return documentDao.insertDocument(doc)
    }

    suspend fun deleteDocument(id: Long) {
        documentDao.deleteDocument(id)
    }

    suspend fun addStopWord(word: String) {
        if (word.isNotBlank()) {
            stopWordDao.insertStopWord(StopWordEntity(word.lowercase().trim()))
        }
    }

    suspend fun removeStopWord(word: String) {
        stopWordDao.deleteStopWord(word.lowercase().trim())
    }
}
