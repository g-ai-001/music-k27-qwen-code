package app.music_k27_qwen_code.utils

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import app.music_k27_qwen_code.data.entity.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MediaScanner {

    suspend fun scanLocalMusic(context: Context, existingSongs: List<Song> = emptyList()): ScanResult = withContext(Dispatchers.IO) {
        val scannedSongs = mutableListOf<Song>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > 30000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(collection, projection, selection, null, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val albumId = cursor.getLong(albumIdCol)
                val albumArtUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"), albumId
                ).toString()

                scannedSongs.add(
                    Song(
                        id = id,
                        title = cursor.getString(titleCol) ?: "未知标题",
                        artist = cursor.getString(artistCol) ?: "未知艺术家",
                        album = cursor.getString(albumCol) ?: "未知专辑",
                        duration = cursor.getLong(durationCol),
                        path = cursor.getString(dataCol) ?: "",
                        albumArtUri = albumArtUri
                    )
                )
            }
        }

        val existingMap = existingSongs.associateBy { it.id }
        val toAdd = scannedSongs.filter { it.id !in existingMap }
        val scannedIds = scannedSongs.map { it.id }.toSet()
        val toRemove = existingSongs.filter { it.id !in scannedIds }
        val unchanged = scannedSongs.filter { it.id in existingMap }

        Logger.i("扫描到 ${scannedSongs.size} 首本地歌曲，新增 ${toAdd.size} 首，移除 ${toRemove.size} 首，不变 ${unchanged.size} 首")
        ScanResult(toAdd, toRemove, unchanged)
    }
}

data class ScanResult(
    val toAdd: List<Song>,
    val toRemove: List<Song>,
    val unchanged: List<Song>
)
