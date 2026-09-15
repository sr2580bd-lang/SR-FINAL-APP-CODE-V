package com.srapp.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.srapp.core.ui.components.PressScale
import com.srapp.core.ui.theme.DangerRed
import com.srapp.core.ui.theme.SignalAmber
import com.srapp.core.ui.theme.SuccessGreen

enum class Strictness(val label: String, val description: String) {
    STANDARD("Standard", "Can override with effort — math, wait time"),
    HARDCORE("Hardcore", "1000-word uninstall + partner approval"),
    NUCLEAR("Nuclear", "Partner must approve everything")
}

@Composable
fun SettingsScreen(onManageBlockedApps: () -> Unit = {}) {
    var strictness by remember { mutableStateOf(Strictness.STANDARD) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text("Profile & Settings", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(20.dp))

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
        SettingsRow(icon = Icons.Filled.PeopleAlt, title = "Accountability Partners", subtitle = "Coming in a later phase")
        SettingsRow(icon = Icons.Filled.NotificationsNone, title = "Notifications", subtitle = "Daily reminders on")
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
                    .background(accent, androidx.compose.foundation.shape.CircleShape)
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
        }
    }
}
