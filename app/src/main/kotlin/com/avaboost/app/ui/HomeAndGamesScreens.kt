package com.avaboost.app.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avaboost.app.data.AvaRepository
import com.avaboost.app.model.*
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun HomeTabScreen(
    profile: UserProfile?, games: List<HubGame>, slides: List<WhatsNewSlide>,
    onAddGameClick: () -> Unit, onOpenMonitor: (HubGame) -> Unit, onLaunchGame: (HubGame) -> Unit, onToast: (String) -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val pagerState = rememberPagerState(pageCount = { slides.size })
    LazyColumn(modifier = Modifier.fillMaxSize().background(AvaColors.VoidBlack), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvaBoostLogo(38.dp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("AVA Boost", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = AvaColors.TextPrimary)
                        Text(profile?.accessLabel ?: "FREE UNLOCKED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AvaColors.UnlockedGreen)
                    }
                }
                DeviceIdPill(profile?.deviceId ?: "--------") {
                    profile?.deviceId?.let { clipboard.setText(AnnotatedString(it)); onToast("Device ID copied: $it") }
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Your games", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AvaColors.TextPrimary)
                Text("${games.size} games", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AvaColors.CyanAccent)
            }
        }
        item {
            if (games.isEmpty()) {
                AvaCard(highlight = true) {
                    Text("No Games Added Yet", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AvaColors.TextPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text("Tap below to select installed games. All features are free & unlocked!", fontSize = 13.sp, color = AvaColors.TextSecondary)
                }
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(games, key = { it.packageName }) { game ->
                        AvaCard(modifier = Modifier.width(172.dp).clickable { onOpenMonitor(game) }) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.SportsEsports, null, tint = AvaColors.CyanAccent, modifier = Modifier.size(30.dp))
                                StatusBadge(if (game.isInstalled) "UNLOCKED" else "MISSING", if (game.isInstalled) AvaColors.UnlockedGreen else AvaColors.WarnAmber)
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(game.versionName, fontSize = 11.sp, color = AvaColors.TextSecondary)
                            Text(game.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AvaColors.TextPrimary, maxLines = 1)
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.LockOpen, null, tint = AvaColors.UnlockedGreen, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (game.isInstalled) "Free • Ready" else "Not installed", fontSize = 12.sp, color = AvaColors.UnlockedGreen)
                            }
                            Spacer(Modifier.height(10.dp))
                            OutlinedButton(onClick = { onLaunchGame(game) }, enabled = game.isInstalled, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 4.dp)) {
                                Text("Launch", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
        item { Text("What's new", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AvaColors.TextPrimary) }
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
                    val s = slides[page]
                    AvaCard(modifier = Modifier.fillMaxWidth().height(145.dp), highlight = true) {
                        StatusBadge(s.tag, AvaColors.CyanAccent); Spacer(Modifier.height(8.dp))
                        Text(s.title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = AvaColors.TextPrimary); Spacer(Modifier.height(4.dp))
                        Text(s.description, fontSize = 13.sp, color = AvaColors.TextSecondary)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(slides.size) { i ->
                        val active = pagerState.currentPage == i
                        Box(modifier = Modifier.size(if (active) 9.dp else 7.dp).clip(CircleShape).background(if (active) AvaColors.CyanAccent else AvaColors.Border))
                    }
                }
            }
        }
        item {
            Button(onClick = onAddGameClick, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = AvaColors.CyanAccent, contentColor = AvaColors.VoidBlack)) {
                Icon(Icons.Rounded.Add, null); Spacer(Modifier.width(8.dp))
                Text("ADD & UNLOCK GAMES (FREE)", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}
