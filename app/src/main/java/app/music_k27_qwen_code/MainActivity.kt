package app.music_k27_qwen_code

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.music_k27_qwen_code.ui.components.BottomNavBar
import app.music_k27_qwen_code.ui.components.MiniPlayer
import app.music_k27_qwen_code.ui.home.HomeScreen
import app.music_k27_qwen_code.ui.me.MeScreen
import app.music_k27_qwen_code.ui.player.PlayerScreen
import app.music_k27_qwen_code.ui.theme.MusicK27QwenCodeTheme
import app.music_k27_qwen_code.utils.Logger
import app.music_k27_qwen_code.viewmodel.SharedPlayerViewModel

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Logger.i("存储权限已授予，触发音乐扫描")
            // 权限授予后由 HomeViewModel 自动扫描
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()
        setContent {
            MusicK27QwenCodeTheme {
                MusicApp()
            }
        }
    }

    private fun requestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        val needRequest = permissions.any {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needRequest) {
            permissionLauncher.launch(permissions)
        }
    }
}

@Composable
fun MusicApp() {
    val navController = rememberNavController()
    val playerViewModel: SharedPlayerViewModel = viewModel()
    val playerState by playerViewModel.uiState.collectAsStateWithLifecycle()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != "player") {
                Column {
                    MiniPlayer(
                        playerState = playerState,
                        onTogglePlay = { playerViewModel.playPause() },
                        onNavigateToPlayer = { navController.navigate("player") },
                        modifier = Modifier
                    )
                    BottomNavBar(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            if (currentRoute != route) {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen(playerViewModel = playerViewModel) }
            composable("me") { MeScreen(playerViewModel = playerViewModel) }
            composable("player") {
                PlayerScreen(
                    playerViewModel = playerViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
