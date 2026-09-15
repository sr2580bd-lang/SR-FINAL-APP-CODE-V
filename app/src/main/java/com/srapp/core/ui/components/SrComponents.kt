package com.srapp.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.srapp.core.ui.motion.SrMotion
import kotlin.math.roundToInt

/**
 * Every clickable "hero" surface in the app (streak card, boss-battle card,
 * focus category tile) should wrap its content in this instead of a plain
 * clickable Modifier — the press-scale is what makes the whole app feel
 * tactile instead of flat/web-like.
 */
@Composable
fun PressScale(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    pressedScale: Float = 0.96f,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = SrMotion.snappy(),
        label = "press_scale"
    )
    Box(
        modifier = modifier
            .scale(scale)
            .pointerInput(Unit) {
                detectTapWithInteraction(interactionSource, onClick)
            },
        content = content
    )
}

private suspend fun androidx.compose.ui.input.pointer.PointerInputScope.detectTapWithInteraction(
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit
) {
    detectTapGestures(
        onPress = {
            val press = androidx.compose.foundation.interaction.PressInteraction.Press(it)
            interactionSource.emit(press)
            val released = tryAwaitRelease()
            interactionSource.emit(
                if (released) androidx.compose.foundation.interaction.PressInteraction.Release(press)
                else androidx.compose.foundation.interaction.PressInteraction.Cancel(press)
            )
        },
        onTap = { onClick() }
    )
}

/** A rounded gradient surface used for hero cards (streak, boss battles, level-up). */
@Composable
fun GradientCard(
    colors: List<Color>,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(28.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(Brush.linearGradient(colors))
            .padding(24.dp),
        content = content
    )
}

/** Frosted-glass style surface for overlay chips/badges on top of imagery or gradients. */
@Composable
fun GlassChip(text: String, modifier: Modifier = Modifier, tint: Color = Color.White) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(tint.copy(alpha = 0.16f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, color = tint, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}

/** Animated count-up number — used for streaks, XP, hours reclaimed. Never jump-cut a big number. */
@Composable
fun CountUpText(
    targetValue: Int,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.displayLarge,
    color: Color = Color.White,
    durationMs: Int = 800
) {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(targetValue) {
        anim.animateTo(
            targetValue.toFloat(),
            animationSpec = tween(durationMs, easing = LinearOutSlowInEasing)
        )
    }
    Text(text = anim.value.roundToInt().toString(), style = style, color = color, modifier = modifier)
}

/**
 * Circular progress ring with a gradient stroke — used for focus-session
 * timers and daily-habit completion. Deliberately not Material's default
 * CircularProgressIndicator so we can gradient the stroke.
 */
@Composable
fun GradientProgressRing(
    progress: Float, // 0f..1f
    modifier: Modifier = Modifier,
    strokeWidth: androidx.compose.ui.unit.Dp = 10.dp,
    startColor: Color,
    endColor: Color,
    trackColor: Color = Color.White.copy(alpha = 0.08f)
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = SrMotion.emphasized(600),
        label = "ring_progress"
    )
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = stroke,
            size = Size(size.width - stroke.width, size.height - stroke.width),
            topLeft = androidx.compose.ui.geometry.Offset(stroke.width / 2, stroke.width / 2)
        )
        drawArc(
            brush = Brush.sweepGradient(listOf(startColor, endColor, startColor)),
            startAngle = -90f,
            sweepAngle = 360f * animatedProgress,
            useCenter = false,
            style = stroke,
            size = Size(size.width - stroke.width, size.height - stroke.width),
            topLeft = androidx.compose.ui.geometry.Offset(stroke.width / 2, stroke.width / 2)
        )
    }
}

/** Consistent section header used across dashboard/habits/stats screens. */
@Composable
fun SectionHeader(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        if (action != null && onAction != null) {
            Text(
                action,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.pointerInput(Unit) {
                    detectTapWithInteraction(MutableInteractionSource(), onAction)
                }
            )
        }
    }
}
