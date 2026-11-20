package com.avaboost.app.model

data class UserProfile(val deviceId: String, val displayName: String, val accessLabel: String, val redeemedCodes: List<String>)
data class HubGame(val packageName: String, val title: String, val versionName: String, val isInstalled: Boolean, val lastSessionActive: Boolean)
data class WhatsNewSlide(val id: String, val tag: String, val title: String, val description: String)
data class DeviceRamInfo(val usedGb: Double, val totalGb: Double)
data class InstalledAppChoice(val packageName: String, val label: String, val versionName: String)
