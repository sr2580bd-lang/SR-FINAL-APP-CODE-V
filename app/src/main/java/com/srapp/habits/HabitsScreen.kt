package com.srapp.habits

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.srapp.core.ui.components.PressScale
import com.srapp.core.ui.components.SectionHeader
import com.srapp.core.ui.motion.SrMotion
import com.srapp.core.ui.theme.SrTheme
import com.srapp.data.LocalRepository
import kotlinx.coroutines.launch

data class HabitItem(val id: String, val label: String, val done: Boolean, val negative: Boolean = false)

private fun defaultHabits() = listOf(
    HabitItem("wake_on_time", "Woke up on time", false),
    HabitItem("no_porn", "No porn today", false),
    HabitItem("exercise", "Exercise completed", true),
    HabitItem("study_session_1", "Study session 1", false),
    HabitItem("study_session_2", "Study session 2", false),
    HabitItem("healthy_meal", "Healthy meal eaten", false),
    HabitItem("meditation", "Meditation done", false),
    HabitItem("reading", "Reading done", false),
    HabitItem("journaling", "Journaling done", false),
    HabitItem("sleep_on_time", "Sleep on time", false)
)

@Composable
fun HabitsScreen(repository: LocalRepository) {
    var habits by remember { mutableStateOf(defaultHabits()) }
    var heatmapValues by remember { mutableStateOf(List(91) { 0f }) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        val remote = runCatching { repository.todayHabits() }.getOrNull() ?: return@LaunchedEffect
        habits = habits.map { habit ->
            remote.find { it.habitType == habit.id }?.let { habit.copy(done = it.completed) } ?: habit
        }
        heatmapValues = runCatching { repository.habitHeatmap() }.getOrDefault(heatmapValues)
    }
    val completed = habits.count { it.done }
    val progress = completed / habits.size.toFloat()

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        SectionHeader(title = "Today's Habits")
        Spacer(Modifier.height(4.dp))
        Text(
            "$completed of ${habits.size} complete",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))
        HabitProgressBar(progress)
        Spacer(Modifier.height(20.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
            items(habits, key = { it.id }) { habit ->
                HabitRow(habit) { toggled ->
                    val completed = !toggled.done
                    habits = habits.map { if (it.id == toggled.id) it.copy(done = completed) else it }
                    scope.launch { runCatching { repository.toggleHabit(toggled.id, completed) } }
                }
            }
            item {
                Spacer(Modifier.height(20.dp))
                Text("Last 90 days", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(10.dp))
                ContributionHeatmap(heatmapValues)
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun HabitProgressBar(progress: Float) {
    val animated by animateFloatAsState(targetValue = progress, animationSpec = SrMotion.emphasized(500), label = "habit_progress")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .fillMaxHeight()
                .background(
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                    ),
                    RoundedCornerShape(50)
                )
        )
    }
}

@Composable
private fun HabitRow(habit: HabitItem, onToggle: (HabitItem) -> Unit) {
    val checkColor by animateCheckColor(habit.done)
    PressScale(onClick = { onToggle(habit) }, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(if (habit.done) checkColor else Color.Transparent, CircleShape)
                    .border(2.dp, if (habit.done) Color.Transparent else MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (habit.done) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            Text(
                habit.label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (habit.done) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (habit.done) FontWeight.Medium else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun animateCheckColor(done: Boolean) = androidx.compose.animation.animateColorAsState(
    targetValue = if (done) SrTheme.extended.success else MaterialTheme.colorScheme.outline,
    animationSpec = SrMotion.emphasized(250),
    label = "habit_check_color"
)

/** GitHub-style heatmap: 13 weeks x 7 days, color intensity = habit completion ratio that day. */
@Composable
private fun ContributionHeatmap(values: List<Float>) {
    val weeks = 13
    val days = 7
    val cellSpacing = 4.dp
    val successColor = SrTheme.extended.success

    Canvas(modifier = Modifier.fillMaxWidth().height(110.dp)) {
        val cellSize = (size.width - (weeks - 1) * cellSpacing.toPx()) / weeks
        val rowHeight = (size.height - (days - 1) * cellSpacing.toPx()) / days
        val cell = minOf(cellSize, rowHeight)
        for (w in 0 until weeks) {
            for (d in 0 until days) {
                val v = values[w * days + d]
                val alpha = 0.12f + v * 0.8f
                drawRoundRect(
                    color = successColor.copy(alpha = alpha),
                    topLeft = androidx.compose.ui.geometry.Offset(
                        w * (cell + cellSpacing.toPx()),
                        d * (cell + cellSpacing.toPx())
                    ),
                    size = Size(cell, cell),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
            }
        }
    }
}
