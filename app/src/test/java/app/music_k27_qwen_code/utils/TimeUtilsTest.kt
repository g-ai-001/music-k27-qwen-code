package app.music_k27_qwen_code.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeUtilsTest {

    @Test
    fun `formatDuration formats zero correctly`() {
        assertEquals("00:00", formatDuration(0))
    }

    @Test
    fun `formatDuration formats seconds only`() {
        assertEquals("00:30", formatDuration(30000))
    }

    @Test
    fun `formatDuration formats minutes and seconds`() {
        assertEquals("03:45", formatDuration(225000))
    }

    @Test
    fun `formatDuration formats hours as minutes`() {
        assertEquals("65:30", formatDuration(3930000))
    }

    @Test
    fun `formatDuration rounds down milliseconds`() {
        assertEquals("01:23", formatDuration(83999))
    }

    @Test
    fun `formatDuration formats single digit with leading zero`() {
        assertEquals("01:05", formatDuration(65000))
        assertEquals("01:05", formatDuration(65001))
    }
}