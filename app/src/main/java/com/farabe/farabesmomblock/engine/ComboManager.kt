package com.farabe.farabesmomblock.engine

/**
 * Event generated when evaluating a turn's combo state.
 */
data class ComboEvent(
    val streak: Int,
    val isComboActive: Boolean,
    val feedbackMessage: String?,
    val celebratoryTitle: String?,
    val pitchMultiplier: Float
)

/**
 * Tracks consecutive turn line-clear streaks and supplies motivational titles.
 */
class ComboManager {

    var currentStreak: Int = 0
        private set

    var maxStreakAchieved: Int = 0
        private set

    /**
     * Updates combo state following a move.
     * @param linesCleared Number of lines cleared in this move.
     * @return [ComboEvent] describing feedback to show and sound modifications.
     */
    fun onMoveCompleted(linesCleared: Int): ComboEvent {
        return if (linesCleared > 0) {
            currentStreak++
            if (currentStreak > maxStreakAchieved) {
                maxStreakAchieved = currentStreak
            }

            val (title, msg) = when (currentStreak) {
                1 -> Pair("GREAT CLEAR!", if (linesCleared > 1) "Multi-Line Cleared!" else "Nice placement, Mom!")
                2 -> Pair("COMBO x2!", "AMAZING! Keep it up!")
                3 -> Pair("COMBO x3!", "SUPER COMBO! You are incredible!")
                4 -> Pair("COMBO x4!", "SENSATIONAL! Pure genius!")
                5 -> Pair("COMBO x5!", "UNBELIEVABLE! Mom's on fire! ❤️")
                else -> Pair("LEGENDARY x$currentStreak!", "OUT OF THIS WORLD, MOM! 🌟")
            }

            // Step pitch up with each consecutive combo, up to 2.0x
            val pitch = 1.0f + ((currentStreak - 1).coerceAtMost(6) * 0.15f)

            ComboEvent(
                streak = currentStreak,
                isComboActive = currentStreak >= 2,
                feedbackMessage = msg,
                celebratoryTitle = title,
                pitchMultiplier = pitch
            )
        } else {
            currentStreak = 0
            ComboEvent(
                streak = 0,
                isComboActive = false,
                feedbackMessage = null,
                celebratoryTitle = null,
                pitchMultiplier = 1.0f
            )
        }
    }

    fun reset() {
        currentStreak = 0
        maxStreakAchieved = 0
    }

    fun restore(streak: Int, maxStreak: Int) {
        currentStreak = streak
        maxStreakAchieved = maxStreak
    }
}
