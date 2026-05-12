package app.music_k27_qwen_code.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import app.music_k27_qwen_code.MusicApplication
import app.music_k27_qwen_code.data.entity.Song
import app.music_k27_qwen_code.service.MusicPlaybackService
import app.music_k27_qwen_code.utils.LyricParser
import app.music_k27_qwen_code.utils.Logger
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlayerUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0,
    val duration: Long = 0,
    val lyrics: List<app.music_k27_qwen_code.utils.LyricLine> = emptyList(),
    val currentLyricIndex: Int = -1,
    val isFavorite: Boolean = false,
    val showLyrics: Boolean = false
)

class SharedPlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val songRepository = (application as MusicApplication).songRepository
    private val favoriteDao = (application as MusicApplication).database.favoriteDao()
    private val recentPlayDao = (application as MusicApplication).database.recentPlayDao()

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var mediaController: MediaController? = null
    private val _playlist = MutableStateFlow<List<Song>>(emptyList())

    init {
        initMediaController(application)
    }

    private fun initMediaController(context: Context) {
        val sessionToken = SessionToken(context, ComponentName(context, MusicPlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            mediaController = controllerFuture.get()
            setupPlayerListener()
        }, MoreExecutors.directExecutor())
    }

    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                mediaItem?.let { item ->
                    val songId = item.mediaId.toLongOrNull()
                        ?: item.localConfiguration?.uri?.lastPathSegment?.toLongOrNull()
                        ?: return
                    viewModelScope.launch {
                        val song = songRepository.getSongById(songId)
                        song?.let { updateCurrentSong(it) }
                    }
                }
            }
        })

        viewModelScope.launch {
            while (true) {
                mediaController?.let { player ->
                    val pos = player.currentPosition.coerceAtLeast(0)
                    val dur = player.duration.coerceAtLeast(0)
                    val lyrics = _uiState.value.lyrics
                    val idx = LyricParser.findCurrentLine(lyrics, pos)
                    _uiState.value = _uiState.value.copy(
                        currentPosition = pos,
                        duration = dur,
                        currentLyricIndex = idx
                    )
                }
                delay(500)
            }
        }
    }

    private fun updateCurrentSong(song: Song) {
        viewModelScope.launch {
            val lyrics = LyricParser.loadLyricFromFile(song.path)
            val isFav = favoriteDao.isFavorite(song.id)
            isFav.collect { fav ->
                _uiState.value = _uiState.value.copy(
                    currentSong = song,
                    lyrics = lyrics,
                    currentLyricIndex = -1,
                    isFavorite = fav,
                    currentPosition = 0,
                    duration = song.duration
                )
            }
        }
        viewModelScope.launch {
            recentPlayDao.insert(app.music_k27_qwen_code.data.entity.RecentPlay(song.id))
        }
    }

    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty() || startIndex !in songs.indices) return
        _playlist.value = songs
        val controller = mediaController ?: return
        controller.clearMediaItems()
        songs.forEach { song ->
            val mediaItem = MediaItem.Builder()
                .setUri(Uri.parse(song.path))
                .setMediaId(song.id.toString())
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setAlbumTitle(song.album)
                        .build()
                )
                .build()
            controller.addMediaItem(mediaItem)
        }
        controller.seekTo(startIndex, 0)
        controller.prepare()
        controller.play()
        Logger.i("播放列表: ${songs.size} 首, 起始索引: $startIndex")
    }

    fun playPause() {
        mediaController?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun next() {
        mediaController?.seekToNextMediaItem()
    }

    fun previous() {
        mediaController?.seekToPreviousMediaItem()
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }

    fun toggleFavorite() {
        val song = _uiState.value.currentSong ?: return
        viewModelScope.launch {
            if (_uiState.value.isFavorite) {
                favoriteDao.removeFavorite(song.id)
            } else {
                favoriteDao.addFavorite(app.music_k27_qwen_code.data.entity.Favorite(song.id))
            }
        }
    }

    fun toggleLyricsMode() {
        _uiState.value = _uiState.value.copy(showLyrics = !_uiState.value.showLyrics)
    }

    override fun onCleared() {
        mediaController?.release()
        super.onCleared()
    }
}
