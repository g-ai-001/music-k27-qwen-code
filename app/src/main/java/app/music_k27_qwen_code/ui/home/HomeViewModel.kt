package app.music_k27_qwen_code.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.music_k27_qwen_code.MusicApplication
import app.music_k27_qwen_code.data.entity.Song
import app.music_k27_qwen_code.utils.MediaScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val songs: List<Song> = emptyList(),
    val isScanning: Boolean = false,
    val searchQuery: String = "",
    val selectedTab: Int = 0
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val songRepository = (application as MusicApplication).songRepository

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            songRepository.allSongs.collect { songs ->
                _uiState.value = _uiState.value.copy(songs = songs)
            }
        }
    }

    fun scanMusic() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true)
            val existingSongs = _uiState.value.songs
            val result = MediaScanner.scanLocalMusic(getApplication(), existingSongs)
            if (result.toRemove.isNotEmpty()) {
                result.toRemove.forEach { songRepository.deleteById(it.id) }
            }
            if (result.toAdd.isNotEmpty()) {
                songRepository.insertAll(result.toAdd)
            }
            _uiState.value = _uiState.value.copy(isScanning = false)
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onTabSelected(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }
}
