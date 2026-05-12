package app.music_k27_qwen_code.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import app.music_k27_qwen_code.data.entity.Playlist
import app.music_k27_qwen_code.data.entity.PlaylistSongMap
import app.music_k27_qwen_code.data.entity.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSongToPlaylist(map: PlaylistSongMap)

    @Query("DELETE FROM playlist_song_map WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    @Query("UPDATE playlists SET name = :newName WHERE id = :id")
    suspend fun updatePlaylistName(id: Long, newName: String)

    @Query("SELECT songId FROM playlist_song_map WHERE playlistId = :playlistId ORDER BY orderIndex ASC")
    fun getSongIdsInPlaylist(playlistId: Long): Flow<List<Long>>

    @Transaction
    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN playlist_song_map psm ON s.id = psm.songId
        WHERE psm.playlistId = :playlistId
        ORDER BY psm.orderIndex ASC
    """)
    fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>>
}
