package com.farabe.farabesmomblock.audio

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Audio Engine supporting both Custom Audio Files (MP3 / WAV / OGG in assets or res/raw)
 * and zero-latency procedural harmonic chime synthesis.
 */
class SoundManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var musicJob: Job? = null
    private var customMediaPlayer: MediaPlayer? = null
    private var voiceMediaPlayer: MediaPlayer? = null
    private var isDucked: Boolean = false

    var sfxVolume: Float = 0.8f
    var musicVolume: Float = 0.5f
        set(value) {
            field = value
            val effective = if (isDucked) value * 0.16f else value
            customMediaPlayer?.setVolume(effective, effective)
        }
    var vibrationEnabled: Boolean = true

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /**
     * Checks if custom MP3/WAV background music exists in assets/custom_music.
     */
    fun findCustomMusicAsset(): String? {
        return try {
            val files = context.assets.list("custom_music") ?: emptyArray()
            files.firstOrNull { it.endsWith(".mp3", ignoreCase = true) || it.endsWith(".wav", ignoreCase = true) || it.endsWith(".ogg", ignoreCase = true) }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Returns friendly label of active music source.
     */
    fun getActiveMusicSource(): String {
        val remoteFile = java.io.File(context.filesDir, "remote_cache/remote_bg_music.mp3")
        if (remoteFile.exists() && remoteFile.length() > 0) return "Farabe Studio Song (Live Sync)"
        val userFile = java.io.File(context.filesDir, "custom_bg_music.mp3")
        if (userFile.exists()) return "Custom Song from Storage"
        val assetFile = findCustomMusicAsset()
        if (assetFile != null) return "Custom Asset ($assetFile)"
        return "Calming Procedural Chimes (Default)"
    }

    fun reloadBackgroundMusic() {
        stopBackgroundMusic()
        startBackgroundMusic()
    }

    /**
     * Plays custom background music if available (remote synced song, storage, or assets),
     * otherwise procedural ambient lullaby chords.
     */
    fun startBackgroundMusic() {
        if (musicVolume <= 0.01f) return

        val remoteFile = java.io.File(context.filesDir, "remote_cache/remote_bg_music.mp3")
        val userFile = java.io.File(context.filesDir, "custom_bg_music.mp3")
        val customMusicFile = findCustomMusicAsset()

        if ((remoteFile.exists() && remoteFile.length() > 0) || userFile.exists() || customMusicFile != null) {
            // Play custom MP3 file!
            try {
                if (customMediaPlayer == null) {
                    val player = MediaPlayer().apply {
                        when {
                            remoteFile.exists() && remoteFile.length() > 0 -> setDataSource(remoteFile.absolutePath)
                            userFile.exists() -> setDataSource(userFile.absolutePath)
                            else -> {
                                val afd: AssetFileDescriptor = context.assets.openFd("custom_music/$customMusicFile")
                                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                                afd.close()
                            }
                        }
                        isLooping = true
                        setVolume(musicVolume, musicVolume)
                        prepare()
                        start()
                    }
                    customMediaPlayer = player
                } else if (customMediaPlayer?.isPlaying == false) {
                    customMediaPlayer?.start()
                }
                return
            } catch (e: Exception) {
                // Fall back to procedural music
            }
        }

        // Procedural Ambient Music fallback
        if (musicJob != null && musicJob?.isActive == true) return
        musicJob = scope.launch {
            val chords = listOf(
                listOf(261.63f, 329.63f, 392.00f, 493.88f), // Cmaj7
                listOf(349.23f, 440.00f, 523.25f, 659.25f), // Fmaj7
                listOf(392.00f, 523.25f, 587.33f),          // Gsus4
                listOf(440.00f, 523.25f, 659.25f, 493.88f)  // Am9
            )

            while (isActive) {
                if (musicVolume > 0.05f) {
                    for (chord in chords) {
                        if (!isActive || musicVolume <= 0.05f) break
                        playChime(chord, List(chord.size) { 450 }, volumeFactor = musicVolume * 0.35f)
                        delay(1800)
                    }
                } else {
                    delay(1000)
                }
            }
        }
    }

    fun stopBackgroundMusic() {
        try {
            customMediaPlayer?.stop()
            customMediaPlayer?.release()
            customMediaPlayer = null
        } catch (e: Exception) {
            // Ignored
        }
        musicJob?.cancel()
        musicJob = null
    }

    /**
     * Synthesizes and plays a pure harmonic chime buffer with soft ADSR envelope.
     */
    private fun playChime(frequencies: List<Float>, durationsMs: List<Int>, volumeFactor: Float = 1.0f) {
        if (sfxVolume <= 0.01f) return

        scope.launch {
            try {
                val sampleRate = 44100
                val totalDuration = durationsMs.sum()
                val totalSamples = (sampleRate * (totalDuration / 1000.0)).toInt()
                val audioData = ShortArray(totalSamples)

                var sampleOffset = 0
                for (i in frequencies.indices) {
                    val freq = frequencies[i]
                    val duration = durationsMs[i]
                    val noteSamples = (sampleRate * (duration / 1000.0)).toInt()

                    for (s in 0 until noteSamples) {
                        val t = s.toDouble() / sampleRate
                        val attack = (s.toDouble() / (sampleRate * 0.005)).coerceAtMost(1.0)
                        val decay = kotlin.math.exp(-3.5 * (s.toDouble() / noteSamples))
                        val envelope = attack * decay

                        val sampleVal = (sin(2.0 * Math.PI * freq * t) * 0.7 +
                                sin(2.0 * Math.PI * (freq * 2.0) * t) * 0.3) * envelope

                        val currentIdx = sampleOffset + s
                        if (currentIdx < totalSamples) {
                            val amp = (sampleVal * 32767 * sfxVolume * volumeFactor).toInt().coerceIn(-32768, 32767)
                            audioData[currentIdx] = amp.toShort()
                        }
                    }
                    sampleOffset += noteSamples
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(totalSamples * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(audioData, 0, totalSamples)
                track.play()
                delay(totalDuration.toLong() + 100)
                track.stop()
                track.release()
            } catch (e: Exception) {
                // Audio safely ignored
            }
        }
    }

    fun vibrate(durationMs: Long = 20, amplitude: Int = VibrationEffect.DEFAULT_AMPLITUDE) {
        if (!vibrationEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (e: Exception) {
            // Ignored
        }
    }

    // --- Sound Effects ---

    fun playPiecePickup() {
        vibrate(15, 60)
        playChime(listOf(659.25f), listOf(60), volumeFactor = 0.5f)
    }

    fun playPieceDrop() {
        vibrate(25, 120)
        playChime(listOf(523.25f), listOf(90), volumeFactor = 0.7f)
    }

    fun playInvalidMove() {
        vibrate(40, 180)
        playChime(listOf(180f, 150f), listOf(60, 80), volumeFactor = 0.6f)
    }

    fun playLineClear(lines: Int) {
        vibrate(45, 160)
        val chord = when (lines) {
            1 -> listOf(523.25f, 659.25f)
            2 -> listOf(523.25f, 659.25f, 783.99f)
            else -> listOf(523.25f, 659.25f, 783.99f, 1046.50f)
        }
        val durations = List(chord.size) { 70 }
        playChime(chord, durations, volumeFactor = 0.9f)
    }

    fun playCombo(comboCount: Int) {
        vibrate(60, 200)
        val baseFreq = 523.25f * (1.0f + (comboCount.coerceAtMost(5) * 0.12f))
        val notes = listOf(baseFreq, baseFreq * 1.25f, baseFreq * 1.5f, baseFreq * 2.0f)
        val durations = listOf(50, 50, 50, 120)
        playChime(notes, durations, volumeFactor = 1.0f)
    }

    fun playUnlockReward() {
        vibrate(80, 220)
        val notes = listOf(587.33f, 739.99f, 880.00f, 1174.66f)
        val durations = listOf(70, 70, 70, 200)
        playChime(notes, durations, volumeFactor = 1.0f)
    }

    fun playGameOver() {
        vibrate(60, 100)
        val notes = listOf(440.0f, 392.0f, 349.23f)
        val durations = listOf(120, 120, 250)
        playChime(notes, durations, volumeFactor = 0.7f)
    }

    /**
     * Plays Farabe's personal voice message (.wav/.mp3/.m4a/.ogg) for Mom with
     * automatic background music ducking so Mom hears his voice crystal clear.
     */
    fun playVoiceClip(file: java.io.File, duckMusic: Boolean = true, onFinished: () -> Unit = {}) {
        if (!file.exists() || file.length() <= 0L) {
            onFinished()
            return
        }
        stopVoice()
        try {
            if (duckMusic) {
                isDucked = true
                val duckedVol = (musicVolume * 0.16f).coerceAtLeast(0.02f)
                customMediaPlayer?.setVolume(duckedVol, duckedVol)
            }
            val player = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setVolume(1.0f, 1.0f)
                setOnCompletionListener {
                    restoreMusicAfterVoice()
                    onFinished()
                }
                setOnErrorListener { _, _, _ ->
                    restoreMusicAfterVoice()
                    onFinished()
                    true
                }
                prepare()
                start()
            }
            voiceMediaPlayer = player
        } catch (_: Exception) {
            restoreMusicAfterVoice()
            onFinished()
        }
    }

    fun stopVoice() {
        try {
            voiceMediaPlayer?.stop()
            voiceMediaPlayer?.release()
        } catch (_: Exception) {
        } finally {
            voiceMediaPlayer = null
            restoreMusicAfterVoice()
        }
    }

    private fun restoreMusicAfterVoice() {
        isDucked = false
        try {
            customMediaPlayer?.setVolume(musicVolume, musicVolume)
        } catch (_: Exception) {
        }
    }

    fun release() {
        stopVoice()
        stopBackgroundMusic()
    }
}
