package app.music_k27_qwen_code.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.music_k27_qwen_code.data.dao.FavoriteDao
import app.music_k27_qwen_code.data.dao.PlaylistDao
import app.music_k27_qwen_code.data.dao.RecentPlayDao
import app.music_k27_qwen_code.data.dao.SongDao
import app.music_k27_qwen_code.data.entity.Favorite
import app.music_k27_qwen_code.data.entity.Playlist
import app.music_k27_qwen_code.data.entity.PlaylistSongMap
import app.music_k27_qwen_code.data.entity.RecentPlay
import app.music_k27_qwen_code.data.entity.Song

@Database(
    entities = [Song::class, Playlist::class, PlaylistSongMap::class, Favorite::class, RecentPlay::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun recentPlayDao(): RecentPlayDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "music_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
