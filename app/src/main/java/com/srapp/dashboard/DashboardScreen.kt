package com.srapp.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.srapp.audio.SoundscapeEngine
import com.srapp.core.ui.components.CountUpText
import com.srapp.core.ui.components.GlassChip
import com.srapp.core.ui.components.GradientCard
import com.srapp.core.ui.components.PressScale
import com.srapp.core.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

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
    onStartFocus: () -> Unit = {},
    onOpenAuthSync: () -> Unit = {},
    onOpenFortress: () -> Unit = {},
    onOpenSoundscape: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenStats: () -> Unit = {}
) {
    val context = LocalContext.current
    val soundscape = remember { SoundscapeEngine.get(context) }
    var showInterventionDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Top Header: App Title, Cloud Sync status badge, and Avatar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "SR APP",
                                style = MaterialTheme.typography.labelLarge,
                                color = NeonCyan,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = ElectricVioletDim,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    "VOIDFORGE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElectricVioletBright,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            greeting(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Stats icon button
                        IconButton(
                            onClick = onOpenStats,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(VoidSurfaceHigh)
                        ) {
                            Icon(
                                Icons.Filled.BarChart,
                                contentDescription = "Stats",
                                tint = NeonCyanBright,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Cloud Sync Pill Affordance
                        PressScale(onClick = onOpenAuthSync) {
                            Surface(
                                color = VoidSurfaceHigh,
                                shape = RoundedCornerShape(50),
                                border = androidx.compose.foundation.BorderStroke(1.dp, VoidOutline),
                                modifier = Modifier.testTag("cloud_sync_header_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(SuccessGreen)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Icon(
                                        Icons.Filled.CloudSync,
                                        contentDescription = "Cloud Sync",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "Sync",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { StreakHeroCard(pornFreeStreakDays, longestStreakDays, level, levelTitle) }

            // God-Tier Spotlight: Mental Fortress AI Card
            item {
                Surface(
                    color = VoidSurfaceRaised,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenFortress() }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(ElectricViolet, NeonCyan)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        "Mental Fortress AI",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        "Multi-turn Gemini 3.1 & 3.5 Recovery Engine",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonCyan
                                    )
                                }
                            }
                            Icon(
                                Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Acute craving or trigger? Engage multi-turn cognitive behavioral restructuring, 5-minute surge deflection, or deep neurochemical journaling.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                color = VoidSurfaceHigh,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "⚡ Flash Lite",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonCyanBright,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                color = VoidSurfaceHigh,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "🧠 Flash 3.5",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                color = VoidSurfaceHigh,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "🔬 Pro 3.1 CBT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElectricVioletBright,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // God-Tier Spotlight: Lyria Soundscape Studio Card
            item {
                Surface(
                    color = VoidSurfaceRaised,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenSoundscape() }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(NeonCyanDim),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.GraphicEq,
                                contentDescription = null,
                                tint = NeonCyanBright,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Lyria Soundscape Studio",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "528Hz Solfeggio & Theta Entrainment Waves",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = NeonCyanBright,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

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

            // Gesture-based Slide to Activate Emergency Panic
            item {
                SlideToPanicBar(
                    onActivated = {
                        soundscape.playTacticalPulse()
                        onPanicPressed()
                        showInterventionDialog = true
                    }
                )
            }
        }

        // In-App Urge De-escalation & 4-7-8 Breathing Intervention Dialog
        if (showInterventionDialog) {
            EmergencyInterventionDialog(
                onDismiss = { showInterventionDialog = false },
                onLockApps = {
                    showInterventionDialog = false
                    onManageBlockedApps()
                }
            )
        }
    }
}

private fun greeting(): String {
    val hour = java.time.LocalTime.now().hour
    return when {
        hour < 5 -> "Still up? Guard your mind."
        hour < 12 -> "Day one, every day."
        hour < 17 -> "Stay locked in."
        hour < 22 -> "Evening check-in."
        else -> "Late night. Danger zone."
    }
}

@Composable
private fun StreakHeroCard(days: Int, longestStreak: Int, level: Int, levelTitle: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "flame_pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    GradientCard(
        colors = listOf(GradientVioletStart, GradientVioletEnd),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                Brush.horizontalGradient(
                    listOf(
                        NeonCyan.copy(alpha = glowAlpha),
                        SignalAmber.copy(alpha = glowAlpha)
                    )
                ),
                RoundedCornerShape(24.dp)
            )
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "🔥 PORN-FREE STREAK",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold
                )
                GlassChip(text = "LVL $level · ${levelTitle.uppercase()}")
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                CountUpText(targetValue = days, color = Color.White)
                Text(
                    if (days == 1) " day clean" else " days clean",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
                )
            }
            Text(
                "Longest streak: $longestStreak days. Every urge defeated builds neuroplastic strength.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.82f)
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

/**
 * Modern gesture-based Slide-to-Activate Emergency Panic button.
 * Prevents accidental taps while providing tactical slide gesture feedback.
 */
@Composable
private fun SlideToPanicBar(
    onActivated: () -> Unit
) {
    val density = LocalDensity.current
    val maxDragPx = with(density) { 220.dp.toPx() }
    var offsetX by remember { mutableFloatStateOf(0f) }

    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "drag_snap"
    )

    val progress = (animatedOffsetX / maxDragPx).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        DangerRed.copy(alpha = 0.15f + progress * 0.45f),
                        DangerRed.copy(alpha = 0.25f + progress * 0.65f)
                    )
                )
            )
            .border(1.dp, DangerRed.copy(alpha = 0.4f + progress * 0.5f), RoundedCornerShape(20.dp))
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // Center Prompt Text
        Text(
            text = if (progress > 0.85f) "RELEASE TO ACTIVATE SOS" else "SLIDE FOR SOS INTERVENTION >>>",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (progress > 0.6f) Color.White else DangerRed,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
        )

        // Draggable Thumb
        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .size(52.dp)
                .clip(CircleShape)
                .background(DangerRed)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        offsetX = (offsetX + delta).coerceIn(0f, maxDragPx)
                    },
                    onDragStopped = {
                        if (offsetX >= maxDragPx * 0.75f) {
                            onActivated()
                        }
                        offsetX = 0f
                    }
                )
                .testTag("panic_drag_thumb"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = "Slide to Panic",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun EmergencyInterventionDialog(
    onDismiss: () -> Unit,
    onLockApps: () -> Unit
) {
    val context = LocalContext.current
    val soundscape = remember { SoundscapeEngine.get(context) }
    var phase by remember { mutableStateOf("Inhale (4s)") }
    var secondsLeft by remember { mutableIntStateOf(4) }
    var currentCycle by remember { mutableIntStateOf(1) }

    LaunchedEffect(Unit) {
        while (currentCycle <= 3) {
            phase = "Inhale slowly through your nose"
            soundscape.playBreathTone(true)
            for (i in 4 downTo 1) {
                secondsLeft = i
                delay(1000)
            }
            phase = "Hold your breath gently"
            soundscape.triggerHapticClick()
            for (i in 7 downTo 1) {
                secondsLeft = i
                delay(1000)
            }
            phase = "Exhale completely through your mouth"
            soundscape.playBreathTone(false)
            for (i in 8 downTo 1) {
                secondsLeft = i
                delay(1000)
            }
            currentCycle++
        }
        phase = "Urge peak passed. You are in control."
        soundscape.playChime()
    }

    val infiniteScale = rememberInfiniteTransition(label = "breathe_pulse")
    val circleScale by infiniteScale.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "circle_scale"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            color = Color(0xFF0D0D14),
            shape = RoundedCornerShape(28.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "EMERGENCY PROTOCOL",
                        style = MaterialTheme.typography.labelLarge,
                        color = DangerRed,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Pause. 4-7-8 Reset",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Urges peak at 90 seconds. Breathe with the rhythm.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.75f),
                        textAlign = TextAlign.Center
                    )
                }

                // Breathing Visualizer
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(220.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size((160 * circleScale).dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(NeonCyan.copy(alpha = 0.35f), Color.Transparent)
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(2.dp, NeonCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "$secondsLeft",
                                style = MaterialTheme.typography.displayMedium,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "sec",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        phase,
                        style = MaterialTheme.typography.titleMedium,
                        color = SignalAmber,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Cycle $currentCycle of 3",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onLockApps,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Lock All Trigger Apps Now", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("I'm Grounded Now — Dismiss", color = Color.White)
                    }
                }
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
