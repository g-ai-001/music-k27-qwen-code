package app.music_k27_qwen_code.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.music_k27_qwen_code.R
import app.music_k27_qwen_code.ui.navigation.Routes

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = if (currentRoute == Routes.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = stringResource(R.string.home)
                )
            },
            label = { Text(stringResource(R.string.home)) },
            selected = currentRoute == Routes.HOME,
            onClick = { onNavigate(Routes.HOME) }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = if (currentRoute == Routes.ME) Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = stringResource(R.string.me)
                )
            },
            label = { Text(stringResource(R.string.me)) },
            selected = currentRoute == Routes.ME,
            onClick = { onNavigate(Routes.ME) }
        )
    }
}
