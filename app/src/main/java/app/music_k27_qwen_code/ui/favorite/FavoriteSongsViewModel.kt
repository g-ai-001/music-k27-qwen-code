package app.music_k27_qwen_code.ui.favorite

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.music_k27_qwen_code.MusicApplication
import app.music_k27_qwen_code.data.entity.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FavoriteUiState(
    val songs: List<Song> = emptyList()
)

class FavoriteSongsViewModel(application: Application) : AndroidViewModel(application) {
    private val favoriteRepository = (application as MusicApplication).favoriteRepository

    private val _uiState = MutableStateFlow(FavoriteUiState())
    val uiState: StateFlow<FavoriteUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            favoriteRepository.favoriteSongs.collect { songs ->
                _uiState.value = FavoriteUiState(songs = songs)
            }
        }
    }

    fun removeFavorite(songId: Long) {
        viewModelScope.launch {
            favoriteRepository.removeFavorite(songId)
        }
    }
}
