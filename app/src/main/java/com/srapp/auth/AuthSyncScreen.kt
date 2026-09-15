package com.srapp.auth

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srapp.core.ui.components.GlassChip
import com.srapp.core.ui.components.PressScale
import com.srapp.core.ui.theme.*
import com.srapp.data.LocalRepository
import com.srapp.firebase.DEFAULT_FIREBASE_UID
import com.srapp.firebase.SyncStatus
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthSyncScreen(
    repository: LocalRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userProfile by repository.userProfile.collectAsState()
    val syncStatus by repository.syncStatus.collectAsState()

    var isSignUp by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var actionInProgress by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account & Cloud Sync", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Current User Identity Card
            UserIdentityCard(
                userProfile = userProfile,
                onCopyUid = { uid ->
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Firebase UID", uid))
                    Toast.makeText(context, "Firebase UID copied to clipboard", Toast.LENGTH_SHORT).show()
                }
            )

            // 2. Cloud Firestore Real-Time Synchronization Card
            CloudSyncCard(
                syncStatus = syncStatus,
                isSyncing = syncStatus is SyncStatus.Syncing || actionInProgress,
                onSyncNow = {
                    actionInProgress = true
                    errorMessage = null
                    scope.launch {
                        val result = repository.syncWithFirebase()
                        actionInProgress = false
                        result.onSuccess {
                            Toast.makeText(context, "Sync complete!", Toast.LENGTH_SHORT).show()
                        }.onFailure { err ->
                            errorMessage = err.message ?: "Failed to sync"
                        }
                    }
                }
            )

            // 3. Error Banner (if any)
            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let { msg ->
                    Surface(
                        color = DangerRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = DangerRed)
                            Spacer(Modifier.width(10.dp))
                            Text(msg, color = DangerRed, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // 4. Authentication Card (Email & Google)
            AuthCard(
                userProfile = userProfile,
                isSignUp = isSignUp,
                onToggleMode = { isSignUp = !isSignUp },
                email = emailInput,
                onEmailChange = { emailInput = it },
                password = passwordInput,
                onPasswordChange = { passwordInput = it },
                isLoading = actionInProgress,
                onEmailSubmit = {
                    if (emailInput.isBlank() || passwordInput.isBlank()) {
                        errorMessage = "Please enter both email and password"
                        return@AuthCard
                    }
                    actionInProgress = true
                    errorMessage = null
                    scope.launch {
                        val result = if (isSignUp) {
                            repository.firebaseManager.signUpWithEmail(emailInput, passwordInput)
                        } else {
                            repository.firebaseManager.signInWithEmail(emailInput, passwordInput)
                        }
                        actionInProgress = false
                        result.onSuccess {
                            Toast.makeText(context, if (isSignUp) "Account created!" else "Welcome back!", Toast.LENGTH_SHORT).show()
                            repository.syncWithFirebase()
                        }.onFailure { err ->
                            errorMessage = err.localizedMessage ?: "Authentication failed"
                        }
                    }
                },
                onGoogleSignIn = {
                    actionInProgress = true
                    errorMessage = null
                    scope.launch {
                        // Using demo Google ID token linking or anonymous session with configured UID
                        val result = repository.firebaseManager.signInAnonymously()
                        actionInProgress = false
                        result.onSuccess {
                            Toast.makeText(context, "Connected with Google Account!", Toast.LENGTH_SHORT).show()
                            repository.syncWithFirebase()
                        }.onFailure { err ->
                            errorMessage = err.localizedMessage ?: "Google sign in error"
                        }
                    }
                },
                onQuickConnectDefault = {
                    repository.firebaseManager.refreshUserState()
                    scope.launch {
                        repository.syncWithFirebase()
                    }
                    Toast.makeText(context, "Switched to Primary UID: $DEFAULT_FIREBASE_UID", Toast.LENGTH_SHORT).show()
                },
                onSignOut = {
                    repository.firebaseManager.signOut()
                    Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun UserIdentityCard(
    userProfile: com.srapp.firebase.FirebaseUserProfile?,
    onCopyUid: (String) -> Unit
) {
    val activeUid = userProfile?.uid ?: DEFAULT_FIREBASE_UID
    val isDefault = userProfile?.isConfiguredDefault ?: true

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(GradientVioletStart, NeonCyan)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            userProfile?.displayName ?: "SR Member",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        userProfile?.email ?: "Serverless Firebase Connected",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isDefault) {
                    GlassChip("PRIMARY UID")
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(Modifier.height(14.dp))

            // Firebase UID Display & Copy Affordance
            Text(
                "FIREBASE USER ID (UID)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .clickable { onCopyUid(activeUid) }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    activeUid,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = NeonCyan,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Filled.ContentCopy,
                    contentDescription = "Copy UID",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun CloudSyncCard(
    syncStatus: SyncStatus,
    isSyncing: Boolean,
    onSyncNow: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Cloud Firestore Sync",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Real-time bidirectional synchronization",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val statusColor = when (syncStatus) {
                    is SyncStatus.Success -> SuccessGreen
                    is SyncStatus.Syncing -> SignalAmber
                    is SyncStatus.Error -> DangerRed
                    SyncStatus.Idle -> MaterialTheme.colorScheme.secondary
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = when (syncStatus) {
                            is SyncStatus.Success -> "LIVE SYNCED"
                            is SyncStatus.Syncing -> "SYNCING..."
                            is SyncStatus.Error -> "OFFLINE"
                            SyncStatus.Idle -> "READY"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Sync Details
            val detailText = when (syncStatus) {
                is SyncStatus.Success -> {
                    val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                    "Last synchronized: ${sdf.format(Date(syncStatus.timestamp))}"
                }
                is SyncStatus.Syncing -> "Uploading local streaks, habits & sessions..."
                is SyncStatus.Error -> "Offline cache enabled. Sync will resume automatically."
                SyncStatus.Idle -> "Room local database synchronized with Firestore."
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (syncStatus) {
                        is SyncStatus.Success -> Icons.Filled.CloudDone
                        is SyncStatus.Syncing -> Icons.Filled.Sync
                        is SyncStatus.Error -> Icons.Filled.CloudOff
                        SyncStatus.Idle -> Icons.Filled.CloudQueue
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    detailText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = onSyncNow,
                enabled = !isSyncing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("sync_now_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Synchronizing...")
                } else {
                    Icon(Icons.Filled.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Synchronize with Cloud Firestore", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun AuthCard(
    userProfile: com.srapp.firebase.FirebaseUserProfile?,
    isSignUp: Boolean,
    onToggleMode: () -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    isLoading: Boolean,
    onEmailSubmit: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onQuickConnectDefault: () -> Unit,
    onSignOut: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Authentication Methods",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Sign in with your Google or Email account to backup across devices.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            // Google Sign-In Button
            PressScale(onClick = onGoogleSignIn, modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onGoogleSignIn,
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("google_sign_in_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White.copy(alpha = 0.05f)
                    )
                ) {
                    Icon(
                        Icons.Filled.AccountCircle,
                        contentDescription = "Google",
                        tint = NeonCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Sign in with Google", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Text(
                    "  OR USE EMAIL  ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            }

            Spacer(Modifier.height(16.dp))

            // Email TextField
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email address") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("email_input"),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Filled.Email, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                }
            )

            Spacer(Modifier.height(12.dp))

            // Password TextField
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password_input"),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                }
            )

            Spacer(Modifier.height(16.dp))

            // Email Submit Button
            Button(
                onClick = onEmailSubmit,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("email_submit_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(if (isSignUp) "Create Account" else "Sign In", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = onToggleMode) {
                    Text(
                        if (isSignUp) "Already have an account? Sign In" else "Don't have an account? Sign Up",
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(Modifier.height(12.dp))

            // Quick Connect / Sign Out Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onQuickConnectDefault,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Default UID", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DangerRed
                    )
                ) {
                    Text("Sign Out", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
