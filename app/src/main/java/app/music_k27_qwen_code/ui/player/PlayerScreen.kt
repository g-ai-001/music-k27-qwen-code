package app.music_k27_qwen_code.ui.player

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.music_k27_qwen_code.ui.components.QueueSheet
import app.music_k27_qwen_code.ui.theme.AccentGreen
import app.music_k27_qwen_code.utils.formatDuration
import app.music_k27_qwen_code.ui.theme.DeepPurple
import app.music_k27_qwen_code.ui.theme.HeartRed
import app.music_k27_qwen_code.utils.LyricLine
import app.music_k27_qwen_code.viewmodel.PlayerUiState
import app.music_k27_qwen_code.viewmodel.RepeatMode
import app.music_k27_qwen_code.viewmodel.SharedPlayerViewModel

@Composable
fun PlayerScreen(
    playerViewModel: SharedPlayerViewModel,
    onBack: () -> Unit
) {
    var showQueue by remember { mutableStateOf(false) }
    val state by playerViewModel.uiState.collectAsStateWithLifecycle()
    val song = state.currentSong ?: return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepPurple.copy(alpha = 0.95f), DeepPurple)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "收起",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = song.title, color = Color.White, fontSize = 14.sp, maxLines = 1)
                    Text(text = song.artist, color = Color.LightGray, fontSize = 12.sp, maxLines = 1)
                }
                IconButton(onClick = { playerViewModel.toggleFavorite() }) {
                    Icon(
                        imageVector = if (state.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "收藏",
                        tint = if (state.isFavorite) HeartRed else Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Crossfade(targetState = state.showLyrics, label = "player_mode") { showLyrics ->
                if (showLyrics) {
                    LyricsMode(state = state)
                } else {
                    CoverMode(songTitle = song.title, artist = song.artist, lyrics = state.lyrics)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(modifier = Modifier.fillMaxWidth()) {
                var sliderPosition by remember { mutableFloatStateOf(0f) }
                LaunchedEffect(state.currentPosition, state.duration) {
                    if (state.duration > 0) {
                        sliderPosition = state.currentPosition.toFloat() / state.duration
                    }
                }
                Slider(
                    value = sliderPosition,
                    onValueChange = { sliderPosition = it },
                    onValueChangeFinished = {
                        playerViewModel.seekTo((sliderPosition * state.duration).toLong())
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = AccentGreen,
                        activeTrackColor = AccentGreen,
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDuration(state.currentPosition),
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = formatDuration(state.duration),
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { playerViewModel.previous() }) {
                        Icon(
                            imageVector = Icons.Filled.SkipPrevious,
                            contentDescription = "上一首",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(AccentGreen)
                            .clickable { playerViewModel.playPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    IconButton(onClick = { playerViewModel.next() }) {
                        Icon(
                            imageVector = Icons.Filled.SkipNext,
                            contentDescription = "下一首",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val shuffleColor = if (state.shuffleEnabled) AccentGreen else Color.White.copy(alpha = 0.7f)
                    IconButton(onClick = { playerViewModel.toggleShuffle() }) {
                        Icon(
                            imageVector = Icons.Filled.Shuffle,
                            contentDescription = "随机播放",
                            tint = shuffleColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    val repeatIcon = when (state.repeatMode) {
                        RepeatMode.ONE -> Icons.Filled.RepeatOne
                        else -> Icons.Filled.Repeat
                    }
                    val repeatColor = if (state.repeatMode != RepeatMode.OFF) {
                        AccentGreen
                    } else {
                        Color.White.copy(alpha = 0.7f)
                    }
                    IconButton(onClick = { playerViewModel.cycleRepeatMode() }) {
                        Icon(
                            imageVector = repeatIcon,
                            contentDescription = "循环模式",
                            tint = repeatColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ToggleButton(text = "词", active = state.showLyrics) {
                        playerViewModel.toggleLyricsMode()
                    }
                    ToggleButton(text = "队列", active = false) {
                        showQueue = true
                    }
                }
            }
        }
    }

    if (showQueue) {
        QueueSheet(
            playerViewModel = playerViewModel,
            onDismiss = { showQueue = false }
        )
    }
}

@Composable
fun CoverMode(songTitle: String, artist: String, lyrics: List<LyricLine>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = songTitle,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = artist,
            color = Color.LightGray,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        if (lyrics.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = lyrics.firstOrNull()?.text ?: "",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LyricsMode(state: PlayerUiState) {
    val scrollState = rememberScrollState()
    LaunchedEffect(state.currentLyricIndex) {
        if (state.currentLyricIndex >= 0) {
            scrollState.animateScrollTo(state.currentLyricIndex * 48)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        state.lyrics.forEachIndexed { index, line ->
            val isCurrent = index == state.currentLyricIndex
            Text(
                text = line.text,
                color = if (isCurrent) Color.White else Color.White.copy(alpha = 0.4f),
                fontSize = if (isCurrent) 20.sp else 16.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .fillMaxWidth()
            )
        }
        if (state.lyrics.isEmpty()) {
            Text("暂无歌词", color = Color.White.copy(alpha = 0.4f), fontSize = 16.sp)
        }
    }
}

@Composable
fun ToggleButton(text: String, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (active) AccentGreen else Color.White.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (active) Color.White else Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp
        )
    }
}
