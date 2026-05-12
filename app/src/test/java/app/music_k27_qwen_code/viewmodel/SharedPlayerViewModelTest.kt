package app.music_k27_qwen_code.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import app.music_k27_qwen_code.MusicApplication
import app.music_k27_qwen_code.data.AppDatabase
import app.music_k27_qwen_code.data.dao.FavoriteDao
import app.music_k27_qwen_code.data.dao.RecentPlayDao
import app.music_k27_qwen_code.data.entity.Song
import app.music_k27_qwen_code.data.repository.PlaybackSettingsRepository
import app.music_k27_qwen_code.data.repository.SongRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SharedPlayerViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var application: MusicApplication
    private lateinit var songRepository: SongRepository
    private lateinit var database: AppDatabase
    private lateinit var favoriteDao: FavoriteDao
    private lateinit var recentPlayDao: RecentPlayDao
    private lateinit var playbackSettingsRepository: PlaybackSettingsRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        songRepository = mockk(relaxed = true)
        favoriteDao = mockk(relaxed = true)
        recentPlayDao = mockk(relaxed = true)
        playbackSettingsRepository = mockk(relaxed = true)
        database = mockk()
        every { database.favoriteDao() } returns favoriteDao
        every { database.recentPlayDao() } returns recentPlayDao
        application = mockk<MusicApplication>()
        every { application.songRepository } returns songRepository
        every { application.database } returns database
        every { application.playbackSettingsRepository } returns playbackSettingsRepository
        every { playbackSettingsRepository.shuffleEnabled } returns MutableStateFlow(false)
        every { playbackSettingsRepository.repeatMode } returns MutableStateFlow(0)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState has correct default values`() = runTest {
        val viewModel = SharedPlayerViewModel(application)
        advanceUntilIdle()

        assertEquals(null, viewModel.uiState.value.currentSong)
        assertEquals(false, viewModel.uiState.value.isPlaying)
        assertEquals(0L, viewModel.uiState.value.currentPosition)
        assertEquals(false, viewModel.uiState.value.showLyrics)
        assertEquals(RepeatMode.OFF, viewModel.uiState.value.repeatMode)
        assertEquals(false, viewModel.uiState.value.shuffleEnabled)
    }

    @Test
    fun `toggleLyricsMode switches showLyrics state`() = runTest {
        val viewModel = SharedPlayerViewModel(application)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showLyrics)
        viewModel.toggleLyricsMode()
        assertTrue(viewModel.uiState.value.showLyrics)
        viewModel.toggleLyricsMode()
        assertFalse(viewModel.uiState.value.showLyrics)
    }

    @Test
    fun `playSongs with empty list does nothing`() = runTest {
        val viewModel = SharedPlayerViewModel(application)
        advanceUntilIdle()

        viewModel.playSongs(emptyList(), 0)
        assertEquals(0, viewModel.playlist.value.size)
    }

    @Test
    fun `playSongs with invalid startIndex does nothing`() = runTest {
        val viewModel = SharedPlayerViewModel(application)
        advanceUntilIdle()

        val songs = listOf(
            Song(id = 1, title = "Song 1", artist = "A", album = "B", duration = 1000, path = "/1", albumArtUri = "")
        )
        viewModel.playSongs(songs, -1)
        assertEquals(0, viewModel.playlist.value.size)
    }

    @Test
    fun `toggleFavorite with null currentSong does nothing`() = runTest {
        val viewModel = SharedPlayerViewModel(application)
        advanceUntilIdle()

        viewModel.toggleFavorite()
        coVerify(exactly = 0) { favoriteDao.addFavorite(any()) }
        coVerify(exactly = 0) { favoriteDao.removeFavorite(any()) }
    }

    @Test
    fun `cycleRepeatMode cycles through modes`() = runTest {
        val viewModel = SharedPlayerViewModel(application)
        val mediaController = mockk<MediaController>(relaxed = true)
        injectMediaController(viewModel, mediaController)

        advanceUntilIdle()

        viewModel.cycleRepeatMode()
        verify { mediaController.repeatMode = Player.REPEAT_MODE_ALL }
    }

    @Test
    fun `toggleShuffle toggles shuffle mode`() = runTest {
        val viewModel = SharedPlayerViewModel(application)
        val mediaController = mockk<MediaController>(relaxed = true)
        injectMediaController(viewModel, mediaController)

        advanceUntilIdle()

        every { mediaController.shuffleModeEnabled } returns false
        viewModel.toggleShuffle()
        verify { mediaController.shuffleModeEnabled = true }
    }

    @Test
    fun `removeFromQueue with invalid index does nothing`() = runTest {
        val viewModel = SharedPlayerViewModel(application)
        val mediaController = mockk<MediaController>(relaxed = true)
        injectMediaController(viewModel, mediaController)

        advanceUntilIdle()

        viewModel.removeFromQueue(-1)
        verify(exactly = 0) { mediaController.removeMediaItem(any()) }
    }

    private fun injectMediaController(viewModel: SharedPlayerViewModel, controller: MediaController) {
        val field = SharedPlayerViewModel::class.java.getDeclaredField("mediaController")
        field.isAccessible = true
        field.set(viewModel, controller)
    }
}
