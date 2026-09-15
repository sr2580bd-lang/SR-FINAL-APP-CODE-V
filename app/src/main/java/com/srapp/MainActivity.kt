package com.srapp

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.srapp.core.ui.theme.GradientVioletStart
import com.srapp.core.ui.theme.NeonCyan
import com.srapp.core.ui.theme.SignalAmber
import com.srapp.core.ui.theme.SrAppTheme
import com.srapp.data.LocalRepository
import com.srapp.navigation.SrNavRoot
import com.srapp.onboarding.OnboardingFlow

private enum class AppStage { ONBOARDING, PERMISSION_GATE, MAIN }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SrAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SrRoot()
                }
            }
        }
    }
}

@Composable
private fun SrRoot() {
    val context = LocalContext.current
    val application = context.applicationContext as SrApplication
    val repository = remember { LocalRepository(application) }
    val onboarded by repository.onboardingCompleted.collectAsState(initial = false)
    val scope = rememberCoroutineScope()
    var accessibilityEnabled by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }
    var skipGate by remember { mutableStateOf(false) }

    val stage = when {
        !onboarded -> AppStage.ONBOARDING
        !accessibilityEnabled && !skipGate -> AppStage.PERMISSION_GATE
        else -> AppStage.MAIN
    }

    AnimatedContent(
        targetState = stage,
        label = "app_stage",
        transitionSpec = { fadeIn() togetherWith fadeOut() }
    ) { current ->
        when (current) {
            AppStage.ONBOARDING -> OnboardingFlow(
                onComplete = { scope.launch { repository.completeOnboarding() } }
            )
            AppStage.PERMISSION_GATE -> AccessibilityPermissionGate(
                onOpenSettings = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                onRecheck = { accessibilityEnabled = isAccessibilityServiceEnabled(context) },
                onSkip = { skipGate = true }
            )
            AppStage.MAIN -> SrNavRoot(repository)
        }
    }
}

@Composable
private fun AccessibilityPermissionGate(
    onOpenSettings: () -> Unit,
    onRecheck: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(GradientVioletStart, NeonCyan)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Shield,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "System Protection Gate",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "SR App utilizes native Accessibility enforcement to shield you from triggering applications and maintain your streak. Turn on \"SR App Blocker\" in Accessibility settings.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onOpenSettings,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("enable_accessibility_button"),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Open Accessibility Settings", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onRecheck,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("I've Enabled It — Verify Now")
        }

        Spacer(Modifier.height(12.dp))

        TextButton(
            onClick = onSkip,
            modifier = Modifier.testTag("explore_app_button")
        ) {
            Text(
                "Explore App (Preview & Offline Mode)",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

private fun isAccessibilityServiceEnabled(context: android.content.Context): Boolean {
    val expectedComponent = "${context.packageName}/com.srapp.blocking.service.AppBlockerAccessibilityService"
    val enabledServices = Settings.Secure.getString(
        context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    val splitter = TextUtils.SimpleStringSplitter(':')
    splitter.setString(enabledServices)
    while (splitter.hasNext()) {
        if (splitter.next().equals(expectedComponent, ignoreCase = true)) return true
    }
    return false
}
