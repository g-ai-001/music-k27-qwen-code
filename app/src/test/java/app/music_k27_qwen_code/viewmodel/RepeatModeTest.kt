package app.music_k27_qwen_code.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Test

class RepeatModeTest {

    @Test
    fun `repeat mode values are correct`() {
        assertEquals(0, RepeatMode.OFF.value)
        assertEquals(1, RepeatMode.ALL.value)
        assertEquals(2, RepeatMode.ONE.value)
    }

    @Test
    fun `repeat mode entries contains all modes`() {
        val entries = RepeatMode.entries
        assertEquals(3, entries.size)
        assertEquals(RepeatMode.OFF, entries[0])
        assertEquals(RepeatMode.ALL, entries[1])
        assertEquals(RepeatMode.ONE, entries[2])
    }

    @Test
    fun `repeat mode find by value works`() {
        assertEquals(RepeatMode.OFF, RepeatMode.entries.find { it.value == 0 })
        assertEquals(RepeatMode.ALL, RepeatMode.entries.find { it.value == 1 })
        assertEquals(RepeatMode.ONE, RepeatMode.entries.find { it.value == 2 })
        assertEquals(null, RepeatMode.entries.find { it.value == 99 })
    }
}
