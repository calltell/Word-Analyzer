package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_words")
data class WordEntity(
    @PrimaryKey val word: String,
    val lemma: String,
    val translation: String? = null,
    val phonetic: String? = null,
    val partOfSpeech: String? = null,
    val definition: String? = null,
    val contextSentence: String? = null,
    val contextTranslation: String? = null,
    val cefrLevel: String? = null,
    val frequency: Int = 1,
    val isLearned: Boolean = false,
    val isFavorite: Boolean = false,
    val customNotes: String? = null,
    val dateAdded: Long = System.currentTimeMillis()
)
