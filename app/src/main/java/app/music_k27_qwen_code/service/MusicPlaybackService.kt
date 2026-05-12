package app.music_k27_qwen_code.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import app.music_k27_qwen_code.MainActivity
import app.music_k27_qwen_code.R
import app.music_k27_qwen_code.utils.Logger

class MusicPlaybackService : MediaSessionService(), AudioManager.OnAudioFocusChangeListener {

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "music_playback_channel"
    }

    private var mediaSession: MediaSession? = null
    private var exoPlayer: ExoPlayer? = null
    private var notificationManager: PlayerNotificationManager? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var hasAudioFocus = false
    private var pausedByFocusLoss = false

    override fun onCreate() {
        super.onCreate()
        Logger.i("MusicPlaybackService onCreate")
        audioManager = getSystemService(AUDIO_SERVICE) as? AudioManager
        val player = ExoPlayer.Builder(this).build().apply {
            setAudioAttributes(
                androidx.media3.common.AudioAttributes.Builder()
                    .setUsage(androidx.media3.common.C.USAGE_MEDIA)
                    .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
        }
        exoPlayer = player
        mediaSession = MediaSession.Builder(this, player)
            .setCallback(MediaSessionCallback())
            .build()
        setupNotification(player)
        setupPlayerListener(player)
    }

    private fun setupNotification(player: ExoPlayer) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        notificationManager = PlayerNotificationManager.Builder(
            this, NOTIFICATION_ID, CHANNEL_ID
        )
            .setChannelNameResourceId(R.string.channel_name)
            .setChannelDescriptionResourceId(R.string.channel_description)
            .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: Player): CharSequence {
                    return player.currentMediaItem?.mediaMetadata?.title ?: getString(R.string.unknown_title)
                }

                override fun createCurrentContentIntent(player: Player): PendingIntent? = pendingIntent

                override fun getCurrentContentText(player: Player): CharSequence? {
                    return player.currentMediaItem?.mediaMetadata?.artist ?: getString(R.string.unknown_artist)
                }

                override fun getCurrentLargeIcon(
                    player: Player,
                    callback: PlayerNotificationManager.BitmapCallback
                ) = null
            })
            .setNotificationListener(object : PlayerNotificationManager.NotificationListener {
                override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
                    startForeground(notificationId, notification)
                }

                override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                    if (dismissedByUser) {
                        stopSelf()
                    }
                }
            })
            .build()
            .apply {
                setPlayer(player)
                setUseRewindAction(false)
                setUseFastForwardAction(false)
                setUseStopAction(false)
                setUsePreviousAction(true)
                setUseNextAction(true)
                setUsePlayPauseActions(true)
            }
    }

    private fun setupPlayerListener(player: ExoPlayer) {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    requestAudioFocus()
                } else {
                    abandonAudioFocus()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                mediaItem?.let {
                    Logger.i("播放歌曲切换: ${it.mediaMetadata.title}")
                }
            }
        })
    }

    private fun requestAudioFocus() {
        if (hasAudioFocus) return
        val am = audioManager ?: return
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setOnAudioFocusChangeListener(this)
                .build()
                .also { audioFocusRequest = it }
            am.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            am.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
        hasAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        Logger.i("请求音频焦点: ${if (hasAudioFocus) "成功" else "失败"}")
    }

    private fun abandonAudioFocus() {
        if (!hasAudioFocus) return
        val am = audioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { am.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            am.abandonAudioFocus(this)
        }
        hasAudioFocus = false
        Logger.i("放弃音频焦点")
    }

    override fun onAudioFocusChange(focusChange: Int) {
        val player = exoPlayer ?: return
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                Logger.i("音频焦点: GAIN")
                if (pausedByFocusLoss && !player.isPlaying) {
                    player.play()
                }
                pausedByFocusLoss = false
                player.volume = 1f
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                Logger.i("音频焦点: LOSS")
                hasAudioFocus = false
                if (player.isPlaying) {
                    pausedByFocusLoss = true
                    player.pause()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                Logger.i("音频焦点: LOSS_TRANSIENT")
                if (player.isPlaying) {
                    pausedByFocusLoss = true
                    player.pause()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                Logger.i("音频焦点: LOSS_TRANSIENT_CAN_DUCK")
                player.volume = 0.2f
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        Logger.i("MusicPlaybackService onDestroy")
        abandonAudioFocus()
        notificationManager?.setPlayer(null)
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        exoPlayer = null
        super.onDestroy()
    }

    private inner class MediaSessionCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val connectionResult = super.onConnect(session, controller)
            val availableSessionCommands = connectionResult.availableSessionCommands
                .buildUpon()
                .build()
            return MediaSession.ConnectionResult.accept(
                availableSessionCommands,
                connectionResult.availablePlayerCommands
            )
        }

        override fun onPause(session: MediaSession, controller: MediaSession.ControllerInfo): ListenableFuture<MediaSession.SessionResult> {
            pausedByFocusLoss = false
            return super.onPause(session, controller)
        }

        override fun onPlay(session: MediaSession, controller: MediaSession.ControllerInfo): ListenableFuture<MediaSession.SessionResult> {
            pausedByFocusLoss = false
            return super.onPlay(session, controller)
        }
    }
}
