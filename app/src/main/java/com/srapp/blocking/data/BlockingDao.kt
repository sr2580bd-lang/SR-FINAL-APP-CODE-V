package com.srapp.blocking.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockingDao {

    // ---- blocked apps ----
    @Query("SELECT * FROM blocked_apps")
    fun observeBlockedApps(): Flow<List<BlockedAppEntity>>

    @Query("SELECT * FROM blocked_apps")
    suspend fun getBlockedAppsOnce(): List<BlockedAppEntity>

    @Query("SELECT packageName FROM blocked_apps")
    suspend fun getBlockedPackageNames(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBlockedApp(app: BlockedAppEntity)

    @Query("DELETE FROM blocked_apps WHERE packageName = :packageName")
    suspend fun removeBlockedApp(packageName: String)

    // ---- block attempts (fuel for Module 7's pattern detection later) ----
    @Insert
    suspend fun logAttempt(attempt: BlockAttemptEntity)

    @Query("SELECT * FROM block_attempts ORDER BY timestamp DESC LIMIT :limit")
    suspend fun recentAttempts(limit: Int = 50): List<BlockAttemptEntity>

    @Query("SELECT COUNT(*) FROM block_attempts WHERE timestamp >= :sinceEpochMs")
    suspend fun attemptCountSince(sinceEpochMs: Long): Int

    // ---- streaks ----
    @Query("SELECT * FROM streaks WHERE streakType = :type")
    fun observeStreak(type: StreakType): Flow<StreakEntity?>

    @Query("SELECT * FROM streaks WHERE streakType = :type LIMIT 1")
    suspend fun getStreak(type: StreakType): StreakEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStreak(streak: StreakEntity)

    @Update
    suspend fun updateStreak(streak: StreakEntity)

    @Query("SELECT * FROM habits WHERE date = :date")
    suspend fun getHabitsForDate(date: String): List<HabitEntity>

    @Query("SELECT * FROM habits")
    suspend fun getAllHabits(): List<HabitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHabit(habit: HabitEntity)

    @Insert
    suspend fun insertFocusSession(session: FocusSessionEntity): Long

    @Query("SELECT * FROM focus_sessions WHERE id = :id LIMIT 1")
    suspend fun getFocusSession(id: Long): FocusSessionEntity?

    @Update
    suspend fun updateFocusSession(session: FocusSessionEntity)

    @Query("SELECT * FROM focus_sessions WHERE completed = 1 AND startTime >= :since ORDER BY startTime ASC")
    suspend fun completedFocusSessionsSince(since: Long): List<FocusSessionEntity>
}
