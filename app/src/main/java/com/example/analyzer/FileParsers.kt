package com.example.analyzer

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.zip.ZipInputStream

object FileParsers {

    fun parseTxt(inputStream: InputStream): String {
        return inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
    }

    fun parseEpub(inputStream: InputStream): String {
        val stringBuilder = StringBuilder()
        ZipInputStream(inputStream).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val name = entry.name.lowercase()
                if (name.endsWith(".html") || name.endsWith(".xhtml") || name.endsWith(".htm")) {
                    val reader = BufferedReader(InputStreamReader(zip, Charsets.UTF_8))
                    val htmlContent = reader.readText()
                    val cleanText = stripHtmlTags(htmlContent)
                    if (cleanText.isNotBlank()) {
                        stringBuilder.append(cleanText).append("\n\n")
                    }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        val result = stringBuilder.toString()
        return if (result.isNotBlank()) result else "Could not extract text from EPUB file."
    }

    fun parsePdf(inputStream: InputStream): String {
        val rawBytes = inputStream.readBytes()
        val rawContent = String(rawBytes, Charsets.ISO_8859_1)
        val sb = StringBuilder()

        // Match PDF text operators like (Text) Tj or [(Text1) (Text2)] TJ
        val tjRegex = Regex("""\(([^()]*)\)\s*Tj""", RegexOption.IGNORE_CASE)
        val tjArrayRegex = Regex("""\[\s*(\(.*?\)\s*)*\]\s*TJ""", RegexOption.IGNORE_CASE)
        val innerTextRegex = Regex("""\((.*?)\)""")

        var foundText = false
        for (match in tjArrayRegex.findAll(rawContent)) {
            val arrayContent = match.value
            for (inner in innerTextRegex.findAll(arrayContent)) {
                val text = inner.groupValues[1]
                if (text.isNotBlank() && isMostlyReadableText(text)) {
                    sb.append(cleanPdfText(text)).append(" ")
                    foundText = true
                }
            }
            sb.append("\n")
        }

        if (!foundText) {
            for (match in tjRegex.findAll(rawContent)) {
                val text = match.groupValues[1]
                if (text.isNotBlank() && isMostlyReadableText(text)) {
                    sb.append(cleanPdfText(text)).append(" ")
                    foundText = true
                }
            }
        }

        // Fallback: search for continuous plain text words if Tj stream was encrypted or compressed
        if (!foundText || sb.length < 50) {
            val fallbackSb = StringBuilder()
            val textBlocks = rawContent.replace(Regex("""[^a-zA-Z0-9\s.,!?'"\-]"""), " ")
            val words = textBlocks.split(Regex("""\s+"""))
            for (w in words) {
                if (w.length in 2..25 && w.any { it.isLetter() }) {
                    fallbackSb.append(w).append(" ")
                }
            }
            if (fallbackSb.length > sb.length) {
                return fallbackSb.toString()
            }
        }

        return if (sb.isNotBlank()) sb.toString() else "Could not extract text from PDF file."
    }

    private fun stripHtmlTags(html: String): String {
        val noStyleScript = html.replace(Regex("""(?s)<(script|style).*?>.*?</\1>"""), " ")
        val noTags = noStyleScript.replace(Regex("""<[^>]*>"""), " ")
        return unescapeHtml(noTags)
    }

    private fun unescapeHtml(text: String): String {
        return text
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }

    private fun cleanPdfText(text: String): String {
        return text.replace("\\)", ")")
            .replace("\\(", "(")
            .replace("\\r", "\n")
            .replace("\\n", "\n")
            .replace("\\t", " ")
    }

    private fun isMostlyReadableText(text: String): Boolean {
        if (text.length < 2) return false
        val letterCount = text.count { it.isLetter() }
        return (letterCount.toDouble() / text.length) >= 0.4
    }
}
