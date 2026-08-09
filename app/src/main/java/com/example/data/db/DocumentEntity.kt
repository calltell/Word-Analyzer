package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analyzed_documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val fileType: String, // "PDF", "EPUB", "TXT", "TEXT"
    val totalWords: Int,
    val uniqueWords: Int,
    val contentSnippet: String,
    val dateAnalyzed: Long = System.currentTimeMillis()
)
