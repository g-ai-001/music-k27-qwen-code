package app.music_k27_qwen_code.data.repository

import app.music_k27_qwen_code.data.dao.RecentPlayDao
import app.music_k27_qwen_code.data.entity.RecentPlay
import kotlinx.coroutines.flow.Flow

class RecentPlayRepository(private val recentPlayDao: RecentPlayDao) {
    val recentPlays: Flow<List<RecentPlay>> = recentPlayDao.getRecentPlays()

    suspend fun insert(recentPlay: RecentPlay) {
        recentPlayDao.insert(recentPlay)
    }
}
