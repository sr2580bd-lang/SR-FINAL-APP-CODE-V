package com.srapp.focus

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.srapp.audio.SoundscapeEngine
import com.srapp.core.ui.components.GradientProgressRing
import com.srapp.core.ui.components.PressScale
import com.srapp.core.ui.theme.NeonCyan
import com.srapp.core.ui.theme.NeonCyanDim
import com.srapp.core.ui.theme.SignalAmber
import com.srapp.data.LocalRepository
import kotlinx.coroutines.launch

data class FocusCategory(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val defaultMinutes: Int
)

private val categories = listOf(
    FocusCategory("job_study", "Job Study", Icons.Filled.School, Color(0xFF8B5CF6), 50),
    FocusCategory("nursing_study", "Nursing Study", Icons.Filled.LocalHospital, Color(0xFFEC4899), 50),
    FocusCategory("degree_study", "Degree Study", Icons.Filled.MenuBook, Color(0xFF22D3EE), 50),
    FocusCategory("personal_improvement", "Personal Growth", Icons.Filled.Psychology, Color(0xFFFBBF24), 30),
    FocusCategory("skill_learning", "Skill Learning", Icons.Filled.Star, Color(0xFF34D399), 45),
    FocusCategory("gym_work", "Gym Work", Icons.Filled.FitnessCenter, Color(0xFFF97316), 60)
)

@Composable
fun FocusScreen(repository: LocalRepository) {
    val context = LocalContext.current
    val soundscape = remember { SoundscapeEngine.get(context) }
    var activeCategory by remember { mutableStateOf<FocusCategory?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = activeCategory,
            label = "focus_flow",
            transitionSpec = { fadeIn() togetherWith fadeOut() }
        ) { active ->
            if (active == null) {
                FocusCategoryPicker(
                    onSelect = {
                        soundscape.playChime()
                        activeCategory = it
                    }
                )
            } else {
                ActiveFocusSession(
                    category = active,
                    repository = repository,
                    onEnd = {
                        soundscape.playTrophyFanfare()
                        activeCategory = null
                    }
                )
            }
        }
    }
}

@Composable
private fun FocusCategoryPicker(onSelect: (FocusCategory) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Focus", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onBackground)
        Text(
            "Pick what you're protecting your time for.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(categories) { category ->
                FocusCategoryTile(category, onClick = { onSelect(category) })
            }
        }
    }
}

@Composable
private fun FocusCategoryTile(category: FocusCategory, onClick: () -> Unit) {
    PressScale(onClick = onClick, modifier = Modifier.aspectRatio(1f)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(category.color.copy(alpha = 0.14f), RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Icon(category.icon, contentDescription = null, tint = category.color, modifier = Modifier.size(32.dp))
                Column {
                    Text(category.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                    Text(
                        "${category.defaultMinutes} min",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveFocusSession(category: FocusCategory, repository: LocalRepository, onEnd: () -> Unit) {
    val totalSeconds = category.defaultMinutes * 60
    var remaining by remember(category) { mutableStateOf(totalSeconds) }
    var running by remember(category) { mutableStateOf(true) }
    var sessionId by remember(category) { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(category) {
        sessionId = runCatching { repository.startFocus(category.id).id }.getOrNull()
    }

    LaunchedEffect(category, running) {
        while (running && remaining > 0) {
            kotlinx.coroutines.delay(1000)
            remaining--
        }
    }

    val progress = 1f - remaining / totalSeconds.toFloat()
    val minutes = remaining / 60
    val seconds = remaining % 60

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            category.title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = category.color
        )
        Spacer(Modifier.height(20.dp))
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(260.dp)) {
            GradientProgressRing(
                progress = progress,
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 14.dp,
                startColor = NeonCyan,
                endColor = category.color
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    String.format("%02d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "All other apps are locked",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { remaining += 300 },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("+5 min", style = MaterialTheme.typography.labelSmall)
            }
            OutlinedButton(
                onClick = { remaining += 900 },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("+15 min", style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { running = !running }, shape = RoundedCornerShape(16.dp)) {
                Text(if (running) "Pause" else "Resume")
            }
            Button(
                onClick = {
                    sessionId?.let { id ->
                        scope.launch {
                            runCatching { repository.endFocus(id) }
                            runCatching { repository.syncWithFirebase() }
                        }
                    }
                    onEnd()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("End Session", color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.SemiBold)
            }
        }
        if (remaining == 0) {
            Spacer(Modifier.height(16.dp))
            Text("Session complete — nice work.", color = SignalAmber, style = MaterialTheme.typography.titleMedium)
        }
    }
}
