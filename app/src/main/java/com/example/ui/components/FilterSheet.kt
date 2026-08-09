package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.SortOption

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterSheet(
    minFrequency: Int,
    minLength: Int,
    selectedCefr: String,
    removeStopWords: Boolean,
    hideLearnedWords: Boolean,
    sortOption: SortOption,
    onMinFrequencyChange: (Int) -> Unit,
    onMinLengthChange: (Int) -> Unit,
    onCefrSelected: (String) -> Unit,
    onRemoveStopWordsChange: (Boolean) -> Unit,
    onHideLearnedWordsChange: (Boolean) -> Unit,
    onSortOptionSelected: (SortOption) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
                .testTag("filter_sheet")
        ) {
            Text(
                text = "فیلترها و مرتب‌سازی (Filters & Sorting)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sorting Options
            Text(
                text = "مرتب‌سازی بر اساس (Sort By):",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = sortOption == SortOption.FREQ_DESC,
                    onClick = { onSortOptionSelected(SortOption.FREQ_DESC) },
                    label = { Text("بیشترین تکرار") }
                )
                FilterChip(
                    selected = sortOption == SortOption.FREQ_ASC,
                    onClick = { onSortOptionSelected(SortOption.FREQ_ASC) },
                    label = { Text("کمترین تکرار") }
                )
                FilterChip(
                    selected = sortOption == SortOption.ALPHA_ASC,
                    onClick = { onSortOptionSelected(SortOption.ALPHA_ASC) },
                    label = { Text("الفبایی A-Z") }
                )
                FilterChip(
                    selected = sortOption == SortOption.CEFR_LEVEL,
                    onClick = { onSortOptionSelected(SortOption.CEFR_LEVEL) },
                    label = { Text("سطح زبان (CEFR)") }
                )
                FilterChip(
                    selected = sortOption == SortOption.LENGTH_DESC,
                    onClick = { onSortOptionSelected(SortOption.LENGTH_DESC) },
                    label = { Text("طول کلمه") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CEFR Level Filter
            Text(
                text = "سطح دشواری لغات (CEFR Level):",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("ALL", "A1", "A2", "B1", "B2", "C1", "C2").forEach { level ->
                    FilterChip(
                        selected = selectedCefr.equals(level, ignoreCase = true),
                        onClick = { onCefrSelected(level) },
                        label = { Text(if (level == "ALL") "همه سطوح" else level) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Minimum Frequency Slider
            Text(
                text = "حداقل تکرار کلمه در متن: $minFrequency بار",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = minFrequency.toFloat(),
                onValueChange = { onMinFrequencyChange(it.toInt()) },
                valueRange = 1f..20f,
                steps = 19,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Minimum Length Slider
            Text(
                text = "حداقل طول کلمه: $minLength حرف",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = minLength.toFloat(),
                onValueChange = { onMinLengthChange(it.toInt()) },
                valueRange = 2f..10f,
                steps = 8,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Switches
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "حذف کلمات بسیار عمومی (Stop Words)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Switch(
                    checked = removeStopWords,
                    onCheckedChange = onRemoveStopWordsChange
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "مخفی کردن لغات یادگرفته شده",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Switch(
                    checked = hideLearnedWords,
                    onCheckedChange = onHideLearnedWordsChange
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
