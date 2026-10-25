package com.hari.androidtvremote.ui.app

import androidx.compose.ui.graphics.Color

enum class RemoteShelfMode(
    val storageValue: String,
    val label: String,
    val description: String
) {
    Applications(
        storageValue = "applications",
        label = "Applications",
        description = "Show quick-launch apps above the remote"
    ),
    MediaButtons(
        storageValue = "media",
        label = "Media buttons",
        description = "Show playback controls above the remote"
    ),
    None(
        storageValue = "none",
        label = "None",
        description = "Hide the top strip on the remote"
    );

    companion object {
        fun fromStorage(value: String?): RemoteShelfMode? {
            return entries.firstOrNull { it.storageValue == value }
        }
    }
}

data class RemoteShortcutApp(
    val id: String,
    val launchName: String,
    val label: String,
    val mark: String,
    val accent: Color,
    val accentSecondary: Color = accent,
    val intentUri: String? = null,
    val isCustom: Boolean = false
)

fun CustomShortcut.toRemoteShortcutApp(): RemoteShortcutApp = RemoteShortcutApp(
    id = id,
    launchName = intentUri,
    label = label,
    mark = mark.ifBlank { label.take(2).uppercase() },
    accent = accentColor,
    accentSecondary = accentColor,
    intentUri = intentUri,
    isCustom = true
)

private val defaultRemoteShortcutApps = listOf(
    RemoteShortcutApp(
        id = "ard_mediathek",
        launchName = "ard://mediathek/",
        label = "ARD Mediathek",
        mark = "ARD",
        accent = Color(0xFF003D8F),
        accentSecondary = Color(0xFF00A5E0)
    ),
    RemoteShortcutApp(
        id = "browser",
        launchName = "https://www.google.com",
        label = "Browser",
        mark = "WEB",
        accent = Color(0xFF00796B),
        accentSecondary = Color(0xFF4DB6AC)
    ),
    RemoteShortcutApp(
        id = "flicky",
        launchName = "market://launch?id=app.flicky",
        label = "Flicky",
        mark = "FLK",
        accent = Color(0xFF3F51B5),
        accentSecondary = Color(0xFF7986CB)
    ),
    RemoteShortcutApp(
        id = "geforce_now",
        launchName = "nvidia://deeplink",
        label = "GeForce NOW",
        mark = "GFN",
        accent = Color(0xFF76B900),
        accentSecondary = Color(0xFF1B5E20)
    ),
    RemoteShortcutApp(
        id = "live_tv",
        launchName = "tvhomescreenchannels://com.mediatek.wwtv.tvcenter/playvideo",
        label = "Live TV",
        mark = "TV",
        accent = Color(0xFF455A64),
        accentSecondary = Color(0xFF90A4AE)
    ),
    RemoteShortcutApp(
        id = "play_srf",
        launchName = "playsrf://home",
        label = "Play SRF",
        mark = "SRF",
        accent = Color(0xFFD81B2C),
        accentSecondary = Color(0xFFFF6F60)
    ),
    RemoteShortcutApp(
        id = "playsuisse",
        launchName = "https://www.playsuisse.ch",
        label = "PlaySuisse",
        mark = "PS",
        accent = Color(0xFF7B1FA2),
        accentSecondary = Color(0xFFE040FB)
    ),
    RemoteShortcutApp(
        id = "unihockey",
        launchName = "swissunihockey://open",
        label = "Unihockey",
        mark = "UHC",
        accent = Color(0xFFD32F2F),
        accentSecondary = Color(0xFFFF7043)
    ),
    RemoteShortcutApp(
        id = "yallo_tv",
        launchName = "https://tv.yallo.ch",
        label = "yallo TV",
        mark = "YAL",
        accent = Color(0xFFFF6F00),
        accentSecondary = Color(0xFFFFB300)
    ),
    RemoteShortcutApp(
        id = "zdf",
        launchName = "https://www.zdf.de",
        label = "ZDF",
        mark = "ZDF",
        accent = Color(0xFFFA7D19),
        accentSecondary = Color(0xFFFFB74D)
    )
)

fun defaultRemoteShortcutOrder(): List<String> = defaultRemoteShortcutApps.map(RemoteShortcutApp::id)

fun getAllKnownShortcutApps(customShortcuts: List<CustomShortcut> = emptyList()): List<RemoteShortcutApp> =
    defaultRemoteShortcutApps + customShortcuts.map { it.toRemoteShortcutApp() }

fun decodeRemoteShortcutOrder(
    raw: String?,
    customShortcuts: List<CustomShortcut> = emptyList()
): List<String> {
    val defaultIds = defaultRemoteShortcutApps.map(RemoteShortcutApp::id)
    val customIds = customShortcuts.map(CustomShortcut::id)
    val allKnownIds = (defaultIds + customIds).toSet()

    if (raw == null) {
        return defaultIds + customIds
    }
    val savedOrder = raw.split(',')
        .map(String::trim)
        .filter(String::isNotBlank)
        .filter { id ->
            if (allKnownIds.isNotEmpty()) id in allKnownIds
            else (id in defaultIds || id.length > 8)
        }
        .distinct()

    val missingCustom = customIds.filter { it !in savedOrder }
    return savedOrder + missingCustom
}

fun encodeRemoteShortcutOrder(order: List<String>): String =
    order.joinToString(",")

fun resolveRemoteShortcutApps(
    order: List<String>,
    customShortcuts: List<CustomShortcut> = emptyList()
): List<RemoteShortcutApp> {
    val allApps = getAllKnownShortcutApps(customShortcuts)
    val appsById = allApps.associateBy(RemoteShortcutApp::id)
    val orderSet = order.toSet()
    val fullOrder = order + customShortcuts.map { it.id }.filter { it !in orderSet }
    return fullOrder.mapNotNull(appsById::get)
}

fun moveRemoteShortcutOrderItem(order: List<String>, fromIndex: Int, direction: Int): List<String> {
    val resolved = order.toMutableList()
    val targetIndex = fromIndex + direction
    if (fromIndex !in resolved.indices || targetIndex !in resolved.indices) {
        return resolved
    }
    val movedItem = resolved.removeAt(fromIndex)
    resolved.add(targetIndex, movedItem)
    return resolved
}

fun getDefaultRemoteShortcutApps(): List<RemoteShortcutApp> = defaultRemoteShortcutApps
