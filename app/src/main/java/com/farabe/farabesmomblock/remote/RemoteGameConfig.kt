package com.farabe.farabesmomblock.remote

import androidx.compose.ui.graphics.Color
import org.json.JSONArray
import org.json.JSONObject

data class RemoteTheme(
    val primaryColorHex: String = "#2B144A",
    val surfaceColorHex: String = "#3B1D63",
    val borderColorHex: String = "#7D48C4",
    val accentColorHex: String = "#5FD63A",
    val goldAccentHex: String = "#FFD23F",
    val boardFrameHex: String = "#321852",
    val tileStyle: String = "GLOSSY_3D_CANDY"
) {
    val primaryColor: Color get() = parseHexColor(primaryColorHex, Color(0xFF2B144A))
    val surfaceColor: Color get() = parseHexColor(surfaceColorHex, Color(0xFF3B1D63))
    val borderColor: Color get() = parseHexColor(borderColorHex, Color(0xFF7D48C4))
    val accentColor: Color get() = parseHexColor(accentColorHex, Color(0xFF5FD63A))
    val goldAccent: Color get() = parseHexColor(goldAccentHex, Color(0xFFFFD23F))
    val boardFrameColor: Color get() = parseHexColor(boardFrameHex, Color(0xFF321852))
}

data class RemoteBackground(
    val homeBackgroundUrl: String = "",
    val gameplayBackgroundUrl: String = "",
    val ambientEffect: String = "FIREFLIES_AND_PETALS"
)

data class RemoteImages(
    val momPhotoUrl: String = "",
    val helperAvatarUrl: String = "",
    val sketchbookImageUrl: String = ""
)

data class RemoteMusic(
    val activeSongTitle: String = "Calming Ambient Chimes (Default)",
    val activeSongUrl: String = ""
)

data class RemoteVoiceConfig(
    val winVoiceUrl: String = "",
    val loseVoiceUrl: String = "",
    val comboVoiceUrl: String = "",
    val enableWinVoice: Boolean = true,
    val enableLoseVoice: Boolean = true,
    val enableComboVoice: Boolean = true,
    val duckMusicWhileSpeaking: Boolean = true
)

data class RemoteGift(
    val giftId: String = "",
    val title: String = "Surprise Gift from Farabe! 🎁",
    val messageBengali: String = "আম্মু, তোমার জন্য ভালোবাসার উপহার ❤️",
    val coinsReward: Int = 250,
    val heartsReward: Int = 3
)

data class RemoteLetter(
    val id: String,
    val title: String,
    val content: String
)

data class RemoteTask(
    val id: String,
    val helperName: String,
    val title: String,
    val target: Int,
    val rewardCoins: Int,
    val rewardGems: Int
)

data class RemoteNotification(
    val notifyId: String = "",
    val title: String = "আম্মু, নতুন সারপ্রাইজ এসেছে! ❤️",
    val body: String = "ফারাবী তোমার জন্য নতুন থিম, গান ও উপহার পাঠিয়েছে।",
    val timestamp: String = ""
)

