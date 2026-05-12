package app.music_k27_qwen_code.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.music_k27_qwen_code.MusicApplication
import app.music_k27_qwen_code.data.entity.Playlist
import app.music_k27_qwen_code.data.entity.Song
import app.music_k27_qwen_code.utils.MediaScanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class HomeUiState(
    val songs: List<Song> = emptyList(),
    val isScanning: Boolean = false,
    val searchQuery: String = "",
    val selectedTab: Int = 0,
    val recentSongs: List<Song> = emptyList(),
    val playlists: List<Playlist> = emptyList()
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val songRepository = (application as MusicApplication).songRepository
    private val recentPlayDao = (application as MusicApplication).database.recentPlayDao()
    private val playlistDao = (application as MusicApplication).database.playlistDao()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                songRepository.allSongs,
                recentPlayDao.getRecentPlays(),
                playlistDao.getAllPlaylists()
            ) { songs, recentPlays, playlists ->
                val recentSongIds = recentPlays.map { it.songId }
                val recentSongs = songs.filter { it.id in recentSongIds }.take(10)
                HomeUiState(
                    songs = songs,
                    recentSongs = recentSongs,
                    playlists = playlists
                )
            }.collect { state ->
                _uiState.value = state
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
