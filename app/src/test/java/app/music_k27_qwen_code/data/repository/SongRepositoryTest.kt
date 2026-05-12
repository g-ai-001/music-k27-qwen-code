package app.music_k27_qwen_code.data.repository

import app.music_k27_qwen_code.data.dao.SongDao
import app.music_k27_qwen_code.data.entity.Song
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SongRepositoryTest {

    private val songDao: SongDao = mockk(relaxed = true)
    private val repository = SongRepository(songDao)

    @Test
    fun `getSongById returns song when exists`() = runTest {
        val song = Song(id = 1, title = "Test Song", artist = "Artist", album = "Album", duration = 300000, path = "/test", albumArtUri = "")
        coEvery { songDao.getSongById(1) } returns song

        val result = repository.getSongById(1)

        assertEquals(song, result)
    }

    @Test
    fun `getSongById returns null when not exists`() = runTest {
        coEvery { songDao.getSongById(999) } returns null

        val result = repository.getSongById(999)

        assertNull(result)
    }

    @Test
    fun `allSongs emits list from dao`() = runTest {
        val songs = listOf(
            Song(id = 1, title = "Song 1", artist = "A", album = "B", duration = 1000, path = "/1", albumArtUri = ""),
            Song(id = 2, title = "Song 2", artist = "C", album = "D", duration = 2000, path = "/2", albumArtUri = "")
        )
        coEvery { songDao.getAllSongs() } returns flowOf(songs)

        val result = repository.allSongs.first()

        assertEquals(2, result.size)
        assertEquals("Song 1", result[0].title)
    }

    @Test
    fun `insertAll delegates to dao`() = runTest {
        val songs = listOf(
            Song(id = 1, title = "Song 1", artist = "A", album = "B", duration = 1000, path = "/1", albumArtUri = "")
        )

        repository.insertAll(songs)

        coVerify { songDao.insertAll(songs) }
    }

    @Test
    fun `deleteById delegates to dao`() = runTest {
        repository.deleteById(1)

        coVerify { songDao.deleteById(1) }
    }
}