package com.avaboost.app

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.avaboost.app.data.AvaDatabase
import com.avaboost.app.data.AvaRepository
import com.avaboost.app.model.HubGame
import com.avaboost.app.ui.*
import kotlinx.coroutines.*

class AvaBoostApp : Application() {
    lateinit var repository: AvaRepository
        private set
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    override fun onCreate() {
        super.onCreate()
        repository = AvaRepository(AvaDatabase.getInstance(this), this)
        scope.launch { repository.ensureSeeded() }
    }
}

private enum class AvaTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Rounded.Home),
    GAMES("Games", Icons.Rounded.Storefront),
    CODES("Codes", Icons.Rounded.VpnKey),
    SETTINGS("Settings", Icons.Rounded.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repo = (application as AvaBoostApp).repository
        setContent { AvaBoostTheme { AvaRoot(repo) } }
    }
}

@Composable
private fun AvaRoot(repository: AvaRepository) {
    val profile by repository.observeProfile().collectAsStateWithLifecycle(initialValue = null)
    val games by repository.observeGames().collectAsStateWithLifecycle(initialValue = emptyList())
    val slides = remember { repository.getWhatsNewSlides() }
    var currentTab by remember { mutableIntStateOf(0) }
    var monitorGame by remember { mutableStateOf<HubGame?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val showToast: (String) -> Unit = { msg -> scope.launch { snackbar.showSnackbar(msg) } }

    Scaffold(
        containerColor = AvaColors.VoidBlack,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            NavigationBar(containerColor = AvaColors.DeepNavy) {
                AvaTab.entries.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = currentTab == index,
                        onClick = { currentTab = index; if (index != 1) monitorGame = null },
                        icon = { Icon(tab.icon, tab.label) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AvaColors.CyanAccent, selectedTextColor = AvaColors.CyanAccent,
                            indicatorColor = AvaColors.ElevatedNavy, unselectedIconColor = AvaColors.TextSecondary, unselectedTextColor = AvaColors.TextSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentTab) {
                0 -> HomeTabScreen(profile, games, slides, { currentTab = 1 }, { monitorGame = it; currentTab = 1 }, { launchOfficialPackage(context, it.packageName, showToast) }, showToast)
                1 -> GamesDirectoryScreen(games, repository, monitorGame, { monitorGame = it }, { launchOfficialPackage(context, it.packageName, showToast) })
                2 -> AccessCodesScreen(profile, repository)
                3 -> SettingsTabScreen(profile, repository)
            }
        }
    }
}