data class RemoteGameConfig(
    val configVersion: Int = 1,
    val appVersionCode: Int = 1,
    val appVersionName: String = "1.0.0",
    val appDownloadUrl: String = "",
    val theme: RemoteTheme = RemoteTheme(),
    val background: RemoteBackground = RemoteBackground(),
    val images: RemoteImages = RemoteImages(),
    val music: RemoteMusic = RemoteMusic(),
    val voice: RemoteVoiceConfig = RemoteVoiceConfig(),
    val activeGift: RemoteGift? = null,
    val customLetters: List<RemoteLetter> = emptyList(),
    val customTasks: List<RemoteTask> = emptyList(),
    val notification: RemoteNotification = RemoteNotification()
) {
    companion object {
        fun fromJson(jsonStr: String): RemoteGameConfig {
            val root = JSONObject(jsonStr)
            val themeObj = root.optJSONObject("theme")
            val bgObj = root.optJSONObject("background")
            val imgObj = root.optJSONObject("images")
            val musicObj = root.optJSONObject("music")
            val voiceObj = root.optJSONObject("voice")
            val giftsObj = root.optJSONObject("gifts")
            val activeGiftObj = giftsObj?.optJSONObject("activeGift")
            val notifyObj = root.optJSONObject("notification")

            val letters = mutableListOf<RemoteLetter>()
            val lettersArr = giftsObj?.optJSONArray("customLetters") ?: JSONArray()
            for (i in 0 until lettersArr.length()) {
                val item = lettersArr.optJSONObject(i) ?: continue
                letters.add(
                    RemoteLetter(
                        id = item.optString("id", "letter_$i"),
                        title = item.optString("title", "Letter from Farabe"),
                        content = item.optString("content", "")
                    )
                )
            }

            val tasks = mutableListOf<RemoteTask>()
            val tasksArr = giftsObj?.optJSONArray("customTasks") ?: JSONArray()
            for (i in 0 until tasksArr.length()) {
                val item = tasksArr.optJSONObject(i) ?: continue
                tasks.add(
                    RemoteTask(
                        id = item.optString("id", "task_$i"),
                        helperName = item.optString("helperName", "Farabe"),
                        title = item.optString("title", "Play with Ammu"),
                        target = item.optInt("target", 3),
                        rewardCoins = item.optInt("rewardCoins", 100),
                        rewardGems = item.optInt("rewardGems", 1)
                    )
                )
            }

            return RemoteGameConfig(
                configVersion = root.optInt("configVersion", 1),
                appVersionCode = root.optInt("appVersionCode", 1),
                appVersionName = root.optString("appVersionName", "1.0.0"),
                appDownloadUrl = root.optString("appDownloadUrl", ""),
                theme = RemoteTheme(
                    primaryColorHex = themeObj?.optString("primaryColorHex", "#2B144A") ?: "#2B144A",
                    surfaceColorHex = themeObj?.optString("surfaceColorHex", "#3B1D63") ?: "#3B1D63",
                    borderColorHex = themeObj?.optString("borderColorHex", "#7D48C4") ?: "#7D48C4",
                    accentColorHex = themeObj?.optString("accentColorHex", "#5FD63A") ?: "#5FD63A",
                    goldAccentHex = themeObj?.optString("goldAccentHex", "#FFD23F") ?: "#FFD23F",
                    boardFrameHex = themeObj?.optString("boardFrameHex", "#321852") ?: "#321852",
                    tileStyle = themeObj?.optString("tileStyle", "GLOSSY_3D_CANDY") ?: "GLOSSY_3D_CANDY"
                ),
                background = RemoteBackground(
                    homeBackgroundUrl = bgObj?.optString("homeBackgroundUrl", "") ?: "",
                    gameplayBackgroundUrl = bgObj?.optString("gameplayBackgroundUrl", "") ?: "",
                    ambientEffect = bgObj?.optString("ambientEffect", "FIREFLIES_AND_PETALS") ?: "FIREFLIES_AND_PETALS"
                ),
                images = RemoteImages(
                    momPhotoUrl = imgObj?.optString("momPhotoUrl", "") ?: "",
                    helperAvatarUrl = imgObj?.optString("helperAvatarUrl", "") ?: "",
                    sketchbookImageUrl = imgObj?.optString("sketchbookImageUrl", "") ?: ""
                ),
                music = RemoteMusic(
                    activeSongTitle = musicObj?.optString("activeSongTitle", "Calming Ambient Chimes (Default)")
                        ?: "Calming Ambient Chimes (Default)",
                    activeSongUrl = musicObj?.optString("activeSongUrl", "") ?: ""
                ),
                voice = RemoteVoiceConfig(
                    winVoiceUrl = voiceObj?.optString("winVoiceUrl", "") ?: "",
                    loseVoiceUrl = voiceObj?.optString("loseVoiceUrl", "") ?: "",
                    comboVoiceUrl = voiceObj?.optString("comboVoiceUrl", "") ?: "",
                    enableWinVoice = voiceObj?.optBoolean("enableWinVoice", true) ?: true,
                    enableLoseVoice = voiceObj?.optBoolean("enableLoseVoice", true) ?: true,
                    enableComboVoice = voiceObj?.optBoolean("enableComboVoice", true) ?: true,
                    duckMusicWhileSpeaking = voiceObj?.optBoolean("duckMusicWhileSpeaking", true) ?: true
                ),
                activeGift = if (activeGiftObj != null) {
                    RemoteGift(
                        giftId = activeGiftObj.optString("giftId", ""),
                        title = activeGiftObj.optString("title", "Gift from Farabe 🎁"),
                        messageBengali = activeGiftObj.optString("messageBengali", ""),
                        coinsReward = activeGiftObj.optInt("coinsReward", 200),
                        heartsReward = activeGiftObj.optInt("heartsReward", 2)
                    )
                } else null,
                customLetters = letters,
                customTasks = tasks,
                notification = RemoteNotification(
                    notifyId = notifyObj?.optString("notifyId", "") ?: "",
                    title = notifyObj?.optString("title", "আম্মু, নতুন সারপ্রাইজ এসেছে! ❤️")
                        ?: "আম্মু, নতুন সারপ্রাইজ এসেছে! ❤️",
                    body = notifyObj?.optString("body", "ফারাবী তোমার জন্য নতুন কিছু পাঠিয়েছে।")
                        ?: "ফারাবী তোমার জন্য নতুন কিছু পাঠিয়েছে।",
                    timestamp = notifyObj?.optString("timestamp", "") ?: ""
                )
            )
        }
    }
}

fun parseHexColor(hex: String, fallback: Color): Color {
    return try {
        val clean = hex.trim().removePrefix("#")
        val value = when (clean.length) {
            6 -> 0xFF000000L or clean.toLong(16)
            8 -> clean.toLong(16)
            else -> return fallback
        }
        Color(value)
    } catch (_: Exception) {
        fallback
    }
}
