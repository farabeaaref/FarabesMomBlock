package com.farabe.farabesmomblock.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mom_block_preferences")

data class UserSettings(
    val soundEffectsVolume: Float = 0.8f,
    val musicVolume: Float = 0.6f,
    val isVibrationEnabled: Boolean = true,
    val isHighContrastEnabled: Boolean = false,
    val isReducedMotionEnabled: Boolean = false,
    val momName: String = "Mom",
    val allTimeHighScore: Int = 0,
    val totalGamesPlayed: Int = 0,
    val coins: Int = 500,
    val hearts: Int = 3,
    val gems: Int = 5,
    val lastSeenConfigVersion: Int = 1,
    val lastClaimedGiftId: String = "",
    val backendUrl: String = "http://10.0.2.2:8787/api/config",
    val cloudUrl: String = "https://raw.githubusercontent.com/farabeaaref/FarabesMomBlock/main/cloud-sync/config.json"
)

class PreferencesManager(private val context: Context) {

    private object Keys {
        val SFX_VOLUME = floatPreferencesKey("sfx_volume")
        val MUSIC_VOLUME = floatPreferencesKey("music_volume")
        val VIBRATION = booleanPreferencesKey("vibration_enabled")
        val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val MOM_NAME = stringPreferencesKey("mom_name")
        val HIGH_SCORE = intPreferencesKey("all_time_high_score")
        val GAMES_PLAYED = intPreferencesKey("total_games_played")
        val COINS = intPreferencesKey("player_coins")
        val HEARTS = intPreferencesKey("player_hearts")
        val GEMS = intPreferencesKey("player_gems")
        val LAST_CONFIG_VERSION = intPreferencesKey("last_config_version")
        val LAST_CLAIMED_GIFT_ID = stringPreferencesKey("last_claimed_gift_id")
        val BACKEND_URL = stringPreferencesKey("backend_url")
        val CLOUD_URL = stringPreferencesKey("cloud_url")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { pref ->
        UserSettings(
            soundEffectsVolume = pref[Keys.SFX_VOLUME] ?: 0.8f,
            musicVolume = pref[Keys.MUSIC_VOLUME] ?: 0.6f,
            isVibrationEnabled = pref[Keys.VIBRATION] ?: true,
            isHighContrastEnabled = pref[Keys.HIGH_CONTRAST] ?: false,
            isReducedMotionEnabled = pref[Keys.REDUCED_MOTION] ?: false,
            momName = pref[Keys.MOM_NAME] ?: "Mom",
            allTimeHighScore = pref[Keys.HIGH_SCORE] ?: 0,
            totalGamesPlayed = pref[Keys.GAMES_PLAYED] ?: 0,
            coins = pref[Keys.COINS] ?: 500,
            hearts = pref[Keys.HEARTS] ?: 3,
            gems = pref[Keys.GEMS] ?: 5,
            lastSeenConfigVersion = pref[Keys.LAST_CONFIG_VERSION] ?: 1,
            lastClaimedGiftId = pref[Keys.LAST_CLAIMED_GIFT_ID] ?: "",
            backendUrl = pref[Keys.BACKEND_URL] ?: "http://10.0.2.2:8787/api/config",
            cloudUrl = pref[Keys.CLOUD_URL] ?: "https://raw.githubusercontent.com/farabeaaref/FarabesMomBlock/main/cloud-sync/config.json"
        )
    }

    suspend fun updateSfxVolume(volume: Float) {
        context.dataStore.edit { it[Keys.SFX_VOLUME] = volume }
    }

    suspend fun updateMusicVolume(volume: Float) {
        context.dataStore.edit { it[Keys.MUSIC_VOLUME] = volume }
    }

    suspend fun updateVibration(enabled: Boolean) {
        context.dataStore.edit { it[Keys.VIBRATION] = enabled }
    }

    suspend fun updateHighContrast(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HIGH_CONTRAST] = enabled }
    }

    suspend fun updateReducedMotion(enabled: Boolean) {
        context.dataStore.edit { it[Keys.REDUCED_MOTION] = enabled }
    }

    suspend fun updateMomName(name: String) {
        context.dataStore.edit { it[Keys.MOM_NAME] = name }
    }

    suspend fun updateHighScore(score: Int) {
        context.dataStore.edit { pref ->
            val current = pref[Keys.HIGH_SCORE] ?: 0
            if (score > current) {
                pref[Keys.HIGH_SCORE] = score
            }
        }
    }

    suspend fun incrementGamesPlayed() {
        context.dataStore.edit { pref ->
            val count = pref[Keys.GAMES_PLAYED] ?: 0
            pref[Keys.GAMES_PLAYED] = count + 1
        }
    }

    suspend fun addCoinsAndHearts(coinsDelta: Int, heartsDelta: Int = 0, gemsDelta: Int = 0) {
        context.dataStore.edit { pref ->
            val curCoins = pref[Keys.COINS] ?: 500
            val curHearts = pref[Keys.HEARTS] ?: 3
            val curGems = pref[Keys.GEMS] ?: 5
            pref[Keys.COINS] = (curCoins + coinsDelta).coerceAtLeast(0)
            pref[Keys.HEARTS] = (curHearts + heartsDelta).coerceIn(1, 99)
            pref[Keys.GEMS] = (curGems + gemsDelta).coerceAtLeast(0)
        }
    }

    suspend fun updateBackendUrl(url: String) {
        context.dataStore.edit { it[Keys.BACKEND_URL] = url.trim() }
    }

    suspend fun updateCloudUrl(url: String) {
        context.dataStore.edit { it[Keys.CLOUD_URL] = url.trim() }
    }

    suspend fun markConfigVersionSeen(version: Int) {
        context.dataStore.edit { it[Keys.LAST_CONFIG_VERSION] = version }
    }

    suspend fun markGiftClaimed(giftId: String) {
        context.dataStore.edit { it[Keys.LAST_CLAIMED_GIFT_ID] = giftId }
    }

    suspend fun resetAllSettings() {
        context.dataStore.edit { it.clear() }
    }
}
