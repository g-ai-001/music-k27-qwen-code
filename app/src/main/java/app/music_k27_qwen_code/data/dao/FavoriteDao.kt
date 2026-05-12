package app.music_k27_qwen_code.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.music_k27_qwen_code.data.entity.Favorite
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT songId FROM favorites")
    fun getFavoriteIds(): Flow<List<Long>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE songId = :songId LIMIT 1)")
    fun isFavorite(songId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: Favorite)

    @Query("DELETE FROM favorites WHERE songId = :songId")
    suspend fun removeFavorite(songId: Long)

    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN favorites f ON s.id = f.songId
        ORDER BY f.songId DESC
    """)
    fun getFavoriteSongs(): Flow<List<app.music_k27_qwen_code.data.entity.Song>>
}
