package app.music_k27_qwen_code

import android.app.Application
import app.music_k27_qwen_code.data.AppDatabase
import app.music_k27_qwen_code.data.repository.FavoriteRepository
import app.music_k27_qwen_code.data.repository.PlaylistRepository
import app.music_k27_qwen_code.data.repository.RecentPlayRepository
import app.music_k27_qwen_code.data.repository.SongRepository
import app.music_k27_qwen_code.utils.Logger

class MusicApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val songRepository by lazy { SongRepository(database.songDao()) }
    val playlistRepository by lazy { PlaylistRepository(database.playlistDao()) }
    val favoriteRepository by lazy { FavoriteRepository(database.favoriteDao()) }
    val recentPlayRepository by lazy { RecentPlayRepository(database.recentPlayDao()) }

    override fun onCreate() {
        super.onCreate()
        Logger.init(this)
        Logger.i("MusicApplication onCreate")
    }
}
