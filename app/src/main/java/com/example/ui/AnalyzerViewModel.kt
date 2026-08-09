package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.analyzer.FileParsers
import com.example.analyzer.TextAnalyzer
import com.example.analyzer.WordItem
import com.example.data.db.AppDatabase
import com.example.data.db.DocumentEntity
import com.example.data.db.WordEntity
import com.example.data.repository.WordRepository
import com.example.dictionary.GeminiTranslator
import com.example.exporter.Exporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

enum class NavTab {
    ANALYZER, SAVED_WORDS, FLASHCARDS, HISTORY, STOP_WORDS
}

enum class SortOption {
    FREQ_DESC, FREQ_ASC, ALPHA_ASC, ALPHA_DESC, CEFR_LEVEL, LENGTH_DESC
}

enum class ExportFormat {
    ANKI_CSV, TXT_REPORT, JSON_DATA, PDF_DOCUMENT
}

data class AnalyzerUiState(
    val currentTab: NavTab = NavTab.ANALYZER,
    val documentTitle: String = "",
    val documentFileType: String = "",
    val totalWords: Int = 0,
    val uniqueWords: Int = 0,
    val wordList: List<WordItem> = emptyList(),
    val filteredWordList: List<WordItem> = emptyList(),
    val isAnalyzing: Boolean = false,
    val analysisProgressText: String = "",
    val searchQuery: String = "",
    val minFrequencyFilter: Int = 1,
    val minLengthFilter: Int = 2,
    val maxLengthFilter: Int = 25,
    val selectedCefrFilter: String = "ALL",
    val removeStopWords: Boolean = true,
    val hideLearnedWords: Boolean = false,
    val sortOption: SortOption = SortOption.FREQ_DESC,
    val selectedWordDetail: WordItem? = null,
    val isTranslating: Boolean = false,
    val savedWords: List<WordEntity> = emptyList(),
    val documentHistory: List<DocumentEntity> = emptyList(),
    val customStopWords: Set<String> = emptySet(),
    val rawExtractedText: String = ""
)

class AnalyzerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = WordRepository(db.wordDao(), db.documentDao(), db.stopWordDao())

    private val _uiState = MutableStateFlow(AnalyzerUiState())
    val uiState: StateFlow<AnalyzerUiState> = _uiState.asStateFlow()

    init {
        // Observe DB flows
        viewModelScope.launch {
            repository.allSavedWords.collectLatest { list ->
                _uiState.value = _uiState.value.copy(savedWords = list)
                updateWordListWithSavedStatus()
            }
        }

        viewModelScope.launch {
            repository.allDocuments.collectLatest { docs ->
                _uiState.value = _uiState.value.copy(documentHistory = docs)
            }
        }

        viewModelScope.launch {
            repository.customStopWords.collectLatest { stops ->
                val set = stops.map { it.word }.toSet()
                _uiState.value = _uiState.value.copy(customStopWords = set)
            }
        }

        // App starts clean without default sample words
    }

    private fun loadSampleText() {
        // Disabled sample text loading per user request
    }

    fun selectTab(tab: NavTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun processFile(inputStream: InputStream, fileName: String, fileType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(
                isAnalyzing = true,
                analysisProgressText = "Reading $fileName..."
            )

            try {
                val text = when (fileType.uppercase()) {
                    "PDF" -> FileParsers.parsePdf(inputStream)
                    "EPUB" -> FileParsers.parseEpub(inputStream)
                    else -> FileParsers.parseTxt(inputStream)
                }

                processRawText(text, fileName, fileType, saveHistory = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    analysisProgressText = "Error reading file: ${e.localizedMessage}"
                )
            }
        }
    }

    fun mergeFile(inputStream: InputStream, fileName: String, fileType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(
                isAnalyzing = true,
                analysisProgressText = "Merging $fileName..."
            )

            try {
                val newText = when (fileType.uppercase()) {
                    "PDF" -> FileParsers.parsePdf(inputStream)
                    "EPUB" -> FileParsers.parseEpub(inputStream)
                    else -> FileParsers.parseTxt(inputStream)
                }

                val currentList = _uiState.value.wordList
                val currentText = _uiState.value.rawExtractedText
                val combinedText = "$currentText\n\n$newText"

                val newResult = TextAnalyzer.analyzeText(
                    text = newText,
                    customStopWords = _uiState.value.customStopWords,
                    filterMinFrequency = 1,
                    filterMinLength = _uiState.value.minLengthFilter,
                    filterMaxLength = _uiState.value.maxLengthFilter,
                    removeStopWords = _uiState.value.removeStopWords
                )

                val mergedList = TextAnalyzer.mergeWordLists(currentList, newResult.wordList)

                val mergedTitle = "${_uiState.value.documentTitle} + $fileName"
                val newTotal = _uiState.value.totalWords + newResult.totalWords

                _uiState.value = _uiState.value.copy(
                    documentTitle = mergedTitle,
                    totalWords = newTotal,
                    uniqueWords = mergedList.size,
                    wordList = mergedList,
                    rawExtractedText = combinedText,
                    isAnalyzing = false,
                    analysisProgressText = ""
                )

                applyFiltersAndSorting()

                repository.saveDocument(
                    DocumentEntity(
                        title = mergedTitle,
                        fileType = "MERGED",
                        totalWords = newTotal,
                        uniqueWords = mergedList.size,
                        contentSnippet = combinedText.take(200)
                    )
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    analysisProgressText = "Error merging file: ${e.localizedMessage}"
                )
            }
        }
    }

    fun processPastedText(text: String, title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(
                isAnalyzing = true,
                analysisProgressText = "Analyzing text..."
            )
            processRawText(text, if (title.isNotBlank()) title else "Pasted Text", "TEXT", saveHistory = true)
        }
    }

    fun processRawText(
        text: String,
        title: String,
        fileType: String,
        saveHistory: Boolean
    ) {
        val result = TextAnalyzer.analyzeText(
            text = text,
            customStopWords = _uiState.value.customStopWords,
            filterMinFrequency = 1,
            filterMinLength = _uiState.value.minLengthFilter,
            filterMaxLength = _uiState.value.maxLengthFilter,
            removeStopWords = _uiState.value.removeStopWords
        )

        _uiState.value = _uiState.value.copy(
            documentTitle = title,
            documentFileType = fileType,
            totalWords = result.totalWords,
            uniqueWords = result.uniqueWords,
            wordList = result.wordList,
            rawExtractedText = text,
            isAnalyzing = false,
            analysisProgressText = ""
        )

        updateWordListWithSavedStatus()
        applyFiltersAndSorting()

        if (saveHistory && result.totalWords > 0) {
            viewModelScope.launch {
                repository.saveDocument(
                    DocumentEntity(
                        title = title,
                        fileType = fileType,
                        totalWords = result.totalWords,
                        uniqueWords = result.uniqueWords,
                        contentSnippet = text.take(200)
                    )
                )
            }
        }
    }

    private fun updateWordListWithSavedStatus() {
        val savedMap = _uiState.value.savedWords.associateBy { it.word.lowercase() }
        val updatedList = _uiState.value.wordList.map { item ->
            val saved = savedMap[item.word.lowercase()]
            if (saved != null) {
                item.copy(
                    translation = saved.translation ?: item.translation,
                    phonetic = saved.phonetic ?: item.phonetic,
                    partOfSpeech = saved.partOfSpeech ?: item.partOfSpeech,
                    definition = saved.definition ?: item.definition,
                    isLearned = saved.isLearned,
                    isFavorite = saved.isFavorite
                )
            } else item
        }
        _uiState.value = _uiState.value.copy(wordList = updatedList)
        applyFiltersAndSorting()
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFiltersAndSorting()
    }

    fun updateMinFrequency(minFreq: Int) {
        _uiState.value = _uiState.value.copy(minFrequencyFilter = minFreq)
        applyFiltersAndSorting()
    }

    fun updateMinLength(minLen: Int) {
        _uiState.value = _uiState.value.copy(minLengthFilter = minLen)
        reAnalyzeCurrentText()
    }

    fun updateCefrFilter(cefr: String) {
        _uiState.value = _uiState.value.copy(selectedCefrFilter = cefr)
        applyFiltersAndSorting()
    }

    fun updateRemoveStopWords(remove: Boolean) {
        _uiState.value = _uiState.value.copy(removeStopWords = remove)
        reAnalyzeCurrentText()
    }

    fun updateHideLearnedWords(hide: Boolean) {
        _uiState.value = _uiState.value.copy(hideLearnedWords = hide)
        applyFiltersAndSorting()
    }

    fun updateSortOption(sort: SortOption) {
        _uiState.value = _uiState.value.copy(sortOption = sort)
        applyFiltersAndSorting()
    }

    private fun reAnalyzeCurrentText() {
        if (_uiState.value.rawExtractedText.isNotBlank()) {
            val title = _uiState.value.documentTitle
            val fileType = _uiState.value.documentFileType
            val text = _uiState.value.rawExtractedText
            processRawText(text, title, fileType, saveHistory = false)
        }
    }

    private fun applyFiltersAndSorting() {
        val currentState = _uiState.value
        val query = currentState.searchQuery.lowercase().trim()
        val minFreq = currentState.minFrequencyFilter
        val cefr = currentState.selectedCefrFilter
        val hideLearned = currentState.hideLearnedWords

        var filtered = currentState.wordList.filter { item ->
            val matchesQuery = query.isBlank() || item.word.lowercase().contains(query) || (item.translation?.contains(query) == true)
            val matchesFreq = item.frequency >= minFreq
            val matchesCefr = cefr == "ALL" || item.cefrLevel.equals(cefr, ignoreCase = true)
            val matchesLearned = !hideLearned || !item.isLearned
            matchesQuery && matchesFreq && matchesCefr && matchesLearned
        }

        filtered = when (currentState.sortOption) {
            SortOption.FREQ_DESC -> filtered.sortedByDescending { it.frequency }
            SortOption.FREQ_ASC -> filtered.sortedBy { it.frequency }
            SortOption.ALPHA_ASC -> filtered.sortedBy { it.word.lowercase() }
            SortOption.ALPHA_DESC -> filtered.sortedByDescending { it.word.lowercase() }
            SortOption.CEFR_LEVEL -> filtered.sortedBy { cefrRank(it.cefrLevel) }
            SortOption.LENGTH_DESC -> filtered.sortedByDescending { it.word.length }
        }

        _uiState.value = _uiState.value.copy(filteredWordList = filtered)
    }

    private fun cefrRank(cefr: String): Int = when (cefr.uppercase()) {
        "A1" -> 1; "A2" -> 2; "B1" -> 3; "B2" -> 4; "C1" -> 5; "C2" -> 6; else -> 3
    }

    fun openWordDetail(item: WordItem) {
        _uiState.value = _uiState.value.copy(selectedWordDetail = item)
        if (item.translation.isNull_or_blank()) {
            translateWord(item)
        }
    }

    fun closeWordDetail() {
        _uiState.value = _uiState.value.copy(selectedWordDetail = null)
    }

    fun translateWord(item: WordItem) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTranslating = true)
            val res = GeminiTranslator.translateWord(item.word, item.contextSentence)

            val updated = item.copy(
                translation = res.translation,
                phonetic = res.phonetic,
                partOfSpeech = res.partOfSpeech,
                definition = res.definition
            )

            _uiState.value = _uiState.value.copy(
                selectedWordDetail = updated,
                isTranslating = false
            )

            // Update in word list
            val newWordList = _uiState.value.wordList.map {
                if (it.word.equals(item.word, ignoreCase = true)) updated else it
            }
            _uiState.value = _uiState.value.copy(wordList = newWordList)
            applyFiltersAndSorting()

            // Save translation in DB
            repository.saveWord(
                WordEntity(
                    word = updated.word,
                    lemma = updated.lemma,
                    translation = updated.translation,
                    phonetic = updated.phonetic,
                    partOfSpeech = updated.partOfSpeech,
                    definition = updated.definition,
                    contextSentence = updated.contextSentence,
                    cefrLevel = updated.cefrLevel,
                    frequency = updated.frequency,
                    isLearned = updated.isLearned,
                    isFavorite = updated.isFavorite
                )
            )
        }
    }

    fun batchTranslateVisibleWords(limit: Int = 15) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTranslating = true)
            val targetList = _uiState.value.filteredWordList.take(limit)

            for (item in targetList) {
                if (item.translation.isNull_or_blank()) {
                    val res = GeminiTranslator.translateWord(item.word, item.contextSentence)
                    val updated = item.copy(
                        translation = res.translation,
                        phonetic = res.phonetic,
                        partOfSpeech = res.partOfSpeech,
                        definition = res.definition
                    )

                    repository.saveWord(
                        WordEntity(
                            word = updated.word,
                            lemma = updated.lemma,
                            translation = updated.translation,
                            phonetic = updated.phonetic,
                            partOfSpeech = updated.partOfSpeech,
                            definition = updated.definition,
                            contextSentence = updated.contextSentence,
                            cefrLevel = updated.cefrLevel,
                            frequency = updated.frequency,
                            isLearned = updated.isLearned,
                            isFavorite = updated.isFavorite
                        )
                    )
                }
            }

            _uiState.value = _uiState.value.copy(isTranslating = false)
        }
    }

    fun toggleLearned(wordItem: WordItem) {
        viewModelScope.launch {
            val newStatus = !wordItem.isLearned
            repository.toggleLearned(wordItem.word, newStatus)

            // Update memory list
            val updatedList = _uiState.value.wordList.map {
                if (it.word.equals(wordItem.word, ignoreCase = true)) it.copy(isLearned = newStatus) else it
            }
            _uiState.value = _uiState.value.copy(
                wordList = updatedList,
                selectedWordDetail = _uiState.value.selectedWordDetail?.let {
                    if (it.word.equals(wordItem.word, ignoreCase = true)) it.copy(isLearned = newStatus) else it
                }
            )
            applyFiltersAndSorting()
        }
    }

    fun toggleFavorite(wordItem: WordItem) {
        viewModelScope.launch {
            val newStatus = !wordItem.isFavorite
            repository.toggleFavorite(wordItem.word, newStatus)

            val updatedList = _uiState.value.wordList.map {
                if (it.word.equals(wordItem.word, ignoreCase = true)) it.copy(isFavorite = newStatus) else it
            }

            _uiState.value = _uiState.value.copy(
                wordList = updatedList,
                selectedWordDetail = _uiState.value.selectedWordDetail?.let {
                    if (it.word.equals(wordItem.word, ignoreCase = true)) it.copy(isFavorite = newStatus) else it
                }
            )

            // If not in DB yet, save
            repository.saveWord(
                WordEntity(
                    word = wordItem.word,
                    lemma = wordItem.lemma,
                    translation = wordItem.translation,
                    phonetic = wordItem.phonetic,
                    partOfSpeech = wordItem.partOfSpeech,
                    definition = wordItem.definition,
                    contextSentence = wordItem.contextSentence,
                    cefrLevel = wordItem.cefrLevel,
                    frequency = wordItem.frequency,
                    isLearned = wordItem.isLearned,
                    isFavorite = newStatus
                )
            )

            applyFiltersAndSorting()
        }
    }

    fun addStopWord(word: String) {
        viewModelScope.launch {
            repository.addStopWord(word)
            reAnalyzeCurrentText()
        }
    }

    fun removeStopWord(word: String) {
        viewModelScope.launch {
            repository.removeStopWord(word)
            reAnalyzeCurrentText()
        }
    }

    fun deleteDocumentHistory(id: Long) {
        viewModelScope.launch {
            repository.deleteDocument(id)
        }
    }

    fun exportData(context: Context, format: ExportFormat): File? {
        val title = if (_uiState.value.documentTitle.isNotBlank()) _uiState.value.documentTitle else "Word_List"
        val words = _uiState.value.filteredWordList
        val total = _uiState.value.totalWords
        val unique = _uiState.value.uniqueWords

        val fileName = "${title.replace(Regex("""[^a-zA-Z0-9]"""), "_")}"

        return when (format) {
            ExportFormat.ANKI_CSV -> {
                val csvContent = Exporter.generateAnkiCsv(words)
                val file = File(context.cacheDir, "${fileName}_anki.csv")
                file.writeText(csvContent, Charsets.UTF_8)
                file
            }
            ExportFormat.TXT_REPORT -> {
                val txtContent = Exporter.generateTxtReport(title, total, unique, words)
                val file = File(context.cacheDir, "${fileName}_report.txt")
                file.writeText(txtContent, Charsets.UTF_8)
                file
            }
            ExportFormat.JSON_DATA -> {
                val jsonContent = Exporter.generateJsonData(title, total, unique, words)
                val file = File(context.cacheDir, "${fileName}_data.json")
                file.writeText(jsonContent, Charsets.UTF_8)
                file
            }
            ExportFormat.PDF_DOCUMENT -> {
                Exporter.generatePdfFile(context, title, total, unique, words)
            }
        }
    }

    private fun String?.isNull_or_blank(): Boolean = this == null || this.isBlank()
}
