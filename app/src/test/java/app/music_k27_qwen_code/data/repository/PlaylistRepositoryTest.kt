package app.music_k27_qwen_code.data.repository

import app.music_k27_qwen_code.data.dao.PlaylistDao
import app.music_k27_qwen_code.data.entity.Playlist
import app.music_k27_qwen_code.data.entity.Song
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistRepositoryTest {

    private val playlistDao: PlaylistDao = mockk(relaxed = true)
    private val repository = PlaylistRepository(playlistDao)

    @Test
    fun `createPlaylist inserts playlist and returns id`() = runTest {
        coEvery { playlistDao.insertPlaylist(any()) } returns 1L

        val result = repository.createPlaylist("My Playlist")

        assertEquals(1L, result)
    }

    @Test
    fun `deletePlaylist delegates to dao`() = runTest {
        repository.deletePlaylist(1)

        coVerify { playlistDao.deletePlaylist(1) }
    }

    @Test
    fun `renamePlaylist delegates to dao`() = runTest {
        repository.renamePlaylist(1, "New Name")

        coVerify { playlistDao.updatePlaylistName(1, "New Name") }
    }

    @Test
    fun `getSongsInPlaylist returns flow from dao`() = runTest {
        val songs = listOf(
            Song(id = 1, title = "Song 1", artist = "A", album = "B", duration = 1000, path = "/1", albumArtUri = "")
        )
        coEvery { playlistDao.getSongsInPlaylist(1) } returns flowOf(songs)

        val result = repository.getSongsInPlaylist(1).first()

        assertEquals(1, result.size)
        assertEquals("Song 1", result[0].title)
    }

    @Test
    fun `addSongToPlaylist delegates to dao`() = runTest {
        repository.addSongToPlaylist(1, 2, 0)

        coVerify { playlistDao.addSongToPlaylist(any()) }
    }

    @Test
    fun `removeSongFromPlaylist delegates to dao`() = runTest {
        repository.removeSongFromPlaylist(1, 2)

        coVerify { playlistDao.removeSongFromPlaylist(1, 2) }
    }
}