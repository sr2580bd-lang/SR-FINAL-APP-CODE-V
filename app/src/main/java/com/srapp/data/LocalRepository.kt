package com.srapp.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.srapp.SrApplication
import com.srapp.blocking.data.FocusSessionEntity
import com.srapp.blocking.data.HabitEntity
import com.srapp.blocking.data.StreakEntity
import com.srapp.blocking.data.StreakType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import kotlin.math.roundToInt

private val Context.localPreferences by preferencesDataStore(name = "sr_local_preferences")
private val onboardingCompletedKey = booleanPreferencesKey("onboarding_completed")
private val customHabitsKey = stringSetPreferencesKey("custom_habits_catalog")

class LocalRepository(private val application: SrApplication) {
    private val dao = application.database.blockingDao()
    val firebaseManager = application.firebaseManager
    val syncStatus = firebaseManager.syncStatus
    val userProfile = firebaseManager.currentUserProfile
    val onboardingCompleted: Flow<Boolean> = application.localPreferences.data.map { it[onboardingCompletedKey] ?: false }

    val customHabits: Flow<List<Pair<String, String>>> = application.localPreferences.data.map { prefs ->
        val raw = prefs[customHabitsKey] ?: emptySet()
        raw.mapNotNull { entry ->
            val parts = entry.split(":::", limit = 2)
            if (parts.size == 2) parts[0] to parts[1] else null
        }
    }

    suspend fun addCustomHabit(id: String, title: String) {
        application.localPreferences.edit { prefs ->
            val current = prefs[customHabitsKey] ?: emptySet()
            prefs[customHabitsKey] = current + "$id:::$title"
        }
    }

    suspend fun syncWithFirebase(): Result<String> {
        return firebaseManager.syncAll(application.database)
    }

    suspend fun completeOnboarding() {
        application.localPreferences.edit { it[onboardingCompletedKey] = true }
    }

    suspend fun todayHabits(): List<HabitData> = withContext(Dispatchers.IO) {
        dao.getHabitsForDate(LocalDate.now().toString()).map { HabitData(it.habitType, it.completed) }
    }

    suspend fun dashboard(): DashboardData = withContext(Dispatchers.IO) {
        val todayStr = LocalDate.now().toString()
        val habits = dao.getHabitsForDate(todayStr)
        var streak = dao.getStreak(StreakType.PORN_FREE)

        if (streak == null) {
            val initialStreak = StreakEntity(
                streakType = StreakType.PORN_FREE,
                currentStreak = 1,
                longestStreak = 1,
                lastUpdated = System.currentTimeMillis(),
                totalRelapses = 0
            )
            dao.upsertStreak(initialStreak)
            streak = initialStreak
        }

        // Calculate real focus hours this week from database
        val now = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000L
        val recentSessions = dao.completedFocusSessionsSince(now - 7 * dayMs)
        val focusMinutesThisWeek = recentSessions.sumOf { it.durationMinutes ?: 0 }
        val focusHoursThisWeek = ((focusMinutesThisWeek / 60.0) * 10.0).roundToInt() / 10.0

        // Calculate all-time XP & dynamic Level
        val allHabits = dao.getAllHabits()
        val totalCompletedHabits = allHabits.count { it.completed }
        val allFocusSessions = dao.completedFocusSessionsSince(0)
        val allFocusMinutes = allFocusSessions.sumOf { it.durationMinutes ?: 0 }

        val currentStreakDays = streak.currentStreak.coerceAtLeast(1)
        val xp = (currentStreakDays * 60) + (totalCompletedHabits * 25) + (allFocusMinutes * 2)
        val level = (1 + (xp / 300)).coerceAtLeast(1)

        val levelTitle = when {
            level <= 2 -> "Seeker"
            level <= 4 -> "Guardian"
            level <= 6 -> "Iron Will"
            level <= 9 -> "Sovereign"
            else -> "Ascendant"
        }

        val totalAchievements = (if (currentStreakDays >= 1) 1 else 0) +
                (if (currentStreakDays >= 3) 1 else 0) +
                (if (currentStreakDays >= 7) 1 else 0) +
                (if (currentStreakDays >= 14) 1 else 0) +
                (if (currentStreakDays >= 30) 1 else 0) +
                (if (allFocusMinutes >= 60) 1 else 0) +
                (if (totalCompletedHabits >= 10) 1 else 0)

        DashboardData(
            pornFreeStreak = currentStreakDays,
            longestStreak = streak.longestStreak.coerceAtLeast(currentStreakDays),
            level = level,
            levelTitle = levelTitle,
            xp = xp,
            habitsCompleted = habits.count { it.completed },
            habitsTotal = 10,
            focusHoursThisWeek = focusHoursThisWeek,
            totalAchievements = totalAchievements
        )
    }

    suspend fun toggleHabit(type: String, completed: Boolean) = withContext(Dispatchers.IO) {
        val todayStr = LocalDate.now().toString()
        dao.upsertHabit(
            HabitEntity(
                date = todayStr,
                habitType = type,
                completed = completed
            )
        )

        // If toggling the primary sobriety habit "no_porn", update streak accordingly
        if (type == "no_porn") {
            val streak = dao.getStreak(StreakType.PORN_FREE)
            if (streak != null) {
                if (completed) {
                    val updated = streak.copy(
                        currentStreak = streak.currentStreak.coerceAtLeast(1),
                        lastUpdated = System.currentTimeMillis()
                    )
                    dao.upsertStreak(updated)
                }
            }
        }
    }

    suspend fun startFocus(category: String): FocusSessionData = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val id = dao.insertFocusSession(FocusSessionEntity(category = category, startTime = startTime))
        FocusSessionData(id = id.toInt(), category = category, startTime = startTime.toString())
    }

    suspend fun endFocus(sessionId: Int): Int = withContext(Dispatchers.IO) {
        val session = dao.getFocusSession(sessionId.toLong()) ?: return@withContext 0
        if (session.completed) return@withContext session.durationMinutes ?: 0
        val endTime = System.currentTimeMillis()
        val duration = ((endTime - session.startTime) / 60_000L).toInt().coerceAtLeast(0)
        dao.updateFocusSession(session.copy(endTime = endTime, durationMinutes = duration, completed = true))
        duration
    }

    suspend fun focusHoursLastWeek(): List<Float> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val day = 24 * 60 * 60 * 1000L
        val sessions = dao.completedFocusSessionsSince(now - 7 * day)
        List(7) { offset ->
            val dayStart = now - (6 - offset) * day
            val dayEnd = dayStart + day
            sessions.filter { it.startTime in dayStart until dayEnd }
                .sumOf { it.durationMinutes ?: 0 } / 60f
        }
    }

    suspend fun logPanic() = withContext(Dispatchers.IO) {
        dao.logAttempt(
            com.srapp.blocking.data.BlockAttemptEntity(
                timestamp = System.currentTimeMillis(),
                packageName = "panic_button",
                interventionCompleted = false,
                interventionType = "panic"
            )
        )
    }

    suspend fun habitHeatmap(): List<Float> = withContext(Dispatchers.IO) {
        val habits = dao.getAllHabits()
        val days = 91
        val now = LocalDate.now()
        List(days) { offset ->
            val dateStr = now.minusDays((days - 1 - offset).toLong()).toString()
            val dayHabits = habits.filter { it.date == dateStr }
            if (dayHabits.isEmpty()) 0f
            else dayHabits.count { it.completed }.toFloat() / 10f // assume 10 habits total
        }
    }
}
