package com.avaboost.app.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avaboost.app.BuildConfig
import com.avaboost.app.data.AvaRepository
import com.avaboost.app.model.HubGame
import com.avaboost.app.model.UserProfile
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun GamesDirectoryScreen(
    games: List<HubGame>, repository: AvaRepository,
    selectedMonitorGame: HubGame?, onSelectMonitorGame: (HubGame?) -> Unit, onLaunchGame: (HubGame) -> Unit
) {
    val scope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }
    if (selectedMonitorGame != null) {
        val ram = remember { repository.getDeviceRamInfo() }
        val ramText = String.format(Locale.US, "RAM %.1f GB / %.1f GB", ram.usedGb, ram.totalGb)
        Column(modifier = Modifier.fillMaxSize().background(AvaColors.VoidBlack)) {
            Row(modifier = Modifier.fillMaxWidth().background(AvaColors.DeepNavy).padding(horizontal = 8.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onSelectMonitorGame(null) }) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = AvaColors.TextPrimary) }
                Text("Session Monitor", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AvaColors.TextPrimary)
            }
            Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${selectedMonitorGame.title} • v${selectedMonitorGame.versionName}", fontSize = 14.sp, color = AvaColors.TextSecondary)
                    Spacer(Modifier.height(14.dp))
                    StatusBadge(if (selectedMonitorGame.lastSessionActive) "READY / LAST LAUNCHED" else "STANDBY • UNLOCKED", AvaColors.CyanAccent)
                    Spacer(Modifier.height(18.dp))
                    Button(onClick = { scope.launch { repository.markSessionLaunched(selectedMonitorGame.packageName, true) }; onLaunchGame(selectedMonitorGame) }, enabled = selectedMonitorGame.isInstalled, colors = ButtonDefaults.buttonColors(containerColor = AvaColors.CyanAccent, contentColor = AvaColors.VoidBlack)) {
                        Icon(Icons.Rounded.Launch, null); Spacer(Modifier.width(8.dp)); Text("Launch Official Game", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Surface(color = AvaColors.DeepNavy, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Memory, null, tint = AvaColors.CyanAccent, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
                    Text("Mode: Free Hub • $ramText", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AvaColors.TextPrimary)
                }
            }
        }
        return
    }
    Box(modifier = Modifier.fillMaxSize().background(AvaColors.VoidBlack)) {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("Game Hub & Monitor", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AvaColors.TextPrimary)
                Text("Tap a game to open its Session & RAM Monitor.", fontSize = 13.sp, color = AvaColors.TextSecondary)
            }
            items(games, key = { it.packageName }) { game ->
                AvaCard(modifier = Modifier.clickable { onSelectMonitorGame(game) }) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(game.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AvaColors.TextPrimary)
                            Text("v${game.versionName} • ${game.packageName}", fontSize = 12.sp, color = AvaColors.TextSecondary)
                        }
                        IconButton(onClick = { scope.launch { repository.removeGame(game.packageName) } }) {
                            Icon(Icons.Rounded.DeleteOutline, "Remove", tint = AvaColors.TextSecondary)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { onSelectMonitorGame(game) }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Rounded.Tune, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Monitor")
                        }
                        Button(onClick = { scope.launch { repository.markSessionLaunched(game.packageName, true) }; onLaunchGame(game) }, enabled = game.isInstalled, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = AvaColors.CyanAccent, contentColor = AvaColors.VoidBlack)) {
                            Icon(Icons.Rounded.Launch, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Launch", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        FloatingActionButton(onClick = { showAddDialog = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp), containerColor = AvaColors.CyanAccent, contentColor = AvaColors.VoidBlack) {
            Icon(Icons.Rounded.Add, "Add Game")
        }
    }
    if (showAddDialog) {
        val choices = remember { repository.queryInstalledLauncherApps() }
        AlertDialog(onDismissRequest = { showAddDialog = false }, containerColor = AvaColors.CardNavy, title = { Text("Select Installed Game") },
            text = {
                LazyColumn(modifier = Modifier.height(320.dp)) {
                    items(choices, key = { it.packageName }) { item ->
                        Column(modifier = Modifier.fillMaxWidth().clickable { scope.launch { repository.addGame(item) }; showAddDialog = false }.padding(vertical = 10.dp)) {
                            Text(item.label, fontWeight = FontWeight.Bold, color = AvaColors.TextPrimary)
                            Text("v${item.versionName} • ${item.packageName}", fontSize = 12.sp, color = AvaColors.TextSecondary)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showAddDialog = false }) { Text("Close") } }
        )
    }
}

