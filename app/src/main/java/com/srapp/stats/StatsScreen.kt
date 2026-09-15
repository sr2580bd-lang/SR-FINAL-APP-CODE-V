package com.srapp.stats

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.srapp.core.ui.components.CountUpText
import com.srapp.core.ui.components.GradientCard
import com.srapp.core.ui.components.SectionHeader
import com.srapp.core.ui.motion.SrMotion
import com.srapp.core.ui.theme.GradientVioletEnd
import com.srapp.core.ui.theme.GradientVioletStart
import com.srapp.core.ui.theme.NeonCyan
import com.srapp.data.LocalRepository

private val weekLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

@Composable
fun StatsScreen(repository: LocalRepository) {
    var focusHoursThisWeek by remember { mutableStateOf(List(7) { 0f }) }
    LaunchedEffect(Unit) {
        focusHoursThisWeek = repository.focusHoursLastWeek()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        SectionHeader(title = "Your Progress")
        Spacer(Modifier.height(16.dp))

        GradientCard(
            colors = listOf(GradientVioletStart, GradientVioletEnd),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text("TIME RECLAIMED", style = MaterialTheme.typography.labelLarge, color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f))
                Row(verticalAlignment = Alignment.Bottom) {
                    CountUpText(targetValue = 270, color = androidx.compose.ui.graphics.Color.White)
                    Text(
                        " hours",
                        style = MaterialTheme.typography.titleLarge,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }
                Text(
                    "That's 11.25 days of your life back since you started.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f)
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Focus hours this week", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(12.dp))
        WeeklyFocusBarChart(focusHoursThisWeek)

        Spacer(Modifier.height(24.dp))
        InsightCard("You're most consistent 6–8 AM. Danger zone: Friday nights.")
        Spacer(Modifier.height(10.dp))
        InsightCard("Exercise days show a 65% lower relapse rate than rest days.")
    }
}

@Composable
private fun WeeklyFocusBarChart(values: List<Float>) {
    val maxValue = values.maxOrNull()?.coerceAtLeast(1f) ?: 1f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        weekLabels.forEachIndexed { index, label ->
            val fraction by animateFloatAsState(
                targetValue = values[index] / maxValue,
                animationSpec = SrMotion.emphasized(600),
                label = "bar_$index"
            )
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .fillMaxHeight(fraction.coerceIn(0.04f, 1f))
                        .background(
                            Brush.verticalGradient(listOf(NeonCyan, GradientVioletStart)),
                            RoundedCornerShape(6.dp)
                        )
                )
                Spacer(Modifier.height(6.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun InsightCard(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text("💡", modifier = Modifier.padding(end = 10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
