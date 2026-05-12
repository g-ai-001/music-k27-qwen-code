package app.music_k27_qwen_code.ui.favorite

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.music_k27_qwen_code.R
import app.music_k27_qwen_code.data.entity.Song
import app.music_k27_qwen_code.ui.components.SongListItem
import app.music_k27_qwen_code.viewmodel.SharedPlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteSongsScreen(
    playerViewModel: SharedPlayerViewModel,
    onBack: () -> Unit
) {
    val viewModel: FavoriteSongsViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf<Song?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_favorites)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        if (state.songs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无收藏歌曲", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.songs) { song ->
                    SongListItem(
                        song = song,
                        onClick = { playerViewModel.playSongs(state.songs, state.songs.indexOf(song)) },
                        trailingIcon = androidx.compose.material.icons.Icons.Filled.Delete,
                        onTrailingClick = {
                            selectedSong = song
                            showDeleteDialog = true
                        },
                        trailingIconTint = androidx.compose.ui.graphics.Color.Red.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }

    if (showDeleteDialog && selectedSong != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("取消收藏") },
            text = { Text("确定取消收藏 \"${selectedSong!!.title}\" 吗？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeFavorite(selectedSong!!.id)
                    showDeleteDialog = false
                    selectedSong = null
                }) {
                    Text("取消收藏", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

