package com.avaboost.app.data

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.avaboost.app.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.util.UUID

class AvaRepository(private val db: AvaDatabase, private val context: Context) {
    fun observeProfile(): Flow<UserProfile?> = db.profileDao().observe().map { e ->
        e?.let { UserProfile(it.deviceId, it.displayName, it.accessLabel, it.redeemedCodesCsv.split(",").filter { c -> c.isNotBlank() }) }
    }

    fun observeGames(): Flow<List<HubGame>> = db.gameDao().observeAll().map { list ->
        list.map { e ->
            val info = getPackageDetails(e.packageName)
            HubGame(e.packageName, e.title, info.first, info.second, e.lastSessionActive)
        }
    }

    fun getWhatsNewSlides(): List<WhatsNewSlide> = listOf(
        WhatsNewSlide("1", "FREE UNLOCKED", "All AVA Boost Features Free", "No locked game cards. Add installed games and launch cleanly via Android intents."),
        WhatsNewSlide("2", "LIVE MONITOR", "Real-Time Device RAM & Session View", "Track live Redmi memory availability and manage active game sessions."),
        WhatsNewSlide("3", "CODES & UPDATES", "Ready for Admin Codes & Signed Updates", "Redeem perk codes and verify APK updates using SHA-256 checks.")
    )

    suspend fun ensureSeeded() { if (db.profileDao().getOnce() == null) db.profileDao().upsert(createFreshProfile()) }
    suspend fun addGame(choice: InstalledAppChoice) { db.gameDao().upsert(UserGameEntity(choice.packageName, choice.label, false)) }
    suspend fun removeGame(packageName: String) { db.gameDao().delete(packageName) }
    suspend fun markSessionLaunched(packageName: String, active: Boolean) { db.gameDao().setSessionActive(packageName, active) }

    suspend fun updateDisplayName(name: String) {
        val current = db.profileDao().getOnce() ?: return
        db.profileDao().upsert(current.copy(displayName = name.trim().take(24).ifEmpty { current.displayName }))
    }

    suspend fun redeemAccessCode(rawCode: String): String {
        val code = rawCode.trim().uppercase()
        if (code.isBlank()) return "Enter a valid code."
        val current = db.profileDao().getOnce() ?: return "Profile unavailable."
        val existing = current.redeemedCodesCsv.split(",").filter { it.isNotBlank() }.toMutableList()
        if (existing.contains(code)) return "Code $code is already redeemed."
        val validOffline = setOf("AVA-FREE-2026", "AVA-VIP-BADGE", "BOOST-100")
        return if (validOffline.contains(code)) {
            existing.add(code)
            db.profileDao().upsert(current.copy(accessLabel = "FREE + SUPPORTER BADGE", redeemedCodesCsv = existing.joinToString(",")))
            "Success! Code $code redeemed."
        } else "Code not recognized offline. Connect Firebase Admin to verify live codes."
    }

    suspend fun resetDeviceId() { db.profileDao().clear(); db.profileDao().upsert(createFreshProfile()) }

    fun queryInstalledLauncherApps(): List<InstalledAppChoice> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        return pm.queryIntentActivities(intent, PackageManager.MATCH_ALL).mapNotNull { info ->
            val pkg = info.activityInfo?.packageName ?: return@mapNotNull null
            if (pkg == context.packageName) return@mapNotNull null
            InstalledAppChoice(pkg, info.loadLabel(pm).toString(), getPackageDetails(pkg).first)
        }.distinctBy { it.packageName }.sortedBy { it.label.lowercase() }
    }

    fun getDeviceRamInfo(): DeviceRamInfo {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)
        val total = memInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
        val avail = memInfo.availMem / (1024.0 * 1024.0 * 1024.0)
        return DeviceRamInfo((total - avail).coerceAtLeast(0.0), total)
    }

    fun getInstalledCertSha256(): String = runCatching {
        val pm = context.packageManager
        val pkgInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong()))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            @Suppress("DEPRECATION") pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
        } else {
            @Suppress("DEPRECATION") pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
        }
        val sig = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pkgInfo.signingInfo?.apkContentsSigners?.firstOrNull()
        else @Suppress("DEPRECATION") pkgInfo.signatures?.firstOrNull() ?: return "UNAVAILABLE"
        MessageDigest.getInstance("SHA-256").digest(sig.toByteArray()).joinToString(":") { "%02X".format(it) }
    }.getOrDefault("UNAVAILABLE")

    private fun getPackageDetails(packageName: String): Pair<String, Boolean> {
        val pm = context.packageManager
        val launchable = pm.getLaunchIntentForPackage(packageName) != null
        val version = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0L))
            else @Suppress("DEPRECATION") pm.getPackageInfo(packageName, 0)
        }.getOrNull()?.versionName ?: "—"
        return version to launchable
    }

    private fun createFreshProfile(): UserProfileEntity {
        val code = UUID.randomUUID().toString().replace("-", "").take(8)
        return UserProfileEntity(1, code, "Player_${code.take(4).uppercase()}", "FREE UNLOCKED", "")
    }
}
