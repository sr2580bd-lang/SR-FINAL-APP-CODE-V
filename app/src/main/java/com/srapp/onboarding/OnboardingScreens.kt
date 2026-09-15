package com.srapp.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.srapp.core.ui.components.PressScale
import com.srapp.core.ui.motion.SrMotion
import com.srapp.core.ui.theme.ElectricViolet
import com.srapp.core.ui.theme.GradientVioletEnd
import com.srapp.core.ui.theme.GradientVioletStart
import kotlinx.coroutines.launch

data class OnboardingChoice(val id: String, val label: String)

private val whyOptions = listOf(
    OnboardingChoice("porn", "Porn addiction"),
    OnboardingChoice("gaming", "Gaming addiction"),
    OnboardingChoice("social", "Social media addiction"),
    OnboardingChoice("discipline", "I need discipline"),
    OnboardingChoice("habits", "Build better habits")
)

@Composable
fun OnboardingFlow(onComplete: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 5 })
    val scope = rememberCoroutineScope()
    var selectedWhy by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0E0E14), Color(0xFF060608)))
            )
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> WelcomePage()
                1 -> WhyPage(selected = selectedWhy, onToggle = { id ->
                    selectedWhy = if (id in selectedWhy) selectedWhy - id else selectedWhy + id
                })
                2 -> GoalsPage()
                3 -> StrictnessPreviewPage()
                4 -> PermissionsPage()
            }
        }

        OnboardingFooter(
            pagerState = pagerState,
            pageCount = 5,
            onNext = {
                scope.launch {
                    if (pagerState.currentPage == 4) onComplete()
                    else pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            }
        )
    }
}

@Composable
private fun OnboardingFooter(
    pagerState: androidx.compose.foundation.pager.PagerState,
    pageCount: Int,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        ) {
            repeat(pageCount) { index ->
                val active = index == pagerState.currentPage
                val width by animateDpAsState(targetValue = if (active) 24.dp else 8.dp, label = "dot_width")
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(8.dp)
                        .width(width)
                        .background(
                            if (active) ElectricViolet else Color.White.copy(alpha = 0.2f),
                            RoundedCornerShape(50)
                        )
                )
            }
        }
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(
                if (pagerState.currentPage == pageCount - 1) "Start My Journey" else "Continue",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun WelcomePage() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(Brush.linearGradient(listOf(GradientVioletStart, GradientVioletEnd)), CircleShape)
        )
        Spacer(Modifier.height(28.dp))
        Text(
            "SR APP",
            style = MaterialTheme.typography.displayMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "This will change your life. Not because it's magic —\nbecause it makes discipline the path of least resistance.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFFCBCBD6),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun WhyPage(selected: Set<String>, onToggle: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(28.dp)) {
        Spacer(Modifier.height(24.dp))
        Text("Why are you here?", style = MaterialTheme.typography.headlineLarge, color = Color.White)
        Text(
            "Select everything that applies — be honest, no one sees this.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFFCBCBD6),
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(Modifier.height(24.dp))
        whyOptions.forEach { option ->
            val isSelected = option.id in selected
            PressScale(onClick = { onToggle(option.id) }, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isSelected) ElectricViolet.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(18.dp)
                ) {
                    Text(
                        option.label,
                        color = if (isSelected) Color.White else Color(0xFFCBCBD6),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalsPage() {
    var career by remember { mutableStateOf("") }
    var health by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(28.dp)) {
        Spacer(Modifier.height(24.dp))
        Text("What do you want to achieve?", style = MaterialTheme.typography.headlineLarge, color = Color.White)
        Spacer(Modifier.height(20.dp))
        OnboardingTextField(value = career, onValueChange = { career = it }, label = "Career / study goal")
        Spacer(Modifier.height(14.dp))
        OnboardingTextField(value = health, onValueChange = { health = it }, label = "Health / personal goal")
    }
}

@Composable
private fun OnboardingTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedTextColor = Color.White,
            focusedTextColor = Color.White,
            unfocusedLabelColor = Color(0xFFCBCBD6),
            unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
            focusedBorderColor = ElectricViolet
        )
    )
}

@Composable
private fun StrictnessPreviewPage() {
    Column(modifier = Modifier.fillMaxSize().padding(28.dp)) {
        Spacer(Modifier.height(24.dp))
        Text("How strict should we be?", style = MaterialTheme.typography.headlineLarge, color = Color.White)
        Text(
            "You can change this anytime in Settings.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFCBCBD6),
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
        )
        listOf(
            "Standard" to "Can override with effort",
            "Hardcore" to "Partner approval required",
            "Nuclear" to "Nearly impossible to bypass"
        ).forEach { (title, desc) ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(desc, color = Color(0xFFCBCBD6), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun PermissionsPage() {
    Column(modifier = Modifier.fillMaxSize().padding(28.dp)) {
        Spacer(Modifier.height(24.dp))
        Text("A few permissions", style = MaterialTheme.typography.headlineLarge, color = Color.White)
        Text(
            "Each one exists for a specific blocking feature — nothing is collected or sent anywhere.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFCBCBD6),
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
        )
        listOf(
            "Accessibility" to "Detects when a blocked app opens",
            "Usage Access" to "Powers your focus-time stats",
            "Notifications" to "Session reminders & streak alerts"
        ).forEach { (title, desc) ->
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
                Box(modifier = Modifier.size(8.dp).background(ElectricViolet, CircleShape).padding(top = 6.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(desc, color = Color(0xFFCBCBD6), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