@Composable
fun AccessCodesScreen(profile: UserProfile?, repository: AvaRepository) {
    val scope = rememberCoroutineScope()
    var codeInput by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf<String?>(null) }
    LazyColumn(modifier = Modifier.fillMaxSize().background(AvaColors.VoidBlack), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Access & Redeem Codes", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AvaColors.TextPrimary) }
        item {
            AvaCard(highlight = true) {
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.LockOpen, null, tint = AvaColors.UnlockedGreen); Spacer(Modifier.width(8.dp)); Text("100% Free & Unlocked Mode", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AvaColors.TextPrimary) }
                Spacer(Modifier.height(6.dp))
                Text("All game launcher & RAM monitor features are free. Status: ${profile?.accessLabel ?: "FREE UNLOCKED"}", fontSize = 13.sp, color = AvaColors.TextSecondary)
            }
        }
        item {
            AvaCard {
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.VpnKey, null, tint = AvaColors.CyanAccent); Spacer(Modifier.width(8.dp)); Text("Redeem Feature / Update Code", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AvaColors.TextPrimary) }
                Spacer(Modifier.height(6.dp)); Text("Demo code: AVA-FREE-2026", fontSize = 12.sp, color = AvaColors.TextSecondary); Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = codeInput, onValueChange = { codeInput = it }, label = { Text("Enter Code") }, singleLine = true, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(10.dp))
                Button(onClick = { scope.launch { feedback = repository.redeemAccessCode(codeInput); codeInput = "" } }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = AvaColors.CyanAccent, contentColor = AvaColors.VoidBlack)) {
                    Text("Redeem Code", fontWeight = FontWeight.Bold)
                }
                feedback?.let { Spacer(Modifier.height(8.dp)); Text(it, fontSize = 13.sp, color = AvaColors.CyanAccent) }
            }
        }
    }
}

@Composable
fun SettingsTabScreen(profile: UserProfile?, repository: AvaRepository) {
    val scope = rememberCoroutineScope()
    val certSha = remember { repository.getInstalledCertSha256() }
    var dialog by remember { mutableStateOf<String?>(null) }
    LazyColumn(modifier = Modifier.fillMaxSize().background(AvaColors.VoidBlack), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Settings & Updates", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AvaColors.TextPrimary) }
        item { SettingItemCard(Icons.Rounded.SystemUpdate, "AVA Boost Updates", "v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE}) • SHA-256 Protected") { dialog = "update" } }
        item { SettingItemCard(Icons.Rounded.Person, "Profile", "${profile?.displayName ?: "Player"} • ID: ${profile?.deviceId ?: ""}") { dialog = "profile" } }
        item { SettingItemCard(Icons.Rounded.Policy, "Privacy Policy", "Local Room DB • Official launcher intents only") { dialog = "privacy" } }
        item { SettingItemCard(Icons.Rounded.HelpOutline, "Help & Support", "Admin updates & Firebase connection info") { dialog = "support" } }
        item { SettingItemCard(Icons.Rounded.Refresh, "Reset Device ID", "Generate a fresh local Device ID") { scope.launch { repository.resetDeviceId() } } }
    }
    if (dialog == "profile") {
        var input by remember(profile?.displayName) { mutableStateOf(profile?.displayName.orEmpty()) }
        AlertDialog(onDismissRequest = { dialog = null }, containerColor = AvaColors.CardNavy, title = { Text("Edit Profile") },
            text = { OutlinedTextField(value = input, onValueChange = { input = it }, label = { Text("Display Name") }, singleLine = true) },
            confirmButton = { TextButton(onClick = { scope.launch { repository.updateDisplayName(input) }; dialog = null }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { dialog = null }) { Text("Cancel") } }
        )
    } else if (dialog != null) {
        val (title, body) = when (dialog) {
            "update" -> "Update Status" to "Version: v${BuildConfig.VERSION_NAME}\nAPK SHA-256:\n${certSha.take(36)}..."
            "privacy" -> "Privacy Policy" to "AVA Boost stores chosen games and Device ID locally in Room DB and never modifies other apps."
            else -> "Help & Admin Guide" to "Connect Firebase Free Tier (Auth + Firestore) when ready to issue live codes and updates."
        }
        AlertDialog(onDismissRequest = { dialog = null }, containerColor = AvaColors.CardNavy, title = { Text(title) }, text = { Text(body) }, confirmButton = { TextButton(onClick = { dialog = null }) { Text("OK") } })
    }
}

@Composable
private fun SettingItemCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    AvaCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = AvaColors.CyanAccent); Spacer(Modifier.width(12.dp))
            Column { Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AvaColors.TextPrimary); Text(subtitle, fontSize = 12.sp, color = AvaColors.TextSecondary) }
        }
    }
}

fun launchOfficialPackage(context: Context, packageName: String, onError: (String) -> Unit) {
    val intent = context.packageManager.getLaunchIntentForPackage(packageName)
    if (intent != null) context.startActivity(intent) else onError("App not installed on device.")
}
