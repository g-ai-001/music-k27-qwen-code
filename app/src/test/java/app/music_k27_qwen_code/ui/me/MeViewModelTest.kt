package app.music_k27_qwen_code.ui.me

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.music_k27_qwen_code.MusicApplication
import app.music_k27_qwen_code.data.AppDatabase
import app.music_k27_qwen_code.data.dao.FavoriteDao
import app.music_k27_qwen_code.data.dao.RecentPlayDao
import app.music_k27_qwen_code.data.entity.Playlist
import app.music_k27_qwen_code.data.entity.RecentPlay
import app.music_k27_qwen_code.data.entity.Song
import app.music_k27_qwen_code.data.repository.PlaylistRepository
import app.music_k27_qwen_code.data.repository.SongRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MeViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var application: MusicApplication
    private lateinit var songRepository: SongRepository
    private lateinit var playlistRepository: PlaylistRepository
    private lateinit var database: AppDatabase
    private lateinit var favoriteDao: FavoriteDao
    private lateinit var recentPlayDao: RecentPlayDao

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        songRepository = mockk()
        playlistRepository = mockk(relaxed = true)
        favoriteDao = mockk()
        recentPlayDao = mockk()
        database = mockk()
        every { database.favoriteDao() } returns favoriteDao
        every { database.recentPlayDao() } returns recentPlayDao
        application = mockk<MusicApplication>()
        every { application.songRepository } returns songRepository
        every { application.playlistRepository } returns playlistRepository
        every { application.database } returns database
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState has zero counts`() = runTest {
        every { songRepository.allSongs } returns MutableStateFlow(emptyList())
        every { favoriteDao.getFavoriteIds() } returns MutableStateFlow(emptyList())
        every { recentPlayDao.getRecentPlays() } returns MutableStateFlow(emptyList())
        every { playlistRepository.allPlaylists } returns MutableStateFlow(emptyList())

        val viewModel = MeViewModel(application)
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.favoriteCount)
        assertEquals(0, viewModel.uiState.value.localCount)
        assertEquals(0, viewModel.uiState.value.recentSongs.size)
        assertEquals(0, viewModel.uiState.value.playlists.size)
    }

    @Test
    fun `uiState reflects favorite and local counts`() = runTest {
        val songs = listOf(
            Song(id = 1, title = "Song 1", artist = "A", album = "B", duration = 1000, path = "/1", albumArtUri = ""),
            Song(id = 2, title = "Song 2", artist = "C", album = "D", duration = 2000, path = "/2", albumArtUri = "")
        )
        val favorites = listOf(1L)
        val playlists = listOf(Playlist(id = 1, name = "My Playlist"))

        every { songRepository.allSongs } returns MutableStateFlow(songs)
        every { favoriteDao.getFavoriteIds() } returns MutableStateFlow(favorites)
        every { recentPlayDao.getRecentPlays() } returns MutableStateFlow(emptyList())
        every { playlistRepository.allPlaylists } returns MutableStateFlow(playlists)

        val viewModel = MeViewModel(application)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.favoriteCount)
        assertEquals(2, viewModel.uiState.value.localCount)
        assertEquals(1, viewModel.uiState.value.playlists.size)
    }

    @Test
    fun `createPlaylist calls repository when name is valid`() = runTest {
        every { songRepository.allSongs } returns MutableStateFlow(emptyList())
        every { favoriteDao.getFavoriteIds() } returns MutableStateFlow(emptyList())
        every { recentPlayDao.getRecentPlays() } returns MutableStateFlow(emptyList())
        every { playlistRepository.allPlaylists } returns MutableStateFlow(emptyList())

        val viewModel = MeViewModel(application)
        viewModel.createPlaylist("New Playlist")
        advanceUntilIdle()

        coVerify { playlistRepository.createPlaylist("New Playlist") }
    }

    @Test
    fun `deletePlaylist calls repository`() = runTest {
        every { songRepository.allSongs } returns MutableStateFlow(emptyList())
        every { favoriteDao.getFavoriteIds() } returns MutableStateFlow(emptyList())
        every { recentPlayDao.getRecentPlays() } returns MutableStateFlow(emptyList())
        every { playlistRepository.allPlaylists } returns MutableStateFlow(emptyList())

        val viewModel = MeViewModel(application)
        viewModel.deletePlaylist(1)
        advanceUntilIdle()

        coVerify { playlistRepository.deletePlaylist(1) }
    }
}