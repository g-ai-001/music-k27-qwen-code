package app.music_k27_qwen_code.ui.me

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.music_k27_qwen_code.MusicApplication
import app.music_k27_qwen_code.data.entity.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class MeUiState(
    val favoriteCount: Int = 0,
    val localCount: Int = 0,
    val recentSongs: List<Song> = emptyList(),
    val playlists: List<app.music_k27_qwen_code.data.entity.Playlist> = emptyList()
)

class MeViewModel(application: Application) : AndroidViewModel(application) {
    private val songRepository = (application as MusicApplication).songRepository
    private val favoriteDao = application.database.favoriteDao()
    private val recentPlayDao = application.database.recentPlayDao()
    private val playlistDao = application.database.playlistDao()

    private val _uiState = MutableStateFlow(MeUiState())
    val uiState: StateFlow<MeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                songRepository.allSongs,
                favoriteDao.getFavoriteIds(),
                recentPlayDao.getRecentPlays(),
                playlistDao.getAllPlaylists()
            ) { songs, favorites, recentPlays, playlists ->
                val recentSongIds = recentPlays.map { it.songId }
                val recentSongs = songs.filter { it.id in recentSongIds }.take(10)
                MeUiState(
                    favoriteCount = favorites.size,
                    localCount = songs.size,
                    recentSongs = recentSongs,
                    playlists = playlists
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}
