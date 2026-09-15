package com.srapp.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.srapp.SrApplication
import com.srapp.blocking.data.FocusSessionEntity
import com.srapp.blocking.data.HabitEntity
import com.srapp.blocking.data.StreakType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.localPreferences by preferencesDataStore(name = "sr_local_preferences")
private val onboardingCompletedKey = booleanPreferencesKey("onboarding_completed")

class LocalRepository(private val application: SrApplication) {
    private val dao = application.database.blockingDao()
    val firebaseManager = application.firebaseManager
    val syncStatus = firebaseManager.syncStatus
    val userProfile = firebaseManager.currentUserProfile
    val onboardingCompleted: Flow<Boolean> = application.localPreferences.data.map { it[onboardingCompletedKey] ?: false }

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
        val habits = dao.getHabitsForDate(LocalDate.now().toString())
        val streak = dao.getStreak(StreakType.PORN_FREE)
        DashboardData(
            pornFreeStreak = streak?.currentStreak ?: 0,
            longestStreak = streak?.longestStreak ?: 0,
            level = 1,
            levelTitle = "Wanderer",
            xp = 0,
            habitsCompleted = habits.count { it.completed },
            habitsTotal = 10,
            focusHoursThisWeek = 0.0,
            totalAchievements = 0
        )
    }

    suspend fun toggleHabit(type: String, completed: Boolean) = withContext(Dispatchers.IO) {
        dao.upsertHabit(
            HabitEntity(
                date = LocalDate.now().toString(),
                habitType = type,
                completed = completed
            )
        )
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
