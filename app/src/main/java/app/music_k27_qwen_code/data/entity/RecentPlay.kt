package app.music_k27_qwen_code.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_plays")
data class RecentPlay(
    @PrimaryKey val songId: Long,
    val playedAt: Long = System.currentTimeMillis()
)
