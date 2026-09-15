package com.srapp.blocking.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BlockType { PERMANENT, SCHEDULED, TIME_LIMITED }

@Entity(tableName = "blocked_apps")
data class BlockedAppEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val blockType: BlockType,
    val dailyLimitMinutes: Int? = null,
    val category: String = "general"
)

@Entity(tableName = "block_attempts")
data class BlockAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val packageName: String,
    val interventionCompleted: Boolean,
    val interventionType: String, // "math" | "reflection" | "wait"
    val triggerNote: String? = null // filled in by the urge-journal question
)

enum class StreakType { PORN_FREE, GAME_FREE, PERFECT_ROUTINE }

@Entity(tableName = "streaks")
data class StreakEntity(
    @PrimaryKey val streakType: StreakType,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis(),
    val totalRelapses: Int = 0
)

@Entity(tableName = "habits", primaryKeys = ["date", "habitType"])
data class HabitEntity(
    val date: String,
    val habitType: String,
    val completed: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val startTime: Long,
    val endTime: Long? = null,
    val durationMinutes: Int? = null,
    val completed: Boolean = false
)
