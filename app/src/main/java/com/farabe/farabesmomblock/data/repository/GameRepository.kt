package com.farabe.farabesmomblock.data.repository

import com.farabe.farabesmomblock.data.datastore.PreferencesManager
import com.farabe.farabesmomblock.data.datastore.UserSettings
import com.farabe.farabesmomblock.data.db.AppDatabase
import com.farabe.farabesmomblock.data.db.DailySurpriseEntity
import com.farabe.farabesmomblock.data.db.FamilyMemoryEntity
import com.farabe.farabesmomblock.data.db.GameSaveEntity
import com.farabe.farabesmomblock.data.db.GardenItemEntity
import com.farabe.farabesmomblock.data.db.SecretLetterEntity
import com.farabe.farabesmomblock.engine.BlockColor
import com.farabe.farabesmomblock.engine.BlockShape
import com.farabe.farabesmomblock.engine.BlockShapeLibrary
import com.farabe.farabesmomblock.engine.Board
import com.farabe.farabesmomblock.engine.GameState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GameRepository(
    private val db: AppDatabase,
    val preferencesManager: PreferencesManager
) {
    val userSettings: Flow<UserSettings> = preferencesManager.settingsFlow
    val gardenItems: Flow<List<GardenItemEntity>> = db.gardenDao().getAllGardenItems()
    val secretLetters: Flow<List<SecretLetterEntity>> = db.secretLettersDao().getAllLetters()
    val familyMemory: Flow<FamilyMemoryEntity?> = db.familyMemoryDao().getFamilyMemory()
    val dailySurprise: Flow<DailySurpriseEntity?> = db.dailySurpriseDao().getDailySurprise()

    suspend fun saveActiveGame(state: GameState, mode: String = "CLASSIC") {
        val boardStr = state.board.serialize()
        val trayStr = state.trayPieces.joinToString(";") { shape ->
            if (shape == null) "null"
            else "${shape.id}:${shape.color.ordinal}"
        }

        val saveEntity = GameSaveEntity(
            boardData = boardStr,
            trayData = trayStr,
            score = state.score,
            highScore = state.highScore,
            comboStreak = state.comboStreak,
            moveCount = state.moveCount,
            linesClearedTotal = state.linesClearedTotal,
            gameMode = mode
        )
        db.gameSaveDao().saveGame(saveEntity)
        preferencesManager.updateHighScore(state.highScore)
    }

    suspend fun loadActiveGame(): Pair<GameState, String>? {
        val save = db.gameSaveDao().getActiveSave() ?: return null
        val board = Board.deserialize(save.boardData) ?: return null

        val colors = BlockColor.values()
        val trayPieces = save.trayData.split(";").map { token ->
            if (token == "null" || token.isBlank()) {
                null
            } else {
                val sub = token.split(":")
                val shapeId = sub[0]
                val colorIdx = sub.getOrNull(1)?.toIntOrNull() ?: 0
                val color = if (colorIdx in colors.indices) colors[colorIdx] else BlockColor.SOFT_PINK
                BlockShapeLibrary.getById(shapeId)?.withColor(color)
            }
        }

        val state = GameState(
            board = board,
            trayPieces = trayPieces,
            score = save.score,
            highScore = save.highScore,
            comboStreak = save.comboStreak,
            moveCount = save.moveCount,
            linesClearedTotal = save.linesClearedTotal,
            isGameOver = false
        )
        return Pair(state, save.gameMode)
    }

    suspend fun clearActiveSave() {
        db.gameSaveDao().clearSave()
    }

    /**
     * Checks score and total lines against garden items and secret letters.
     * Unlocks newly earned milestones.
     */
    suspend fun checkAndUnlockMilestones(currentScore: Int, totalLines: Int): List<String> {
        val unlockedMessages = mutableListOf<String>()
        val now = System.currentTimeMillis()

        // Check Garden
        val gardenList = gardenItems.first()
        for (item in gardenList) {
            if (!item.isUnlocked && (currentScore >= item.requiredScore || totalLines >= item.requiredLines)) {
                db.gardenDao().unlockItem(item.id, now)
                unlockedMessages.add("New Garden Gift Unlocked: ${item.symbolEmoji} ${item.name}!")
            }
        }

        // Check Secret Letters
        val lettersList = secretLetters.first()
        for (letter in lettersList) {
            if (!letter.isUnlocked && (currentScore >= letter.requiredScore || totalLines >= letter.requiredLines)) {
                db.secretLettersDao().unlockLetter(letter.id, now)
                unlockedMessages.add("New Letter from Farabe: \"${letter.title}\" ❤️")
            }
        }

        return unlockedMessages
    }

    suspend fun markLetterRead(letterId: String) {
        db.secretLettersDao().markAsRead(letterId)
    }

    suspend fun updateLetter(letterId: String, title: String, content: String) {
        db.secretLettersDao().updateLetter(letterId, title, content)
    }

    suspend fun updateFamilyMemory(title: String, note: String, photoUri: String?) {
        db.familyMemoryDao().saveFamilyMemory(
            FamilyMemoryEntity(title = title, personalNote = note, photoUri = photoUri)
        )
    }

    suspend fun checkDailySurprise(): String? {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val surprise = dailySurprise.first() ?: DailySurpriseEntity()

        if (surprise.lastClaimedDate != today) {
            val newStreak = surprise.streakDays + 1
            val addedSeeds = 50 + (newStreak * 10)
            db.dailySurpriseDao().saveDailySurprise(
                surprise.copy(
                    lastClaimedDate = today,
                    streakDays = newStreak,
                    totalSeeds = surprise.totalSeeds + addedSeeds
                )
            )
            return "Good morning, Mom! ❤️ Today's gift: $addedSeeds Garden Bloom Seeds for your garden!"
        }
        return null
    }
}
