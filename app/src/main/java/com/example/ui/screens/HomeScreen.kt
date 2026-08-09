package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.analyzer.WordItem
import com.example.ui.AnalyzerUiState
import com.example.ui.components.WordCard

@Composable
fun HomeScreen(
    uiState: AnalyzerUiState,
    onWordClick: (WordItem) -> Unit,
    onToggleFavorite: (WordItem) -> Unit,
    onToggleLearned: (WordItem) -> Unit,
    onQuickTranslate: (WordItem) -> Unit,
    onOpenImportFile: () -> Unit,
    onOpenPasteDialog: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Document Header Overview
            DocumentHeaderCard(
                title = uiState.documentTitle,
                fileType = uiState.documentFileType,
                totalWords = uiState.totalWords,
                uniqueWords = uiState.uniqueWords,
                displayedCount = uiState.filteredWordList.size,
                onImportFile = onOpenImportFile,
                onPasteText = onOpenPasteDialog
            )

            // Analysis Progress Indicator
            if (uiState.isAnalyzing) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.analysisProgressText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Word List or Empty State
            if (uiState.filteredWordList.isEmpty() && !uiState.isAnalyzing) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "هیچ کلمه‌ای یافت نشد!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "فایل PDF/EPUB/TXT جدیدی وارد کنید یا فیلترهای جستجو را بازنشانی نمایید.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(onClick = onOpenImportFile) {
                            Icon(imageVector = Icons.Default.UploadFile, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("انتخاب فایل جدید")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp)
                        .testTag("word_list_lazy_column")
                ) {
                    items(
                        items = uiState.filteredWordList,
                        key = { it.word }
                    ) { item ->
                        WordCard(
                            item = item,
                            onClick = { onWordClick(item) },
                            onToggleFavorite = { onToggleFavorite(item) },
                            onToggleLearned = { onToggleLearned(item) },
                            onQuickTranslate = { onQuickTranslate(item) }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Import
        FloatingActionButton(
            onClick = onOpenImportFile,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 20.dp)
                .testTag("fab_import_file")
        ) {
            Icon(imageVector = Icons.Default.UploadFile, contentDescription = "Import File")
        }
    }
}

@Composable
private fun DocumentHeaderCard(
    title: String,
    fileType: String,
    totalWords: Int,
    uniqueWords: Int,
    displayedCount: Int,
    onImportFile: () -> Unit,
    onPasteText: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (title.isNotBlank()) title else "عنوان مدرک (Document Title)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "فرمت: ${if (fileType.isNotBlank()) fileType else "TXT/PDF/EPUB"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }

                Row {
                    OutlinedButton(
                        onClick = onPasteText,
                        modifier = Modifier.testTag("paste_text_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("پیست متن")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onImportFile,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("import_file_header_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("بازکردن فایل")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Statistics Bar
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(8.dp)
            ) {
                StatItem(label = "کل کلمات", value = "$totalWords")
                StatItem(label = "کلمات یکتا", value = "$uniqueWords")
                StatItem(label = "کلمات نمایش‌داده", value = "$displayedCount")
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
