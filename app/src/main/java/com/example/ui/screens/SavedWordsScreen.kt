package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.analyzer.WordItem
import com.example.data.db.WordEntity
import com.example.ui.components.WordCard

@Composable
fun SavedWordsScreen(
    savedWords: List<WordEntity>,
    onWordClick: (WordItem) -> Unit,
    onToggleFavorite: (WordItem) -> Unit,
    onToggleLearned: (WordItem) -> Unit,
    onQuickTranslate: (WordItem) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, FAVORITES, LEARNED

    val filteredList = when (selectedFilter) {
        "FAVORITES" -> savedWords.filter { it.isFavorite }
        "LEARNED" -> savedWords.filter { it.isLearned }
        else -> savedWords
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("saved_words_screen")
    ) {
        // Filter Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            FilterChip(
                selected = selectedFilter == "ALL",
                onClick = { selectedFilter = "ALL" },
                label = { Text("همه ذخیره‌ها (${savedWords.size})") },
                modifier = Modifier.padding(end = 8.dp)
            )
            FilterChip(
                selected = selectedFilter == "FAVORITES",
                onClick = { selectedFilter = "FAVORITES" },
                label = { Text("نشان‌شده‌ها (${savedWords.count { it.isFavorite }})") },
                modifier = Modifier.padding(end = 8.dp)
            )
            FilterChip(
                selected = selectedFilter == "LEARNED",
                onClick = { selectedFilter = "LEARNED" },
                label = { Text("یادگرفته‌ها (${savedWords.count { it.isLearned }})") }
            )
        }

        if (filteredList.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "هیچ کلمه ذخیره‌شده‌ای در این بخش نیست",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "با لمس آیکون ستاره در لیست کلمات، لغات مورد نظر خود را ذخیره کنید.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp)
            ) {
                items(
                    items = filteredList,
                    key = { it.word }
                ) { entity ->
                    val wordItem = WordItem(
                        rank = 0,
                        word = entity.word,
                        lemma = entity.lemma ?: entity.word,
                        frequency = entity.frequency,
                        percentage = 0f,
                        contextSentence = entity.contextSentence ?: "",
                        cefrLevel = entity.cefrLevel ?: "B1",
                        translation = entity.translation,
                        phonetic = entity.phonetic,
                        partOfSpeech = entity.partOfSpeech,
                        definition = entity.definition,
                        isLearned = entity.isLearned,
                        isFavorite = entity.isFavorite
                    )

                    WordCard(
                        item = wordItem,
                        onClick = { onWordClick(wordItem) },
                        onToggleFavorite = { onToggleFavorite(wordItem) },
                        onToggleLearned = { onToggleLearned(wordItem) },
                        onQuickTranslate = { onQuickTranslate(wordItem) }
                    )
                }
            }
        }
    }
}
