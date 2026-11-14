package com.farabe.farabesmomblock.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        GameSaveEntity::class,
        GardenItemEntity::class,
        SecretLetterEntity::class,
        FamilyMemoryEntity::class,
        DailySurpriseEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun gameSaveDao(): GameSaveDao
    abstract fun gardenDao(): GardenDao
    abstract fun secretLettersDao(): SecretLettersDao
    abstract fun familyMemoryDao(): FamilyMemoryDao
    abstract fun dailySurpriseDao(): DailySurpriseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "farabes_mom_block.db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialGarden(database.gardenDao())
                        populateInitialLetters(database.secretLettersDao())
                        database.familyMemoryDao().saveFamilyMemory(FamilyMemoryEntity())
                        database.dailySurpriseDao().saveDailySurprise(DailySurpriseEntity())
                    }
                }
            }

            private suspend fun populateInitialGarden(dao: GardenDao) {
                val items = listOf(
                    // Flowers
                    GardenItemEntity("flower_rose", "Mom's Pink Rose", "FLOWER", "A delicate fragrant rose blooming with gentle love.", "🌹", isUnlocked = true, requiredScore = 0),
                    GardenItemEntity("flower_daisy", "Sunny Daisy", "FLOWER", "Radiant petals bringing sunshine and laughter.", "🌼", requiredScore = 200),
                    GardenItemEntity("flower_tulip", "Spring Tulip", "FLOWER", "Graceful spring tulip painted in tender pastel pink.", "🌷", requiredScore = 500),
                    GardenItemEntity("flower_sunflower", "Golden Sunflower", "FLOWER", "Tall and cheerful, always turning towards warmth.", "🌻", requiredScore = 1000),
                    GardenItemEntity("flower_lavender", "Sweet Lavender", "FLOWER", "A calming aroma that brings peaceful relaxation to Mom.", "🪻", requiredScore = 1800),
                    GardenItemEntity("flower_cherry", "Cherry Blossom", "FLOWER", "Gentle pink petals floating through warm breeze.", "🌸", requiredScore = 3000),
                    GardenItemEntity("flower_lotus", "Serene Lotus", "FLOWER", "Purity and tranquil beauty resting on clear waters.", "🪷", requiredScore = 5000),

                    // Creatures & Butterflies
                    GardenItemEntity("critter_butterfly", "Monarch Butterfly", "BUTTERFLY", "Fluttering gently among Mom's favorite flowers.", "🦋", requiredScore = 350),
                    GardenItemEntity("critter_blue_morpho", "Blue Morpho", "BUTTERFLY", "Shimmering sapphire wings dancing in the sun.", "✨", requiredScore = 1400),
                    GardenItemEntity("critter_hummingbird", "Emerald Hummingbird", "BUTTERFLY", "A tiny visitor singing a sweet melody.", "🕊️", requiredScore = 4000),

                    // Garden Features & Plants
                    GardenItemEntity("plant_lemon_tree", "Sunny Lemon Tree", "TREE", "Fresh citrus aroma filling Mom's garden path.", "🍋", requiredScore = 800),
                    GardenItemEntity("plant_bonsai", "Peaceful Bonsai", "TREE", "Symbol of patience, harmony, and enduring love.", "🪴", requiredScore = 2200),
                    GardenItemEntity("decor_stone_path", "Cobblestone Path", "DECORATION", "A smooth path leading to Mom's secret haven.", "🪨", requiredScore = 1200),
                    GardenItemEntity("decor_bench", "Mom's Resting Bench", "DECORATION", "A cozy shaded wooden bench surrounded by flowers.", "🪑", requiredScore = 2500),
                    GardenItemEntity("decor_fountain", "Singing Water Fountain", "DECORATION", "Gentle bubbling water creating pure tranquility.", "⛲", requiredScore = 4500),
                    GardenItemEntity("decor_fairy_lights", "Glowing Fairy Lights", "DECORATION", "Soft evening lights that make the garden sparkle.", "🏮", requiredScore = 6000),

                    // Themes
                    GardenItemEntity("theme_sunset", "Sunset Glow Haven", "THEME", "Warm golden hour sky wrapping the garden in amber light.", "🌅", requiredScore = 3500),
                    GardenItemEntity("theme_starlight", "Starlight Dreams", "THEME", "Twinkling night sky full of peace and quiet joy.", "🌌", requiredScore = 8000)
                )
                dao.insertDefaultItems(items)
            }

            private suspend fun populateInitialLetters(dao: SecretLettersDao) {
                val letters = listOf(
                    SecretLetterEntity(
                        id = "letter_first_win",
                        title = "Your first victory, Mom! ❤️",
                        content = "I'm so happy watching you play!\n\nEvery puzzle you solve brings a smile to my face. This whole game was crafted with tender care, thinking of you at every step.\n\nKeep having fun, Mom!",
                        milestoneLabel = "First Lines Cleared",
                        requiredScore = 0,
                        requiredLines = 1,
                        isUnlocked = true
                    ),
                    SecretLetterEntity(
                        id = "letter_500",
                        title = "You are amazing, Mom!",
                        content = "Watching you clear blocks with such clever thinking makes me so proud.\n\nYou always have that special touch that makes everything look effortless.\n\nTake your time and enjoy every moment of your day!",
                        milestoneLabel = "Reach 500 Points",
                        requiredScore = 500,
                        requiredLines = 10
                    ),
                    SecretLetterEntity(
                        id = "letter_2500",
                        title = "Every little achievement makes me proud of you.",
                        content = "Mom, you've always worked so hard for us and poured your entire heart into everything you do.\n\nSeeing you relax, smile, and enjoy this little game is the sweetest reward I could ever ask for.\n\nYou deserve all the happiness in the world.",
                        milestoneLabel = "Reach 2,500 Points",
                        requiredScore = 2500,
                        requiredLines = 40
                    ),
                    SecretLetterEntity(
                        id = "letter_5000",
                        title = "No matter how old I grow, I'll always be your child.",
                        content = "Thank you for everything, Mom.\n\nFor your warm embraces, your delicious food, your endless patience, and your unconditional love that never falters.\n\nNo matter where life takes me or how old I become, I will always be your boy, and you will always be my greatest blessing.\n\nThank you for being my mother.",
                        milestoneLabel = "Reach 5,000 Points",
                        requiredScore = 5000,
                        requiredLines = 80
                    ),
                    SecretLetterEntity(
                        id = "letter_10000",
                        title = "The Greatest Mom in the Whole World 🌸",
                        content = "Reaching this score is incredible, but having you as my mother is the greatest victory of my entire life.\n\nYou are my hero, my comfort, and my brightest inspiration.\n\nWith all my love,\nFarabe ❤️",
                        milestoneLabel = "Reach 10,000 Points",
                        requiredScore = 10000,
                        requiredLines = 150
                    )
                )
                dao.insertDefaultLetters(letters)
            }
        }
    }
}
