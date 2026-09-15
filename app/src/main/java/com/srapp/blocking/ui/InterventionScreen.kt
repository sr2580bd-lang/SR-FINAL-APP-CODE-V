package com.srapp.blocking.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srapp.core.ui.theme.DangerRed
import com.srapp.core.ui.theme.GradientDangerEnd
import com.srapp.core.ui.theme.GradientDangerStart
import com.srapp.core.ui.theme.SrTheme
import kotlinx.coroutines.flow.StateFlow

@Composable
fun InterventionScreen(
    state: StateFlow<InterventionState>,
    onAnswerReflection: (String) -> Unit,
    onMathAnswer: (Int) -> Unit,
    onFinished: () -> Unit
) {
    val current by state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(GradientDangerEnd, Color(0xFF060608))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            InterventionHeader()

            Spacer(Modifier.height(24.dp))

            AnimatedContent(
                targetState = current.step,
                label = "intervention_step",
                transitionSpec = { fadeThroughTransition() }
            ) { step ->
                when (step) {
                    InterventionStep.WAIT -> WaitStep(current.secondsRemaining)
                    InterventionStep.REFLECTION -> ReflectionStep(onAnswerReflection)
                    InterventionStep.MATH -> MathStep(current, onMathAnswer)
                    InterventionStep.DONE -> DoneStep(onFinished)
                }
            }
        }
    }
}

private fun fadeThroughTransition() =
    (androidx.compose.animation.fadeIn(tween(300)))
        .togetherWith(androidx.compose.animation.fadeOut(tween(150)))

@Composable
private fun InterventionHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(DangerRed.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Block,
                contentDescription = null,
                tint = DangerRed,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "HOLD ON.",
            style = MaterialTheme.typography.displayMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Text(
            text = "This is the moment your future self needs you to pause.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFFCBCBD6),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp)
        )
    }
}

@Composable
private fun WaitStep(secondsRemaining: Int) {
    val progress by animateFloatAsState(
        targetValue = 1f - (secondsRemaining / 60f),
        animationSpec = tween(900), label = "wait_progress"
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(160.dp),
                strokeWidth = 8.dp,
                color = DangerRed,
                trackColor = Color.White.copy(alpha = 0.08f)
            )
            Text(
                text = "$secondsRemaining",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "Just breathe. The urge always passes — usually inside a minute.",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFFCBCBD6),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ReflectionStep(onAnswerReflection: (String) -> Unit) {
    var q1 by remember { mutableStateOf("") }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "What should you be doing instead, right now?",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = q1,
            onValueChange = { q1 = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Type an honest answer…") },
            minLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { if (q1.isNotBlank()) onAnswerReflection(q1) },
            enabled = q1.trim().length >= 5,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Continue", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MathStep(state: InterventionState, onMathAnswer: (Int) -> Unit) {
    var input by remember(state.mathProblem) { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Problem ${state.mathAttemptIndex + 1} of ${state.mathTotal}",
            style = MaterialTheme.typography.labelLarge,
            color = SrTheme.extended.xpGold
        )
        Spacer(Modifier.height(12.dp))
        Text(
            state.mathProblem.prompt,
            style = MaterialTheme.typography.displayMedium,
            color = Color.White
        )
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = input,
            onValueChange = { input = it.filter { c -> c.isDigit() || c == '-' } },
            modifier = Modifier.width(160.dp),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 24.sp, textAlign = TextAlign.Center, color = Color.White
            ),
            isError = state.mathWrongLastTry,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White
            )
        )
        if (state.mathWrongLastTry) {
            Text(
                "Not quite — try again.",
                color = DangerRed,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { input.toIntOrNull()?.let(onMathAnswer) },
            enabled = input.isNotBlank(),
            modifier = Modifier.width(200.dp).height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Submit", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DoneStep(onFinished: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "You made it through.",
            style = MaterialTheme.typography.headlineLarge,
            color = SrTheme.extended.success,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "That's a real win. Taking you back home now.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFFCBCBD6),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onFinished,
            modifier = Modifier.width(220.dp).height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Back to Home", fontWeight = FontWeight.Bold)
        }
    }
}
