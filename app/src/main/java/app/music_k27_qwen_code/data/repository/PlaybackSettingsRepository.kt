package app.music_k27_qwen_code.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.music_k27_qwen_code.utils.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "playback_settings")

class PlaybackSettingsRepository(private val context: Context) {

    companion object {
        private val SHUFFLE_ENABLED = booleanPreferencesKey("shuffle_enabled")
        private val REPEAT_MODE = intPreferencesKey("repeat_mode")
    }

    val shuffleEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[SHUFFLE_ENABLED] ?: false }
        .also { Logger.d("PlaybackSettingsRepository shuffle flow initialized") }

    val repeatMode: Flow<Int> = context.dataStore.data
        .map { it[REPEAT_MODE] ?: 0 }
        .also { Logger.d("PlaybackSettingsRepository repeat flow initialized") }

    suspend fun setShuffleEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SHUFFLE_ENABLED] = enabled
        }
        Logger.i("播放设置: 随机播放=$enabled")
    }

    suspend fun setRepeatMode(mode: Int) {
        context.dataStore.edit { prefs ->
            prefs[REPEAT_MODE] = mode
        }
        Logger.i("播放设置: 循环模式=$mode")
    }
}
