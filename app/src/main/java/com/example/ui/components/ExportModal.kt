package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.ExportFormat

@Composable
fun ExportModal(
    onDismiss: () -> Unit,
    onExportSelected: (ExportFormat) -> Unit
) {
    var selectedFormat by remember { mutableStateOf(ExportFormat.ANKI_CSV) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("export_modal")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "خروجی لغات (Export Vocabulary)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "فرمت مورد نظر خود را برای استخراج لیست لغات انتخاب کنید:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                ExportOptionCard(
                    title = "Anki CSV (فلش‌کارت انکی)",
                    description = "شامل کلمه، تلفظ، ترجمه فارسی، تعریف و جمله نمونه آماده وارد کردن به Anki",
                    icon = Icons.Default.Style,
                    isSelected = selectedFormat == ExportFormat.ANKI_CSV,
                    onClick = { selectedFormat = ExportFormat.ANKI_CSV }
                )

                Spacer(modifier = Modifier.height(8.dp))

                ExportOptionCard(
                    title = "گزارش متنی (TXT Report)",
                    description = "فایل متنی مرتب شامل جدول رتبه‌بندی کلمات و آمار",
                    icon = Icons.Default.Description,
                    isSelected = selectedFormat == ExportFormat.TXT_REPORT,
                    onClick = { selectedFormat = ExportFormat.TXT_REPORT }
                )

                Spacer(modifier = Modifier.height(8.dp))

                ExportOptionCard(
                    title = "سند قابل چاپ (PDF Report)",
                    description = "فایل PDF مرتب با جدول و آماده پرینت",
                    icon = Icons.Default.PictureAsPdf,
                    isSelected = selectedFormat == ExportFormat.PDF_DOCUMENT,
                    onClick = { selectedFormat = ExportFormat.PDF_DOCUMENT }
                )

                Spacer(modifier = Modifier.height(8.dp))

                ExportOptionCard(
                    title = "داده‌های کامل (JSON Data)",
                    description = "فرمت ساختاریافته کامل برای برنامه‌نویسان و تحلیل داده",
                    icon = Icons.Default.Code,
                    isSelected = selectedFormat == ExportFormat.JSON_DATA,
                    onClick = { selectedFormat = ExportFormat.JSON_DATA }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("انصراف")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onExportSelected(selectedFormat) },
                        modifier = Modifier.testTag("confirm_export_button")
                    ) {
                        Text("ایجاد خروجی (Export)")
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
