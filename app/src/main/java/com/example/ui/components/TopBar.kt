package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.NavTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    currentTab: NavTab,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onOpenFilterSheet: () -> Unit,
    onOpenExportModal: () -> Unit,
    onOpenPasteDialog: () -> Unit,
    onBatchTranslate: () -> Unit,
    onSelectFileToMerge: () -> Unit
) {
    var isSearchActive by remember { mutableStateOf(false) }

    Surface(
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        TopAppBar(
            title = {
                if (isSearchActive) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("جستجو لغات / Search words...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp)
                            .testTag("top_bar_search_input")
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when (currentTab) {
                                NavTab.ANALYZER -> "تحلیلگر لغات (Word Analyzer)"
                                NavTab.SAVED_WORDS -> "لغات ذخیره‌شده (Saved Words)"
                                NavTab.FLASHCARDS -> "تمرین فلش‌کارت (Flashcards)"
                                NavTab.HISTORY -> "تاریخچه کتب (Document History)"
                                NavTab.STOP_WORDS -> "کلمات نادیده‌گرفته (Stop Words)"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
            actions = {
                if (currentTab == NavTab.ANALYZER) {
                    IconButton(
                        onClick = { isSearchActive = !isSearchActive },
                        modifier = Modifier.testTag("search_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }

                    IconButton(
                        onClick = onOpenFilterSheet,
                        modifier = Modifier.testTag("filter_sheet_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filters"
                        )
                    }

                    IconButton(
                        onClick = onBatchTranslate,
                        modifier = Modifier.testTag("batch_translate_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Translate List",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = onSelectFileToMerge,
                        modifier = Modifier.testTag("merge_file_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MergeType,
                            contentDescription = "Merge File"
                        )
                    }

                    IconButton(
                        onClick = onOpenExportModal,
                        modifier = Modifier.testTag("export_modal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Export"
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}
