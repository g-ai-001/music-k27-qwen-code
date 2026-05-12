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
    private val favoriteDao = (application as MusicApplication).database.favoriteDao()
    private val recentPlayDao = (application as MusicApplication).database.recentPlayDao()
    private val playlistRepository = (application as MusicApplication).playlistRepository

    private val _uiState = MutableStateFlow(MeUiState())
    val uiState: StateFlow<MeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                songRepository.allSongs,
                favoriteDao.getFavoriteIds(),
                recentPlayDao.getRecentPlays(),
                playlistRepository.allPlaylists
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

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                playlistRepository.createPlaylist(name)
            }
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch {
            playlistRepository.deletePlaylist(id)
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            val existingIds = playlistRepository.getPlaylistSongIds(playlistId)
            if (songId !in existingIds) {
                val orderIndex = existingIds.size
                playlistRepository.addSongToPlaylist(playlistId, songId, orderIndex)
            }
        }
    }
}
