package com.avaboost.app.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object AvaColors {
    val VoidBlack = Color(0xFF060A12)
    val DeepNavy = Color(0xFF0C1220)
    val CardNavy = Color(0xFF141C2E)
    val ElevatedNavy = Color(0xFF1D2842)
    val CyanAccent = Color(0xFF28B8FF)
    val ElectricBlue = Color(0xFF1A73E8)
    val UnlockedGreen = Color(0xFF22C55E)
    val WarnAmber = Color(0xFFF59E0B)
    val TextPrimary = Color(0xFFF8FAFC)
    val TextSecondary = Color(0xFF94A3B8)
    val Border = Color(0xFF222E46)
}

@Composable
fun AvaBoostTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = AvaColors.CyanAccent, onPrimary = AvaColors.VoidBlack,
            background = AvaColors.VoidBlack, surface = AvaColors.DeepNavy,
            surfaceVariant = AvaColors.CardNavy, onBackground = AvaColors.TextPrimary,
            onSurface = AvaColors.TextPrimary, onSurfaceVariant = AvaColors.TextSecondary, outline = AvaColors.Border
        ),
        content = content
    )
}

@Composable
fun AvaBoostLogo(size: Dp = 36.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width; val h = this.size.height
        val top = Path().apply { moveTo(w*0.5f, h*0.08f); lineTo(w*0.90f, h*0.34f); lineTo(w*0.5f, h*0.60f); lineTo(w*0.10f, h*0.34f); close() }
        drawPath(top, color = AvaColors.CyanAccent)
        val bottom = Path().apply { moveTo(w*0.10f, h*0.54f); lineTo(w*0.5f, h*0.80f); lineTo(w*0.90f, h*0.54f); lineTo(w*0.90f, h*0.67f); lineTo(w*0.5f, h*0.93f); lineTo(w*0.10f, h*0.67f); close() }
        drawPath(bottom, color = AvaColors.ElectricBlue)
    }
}

@Composable
fun AvaCard(modifier: Modifier = Modifier, highlight: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier, shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AvaColors.CardNavy),
        border = BorderStroke(1.dp, if (highlight) AvaColors.CyanAccent.copy(alpha = 0.45f) else AvaColors.Border)
    ) { Column(modifier = Modifier.padding(14.dp), content = content) }
}

@Composable
fun DeviceIdPill(deviceId: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.End) {
        Text("DEVICE ID", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = AvaColors.TextSecondary, letterSpacing = 0.8.sp)
        Surface(modifier = Modifier.clickable(onClick = onClick), shape = RoundedCornerShape(50), color = AvaColors.CardNavy, border = BorderStroke(1.dp, AvaColors.Border)) {
            Text(deviceId, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AvaColors.TextPrimary)
        }
    }
}

@Composable
fun StatusBadge(text: String, color: Color) {
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.16f), border = BorderStroke(1.dp, color.copy(alpha = 0.4f))) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
