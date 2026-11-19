package com.farabe.farabesmomblock.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val downloadUrl: String,
    val titleBengali: String = "আম্মু, নতুন সারপ্রাইজ এসেছে! ❤️",
    val descriptionBengali: String = "ফারাবী তোমার জন্য নতুন গান ও সুন্দর ফিচার যোগ করেছে। এখনই আপডেট করে নাও!",
    val releaseNotes: String = ""
)

/**
 * Handles Over-The-Air (OTA) updates for Farabe's Mom - Block.
 * Checks for new releases, downloads APKs with progress reporting,
 * and launches Android's native package installer with full data retention.
 */
class AppUpdater(private val context: Context) {

    companion object {
        // Live 24/7 Railway backend config URL (primary) + raw GitHub manifest URL (fallback).
        const val RAILWAY_CONFIG_URL = "https://live-game-studio-production.up.railway.app/api/config"
        const val UPDATE_MANIFEST_URL = "https://raw.githubusercontent.com/farabeaaref/FarabesMomBlock/main/version.json"
    }

    /**
     * Current installed version code.
     */
    fun getCurrentVersionCode(): Int {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).versionCode
            }
        } catch (e: Exception) {
            1
        }
    }

    /**
     * Current installed version name.
     */
    fun getCurrentVersionName(): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    /**
     * Checks online for a new version.
     * Fails silently and returns null if offline (offline-first design).
     */
    suspend fun checkForUpdate(manifestUrl: String = UPDATE_MANIFEST_URL): UpdateInfo? = withContext(Dispatchers.IO) {
        val currentCode = getCurrentVersionCode()

        // 1. Check live 24/7 Railway Studio config first
        try {
            val railConn = (URL(RAILWAY_CONFIG_URL).openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5000
                instanceFollowRedirects = true
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
            }
            if (railConn.responseCode == HttpURLConnection.HTTP_OK) {
                val json = JSONObject(railConn.inputStream.bufferedReader().use { it.readText() })
                val rCode = json.optInt("appVersionCode", 0)
                val rName = json.optString("appVersionName", "")
                val rUrl = json.optString("appDownloadUrl", "")
                val notifyObj = json.optJSONObject("notification")
                val rNotes = json.optString("appReleaseNotes", "")
                if (rCode > currentCode && rUrl.isNotBlank()) {
                    return@withContext UpdateInfo(
                        versionCode = rCode,
                        versionName = rName.ifBlank { "1.$rCode" },
                        downloadUrl = rUrl,
                        titleBengali = notifyObj?.optString("title")?.takeIf { it.isNotBlank() }
                            ?: "আম্মু, গেমের নতুন আপডেট এসেছে! 📲❤️",
                        descriptionBengali = notifyObj?.optString("body")?.takeIf { it.isNotBlank() }
                            ?: "ফারাবী তোমার জন্য নতুন ফিচার যোগ করেছে। এখনই আপডেট করে নাও!",
                        releaseNotes = rNotes
                    )
                }
            }
        } catch (_: Exception) {
            // Fall through to GitHub version.json check
        }

        // 2. Fallback: Check GitHub version.json
        try {
            val url = URL(manifestUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5000
                instanceFollowRedirects = true
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
            }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext null
            }

            val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(jsonString)

            val remoteVersionCode = json.optInt("versionCode", 0)
            val remoteVersionName = json.optString("versionName", "")
            val downloadUrl = json.optString("downloadUrl", "")
            val titleBengali = json.optString("titleBengali", "আম্মু, নতুন সারপ্রাইজ এসেছে! ❤️")
            val descriptionBengali = json.optString(
                "descriptionBengali",
                "ফারাবী তোমার জন্য নতুন গান ও সুন্দর ফিচার যোগ করেছে।"
            )
            val releaseNotes = json.optString("releaseNotes", "")

            if (remoteVersionCode > currentCode && downloadUrl.isNotBlank()) {
                UpdateInfo(
                    versionCode = remoteVersionCode,
                    versionName = remoteVersionName,
                    downloadUrl = downloadUrl,
                    titleBengali = titleBengali,
                    descriptionBengali = descriptionBengali,
                    releaseNotes = releaseNotes
                )
            } else {
                null
            }
        } catch (e: Exception) {
            // Silently ignore network failures - offline first
            null
        }
    }

    /**
     * Downloads the APK file to cache and launches the Android installer.
     */
    suspend fun downloadAndInstall(
        updateInfo: UpdateInfo,
        onProgress: (Float) -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val updateDir = File(context.cacheDir, "updates").apply { mkdirs() }
            val apkFile = File(updateDir, "FarabesMomBlock-v${updateInfo.versionName}.apk")

            // If file already exists and matches download size, proceed to install
            val url = URL(updateInfo.downloadUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 15000
                instanceFollowRedirects = true
            }

            val totalSize = connection.contentLength.toFloat()

            connection.inputStream.use { input ->
                FileOutputStream(apkFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalDownloaded = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalDownloaded += bytesRead
                        if (totalSize > 0) {
                            val progress = (totalDownloaded / totalSize).coerceIn(0f, 1f)
                            withContext(Dispatchers.Main) {
                                onProgress(progress)
                            }
                        }
                    }
                }
            }

            withContext(Dispatchers.Main) {
                onProgress(1.0f)
                launchInstaller(apkFile)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError(e.localizedMessage ?: "Download failed")
            }
        }
    }

    /**
     * Launches Android's Package Installer for the downloaded APK.
     */
    fun launchInstaller(apkFile: File) {
        try {
            // Android 8.0+ (API 26+) check for unknown sources permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val manageIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(manageIntent)
                }
            }

            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(installIntent)
        } catch (e: Exception) {
            // Ignored
        }
    }
}
