package com.srapp.music.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.srapp.core.ui.theme.*
import com.srapp.gemini.LyriaModelType
import com.srapp.music.LyriaMusicViewModel
import com.srapp.music.MusicPlaybackState
import com.srapp.music.SoundscapePreset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundscapeStudioScreen(
    viewModel: LyriaMusicViewModel = viewModel()
) {
    val playbackState by viewModel.playbackState.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val activePreset by viewModel.activePreset.collectAsState()
    val customPrompt by viewModel.customPrompt.collectAsState()
    val isLooping by viewModel.isLooping.collectAsState()
    val waveAmplitudes by viewModel.waveAmplitudes.collectAsState()

    val isPlaying = playbackState is MusicPlaybackState.Playing

    Scaffold(
        containerColor = VoidBlack,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(NeonCyan, ElectricViolet)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = VoidBlack,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Lyria Soundscape Studio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Generative Audio & Binaural Entrainment",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonCyan
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VoidBlack)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(VoidBlack),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Visualizer & Player Card
            item {
                SoundscapeVisualizerCard(
                    playbackState = playbackState,
                    activePreset = activePreset,
                    waveAmplitudes = waveAmplitudes,
                    isPlaying = isPlaying,
                    isLooping = isLooping,
                    onTogglePlay = {
                        when (playbackState) {
                            is MusicPlaybackState.Playing -> viewModel.pausePlayback()
                            is MusicPlaybackState.Paused -> viewModel.resumePlayback()
                            else -> viewModel.startSoundscape()
                        }
                    },
                    onStop = { viewModel.stopPlayback() },
                    onToggleLoop = { viewModel.toggleLoop() }
                )
            }

            // Model Architecture Selector
            item {
                Text(
                    text = "Lyria AI Architecture",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LyriaModelType.entries.forEach { model ->
                        val isSelected = model == selectedModel
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { viewModel.selectModel(model) },
                            color = if (isSelected) VoidSurfaceHigh else VoidSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) NeonCyan else VoidOutline
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = model.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) NeonCyanBright else TextPrimary
                                    )
                                    Surface(
                                        color = if (isSelected) NeonCyanDim else VoidSurfaceRaised,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = model.durationLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) NeonCyanBright else TextSecondary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = model.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Presets Section Header
            item {
                Text(
                    text = "Acoustic & Binaural Presets",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Preset Cards
            items(viewModel.presets, key = { it.id }) { preset ->
                PresetCard(
                    preset = preset,
                    isSelected = preset.id == activePreset.id,
                    onSelect = { viewModel.selectPreset(preset) }
                )
            }

            // Custom Generative Prompt Card
            item {
                Surface(
                    color = VoidSurface,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VoidOutline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Custom Soundscape Prompt",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Direct Lyria 3 to synthesize custom acoustic textures or frequencies",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        TextField(
                            value = customPrompt,
                            onValueChange = { viewModel.updateCustomPrompt(it) },
                            placeholder = {
                                Text(
                                    "E.g., 432Hz deep meditative ambient rain with gentle synth pads...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextDisabled
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(VoidSurfaceHigh),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = VoidSurfaceHigh,
                                unfocusedContainerColor = VoidSurfaceHigh,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.startSoundscape() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricViolet
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Synthesize with ${selectedModel.displayName}",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SoundscapeVisualizerCard(
    playbackState: MusicPlaybackState,
    activePreset: SoundscapePreset,
    waveAmplitudes: List<Float>,
    isPlaying: Boolean,
    isLooping: Boolean,
    onTogglePlay: () -> Unit,
    onStop: () -> Unit,
    onToggleLoop: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "halo")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = if (isPlaying) 1.04f else 0.98f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Surface(
        color = VoidSurfaceRaised,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isPlaying) NeonCyan.copy(alpha = 0.6f) else VoidOutline
        ),
        modifier = Modifier
            .fillMaxWidth()
            .scale(if (isPlaying) pulseScale else 1f)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Track Info Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (isPlaying) SuccessGreenDim else VoidSurfaceHigh,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = when (playbackState) {
                            is MusicPlaybackState.Playing -> if (playbackState.isProcedural) "LIVE PROCEDURAL" else "LYRIA GENERATED"
                            is MusicPlaybackState.Generating -> "SYNTHESIZING..."
                            is MusicPlaybackState.Paused -> "PAUSED"
                            else -> "STANDBY"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isPlaying) SuccessGreen else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                IconButton(
                    onClick = onToggleLoop,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "Loop",
                        tint = if (isLooping) NeonCyan else TextDisabled,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = activePreset.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = activePreset.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Waveform Graphic Visualizer Bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(VoidSurface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                waveAmplitudes.forEachIndexed { idx, amp ->
                    val barHeight = (amp * 44).dp.coerceAtLeast(4.dp)
                    val barColor = if (idx % 2 == 0) NeonCyan else ElectricVioletBright
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(barHeight)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isPlaying) barColor else TextDisabled.copy(alpha = 0.3f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Player Action Buttons
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onStop,
                    enabled = isPlaying || playbackState is MusicPlaybackState.Paused,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(VoidSurfaceHigh)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        tint = if (isPlaying || playbackState is MusicPlaybackState.Paused) DangerRed else TextDisabled
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                Button(
                    onClick = onTogglePlay,
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlaying) NeonCyan else ElectricViolet
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = if (isPlaying) VoidBlack else Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetCard(
    preset: SoundscapePreset,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        color = if (isSelected) VoidSurfaceHigh else VoidSurface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) ElectricViolet else VoidOutline
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) ElectricVioletDim else VoidSurfaceRaised),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (preset.iconName) {
                        "wave" -> Icons.Default.Waves
                        "chime" -> Icons.Default.NotificationsActive
                        "rain" -> Icons.Default.WaterDrop
                        "bolt" -> Icons.Default.Bolt
                        else -> Icons.Default.Nightlight
                    },
                    contentDescription = null,
                    tint = if (isSelected) NeonCyanBright else TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = preset.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) TextPrimary else TextSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = preset.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDisabled,
                    fontSize = 12.sp
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = ElectricVioletBright,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
