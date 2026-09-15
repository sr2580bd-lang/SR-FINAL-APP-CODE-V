package com.srapp.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.srapp.audio.SoundscapeEngine
import com.srapp.auth.AuthSyncScreen
import com.srapp.blocking.ui.BlockedAppsScreen
import com.srapp.core.ui.theme.*
import com.srapp.dashboard.DashboardScreen
import com.srapp.data.LocalRepository
import com.srapp.focus.FocusScreen
import com.srapp.gemini.ui.GeminiChatScreen
import com.srapp.habits.HabitsScreen
import com.srapp.music.ui.SoundscapeStudioScreen
import com.srapp.settings.SettingsScreen
import com.srapp.stats.StatsScreen
import kotlinx.coroutines.launch

sealed class SrDestination(val route: String, val label: String, val filled: ImageVector, val outlined: ImageVector) {
    data object Home : SrDestination("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    data object Fortress : SrDestination("fortress", "Fortress", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)
    data object Soundscape : SrDestination("soundscape", "Audio", Icons.Filled.GraphicEq, Icons.Outlined.GraphicEq)
    data object Focus : SrDestination("focus", "Focus", Icons.Filled.Timer, Icons.Outlined.Timer)
    data object Habits : SrDestination("habits", "Habits", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle)
    data object Stats : SrDestination("stats", "Stats", Icons.Filled.BarChart, Icons.Outlined.BarChart)
    data object Profile : SrDestination("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

private val bottomDestinations = listOf(
    SrDestination.Home,
    SrDestination.Fortress,
    SrDestination.Soundscape,
    SrDestination.Focus,
    SrDestination.Habits
)

/** Root composable: bottom-nav Scaffold wiring together every top-level screen. */
@Composable
fun SrNavRoot(repository: LocalRepository) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    var dashboard by remember { mutableStateOf<com.srapp.data.DashboardData?>(null) }

    LaunchedEffect(currentBackStackEntry?.destination?.route) {
        dashboard = runCatching { repository.dashboard() }.getOrNull()
    }

    Scaffold(
        containerColor = VoidBlack,
        bottomBar = {
            val route = currentBackStackEntry?.destination?.route
            if (route != "auth_sync" && route != "blocked-apps" && route != "stats" && route != "profile") {
                SrBottomBar(navController)
            }
        }
    ) { inner ->
        NavHost(
            navController = navController,
            startDestination = SrDestination.Home.route,
            modifier = Modifier.padding(inner)
        ) {
            composable(SrDestination.Home.route) {
                DashboardScreen(
                    pornFreeStreakDays = dashboard?.pornFreeStreak ?: 0,
                    longestStreakDays = dashboard?.longestStreak ?: 0,
                    level = dashboard?.level ?: 1,
                    levelTitle = dashboard?.levelTitle ?: "Wanderer",
                    habitsCompleted = dashboard?.habitsCompleted ?: 0,
                    habitsTotal = dashboard?.habitsTotal ?: 10,
                    onManageBlockedApps = {
                        navController.navigate("blocked-apps")
                    },
                    onPanicPressed = { scope.launch { runCatching { repository.logPanic() } } },
                    onStartFocus = {
                        navController.navigate(SrDestination.Focus.route) {
                            launchSingleTop = true
                        }
                    },
                    onOpenAuthSync = {
                        navController.navigate("auth_sync")
                    },
                    onOpenFortress = {
                        navController.navigate(SrDestination.Fortress.route) {
                            launchSingleTop = true
                        }
                    },
                    onOpenSoundscape = {
                        navController.navigate(SrDestination.Soundscape.route) {
                            launchSingleTop = true
                        }
                    },
                    onOpenProfile = {
                        navController.navigate(SrDestination.Profile.route) {
                            launchSingleTop = true
                        }
                    },
                    onOpenStats = {
                        navController.navigate(SrDestination.Stats.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(SrDestination.Fortress.route) { GeminiChatScreen() }
            composable(SrDestination.Soundscape.route) { SoundscapeStudioScreen() }
            composable(SrDestination.Focus.route) { FocusScreen(repository) }
            composable(SrDestination.Habits.route) { HabitsScreen(repository) }
            composable(SrDestination.Stats.route) { StatsScreen(repository) }
            composable(SrDestination.Profile.route) {
                SettingsScreen(
                    repository = repository,
                    onManageBlockedApps = { navController.navigate("blocked-apps") },
                    onOpenAuthSync = { navController.navigate("auth_sync") }
                )
            }
            composable("blocked-apps") { BlockedAppsScreen(onBack = { navController.popBackStack() }) }
            composable("auth_sync") {
                AuthSyncScreen(
                    repository = repository,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun SrBottomBar(navController: NavHostController) {
    val context = LocalContext.current
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination

    Surface(
        color = VoidSurface.copy(alpha = 0.95f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, VoidOutline.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomDestinations.forEach { dest ->
                val selected = currentRoute?.hierarchy?.any { it.route == dest.route } == true
                SrBottomBarItem(
                    destination = dest,
                    selected = selected,
                    onClick = {
                        SoundscapeEngine.get(context).playClick()
                        navController.navigate(dest.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SrBottomBarItem(destination: SrDestination, selected: Boolean, onClick: () -> Unit) {
    val contentColor = if (selected) NeonCyanBright else TextSecondary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 2.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(30.dp)
                .background(
                    if (selected) Brush.linearGradient(
                        listOf(ElectricVioletDim, VoidSurfaceHigh)
                    ) else SolidColor(Color.Transparent),
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (selected) destination.filled else destination.outlined,
                contentDescription = destination.label,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            destination.label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 10.sp
        )
    }
}
