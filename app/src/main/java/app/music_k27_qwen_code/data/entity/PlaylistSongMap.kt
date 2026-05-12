package app.music_k27_qwen_code.data.entity

import androidx.room.Entity

@Entity(
    tableName = "playlist_song_map",
    primaryKeys = ["playlistId", "songId"]
)
data class PlaylistSongMap(
    val playlistId: Long,
    val songId: Long,
    val orderIndex: Int = 0
)
