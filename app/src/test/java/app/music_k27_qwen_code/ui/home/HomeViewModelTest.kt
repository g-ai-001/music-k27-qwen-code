package app.music_k27_qwen_code.ui.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.music_k27_qwen_code.MusicApplication
import app.music_k27_qwen_code.data.AppDatabase
import app.music_k27_qwen_code.data.dao.PlaylistDao
import app.music_k27_qwen_code.data.dao.RecentPlayDao
import app.music_k27_qwen_code.data.entity.Playlist
import app.music_k27_qwen_code.data.entity.RecentPlay
import app.music_k27_qwen_code.data.entity.Song
import app.music_k27_qwen_code.data.repository.SongRepository
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
class HomeViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var application: MusicApplication
    private lateinit var songRepository: SongRepository
    private lateinit var database: AppDatabase
    private lateinit var recentPlayDao: RecentPlayDao
    private lateinit var playlistDao: PlaylistDao

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        songRepository = mockk()
        recentPlayDao = mockk()
        playlistDao = mockk()
        database = mockk()
        every { database.recentPlayDao() } returns recentPlayDao
        every { database.playlistDao() } returns playlistDao
        application = mockk<MusicApplication>()
        every { application.songRepository } returns songRepository
        every { application.database } returns database
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState has correct default values`() = runTest {
        every { songRepository.allSongs } returns MutableStateFlow(emptyList())
        every { recentPlayDao.getRecentPlays() } returns MutableStateFlow(emptyList())
        every { playlistDao.getAllPlaylists() } returns MutableStateFlow(emptyList())

        val viewModel = HomeViewModel(application)
        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isScanning)
        assertEquals("", viewModel.uiState.value.searchQuery)
        assertEquals(0, viewModel.uiState.value.selectedTab)
        assertEquals(0, viewModel.uiState.value.songs.size)
    }

    @Test
    fun `onSearchQueryChange updates query`() = runTest {
        every { songRepository.allSongs } returns MutableStateFlow(emptyList())
        every { recentPlayDao.getRecentPlays() } returns MutableStateFlow(emptyList())
        every { playlistDao.getAllPlaylists() } returns MutableStateFlow(emptyList())

        val viewModel = HomeViewModel(application)
        viewModel.onSearchQueryChange("test")

        assertEquals("test", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun `onTabSelected updates tab index`() = runTest {
        every { songRepository.allSongs } returns MutableStateFlow(emptyList())
        every { recentPlayDao.getRecentPlays() } returns MutableStateFlow(emptyList())
        every { playlistDao.getAllPlaylists() } returns MutableStateFlow(emptyList())

        val viewModel = HomeViewModel(application)
        viewModel.onTabSelected(2)

        assertEquals(2, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun `uiState combines songs recentPlays and playlists`() = runTest {
        val songs = listOf(
            Song(id = 1, title = "Song 1", artist = "A", album = "B", duration = 1000, path = "/1", albumArtUri = "")
        )
        val recentPlays = listOf(RecentPlay(songId = 1))
        val playlists = listOf(Playlist(id = 1, name = "Playlist 1"))

        every { songRepository.allSongs } returns MutableStateFlow(songs)
        every { recentPlayDao.getRecentPlays() } returns MutableStateFlow(recentPlays)
        every { playlistDao.getAllPlaylists() } returns MutableStateFlow(playlists)

        val viewModel = HomeViewModel(application)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.songs.size)
        assertEquals(1, viewModel.uiState.value.recentSongs.size)
        assertEquals(1, viewModel.uiState.value.playlists.size)
    }
}