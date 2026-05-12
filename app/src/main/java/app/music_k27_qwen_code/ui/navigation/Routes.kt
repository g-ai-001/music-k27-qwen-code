package app.music_k27_qwen_code.ui.navigation

object Routes {
    const val HOME = "home"
    const val ME = "me"
    const val PLAYER = "player"
    const val FAVORITES = "favorites"
    const val PLAYLIST_DETAIL = "playlist_detail/{playlistId}/{playlistName}"

    fun playlistDetail(playlistId: Long, playlistName: String): String {
        return "playlist_detail/$playlistId/$playlistName"
    }
}