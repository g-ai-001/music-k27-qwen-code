package app.music_k27_qwen_code.ui.favorite

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.music_k27_qwen_code.MusicApplication
import app.music_k27_qwen_code.data.entity.Song
import app.music_k27_qwen_code.data.repository.FavoriteRepository
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
class FavoriteSongsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var application: MusicApplication
    private lateinit var favoriteRepository: FavoriteRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        favoriteRepository = mockk(relaxed = true)
        application = mockk<MusicApplication>()
        every { application.favoriteRepository } returns favoriteRepository
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState emits favorite songs from repository`() = runTest {
        val songs = listOf(
            Song(id = 1, title = "Song 1", artist = "A", album = "B", duration = 1000, path = "/1", albumArtUri = "")
        )
        every { favoriteRepository.favoriteSongs } returns MutableStateFlow(songs)

        val viewModel = FavoriteSongsViewModel(application)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.songs.size)
        assertEquals("Song 1", viewModel.uiState.value.songs[0].title)
    }

    @Test
    fun `removeFavorite calls repository`() = runTest {
        every { favoriteRepository.favoriteSongs } returns MutableStateFlow(emptyList())

        val viewModel = FavoriteSongsViewModel(application)
        viewModel.removeFavorite(1)
        advanceUntilIdle()

        coVerify { favoriteRepository.removeFavorite(1) }
    }
}