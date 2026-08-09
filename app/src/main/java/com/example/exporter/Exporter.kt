package com.example.exporter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.analyzer.WordItem
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object Exporter {

    fun generateAnkiCsv(
        words: List<WordItem>,
        includeContext: Boolean = true
    ): String {
        val sb = StringBuilder()
        // UTF-8 BOM for proper Unicode rendering in Anki & Excel
        sb.append("\uFEFF")
        sb.append("Word,Phonetic,Persian Translation,Definition,Context Sentence,Frequency,CEFR Level\n")

        for (w in words) {
            val word = escapeCsv(w.word)
            val phonetic = escapeCsv(w.phonetic ?: "")
            val translation = escapeCsv(w.translation ?: "")
            val definition = escapeCsv(w.definition ?: "")
            val context = if (includeContext) escapeCsv(w.contextSentence) else ""
            val freq = w.frequency
            val cefr = w.cefrLevel

            sb.append("$word,$phonetic,$translation,$definition,$context,$freq,$cefr\n")
        }

        return sb.toString()
    }

    fun generateTxtReport(
        title: String,
        totalWords: Int,
        uniqueWords: Int,
        words: List<WordItem>
    ): String {
        val sb = StringBuilder()
        sb.append("=========================================\n")
        sb.append("      WORD ANALYSIS REPORT: $title      \n")
        sb.append("=========================================\n")
        sb.append("Total Words in Text: $totalWords\n")
        sb.append("Unique Words Extracted: $uniqueWords\n")
        sb.append("Date: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}\n")
        sb.append("-----------------------------------------\n\n")

        sb.append(String.format("%-5s | %-18s | %-8s | %-6s | %s\n", "Rank", "Word (Lemma)", "Freq", "CEFR", "Persian Translation"))
        sb.append("-------------------------------------------------------------------\n")

        for (w in words) {
            val trans = w.translation ?: "-"
            sb.append(String.format("%-5d | %-18s | %-8d | %-6s | %s\n", w.rank, w.word, w.frequency, w.cefrLevel, trans))
            if (w.contextSentence.isNotBlank()) {
                sb.append("      Context: \"${w.contextSentence}\"\n")
            }
        }

        return sb.toString()
    }

    fun generateJsonData(
        title: String,
        totalWords: Int,
        uniqueWords: Int,
        words: List<WordItem>
    ): String {
        val root = JSONObject()
        root.put("title", title)
        root.put("totalWords", totalWords)
        root.put("uniqueWords", uniqueWords)
        root.put("exportedAt", System.currentTimeMillis())

        val array = JSONArray()
        for (w in words) {
            val item = JSONObject()
            item.put("rank", w.rank)
            item.put("word", w.word)
            item.put("lemma", w.lemma)
            item.put("frequency", w.frequency)
            item.put("percentage", w.percentage)
            item.put("cefrLevel", w.cefrLevel)
            item.put("translation", w.translation ?: "")
            item.put("phonetic", w.phonetic ?: "")
            item.put("definition", w.definition ?: "")
            item.put("contextSentence", w.contextSentence)
            array.put(item)
        }

        root.put("words", array)
        return root.toString(2)
    }

    fun generatePdfFile(
        context: Context,
        title: String,
        totalWords: Int,
        uniqueWords: Int,
        words: List<WordItem>
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 page
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.parseColor("#1E1B4B")
            textSize = 20f
            isFakeBoldText = true
        }

        val headerPaint = Paint().apply {
            color = Color.parseColor("#4338CA")
            textSize = 12f
            isFakeBoldText = true
        }

        val textPaint = Paint().apply {
            color = Color.parseColor("#1F2937")
            textSize = 10f
        }

        val linePaint = Paint().apply {
            color = Color.parseColor("#E5E7EB")
            strokeWidth = 1f
        }

        var y = 40f
        canvas.drawText("Word Analysis Report: $title", 40f, y, titlePaint)
        y += 24f
        canvas.drawText("Total Words: $totalWords  |  Unique Words: $uniqueWords", 40f, y, textPaint)
        y += 20f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 20f

        // Table Header
        canvas.drawText("Rank", 40f, y, headerPaint)
        canvas.drawText("Word", 90f, y, headerPaint)
        canvas.drawText("Freq", 240f, y, headerPaint)
        canvas.drawText("CEFR", 300f, y, headerPaint)
        canvas.drawText("Translation", 370f, y, headerPaint)
        y += 12f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 18f

        var pageNum = 1

        for (w in words) {
            if (y > 800f) {
                pdfDocument.finishPage(page)
                pageNum++
                val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNum).create()
                page = pdfDocument.startPage(newPageInfo)
                canvas = page.canvas
                y = 40f

                // Header on new page
                canvas.drawText("Rank", 40f, y, headerPaint)
                canvas.drawText("Word", 90f, y, headerPaint)
                canvas.drawText("Freq", 240f, y, headerPaint)
                canvas.drawText("CEFR", 300f, y, headerPaint)
                canvas.drawText("Translation", 370f, y, headerPaint)
                y += 12f
                canvas.drawLine(40f, y, 555f, y, linePaint)
                y += 18f
            }

            canvas.drawText("#${w.rank}", 40f, y, textPaint)
            canvas.drawText(w.word.take(20), 90f, y, textPaint)
            canvas.drawText("${w.frequency}", 240f, y, textPaint)
            canvas.drawText(w.cefrLevel, 300f, y, textPaint)
            canvas.drawText(w.translation ?: "-", 370f, y, textPaint)

            y += 18f
        }

        pdfDocument.finishPage(page)

        val outputFile = File(context.cacheDir, "${title.replace(Regex("""[^a-zA-Z0-9]"""), "_")}_report.pdf")
        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return outputFile
    }

    private fun escapeCsv(text: String): String {
        val clean = text.replace("\"", "\"\"")
        return if (clean.contains(",") || clean.contains("\n") || clean.contains("\"")) {
            "\"$clean\""
        } else {
            clean
        }
    }
}
