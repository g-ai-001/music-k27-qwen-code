package app.music_k27_qwen_code.data.repository

import app.music_k27_qwen_code.data.dao.PlaylistDao
import app.music_k27_qwen_code.data.entity.Playlist
import app.music_k27_qwen_code.data.entity.PlaylistSongMap
import app.music_k27_qwen_code.data.entity.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class PlaylistRepository(private val playlistDao: PlaylistDao) {
    val allPlaylists: Flow<List<Playlist>> = playlistDao.getAllPlaylists()

    suspend fun createPlaylist(name: String): Long {
        return playlistDao.insertPlaylist(Playlist(name = name))
    }

    suspend fun deletePlaylist(id: Long) = playlistDao.deletePlaylist(id)

    suspend fun renamePlaylist(id: Long, newName: String) {
        playlistDao.updatePlaylistName(id, newName)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long, orderIndex: Int) {
        playlistDao.addSongToPlaylist(PlaylistSongMap(playlistId, songId, orderIndex))
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>> {
        return playlistDao.getSongsInPlaylist(playlistId)
    }

    suspend fun getPlaylistSongIds(playlistId: Long): List<Long> {
        return playlistDao.getSongIdsInPlaylist(playlistId).first()
    }
}
