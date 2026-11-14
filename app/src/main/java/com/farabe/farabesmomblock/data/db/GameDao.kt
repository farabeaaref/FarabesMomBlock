package com.farabe.farabesmomblock.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameSaveDao {
    @Query("SELECT * FROM active_game_save WHERE id = 1 LIMIT 1")
    suspend fun getActiveSave(): GameSaveEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGame(save: GameSaveEntity)

    @Query("DELETE FROM active_game_save")
    suspend fun clearSave()
}

@Dao
interface GardenDao {
    @Query("SELECT * FROM garden_items ORDER BY requiredScore ASC, requiredLines ASC")
    fun getAllGardenItems(): Flow<List<GardenItemEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaultItems(items: List<GardenItemEntity>)

    @Query("UPDATE garden_items SET isUnlocked = 1, unlockedAt = :timestamp WHERE id = :id")
    suspend fun unlockItem(id: String, timestamp: Long)

    @Query("SELECT COUNT(*) FROM garden_items WHERE isUnlocked = 1")
    fun getUnlockedCount(): Flow<Int>
}

@Dao
interface SecretLettersDao {
    @Query("SELECT * FROM secret_letters ORDER BY requiredScore ASC")
    fun getAllLetters(): Flow<List<SecretLetterEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaultLetters(letters: List<SecretLetterEntity>)

    @Query("UPDATE secret_letters SET isUnlocked = 1, unlockedAt = :timestamp WHERE id = :id")
    suspend fun unlockLetter(id: String, timestamp: Long)

    @Query("UPDATE secret_letters SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE secret_letters SET title = :title, content = :content WHERE id = :id")
    suspend fun updateLetter(id: String, title: String, content: String)
}

@Dao
interface FamilyMemoryDao {
    @Query("SELECT * FROM family_memory WHERE id = 1 LIMIT 1")
    fun getFamilyMemory(): Flow<FamilyMemoryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFamilyMemory(memory: FamilyMemoryEntity)
}

@Dao
interface DailySurpriseDao {
    @Query("SELECT * FROM daily_surprises WHERE id = 1 LIMIT 1")
    fun getDailySurprise(): Flow<DailySurpriseEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDailySurprise(entity: DailySurpriseEntity)
}
