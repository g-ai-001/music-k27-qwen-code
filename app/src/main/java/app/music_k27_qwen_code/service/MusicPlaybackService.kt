package app.music_k27_qwen_code.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import app.music_k27_qwen_code.MainActivity
import app.music_k27_qwen_code.R
import app.music_k27_qwen_code.utils.Logger

class MusicPlaybackService : MediaSessionService() {
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "music_playback_channel"
    }

    private var mediaSession: MediaSession? = null
    private var exoPlayer: ExoPlayer? = null
    private var notificationManager: PlayerNotificationManager? = null

    override fun onCreate() {
        super.onCreate()
        Logger.i("MusicPlaybackService onCreate")
        val player = ExoPlayer.Builder(this).build()
        exoPlayer = player
        mediaSession = MediaSession.Builder(this, player).build()
        setupNotification(player)
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
                    stopSelf()
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

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        Logger.i("MusicPlaybackService onDestroy")
        notificationManager?.setPlayer(null)
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        exoPlayer = null
        super.onDestroy()
    }
}
