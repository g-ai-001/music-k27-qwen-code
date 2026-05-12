package app.music_k27_qwen_code.utils

import java.io.File
import java.util.regex.Pattern

data class LyricLine(val time: Long, val text: String)

object LyricParser {
    private val PATTERN = Pattern.compile("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\](.*)")

    fun parse(lrcContent: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()
        lrcContent.lines().forEach { line ->
            val matcher = PATTERN.matcher(line.trim())
            while (matcher.find()) {
                val min = matcher.group(1)?.toLongOrNull() ?: 0
                val sec = matcher.group(2)?.toLongOrNull() ?: 0
                val msStr = matcher.group(3) ?: "00"
                val ms = if (msStr.length == 2) msStr.toLong() * 10 else msStr.toLong()
                val text = matcher.group(4)?.trim() ?: ""
                val timeMs = (min * 60 + sec) * 1000 + ms
                if (text.isNotBlank()) {
                    lines.add(LyricLine(timeMs, text))
                }
            }
        }
        return lines.sortedBy { it.time }
    }

    fun loadLyricFromFile(songPath: String): List<LyricLine> {
        val lrcFile = File(songPath.replaceAfterLast(".", "lrc"))
        return if (lrcFile.exists()) {
            parse(lrcFile.readText())
        } else {
            emptyList()
        }
    }

    fun findCurrentLine(lines: List<LyricLine>, currentMs: Long): Int {
        if (lines.isEmpty()) return -1
        var index = 0
        for (i in lines.indices) {
            if (lines[i].time <= currentMs) {
                index = i
            } else {
                break
            }
        }
        return index
    }
}
