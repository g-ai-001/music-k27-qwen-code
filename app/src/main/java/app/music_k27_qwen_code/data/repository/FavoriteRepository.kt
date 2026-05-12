package app.music_k27_qwen_code.data.repository

import app.music_k27_qwen_code.data.dao.FavoriteDao
import app.music_k27_qwen_code.data.entity.Favorite
import app.music_k27_qwen_code.data.entity.Song
import kotlinx.coroutines.flow.Flow

class FavoriteRepository(private val favoriteDao: FavoriteDao) {
    val favoriteSongs: Flow<List<Song>> = favoriteDao.getFavoriteSongs()
    val favoriteIds: Flow<List<Long>> = favoriteDao.getFavoriteIds()

    fun isFavorite(songId: Long): Flow<Boolean> = favoriteDao.isFavorite(songId)

    suspend fun addFavorite(songId: Long) {
        favoriteDao.addFavorite(Favorite(songId))
    }

    suspend fun removeFavorite(songId: Long) {
        favoriteDao.removeFavorite(songId)
    }
}
