package com.srapp.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.srapp.core.ui.components.CountUpText
import com.srapp.core.ui.components.GlassChip
import com.srapp.core.ui.components.GradientCard
import com.srapp.core.ui.components.PressScale
import com.srapp.core.ui.theme.DangerRed
import com.srapp.core.ui.theme.GradientVioletEnd
import com.srapp.core.ui.theme.GradientVioletStart

@Composable
fun DashboardScreen(
    pornFreeStreakDays: Int,
    longestStreakDays: Int,
    level: Int,
    levelTitle: String,
    habitsCompleted: Int,
    habitsTotal: Int,
    onManageBlockedApps: () -> Unit,
    onPanicPressed: () -> Unit = {},
    onStartFocus: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                Text(
                    "SR APP",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    greeting(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        item { StreakHeroCard(pornFreeStreakDays, longestStreakDays, level, levelTitle) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatChip(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Shield,
                    label = "Habits done",
                    value = "$habitsCompleted/$habitsTotal"
                )
                StatChip(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.LocalFireDepartment,
                    label = "Level",
                    value = level.toString()
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    label = "Start Focus",
                    onClick = onStartFocus,
                    filled = true
                )
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    label = "Manage Apps",
                    onClick = onManageBlockedApps,
                    filled = false
                )
            }
        }

        item { PanicButton(onPanicPressed) }
    }
}

private fun greeting(): String {
    val hour = java.time.LocalTime.now().hour
    return when {
        hour < 5 -> "Still up? Be honest with yourself."
        hour < 12 -> "Good morning. Day one, every day."
        hour < 17 -> "Stay locked in."
        hour < 22 -> "Evening check-in."
        else -> "Late night. This is the danger zone."
    }
}

@Composable
private fun StreakHeroCard(days: Int, longestStreak: Int, level: Int, levelTitle: String) {
    GradientCard(
        colors = listOf(GradientVioletStart, GradientVioletEnd),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "🔥 PORN-FREE STREAK",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.85f)
                )
                GlassChip(text = "LEVEL $level · ${levelTitle.uppercase()}")
            }
            Row(verticalAlignment = Alignment.Bottom) {
                CountUpText(targetValue = days, color = Color.White)
                Text(
                    if (days == 1) " day" else " days",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
            Text(
                "Longest streak: $longestStreak days. Don't break the chain today.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun QuickActionButton(modifier: Modifier = Modifier, label: String, onClick: () -> Unit, filled: Boolean) {
    PressScale(onClick = onClick, modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(
                    if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                color = if (filled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun PanicButton(onClick: () -> Unit) {
    PressScale(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DangerRed.copy(alpha = 0.14f), RoundedCornerShape(20.dp))
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = DangerRed)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("I'm about to relapse — help", color = DangerRed, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("Locks everything instantly, no questions asked.", color = DangerRed.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun StatChip(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
