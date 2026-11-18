package com.farabe.farabesmomblock.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.farabe.farabesmomblock.data.datastore.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Properties

data class SyncOutcome(
    val success: Boolean,
    val hasNewContent: Boolean,
    val songUpdated: Boolean,
    val config: RemoteGameConfig,
    val message: String
)

/**
 * Fetches live theme, background, images, songs, gifts, and notifications from
 * Farabe's Node.js Live Game Studio or GitHub cloud-sync URL, caching everything
 * locally for 100% offline gameplay.
 */
class RemoteConfigManager(
    private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    private val cacheDir = File(context.filesDir, "remote_cache").apply { mkdirs() }
    private val cachedConfigFile = File(cacheDir, "config.json")
    private val urlIndexFile = File(cacheDir, "media_urls.properties")

    private val _configFlow = MutableStateFlow(loadCachedConfig())
    val configFlow: StateFlow<RemoteGameConfig> = _configFlow.asStateFlow()

    private val _mediaRevision = MutableStateFlow(0)
    val mediaRevision: StateFlow<Int> = _mediaRevision.asStateFlow()

    val cachedHomeBgFile: File get() = File(cacheDir, "home_bg.img")
    val cachedGameBgFile: File get() = File(cacheDir, "game_bg.img")
    val cachedMomPhotoFile: File get() = File(cacheDir, "mom_photo.img")
    val cachedHelperAvatarFile: File get() = File(cacheDir, "helper_avatar.img")
    val cachedSketchbookFile: File get() = File(cacheDir, "sketchbook.img")
    val cachedRemoteSongFile: File get() = File(cacheDir, "remote_bg_music.mp3")
    val cachedWinVoiceFile: File get() = File(cacheDir, "win_voice.aud")
    val cachedLoseVoiceFile: File get() = File(cacheDir, "lose_voice.aud")
    val cachedComboVoiceFile: File get() = File(cacheDir, "combo_voice.aud")

    private fun loadCachedConfig(): RemoteGameConfig {
        return try {
            if (cachedConfigFile.exists()) {
                RemoteGameConfig.fromJson(cachedConfigFile.readText())
            } else {
                RemoteGameConfig()
            }
        } catch (_: Exception) {
            RemoteGameConfig()
        }
    }

    fun loadCachedBitmap(file: File): Bitmap? {
        return com.farabe.farabesmomblock.util.BitmapUtils.decodeBitmapFileWithExif(file)
    }

    private fun isLikelyEmulator(): Boolean {
        val fp = android.os.Build.FINGERPRINT
        val hw = android.os.Build.HARDWARE
        val model = android.os.Build.MODEL
        return fp.contains("generic", ignoreCase = true) ||
            fp.contains("emulator", ignoreCase = true) ||
            hw.contains("ranchu", ignoreCase = true) ||
            hw.contains("goldfish", ignoreCase = true) ||
            model.contains("Emulator", ignoreCase = true) ||
            model.contains("sdk_gphone", ignoreCase = true)
    }

    suspend fun syncNow(postNotificationIfUpdated: Boolean): SyncOutcome = withContext(Dispatchers.IO) {
        val settings = preferencesManager.settingsFlow.first()
        val onEmulator = isLikelyEmulator()
        val candidateUrls = listOfNotNull(
            settings.backendUrl.takeIf { it.isNotBlank() && (onEmulator || !it.contains("10.0.2.2")) },
            com.farabe.farabesmomblock.data.datastore.UserSettings.DEFAULT_LIVE_BACKEND_URL,
            settings.cloudUrl.takeIf { it.isNotBlank() }
        ).distinct()

        for (endpoint in candidateUrls) {
            try {
                val isLocal = endpoint.contains("10.0.2.2") || endpoint.contains("192.168.")
                val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                    connectTimeout = if (isLocal) 900 else 7000
                    readTimeout = if (isLocal) 2500 else 10000
                    instanceFollowRedirects = true
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/json")
                }
                if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                    conn.disconnect()
                    continue
                }

                val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()

                val remoteConfig = RemoteGameConfig.fromJson(jsonStr)
                cachedConfigFile.writeText(jsonStr)

                // Auto-discover and persist updated public backend URL if pushed via cloud-sync
                if (remoteConfig.liveBackendUrl.startsWith("https://") &&
                    remoteConfig.liveBackendUrl != settings.backendUrl
                ) {
                    preferencesManager.updateBackendUrl(remoteConfig.liveBackendUrl)
                }

                val props = Properties().apply {
                    if (urlIndexFile.exists()) urlIndexFile.inputStream().use { load(it) }
                }

                var anyMediaChanged = false
                var songChanged = false

                anyMediaChanged = syncAsset("home_bg", remoteConfig.background.homeBackgroundUrl, cachedHomeBgFile, props) || anyMediaChanged
                anyMediaChanged = syncAsset("game_bg", remoteConfig.background.gameplayBackgroundUrl, cachedGameBgFile, props) || anyMediaChanged
                anyMediaChanged = syncAsset("mom_photo", remoteConfig.images.momPhotoUrl, cachedMomPhotoFile, props) || anyMediaChanged
                anyMediaChanged = syncAsset("helper_avatar", remoteConfig.images.helperAvatarUrl, cachedHelperAvatarFile, props) || anyMediaChanged
                anyMediaChanged = syncAsset("sketchbook", remoteConfig.images.sketchbookImageUrl, cachedSketchbookFile, props) || anyMediaChanged

                if (syncAsset("song", remoteConfig.music.activeSongUrl, cachedRemoteSongFile, props)) {
                    songChanged = true
                    anyMediaChanged = true
                }
                anyMediaChanged = syncAsset("win_voice", remoteConfig.voice.winVoiceUrl, cachedWinVoiceFile, props) || anyMediaChanged
                anyMediaChanged = syncAsset("lose_voice", remoteConfig.voice.loseVoiceUrl, cachedLoseVoiceFile, props) || anyMediaChanged
                anyMediaChanged = syncAsset("combo_voice", remoteConfig.voice.comboVoiceUrl, cachedComboVoiceFile, props) || anyMediaChanged

                urlIndexFile.outputStream().use { props.store(it, "Cached remote media URLs") }

                val prevSeenVersion = settings.lastSeenConfigVersion
                val hasNew = remoteConfig.configVersion > prevSeenVersion

                _configFlow.value = remoteConfig
                if (anyMediaChanged || hasNew) {
                    _mediaRevision.value = _mediaRevision.value + 1
                }

                if (hasNew) {
                    if (postNotificationIfUpdated) {
                        NotificationHelper.showUpdateNotification(
                            context = context,
                            title = remoteConfig.notification.title,
                            body = remoteConfig.notification.body,
                            notificationId = 2000 + remoteConfig.configVersion
                        )
                    }
                    preferencesManager.markConfigVersionSeen(remoteConfig.configVersion)
                }

                return@withContext SyncOutcome(
                    success = true,
                    hasNewContent = hasNew,
                    songUpdated = songChanged,
                    config = remoteConfig,
                    message = if (hasNew) remoteConfig.notification.title else "Synced v${remoteConfig.configVersion}"
                )
            } catch (_: Exception) {
                // Try next URL or stay cleanly offline
            }
        }

        SyncOutcome(
            success = false,
            hasNewContent = false,
            songUpdated = false,
            config = _configFlow.value,
            message = "Offline mode (using cached theme & music)"
        )
    }

    private fun syncAsset(key: String, url: String, targetFile: File, props: Properties): Boolean {
        if (url.isBlank()) {
            if (targetFile.exists()) {
                targetFile.delete()
                props.remove(key)
                return true
            }
            return false
        }
        val prevUrl = props.getProperty(key, "")
        if (prevUrl == url && targetFile.exists() && targetFile.length() > 0) {
            return false
        }
        return try {
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 15000
                instanceFollowRedirects = true
            }
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val tmp = File(cacheDir, "${key}.tmp")
                conn.inputStream.use { input ->
                    FileOutputStream(tmp).use { output -> input.copyTo(output) }
                }
                conn.disconnect()
                if (tmp.length() > 0) {
                    if (targetFile.exists()) targetFile.delete()
                    tmp.renameTo(targetFile)
                    props.setProperty(key, url)
                    return true
                }
            }
            false
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Sends live player telemetry heartbeat to Farabe's Game Control Studio
     * and returns any pending remote-control commands for this player!
     */
    suspend fun sendTelemetryHeartbeat(payloadJson: String): String? = withContext(Dispatchers.IO) {
        try {
            val settings = preferencesManager.settingsFlow.first()
            val rawUrl = settings.backendUrl.trim()
            val configUrl = if (rawUrl.isBlank() || rawUrl.contains("10.0.2.2") || rawUrl.contains("localhost")) {
                com.farabe.farabesmomblock.data.datastore.UserSettings.DEFAULT_LIVE_BACKEND_URL
            } else {
                rawUrl
            }
            val baseOrigin = configUrl.substringBefore("/api/").trimEnd('/')
            if (baseOrigin.isBlank()) return@withContext null

            val heartbeatUrl = "$baseOrigin/api/telemetry/heartbeat"
            val conn = (URL(heartbeatUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5000
                instanceFollowRedirects = true
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
            conn.outputStream.use { out ->
                out.write(payloadJson.toByteArray(Charsets.UTF_8))
            }
            val code = conn.responseCode
            val responseBody = if (code == HttpURLConnection.HTTP_OK) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else null
            conn.disconnect()
            responseBody
        } catch (_: Exception) {
            null
        }
    }
}
