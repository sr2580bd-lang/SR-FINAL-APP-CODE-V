package com.srapp.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.srapp.blocking.data.BlockedAppEntity
import com.srapp.blocking.data.FocusSessionEntity
import com.srapp.blocking.data.HabitEntity
import com.srapp.blocking.data.StreakEntity
import com.srapp.blocking.data.StreakType
import com.srapp.core.data.SrDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * Primary configured Firebase UID as requested by product specification.
 */
const val DEFAULT_FIREBASE_UID = "MReyTtl9ewUfjVHUZHl7Y8x2D5S2"

data class FirebaseUserProfile(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val isAnonymous: Boolean,
    val isConfiguredDefault: Boolean = false
)

sealed class SyncStatus {
    data object Idle : SyncStatus()
    data object Syncing : SyncStatus()
    data class Success(val timestamp: Long, val details: String) : SyncStatus()
    data class Error(val error: String) : SyncStatus()
}

class FirebaseManager private constructor(private val context: Context) {

    private val tag = "FirebaseManager"
    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _currentUserProfile = MutableStateFlow<FirebaseUserProfile?>(null)
    val currentUserProfile: StateFlow<FirebaseUserProfile?> = _currentUserProfile.asStateFlow()

    init {
        initFirebase()
    }

    private fun initFirebase() {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            auth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()

            // Update user state initially
            refreshUserState()

            // Listen for auth state changes
            auth?.addAuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                if (user != null) {
                    _currentUserProfile.value = mapUser(user)
                } else {
                    // Fall back to configured default profile state
                    _currentUserProfile.value = FirebaseUserProfile(
                        uid = DEFAULT_FIREBASE_UID,
                        email = "user@sr-app.wellness",
                        displayName = "SR Champion",
                        photoUrl = null,
                        isAnonymous = true,
                        isConfiguredDefault = true
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize Firebase: ${e.message}", e)
            _currentUserProfile.value = FirebaseUserProfile(
                uid = DEFAULT_FIREBASE_UID,
                email = "offline@sr-app.local",
                displayName = "Offline Champion",
                photoUrl = null,
                isAnonymous = true,
                isConfiguredDefault = true
            )
        }
    }

    private fun mapUser(user: FirebaseUser): FirebaseUserProfile {
        return FirebaseUserProfile(
            uid = user.uid,
            email = user.email,
            displayName = user.displayName ?: if (user.isAnonymous) "Guest User" else "SR Member",
            photoUrl = user.photoUrl?.toString(),
            isAnonymous = user.isAnonymous,
            isConfiguredDefault = user.uid == DEFAULT_FIREBASE_UID
        )
    }

    fun refreshUserState() {
        val user = auth?.currentUser
        if (user != null) {
            _currentUserProfile.value = mapUser(user)
        } else {
            _currentUserProfile.value = FirebaseUserProfile(
                uid = DEFAULT_FIREBASE_UID,
                email = "user@sr-app.wellness",
                displayName = "SR Champion",
                photoUrl = null,
                isAnonymous = true,
                isConfiguredDefault = true
            )
        }
    }

    fun activeUid(): String {
        return auth?.currentUser?.uid ?: DEFAULT_FIREBASE_UID
    }

    // ----------------------------------------------------
    // Authentication Operations
    // ----------------------------------------------------

    suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseUserProfile> = withContext(Dispatchers.IO) {
        try {
            val authInstance = auth ?: return@withContext Result.failure(IllegalStateException("Firebase Auth not initialized"))
            val authResult = authInstance.signInWithEmailAndPassword(email.trim(), pass).await()
            val user = authResult.user ?: return@withContext Result.failure(IllegalStateException("No user returned"))
            val profile = mapUser(user)
            _currentUserProfile.value = profile
            Result.success(profile)
        } catch (e: Exception) {
            Log.e(tag, "Sign in failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(email: String, pass: String): Result<FirebaseUserProfile> = withContext(Dispatchers.IO) {
        try {
            val authInstance = auth ?: return@withContext Result.failure(IllegalStateException("Firebase Auth not initialized"))
            val authResult = authInstance.createUserWithEmailAndPassword(email.trim(), pass).await()
            val user = authResult.user ?: return@withContext Result.failure(IllegalStateException("No user created"))
            val profile = mapUser(user)
            _currentUserProfile.value = profile
            Result.success(profile)
        } catch (e: Exception) {
            Log.e(tag, "Sign up failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogleIdToken(idToken: String): Result<FirebaseUserProfile> = withContext(Dispatchers.IO) {
        try {
            val authInstance = auth ?: return@withContext Result.failure(IllegalStateException("Firebase Auth not initialized"))
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = authInstance.signInWithCredential(credential).await()
            val user = authResult.user ?: return@withContext Result.failure(IllegalStateException("No Google user returned"))
            val profile = mapUser(user)
            _currentUserProfile.value = profile
            Result.success(profile)
        } catch (e: Exception) {
            Log.e(tag, "Google sign in failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun signInAnonymously(): Result<FirebaseUserProfile> = withContext(Dispatchers.IO) {
        try {
            val authInstance = auth ?: return@withContext Result.failure(IllegalStateException("Firebase Auth not initialized"))
            val authResult = authInstance.signInAnonymously().await()
            val user = authResult.user ?: return@withContext Result.failure(IllegalStateException("No anonymous user returned"))
            val profile = mapUser(user)
            _currentUserProfile.value = profile
            Result.success(profile)
        } catch (e: Exception) {
            Log.e(tag, "Anonymous sign in failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        auth?.signOut()
        refreshUserState()
    }

    // ----------------------------------------------------
    // Cloud Firestore Synchronization
    // ----------------------------------------------------

    suspend fun syncAll(database: SrDatabase): Result<String> = withContext(Dispatchers.IO) {
        _syncStatus.value = SyncStatus.Syncing
        val db = firestore
        if (db == null) {
            val msg = "Firestore instance not available"
            _syncStatus.value = SyncStatus.Error(msg)
            return@withContext Result.failure(IllegalStateException(msg))
        }

        val uid = activeUid()
        val dao = database.blockingDao()

        try {
            // 1. Upload local streaks and dashboard data
            val streak = dao.getStreak(StreakType.PORN_FREE)
            val todayHabits = dao.getHabitsForDate(LocalDate.now().toString())
            val blockedApps = dao.getBlockedAppsOnce()
            val recentSessions = dao.completedFocusSessionsSince(System.currentTimeMillis() - 30L * 86_400_000L)

            val userDocRef = db.collection("users").document(uid)
            val userData: Map<String, Any> = mapOf(
                "uid" to uid,
                "email" to (_currentUserProfile.value?.email ?: ""),
                "currentStreak" to (streak?.currentStreak ?: 0),
                "longestStreak" to (streak?.longestStreak ?: 0),
                "habitsDoneToday" to todayHabits.count { it.completed },
                "totalBlockedApps" to blockedApps.size,
                "lastSyncTimestamp" to System.currentTimeMillis(),
                "platform" to "Android Native",
                "appVersion" to "0.1.0-phase1"
            )
            userDocRef.set(userData, SetOptions.merge()).await()

            // 2. Upload today's habits to subcollection
            val habitsCollection = userDocRef.collection("habits")
            for (habit in todayHabits) {
                val docId = "${habit.date}_${habit.habitType}"
                val habitData: Map<String, Any> = mapOf(
                    "habitType" to habit.habitType,
                    "date" to habit.date,
                    "completed" to habit.completed,
                    "updatedAt" to System.currentTimeMillis()
                )
                habitsCollection.document(docId).set(habitData, SetOptions.merge()).await()
            }

            // 3. Upload blocked apps
            val blockedCollection = userDocRef.collection("blocked_apps")
            for (app in blockedApps) {
                val docId = app.packageName.replace(".", "_")
                val appData: Map<String, Any> = mapOf(
                    "packageName" to app.packageName,
                    "appName" to app.appName,
                    "blockType" to app.blockType.name
                )
                blockedCollection.document(docId).set(appData, SetOptions.merge()).await()
            }

            // 4. Upload recent focus sessions
            val focusCollection = userDocRef.collection("focus_sessions")
            for (session in recentSessions) {
                val docId = "session_${session.id}"
                val sessionData: Map<String, Any> = mapOf(
                    "id" to session.id,
                    "category" to session.category,
                    "startTime" to session.startTime,
                    "endTime" to (session.endTime ?: 0L),
                    "durationMinutes" to (session.durationMinutes ?: 0),
                    "completed" to session.completed
                )
                focusCollection.document(docId).set(sessionData, SetOptions.merge()).await()
            }

            // 5. Download any remote blocked apps or settings and merge locally
            try {
                val remoteApps = blockedCollection.get().await()
                for (doc in remoteApps.documents) {
                    val pkg = doc.getString("packageName") ?: continue
                    val name = doc.getString("appName") ?: pkg
                    val typeStr = doc.getString("blockType") ?: "PERMANENT"
                    val bType = runCatching { com.srapp.blocking.data.BlockType.valueOf(typeStr) }
                        .getOrDefault(com.srapp.blocking.data.BlockType.PERMANENT)
                    dao.upsertBlockedApp(
                        BlockedAppEntity(
                            packageName = pkg,
                            appName = name,
                            blockType = bType
                        )
                    )
                }
            } catch (e: Exception) {
                Log.w(tag, "Remote app fetch warning (non-fatal): ${e.message}")
            }

            val summary = "Synced with Cloud Firestore: UID $uid"
            _syncStatus.value = SyncStatus.Success(System.currentTimeMillis(), summary)
            Result.success(summary)
        } catch (e: Exception) {
            Log.e(tag, "Firestore sync failed: ${e.message}", e)
            val errorMsg = e.message ?: "Sync error"
            _syncStatus.value = SyncStatus.Error(errorMsg)
            Result.failure(e)
        }
    }

    companion object {
        @Volatile
        private var instance: FirebaseManager? = null

        fun get(context: Context): FirebaseManager =
            instance ?: synchronized(this) {
                instance ?: FirebaseManager(context.applicationContext).also { instance = it }
            }
    }
}
