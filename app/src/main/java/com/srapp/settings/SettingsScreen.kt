package com.srapp.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.srapp.core.ui.components.GlassChip
import com.srapp.core.ui.components.PressScale
import com.srapp.core.ui.theme.*
import com.srapp.data.LocalRepository
import com.srapp.firebase.DEFAULT_FIREBASE_UID

enum class Strictness(val label: String, val description: String) {
    STANDARD("Standard", "Can override with effort — math, wait time"),
    HARDCORE("Hardcore", "1000-word uninstall + partner approval"),
    NUCLEAR("Nuclear", "Partner must approve everything")
}

@Composable
fun SettingsScreen(
    repository: LocalRepository? = null,
    onManageBlockedApps: () -> Unit = {},
    onOpenAuthSync: () -> Unit = {}
) {
    var strictness by remember { mutableStateOf(Strictness.STANDARD) }
    val userProfile by repository?.userProfile?.collectAsState() ?: remember { mutableStateOf(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            "Profile & Settings",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(20.dp))

        // Account & Firebase Cloud Sync Card
        Text("Account & Serverless Sync", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(10.dp))
        PressScale(onClick = onOpenAuthSync, modifier = Modifier.fillMaxWidth()) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("account_sync_settings_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(GradientVioletStart, NeonCyan))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.CloudSync, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                userProfile?.displayName ?: "SR Member",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.width(8.dp))
                            GlassChip("FIREBASE")
                        }
                        Text(
                            "UID: ${(userProfile?.uid ?: DEFAULT_FIREBASE_UID).take(12)}...",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = NeonCyan
                        )
                    }
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("Strictness Level", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(10.dp))
        Strictness.entries.forEach { level ->
            StrictnessRow(level, selected = strictness == level, onSelect = { strictness = level })
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(24.dp))
        Text("General", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(10.dp))
        SettingsRow(icon = Icons.Filled.Shield, title = "Manage Blocked Apps", subtitle = "Configure permanent protection", onClick = onManageBlockedApps)
        SettingsRow(icon = Icons.Filled.CloudDone, title = "Firestore Cloud Backup", subtitle = "Synchronize across devices", onClick = onOpenAuthSync)
        SettingsRow(icon = Icons.Filled.PeopleAlt, title = "Accountability Partners", subtitle = "Link partner device via Firebase UID")
        SettingsRow(icon = Icons.Filled.NotificationsNone, title = "Notifications", subtitle = "Daily morning & night check-in reminders")
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun StrictnessRow(level: Strictness, selected: Boolean, onSelect: () -> Unit) {
    val accent = when (level) {
        Strictness.STANDARD -> SuccessGreen
        Strictness.HARDCORE -> SignalAmber
        Strictness.NUCLEAR -> DangerRed
    }
    PressScale(onClick = onSelect, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (selected) accent.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(accent, CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(level.label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                Text(level.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (selected) {
                Text("ACTIVE", style = MaterialTheme.typography.labelSmall, color = accent, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit = {}) {
    PressScale(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
    }
}
