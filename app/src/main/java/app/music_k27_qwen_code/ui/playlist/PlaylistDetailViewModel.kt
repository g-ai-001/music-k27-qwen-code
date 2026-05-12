package app.music_k27_qwen_code.ui.playlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.music_k27_qwen_code.MusicApplication
import app.music_k27_qwen_code.data.entity.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlaylistDetailUiState(
    val playlistName: String = "",
    val songs: List<Song> = emptyList()
)

class PlaylistDetailViewModel(
    application: Application,
    private val playlistId: Long
) : AndroidViewModel(application) {
    private val playlistRepository = (application as MusicApplication).playlistRepository

    private val _uiState = MutableStateFlow(PlaylistDetailUiState())
    val uiState: StateFlow<PlaylistDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            playlistRepository.getSongsInPlaylist(playlistId).collect { songs ->
                _uiState.value = _uiState.value.copy(songs = songs)
            }
        }
    }

    fun renamePlaylist(newName: String) {
        viewModelScope.launch {
            playlistRepository.renamePlaylist(playlistId, newName)
            _uiState.value = _uiState.value.copy(playlistName = newName)
        }
    }

    fun removeSong(songId: Long) {
        viewModelScope.launch {
            playlistRepository.removeSongFromPlaylist(playlistId, songId)
        }
    }
}
