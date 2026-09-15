package com.srapp.blocking.ui

import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.srapp.SrApplication
import com.srapp.blocking.data.BlockType
import com.srapp.blocking.data.BlockedAppEntity
import kotlinx.coroutines.launch

private data class InstalledApp(val packageName: String, val label: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedAppsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val application = context.applicationContext as SrApplication
    val blockedApps by application.database.blockingDao().observeBlockedApps().collectAsState(initial = emptyList())
    var installedApps by remember { mutableStateOf(emptyList<InstalledApp>()) }
    var query by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        installedApps = context.packageManager.queryIntentActivities(launcherIntent, PackageManager.MATCH_ALL)
            .map { resolveInfo ->
                InstalledApp(
                    packageName = resolveInfo.activityInfo.packageName,
                    label = resolveInfo.loadLabel(context.packageManager).toString()
                )
            }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    val filteredApps = installedApps.filter { app ->
        query.isBlank() || app.label.contains(query, ignoreCase = true) || app.packageName.contains(query, ignoreCase = true)
    }
    val blockedPackages = blockedApps.map { it.packageName }.toSet()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage blocked apps") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                placeholder = { Text("Search installed apps") }
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "${blockedApps.size} apps protected. Permanent blocks are checked by the accessibility service.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    val isBlocked = app.packageName in blockedPackages
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(app.label, style = MaterialTheme.typography.bodyLarge)
                            Text(app.packageName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isBlocked,
                            onCheckedChange = { checked ->
                                scope.launch {
                                    if (checked) {
                                        application.database.blockingDao().upsertBlockedApp(
                                            BlockedAppEntity(
                                                packageName = app.packageName,
                                                appName = app.label,
                                                blockType = BlockType.PERMANENT
                                            )
                                        )
                                    } else {
                                        application.database.blockingDao().removeBlockedApp(app.packageName)
                                    }
                                    application.refreshBlocklistCache()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
