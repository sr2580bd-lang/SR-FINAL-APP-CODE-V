package com.srapp

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.srapp.core.ui.theme.SrAppTheme
import com.srapp.data.LocalRepository
import com.srapp.navigation.SrNavRoot
import com.srapp.onboarding.OnboardingFlow

private enum class AppStage { ONBOARDING, PERMISSION_GATE, MAIN }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SrAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
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

    val stage = when {
        !onboarded -> AppStage.ONBOARDING
        !accessibilityEnabled -> AppStage.PERMISSION_GATE
        else -> AppStage.MAIN
    }

    AnimatedContent(
        targetState = stage,
        label = "app_stage",
        transitionSpec = { fadeIn() togetherWith fadeOut() }
    ) { current ->
        when (current) {
            AppStage.ONBOARDING -> OnboardingFlow(onComplete = { scope.launch { repository.completeOnboarding() } })
            AppStage.PERMISSION_GATE -> AccessibilityPermissionGate(
                onOpenSettings = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                onRecheck = { accessibilityEnabled = isAccessibilityServiceEnabled(context) }
            )
            AppStage.MAIN -> SrNavRoot(repository)
        }
    }
}

@Composable
private fun AccessibilityPermissionGate(onOpenSettings: () -> Unit, onRecheck: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("One permission to enable", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Text(
            "SR App needs Accessibility access to detect and block apps. " +
                "Tap below, find \"SR App Blocker\", and turn it on.",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onOpenSettings) { Text("Open Accessibility Settings") }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onRecheck) { Text("I've enabled it — check again") }
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
