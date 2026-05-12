package app.music_k27_qwen_code.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class LyricParserTest {

    @Test
    fun `parse valid lrc content`() {
        val lrc = """
            [00:00.50]歌词第一行
            [00:02.30]歌词第二行
            [00:05.00]歌词第三行
        """.trimIndent()

        val result = LyricParser.parse(lrc)

        assertEquals(3, result.size)
        assertEquals(500, result[0].time)
        assertEquals("歌词第一行", result[0].text)
        assertEquals(2300, result[1].time)
        assertEquals("歌词第二行", result[1].text)
        assertEquals(5000, result[2].time)
        assertEquals("歌词第三行", result[2].text)
    }

    @Test
    fun `parse lrc with three digit milliseconds`() {
        val lrc = "[01:23.456]测试歌词"
        val result = LyricParser.parse(lrc)

        assertEquals(1, result.size)
        assertEquals(83456, result[0].time)
        assertEquals("测试歌词", result[0].text)
    }

    @Test
    fun `parse empty lrc content returns empty list`() {
        val result = LyricParser.parse("")
        assertEquals(0, result.size)
    }

    @Test
    fun `parse invalid lrc lines are ignored`() {
        val lrc = """
            [00:00.50]有效歌词
            无效行
            [aa:bb.cc]无效时间
        """.trimIndent()

        val result = LyricParser.parse(lrc)
        assertEquals(1, result.size)
        assertEquals("有效歌词", result[0].text)
    }

    @Test
    fun `findCurrentLine returns correct index`() {
        val lines = listOf(
            LyricLine(0, "第一行"),
            LyricLine(2000, "第二行"),
            LyricLine(5000, "第三行")
        )

        assertEquals(-1, LyricParser.findCurrentLine(emptyList(), 1000))
        assertEquals(0, LyricParser.findCurrentLine(lines, 0))
        assertEquals(0, LyricParser.findCurrentLine(lines, 1000))
        assertEquals(1, LyricParser.findCurrentLine(lines, 2000))
        assertEquals(1, LyricParser.findCurrentLine(lines, 4999))
        assertEquals(2, LyricParser.findCurrentLine(lines, 5000))
        assertEquals(2, LyricParser.findCurrentLine(lines, 10000))
    }

    @Test
    fun `loadLyricFromFile returns empty when file not exists`() {
        val result = LyricParser.loadLyricFromFile("/nonexistent/path/song.mp3")
        assertEquals(0, result.size)
    }

    @Test
    fun `parse sorts lines by time`() {
        val lrc = """
            [00:05.00]第三行
            [00:01.00]第一行
            [00:03.00]第二行
        """.trimIndent()

        val result = LyricParser.parse(lrc)
        assertEquals("第一行", result[0].text)
        assertEquals("第二行", result[1].text)
        assertEquals("第三行", result[2].text)
    }
}