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
import kotlinx.coroutines.launch

data class PlayerUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0,
    val duration: Long = 0,
    val lyrics: List<app.music_k27_qwen_code.utils.LyricLine> = emptyList(),
    val currentLyricIndex: Int = -1,
    val isFavorite: Boolean = false,
    val showLyrics: Boolean = false,
    val queueSongs: List<Song> = emptyList()
)

class SharedPlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val songRepository = (application as MusicApplication).songRepository
    private val favoriteDao = (application as MusicApplication).database.favoriteDao()
    private val recentPlayDao = (application as MusicApplication).database.recentPlayDao()

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var mediaController: MediaController? = null
    private val _playlist = MutableStateFlow<List<Song>>(emptyList())
    val playlist: StateFlow<List<Song>> = _playlist.asStateFlow()

    private var favoriteCollectJob: Job? = null
    private var positionUpdateJob: Job? = null

    init {
        initMediaController(application)
    }

    private fun initMediaController(context: Context) {
        try {
            val sessionToken = SessionToken(context, ComponentName(context, MusicPlaybackService::class.java))
            val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
            controllerFuture.addListener({
                try {
                    mediaController = controllerFuture.get()
                    setupPlayerListener()
                    startPositionUpdates()
                } catch (e: Exception) {
                    Logger.e("MediaController 初始化失败", e)
                }
            }, MoreExecutors.directExecutor())
        } catch (e: Exception) {
            Logger.e("MediaSessionToken 创建失败", e)
        }
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
                        try {
                            val song = songRepository.getSongById(songId)
                            song?.let { updateCurrentSong(it) }
                        } catch (e: Exception) {
                            Logger.e("获取歌曲信息失败: $songId", e)
                        }
                    }
                }
            }
        })
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = viewModelScope.launch {
            while (true) {
                try {
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
                } catch (e: Exception) {
                    Logger.e("播放位置更新失败", e)
                }
                delay(500)
            }
        }
    }

    private fun updateCurrentSong(song: Song) {
        // 取消旧的收藏状态收集
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

        // 启动新的收藏状态收集
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
            Logger.e(" seekTo 操作失败", e)
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

    override fun onCleared() {
        favoriteCollectJob?.cancel()
        positionUpdateJob?.cancel()
        mediaController?.release()
        super.onCleared()
    }
}