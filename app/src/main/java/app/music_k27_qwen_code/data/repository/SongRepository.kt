package app.music_k27_qwen_code.data.repository

import app.music_k27_qwen_code.data.dao.SongDao
import app.music_k27_qwen_code.data.entity.Song
import kotlinx.coroutines.flow.Flow

class SongRepository(private val songDao: SongDao) {
    val allSongs: Flow<List<Song>> = songDao.getAllSongs()

    suspend fun getSongById(id: Long): Song? = songDao.getSongById(id)

    suspend fun insertAll(songs: List<Song>) = songDao.insertAll(songs)

    suspend fun deleteAll() = songDao.deleteAll()

    fun searchSongs(query: String): Flow<List<Song>> = songDao.searchSongs(query)
}
