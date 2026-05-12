package app.music_k27_qwen_code.ui.playlist

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PlaylistDetailViewModelFactory(
    private val application: Application,
    private val playlistId: Long
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PlaylistDetailViewModel(application, playlistId) as T
    }
}
