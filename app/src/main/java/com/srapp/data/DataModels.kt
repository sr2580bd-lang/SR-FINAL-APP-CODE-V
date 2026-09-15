package com.srapp.data

data class HabitData(
    val habitType: String,
    val completed: Boolean
)

data class DashboardData(
    val pornFreeStreak: Int,
    val longestStreak: Int,
    val level: Int,
    val levelTitle: String,
    val xp: Int,
    val habitsCompleted: Int,
    val habitsTotal: Int,
    val focusHoursThisWeek: Double,
    val totalAchievements: Int
)

data class FocusSessionData(
    val id: Int,
    val category: String,
    val startTime: String
)
