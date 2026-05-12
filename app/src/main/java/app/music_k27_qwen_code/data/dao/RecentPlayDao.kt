package app.music_k27_qwen_code.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.music_k27_qwen_code.data.entity.RecentPlay
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentPlayDao {
    @Query("SELECT * FROM recent_plays ORDER BY playedAt DESC LIMIT 20")
    fun getRecentPlays(): Flow<List<RecentPlay>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recentPlay: RecentPlay)
}
