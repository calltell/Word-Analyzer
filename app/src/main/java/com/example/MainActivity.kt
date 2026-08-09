package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.analyzer.WordItem
import com.example.exporter.Exporter
import com.example.ui.AnalyzerViewModel
import com.example.ui.ExportFormat
import com.example.ui.NavTab
import com.example.ui.components.BottomNav
import com.example.ui.components.ExportModal
import com.example.ui.components.FilterSheet
import com.example.ui.components.PasteTextDialog
import com.example.ui.components.TopBar
import com.example.ui.components.WordDetailModal
import com.example.ui.screens.FlashcardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SavedWordsScreen
import com.example.ui.screens.StopWordsScreen
import com.example.ui.theme.WordAnalyzerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WordAnalyzerTheme {
                val viewModel: AnalyzerViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                val snackbarHostState = remember { SnackbarHostState() }

                var isPasteModalOpen by remember { mutableStateOf(false) }
                var isFilterModalOpen by remember { mutableStateOf(false) }
                var isExportModalOpen by remember { mutableStateOf(false) }

                fun handleUri(uri: Uri, isMerge: Boolean = false) {
                    try {
                        var fileName = "Document"
                        var fileExtension = "TXT"

                        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (nameIndex != -1 && cursor.moveToFirst()) {
                                fileName = cursor.getString(nameIndex)
                                val ext = fileName.substringAfterLast('.', "")
                                if (ext.isNotBlank()) fileExtension = ext.uppercase()
                            }
                        }

                        val inputStream = context.contentResolver.openInputStream(uri)
                        if (inputStream != null) {
                            if (isMerge) {
                                viewModel.mergeFile(inputStream, fileName, fileExtension)
                            } else {
                                viewModel.processFile(inputStream, fileName, fileExtension)
                            }
                        } else {
                            Toast.makeText(context, "امکان خواندن فایل وجود ندارد", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "خطا در بارگذاری فایل: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }

                val openDocumentLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument()
                ) { uri: Uri? ->
                    uri?.let { handleUri(it, isMerge = false) }
                }

                val mergeDocumentLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument()
                ) { uri: Uri? ->
                    uri?.let { handleUri(it, isMerge = true) }
                }

                val mimeTypes = arrayOf(
                    "application/pdf",
                    "application/epub+zip",
                    "text/plain",
                    "application/octet-stream"
                )

                val handleExport: (ExportFormat, Boolean) -> Unit = { format, includeContext ->
                    scope.launch(Dispatchers.IO) {
                        val wordsToExport = if (uiState.filteredWordList.isNotEmpty()) {
                            uiState.filteredWordList
                        } else {
                            uiState.wordList
                        }

                        val title = uiState.documentTitle.ifBlank { "Analysis_Result" }

                        val file: File? = when (format) {
                            ExportFormat.ANKI_CSV -> {
                                val content = Exporter.generateAnkiCsv(wordsToExport, includeContext)
                                val f = File(cacheDir, "${title}_anki.csv")
                                FileOutputStream(f).use { it.write(content.toByteArray(Charsets.UTF_8)) }
                                f
                            }
                            ExportFormat.TXT_REPORT -> {
                                val content = Exporter.generateTxtReport(title, uiState.totalWords, uiState.uniqueWords, wordsToExport)
                                val f = File(cacheDir, "${title}_report.txt")
                                FileOutputStream(f).use { it.write(content.toByteArray(Charsets.UTF_8)) }
                                f
                            }
                            ExportFormat.JSON_DATA -> {
                                val content = Exporter.generateJsonData(title, uiState.totalWords, uiState.uniqueWords, wordsToExport)
                                val f = File(cacheDir, "${title}_data.json")
                                FileOutputStream(f).use { it.write(content.toByteArray(Charsets.UTF_8)) }
                                f
                            }
                            ExportFormat.PDF_DOCUMENT -> {
                                Exporter.generatePdfFile(context, title, uiState.totalWords, uiState.uniqueWords, wordsToExport)
                            }
                        }

                        if (file != null && file.exists()) {
                            withContext(Dispatchers.Main) {
                                isExportModalOpen = false
                                shareFile(file, format)
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "خطا در تولید فایل خروجی", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                Scaffold(
                    topBar = {
                        TopBar(
                            currentTab = uiState.currentTab,
                            searchQuery = uiState.searchQuery,
                            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                            onOpenFilterSheet = { isFilterModalOpen = true },
                            onOpenExportModal = { isExportModalOpen = true },
                            onOpenPasteDialog = { isPasteModalOpen = true },
                            onBatchTranslate = { viewModel.batchTranslateVisibleWords() },
                            onSelectFileToMerge = { mergeDocumentLauncher.launch(mimeTypes) }
                        )
                    },
                    bottomBar = {
                        BottomNav(
                            currentTab = uiState.currentTab,
                            onTabSelected = { viewModel.selectTab(it) }
                        )
                    },
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        when (uiState.currentTab) {
                            NavTab.ANALYZER -> {
                                HomeScreen(
                                    uiState = uiState,
                                    onWordClick = { viewModel.openWordDetail(it) },
                                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                                    onToggleLearned = { viewModel.toggleLearned(it) },
                                    onQuickTranslate = { viewModel.translateWord(it) },
                                    onOpenImportFile = { openDocumentLauncher.launch(mimeTypes) },
                                    onOpenPasteDialog = { isPasteModalOpen = true }
                                )
                            }
                            NavTab.SAVED_WORDS -> {
                                SavedWordsScreen(
                                    savedWords = uiState.savedWords,
                                    onWordClick = { viewModel.openWordDetail(it) },
                                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                                    onToggleLearned = { viewModel.toggleLearned(it) },
                                    onQuickTranslate = { viewModel.translateWord(it) }
                                )
                            }
                            NavTab.FLASHCARDS -> {
                                FlashcardScreen(
                                    wordList = if (uiState.savedWords.isNotEmpty()) {
                                        uiState.savedWords.map { entity ->
                                            WordItem(
                                                word = entity.word,
                                                lemma = entity.lemma ?: entity.word,
                                                frequency = entity.frequency,
                                                translation = entity.translation,
                                                phonetic = entity.phonetic,
                                                partOfSpeech = entity.partOfSpeech,
                                                definition = entity.definition,
                                                contextSentence = entity.contextSentence ?: "",
                                                cefrLevel = entity.cefrLevel ?: "B1",
                                                isLearned = entity.isLearned,
                                                isFavorite = entity.isFavorite
                                            )
                                        }
                                    } else {
                                        uiState.wordList
                                    },
                                    onToggleLearned = { viewModel.toggleLearned(it) }
                                )
                            }
                            NavTab.HISTORY -> {
                                HistoryScreen(
                                    historyList = uiState.documentHistory,
                                    onDeleteDocument = { viewModel.deleteDocumentHistory(it) }
                                )
                            }
                            NavTab.STOP_WORDS -> {
                                StopWordsScreen(
                                    customStopWords = uiState.customStopWords,
                                    onAddStopWord = { viewModel.addStopWord(it) },
                                    onRemoveStopWord = { viewModel.removeStopWord(it) }
                                )
                            }
                        }

                        uiState.selectedWordDetail?.let { word ->
                            WordDetailModal(
                                item = word,
                                isTranslating = uiState.isTranslating,
                                onDismiss = { viewModel.closeWordDetail() },
                                onReTranslate = { viewModel.translateWord(word) },
                                onToggleFavorite = { viewModel.toggleFavorite(word) },
                                onToggleLearned = { viewModel.toggleLearned(word) }
                            )
                        }

                        if (isFilterModalOpen) {
                            FilterSheet(
                                minFrequency = uiState.minFrequencyFilter,
                                minLength = uiState.minLengthFilter,
                                selectedCefr = uiState.selectedCefrFilter,
                                removeStopWords = uiState.removeStopWords,
                                hideLearnedWords = uiState.hideLearnedWords,
                                sortOption = uiState.sortOption,
                                onMinFrequencyChange = { viewModel.updateMinFrequency(it) },
                                onMinLengthChange = { viewModel.updateMinLength(it) },
                                onCefrSelected = { viewModel.updateCefrFilter(it) },
                                onRemoveStopWordsChange = { viewModel.updateRemoveStopWords(it) },
                                onHideLearnedWordsChange = { viewModel.updateHideLearnedWords(it) },
                                onSortOptionSelected = { viewModel.updateSortOption(it) },
                                onDismiss = { isFilterModalOpen = false }
                            )
                        }

                        if (isExportModalOpen) {
                            ExportModal(
                                onDismiss = { isExportModalOpen = false },
                                onExportSelected = { format ->
                                    handleExport(format, true)
                                }
                            )
                        }

                        if (isPasteModalOpen) {
                            PasteTextDialog(
                                onConfirm = { title, content ->
                                    viewModel.processPastedText(content, title)
                                    isPasteModalOpen = false
                                },
                                onDismiss = { isPasteModalOpen = false }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun shareFile(file: File, format: ExportFormat) {
        val uri: Uri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            file
        )

        val mimeType = when (format) {
            ExportFormat.ANKI_CSV -> "text/csv"
            ExportFormat.TXT_REPORT -> "text/plain"
            ExportFormat.JSON_DATA -> "application/json"
            ExportFormat.PDF_DOCUMENT -> "application/pdf"
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری فایل خروجی"))
    }
}
