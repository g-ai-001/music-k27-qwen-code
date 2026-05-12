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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class RepeatMode(val value: Int) {
    OFF(0),
    ALL(1),
    ONE(2)
}

data class PlayerUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0,
    val duration: Long = 0,
    val lyrics: List<app.music_k27_qwen_code.utils.LyricLine> = emptyList(),
    val currentLyricIndex: Int = -1,
    val isFavorite: Boolean = false,
    val showLyrics: Boolean = false,
    val queueSongs: List<Song> = emptyList(),
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF
)

class SharedPlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val songRepository = (application as MusicApplication).songRepository
    private val favoriteDao = (application as MusicApplication).database.favoriteDao()
    private val recentPlayDao = (application as MusicApplication).database.recentPlayDao()
    private val playbackSettingsRepository = (application as MusicApplication).playbackSettingsRepository

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var mediaController: MediaController? = null
    private val _playlist = MutableStateFlow<List<Song>>(emptyList())
    val playlist: StateFlow<List<Song>> = _playlist.asStateFlow()

    private var favoriteCollectJob: Job? = null
    private var positionUpdateJob: Job? = null
    private var settingsCollectJob: Job? = null
    private var playerListener: Player.Listener? = null

    init {
        initMediaController(application)
        loadPlaybackSettings()
    }

    protected open fun initMediaController(context: Context) {
        try {
            val sessionToken = SessionToken(context, ComponentName(context, MusicPlaybackService::class.java))
            val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
            controllerFuture.addListener({
                try {
                    onMediaControllerReady(controllerFuture.get())
                } catch (e: Exception) {
                    Logger.e("MediaController 初始化失败", e)
                }
            }, MoreExecutors.directExecutor())
        } catch (e: Exception) {
            Logger.e("MediaSessionToken 创建失败", e)
        }
    }

    protected open fun onMediaControllerReady(controller: MediaController?) {
        controller?.let {
            mediaController = it
            setupPlayerListener()
            startPositionUpdates()
            applyPlaybackSettingsToPlayer()
        }
    }

    private fun loadPlaybackSettings() {
        settingsCollectJob?.cancel()
        settingsCollectJob = viewModelScope.launch {
            try {
                launch {
                    playbackSettingsRepository.shuffleEnabled.collect { enabled ->
                        _uiState.value = _uiState.value.copy(shuffleEnabled = enabled)
                        mediaController?.let {
                            if (it.shuffleModeEnabled != enabled) {
                                it.shuffleModeEnabled = enabled
                            }
                        }
                    }
                }
                launch {
                    playbackSettingsRepository.repeatMode.collect { modeValue ->
                        val mode = RepeatMode.entries.find { it.value == modeValue } ?: RepeatMode.OFF
                        _uiState.value = _uiState.value.copy(repeatMode = mode)
                        mediaController?.let {
                            val exoRepeatMode = when (mode) {
                                RepeatMode.OFF -> Player.REPEAT_MODE_OFF
                                RepeatMode.ALL -> Player.REPEAT_MODE_ALL
                                RepeatMode.ONE -> Player.REPEAT_MODE_ONE
                            }
                            if (it.repeatMode != exoRepeatMode) {
                                it.repeatMode = exoRepeatMode
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Logger.e("加载播放设置失败", e)
            }
        }
    }

    private fun applyPlaybackSettingsToPlayer() {
        val controller = mediaController ?: return
        viewModelScope.launch {
            try {
                val shuffle = playbackSettingsRepository.shuffleEnabled.first()
                controller.shuffleModeEnabled = shuffle
                val repeatValue = playbackSettingsRepository.repeatMode.first()
                controller.repeatMode = when (repeatValue) {
                    0 -> Player.REPEAT_MODE_OFF
                    1 -> Player.REPEAT_MODE_ALL
                    2 -> Player.REPEAT_MODE_ONE
                    else -> Player.REPEAT_MODE_OFF
                }
            } catch (e: Exception) {
                Logger.e("应用播放设置到播放器失败", e)
            }
        }
    }

    private fun setupPlayerListener() {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                mediaItem?.let { item ->
                    val songId = item.mediaId.toLongOrNull()
                        ?: item.localConfiguration?.uri?.lastPathSegment?.toLongOrNull()
                        ?: return
                    viewModelScope.launch {
                        try {
                            val song = songRepository.getSongById(songId)
                            song?.let { updateCurrentSong(it) }
                        } catch (e: Exception) {
                            Logger.e("获取歌曲信息失败: $songId", e)
                        }
                    }
                }
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _uiState.value = _uiState.value.copy(shuffleEnabled = shuffleModeEnabled)
                viewModelScope.launch {
                    try {
                        playbackSettingsRepository.setShuffleEnabled(shuffleModeEnabled)
                    } catch (e: Exception) {
                        Logger.e("保存随机播放设置失败", e)
                    }
                }
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                val mode = when (repeatMode) {
                    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                    else -> RepeatMode.OFF
                }
                _uiState.value = _uiState.value.copy(repeatMode = mode)
                viewModelScope.launch {
                    try {
                        playbackSettingsRepository.setRepeatMode(mode.value)
                    } catch (e: Exception) {
                        Logger.e("保存循环模式设置失败", e)
                    }
                }
            }
        }
        playerListener = listener
        mediaController?.addListener(listener)
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        val player = mediaController ?: return
        positionUpdateJob = viewModelScope.launch {
            while (isActive) {
                try {
                    val pos = player.currentPosition.coerceAtLeast(0)
                    val dur = player.duration.coerceAtLeast(0)
                    val lyrics = _uiState.value.lyrics
                    val idx = LyricParser.findCurrentLine(lyrics, pos)
                    _uiState.value = _uiState.value.copy(
                        currentPosition = pos,
                        duration = dur,
                        currentLyricIndex = idx
                    )
                } catch (e: Exception) {
                    Logger.e("播放位置更新失败", e)
                }
                delay(500)
            }
        }
    }

    private fun updateCurrentSong(song: Song) {
        favoriteCollectJob?.cancel()

        viewModelScope.launch {
            try {
                val lyrics = LyricParser.loadLyricFromFile(song.path)
                _uiState.value = _uiState.value.copy(
                    currentSong = song,
                    lyrics = lyrics,
                    currentLyricIndex = -1,
                    currentPosition = 0,
                    duration = song.duration
                )
            } catch (e: Exception) {
                Logger.e("歌词加载失败: ${song.path}", e)
                _uiState.value = _uiState.value.copy(
                    currentSong = song,
                    lyrics = emptyList(),
                    currentLyricIndex = -1,
                    currentPosition = 0,
                    duration = song.duration
                )
            }
        }

        favoriteCollectJob = viewModelScope.launch {
            try {
                favoriteDao.isFavorite(song.id).collect { isFav ->
                    _uiState.value = _uiState.value.copy(isFavorite = isFav)
                }
            } catch (e: Exception) {
                Logger.e("收藏状态监听失败: ${song.id}", e)
            }
        }

        viewModelScope.launch {
            try {
                recentPlayDao.insert(app.music_k27_qwen_code.data.entity.RecentPlay(song.id))
            } catch (e: Exception) {
                Logger.e("最近播放记录插入失败: ${song.id}", e)
            }
        }
    }

    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty() || startIndex !in songs.indices) {
            Logger.w("播放列表为空或起始索引无效")
            return
        }
        _playlist.value = songs
        val controller = mediaController ?: run {
            Logger.w("MediaController 未初始化，无法播放")
            return
        }
        try {
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
            _uiState.value = _uiState.value.copy(queueSongs = songs)
            Logger.i("播放列表: ${songs.size} 首, 起始索引: $startIndex")
        } catch (e: Exception) {
            Logger.e("播放歌曲失败", e)
        }
    }

    fun removeFromQueue(index: Int) {
        val controller = mediaController ?: return
        val currentList = _playlist.value.toMutableList()
        if (index !in currentList.indices) return
        currentList.removeAt(index)
        _playlist.value = currentList
        _uiState.value = _uiState.value.copy(queueSongs = currentList)
        controller.removeMediaItem(index)
        Logger.i("从播放队列移除歌曲, 索引: $index")
    }

    fun clearQueue() {
        val controller = mediaController ?: return
        _playlist.value = emptyList()
        _uiState.value = _uiState.value.copy(queueSongs = emptyList())
        controller.clearMediaItems()
        Logger.i("清空播放队列")
    }

    fun playPause() {
        try {
            mediaController?.let {
                if (it.isPlaying) it.pause() else it.play()
            }
        } catch (e: Exception) {
            Logger.e("播放/暂停操作失败", e)
        }
    }

    fun next() {
        try {
            mediaController?.seekToNextMediaItem()
        } catch (e: Exception) {
            Logger.e("下一首操作失败", e)
        }
    }

    fun previous() {
        try {
            mediaController?.seekToPreviousMediaItem()
        } catch (e: Exception) {
            Logger.e("上一首操作失败", e)
        }
    }

    fun seekTo(position: Long) {
        try {
            mediaController?.seekTo(position)
        } catch (e: Exception) {
            Logger.e("seekTo 操作失败", e)
        }
    }

    fun toggleFavorite() {
        val song = _uiState.value.currentSong ?: return
        viewModelScope.launch {
            try {
                if (_uiState.value.isFavorite) {
                    favoriteDao.removeFavorite(song.id)
                } else {
                    favoriteDao.addFavorite(app.music_k27_qwen_code.data.entity.Favorite(song.id))
                }
            } catch (e: Exception) {
                Logger.e("切换收藏状态失败: ${song.id}", e)
            }
        }
    }

    fun toggleLyricsMode() {
        _uiState.value = _uiState.value.copy(showLyrics = !_uiState.value.showLyrics)
    }

    fun toggleShuffle() {
        val controller = mediaController ?: return
        try {
            controller.shuffleModeEnabled = !controller.shuffleModeEnabled
        } catch (e: Exception) {
            Logger.e("切换随机播放失败", e)
        }
    }

    fun cycleRepeatMode() {
        val controller = mediaController ?: return
        try {
            val nextMode = when (_uiState.value.repeatMode) {
                RepeatMode.OFF -> Player.REPEAT_MODE_ALL
                RepeatMode.ALL -> Player.REPEAT_MODE_ONE
                RepeatMode.ONE -> Player.REPEAT_MODE_OFF
            }
            controller.repeatMode = nextMode
        } catch (e: Exception) {
            Logger.e("切换循环模式失败", e)
        }
    }

    override fun onCleared() {
        favoriteCollectJob?.cancel()
        positionUpdateJob?.cancel()
        settingsCollectJob?.cancel()
        val listener = playerListener
        val controller = mediaController
        if (listener != null && controller != null) {
            controller.removeListener(listener)
        }
        playerListener = null
        mediaController?.release()
        mediaController = null
        super.onCleared()
    }
}
