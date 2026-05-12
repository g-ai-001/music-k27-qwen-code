package app.music_k27_qwen_code.data.repository

import app.music_k27_qwen_code.data.dao.PlaylistDao
import app.music_k27_qwen_code.data.entity.Playlist
import app.music_k27_qwen_code.data.entity.PlaylistSongMap
import app.music_k27_qwen_code.data.entity.Song
import kotlinx.coroutines.flow.Flow

class PlaylistRepository(private val playlistDao: PlaylistDao) {
    val allPlaylists: Flow<List<Playlist>> = playlistDao.getAllPlaylists()

    suspend fun createPlaylist(name: String): Long {
        return playlistDao.insertPlaylist(Playlist(name = name))
    }

    suspend fun deletePlaylist(id: Long) = playlistDao.deletePlaylist(id)

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long, orderIndex: Int) {
        playlistDao.addSongToPlaylist(PlaylistSongMap(playlistId, songId, orderIndex))
    }

    fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>> {
        return playlistDao.getSongsInPlaylist(playlistId)
    }
}
