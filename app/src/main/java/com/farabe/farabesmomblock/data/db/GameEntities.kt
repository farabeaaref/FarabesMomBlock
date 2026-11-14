package com.farabe.farabesmomblock.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persists active game state for seamless offline resume.
 */
@Entity(tableName = "active_game_save")
data class GameSaveEntity(
    @PrimaryKey val id: Int = 1,
    val boardData: String,
    val trayData: String,
    val score: Int,
    val highScore: Int,
    val comboStreak: Int,
    val moveCount: Int,
    val linesClearedTotal: Int,
    val gameMode: String = "CLASSIC",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Items unlocked in Mom's Garden as progression milestones.
 */
@Entity(tableName = "garden_items")
data class GardenItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String, // FLOWER, TREE, BUTTERFLY, DECORATION, THEME
    val description: String,
    val symbolEmoji: String,
    val isUnlocked: Boolean = false,
    val requiredScore: Int = 0,
    val requiredLines: Int = 0,
    val unlockedAt: Long? = null
)

/**
 * Secret personal letters from Farabe to Mom unlocked at specific milestones.
 */
@Entity(tableName = "secret_letters")
data class SecretLetterEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val milestoneLabel: String,
    val requiredScore: Int,
    val requiredLines: Int,
    val isUnlocked: Boolean = false,
    val isRead: Boolean = false,
    val isEditable: Boolean = true,
    val unlockedAt: Long? = null
)

/**
 * Family photo memory card with Farabe's personal note for Mom.
 */
@Entity(tableName = "family_memory")
data class FamilyMemoryEntity(
    @PrimaryKey val id: Int = 1,
    val title: String = "A Special Memory with Mom",
    val personalNote: String = "Thank you for always being my biggest inspiration, my comfort, and my guide in life. Every smile of yours lights up my world.",
    val photoUri: String? = null,
    val dateAdded: Long = System.currentTimeMillis()
)

/**
 * Tracks daily surprises and positive messages for Mom.
 */
@Entity(tableName = "daily_surprises")
data class DailySurpriseEntity(
    @PrimaryKey val id: Int = 1,
    val lastClaimedDate: String = "",
    val streakDays: Int = 0,
    val totalSeeds: Int = 0
)
