# SR App: Comprehensive Architecture & Development Roadmap

**Senior Android Developer & UI/UX Design System Specification**  
*Platform: Android Native (Kotlin, Jetpack Compose, Room, Kotlin Coroutines & Flow)*  
*Serverless Backend: Firebase Authentication (Google & Email/Password) + Cloud Firestore Offline-First Sync*  
*Configured Primary Firebase UID: `MReyTtl9ewUfjVHUZHl7Y8x2D5S2`*

---

## 1. Executive Summary & Vision

SR App is an uncompromising, modern, privacy-first addiction recovery and self-mastery platform designed exclusively for Android. It combines an unyielding native blocking engine (`AccessibilityService` + Device Administrator) with an alluring, dark-luxury aesthetic inspired by high-end cyberpunk & minimalist wellness interfaces (AMOLED `#060608`, Neon Cyan, Signal Amber, and Velvet Violet).

### Core Pillars
1. **Zero-Lapse Enforcement (Native Android Core)**:
   - High-performance in-memory cache synchronized with Room database.
   - Zero I/O on window-change hot paths to guarantee 60fps OS fluidity.
   - Multi-tiered strictness: Standard, Hardcore, and Nuclear accountability.
2. **Serverless Cloud Synchronization (Firebase)**:
   - Auth with Google Sign-In and Email/Password with real-time auth state listening.
   - Primary UID mapping (`MReyTtl9ewUfjVHUZHl7Y8x2D5S2`) for immediate identity continuity.
   - Cloud Firestore bidirectional sync: Streaks, Habits, Focus Sessions, and Blocked Apps.
   - Offline-first durability with automatic background sync when network is restored.
3. **Tactile, Gesture-Driven Modern UI/UX**:
   - Slide-to-Activate SOS Emergency Panic slider (prevents panic misclicks, tactical resistance).
   - In-app 4-7-8 Breathing circle with animated pulse, breath guidance, and rhythm countdowns.
   - Gesture-based habit toggles and draggable/stepper focus timer dials.
   - Fluid AnimatedContent and spring physics transitions.

---

## 2. Serverless Firebase Backend Architecture

### 2.1 Authentication & User ID Management
- **Primary Configured UID**: `MReyTtl9ewUfjVHUZHl7Y8x2D5S2`
- **Supported Auth Providers**:
  1. **Google Sign-In**: Integrated using Google Identity & `GoogleAuthProvider` credential tokens.
  2. **Email & Password**: Direct `signInWithEmailAndPassword` and `createUserWithEmailAndPassword`.
  3. **Anonymous / Local-to-Cloud Linking**: Unauthenticated local sessions seamlessly associate with the primary UID, allowing instant offline usage without blocking onboarding.
- **Account & Cloud Sync UI (`AuthSyncScreen`)**:
  - Live identity card with single-tap clipboard copy for the active Firebase UID.
  - Real-time sync trigger with status chips (`LIVE SYNCED`, `SYNCING...`, `OFFLINE`).
  - Fast-switching between primary UID and custom authenticated accounts.

### 2.2 Cloud Firestore Data Schema
All user data is partitioned under `users/{uid}`:

```
users/
  └── {userId}/   (e.g., MReyTtl9ewUfjVHUZHl7Y8x2D5S2)
        ├── uid: string
        ├── email: string
        ├── currentStreak: number
        ├── longestStreak: number
        ├── habitsDoneToday: number
        ├── totalBlockedApps: number
        ├── lastSyncTimestamp: timestamp
        ├── platform: "Android Native"
        │
        ├── habits/
        │     └── {date}_{habitType}/
        │           ├── habitType: string
        │           ├── date: "YYYY-MM-DD"
        │           ├── completed: boolean
        │           └── updatedAt: timestamp
        │
        ├── blocked_apps/
        │     └── {packageName_sanitized}/
        │           ├── packageName: string
        │           ├── appName: string
        │           └── blockType: "PERMANENT" | "SCHEDULED" | "TIME_LIMITED"
        │
        └── focus_sessions/
              └── session_{id}/
                    ├── id: number
                    ├── category: string
                    ├── startTime: timestamp
                    ├── endTime: timestamp
                    ├── durationMinutes: number
                    └── completed: boolean
```

### 2.3 Synchronization Lifecycle (`FirebaseManager.kt`)
1. **Local-to-Cloud (Upload)**:
   - On habit toggle, focus session finish, or manual "Sync Now" tap: Room DAO queries snapshot current state and upserts merged documents to Firestore via `SetOptions.merge()`.
2. **Cloud-to-Local (Download & Conflict Resolution)**:
   - Remote blocklists and habit progress are retrieved and upserted into Room with `OnConflictStrategy.REPLACE`.
3. **Offline Caching**:
   - Firestore's local cache ensures writes are persisted locally on-device and automatically synchronizes when network connectivity returns.

---

## 3. UI/UX Motion & Design System

### 3.1 Design Tokens
- **Background**: `#060608` (AMOLED Black)
- **Surfaces**: `#111118` (Surface Dark), `#1A1A26` (Surface Variant)
- **Primary Neon Accent**: `#22D3EE` (Cyan Glow)
- **Secondary Violet**: `#8B5CF6` (Velvet Violet)
- **Warning / Relapse Urge**: `#EF4444` (Danger Red)
- **Discipline Gold / XP**: `#F59E0B` (Amber Flame)
- **Success / Clean**: `#10B981` (Emerald Green)

### 3.2 Gesture Interactions
- **Slide-to-Activate SOS Panic**:
  - Horizontal drag physics (`rememberDraggableState` + `IntOffset`).
  - Spring-release returns thumb to resting position if dragged less than 75% across.
  - Dragging > 75% triggers emergency protocol with immediate visual transformation.
- **Urge De-escalation (4-7-8 Breathing Circle)**:
  - 4s Inhale (expanding circle), 7s Hold (steady pulse), 8s Exhale (contracting circle).
  - Designed based on neurobiological parasympathetic activation to lower dopamine cravings.
- **Dynamic Focus Presets**:
  - Quick +5m and +15m increments, animated circular gradient countdown ring.

---

## 4. Engineering Bug Fixes & Improvements

1. **Firebase / Kotlin Compiler Metadata Mismatch**:
   - *Cause*: Latest Firebase Auth dependencies compiled with Kotlin 2.3 metadata while local toolchain is 2.0.21.
   - *Fix*: Configured `kotlin.compiler.skipMetadataVersionCheck=true` in `gradle.properties` and `freeCompilerArgs += listOf("-Xskip-metadata-version-check")` in `app/build.gradle.kts`.
2. **Accessibility Gate Soft-Lock**:
   - *Cause*: `AccessibilityPermissionGate` previously locked the UI entirely if accessibility was off, blocking app discovery and UI testing in standard emulator environments.
   - *Fix*: Added "Explore App (Preview Mode)" affordance, allowing immediate access to habits, focus timer, cloud sync, and dashboard while maintaining prominent security prompts.
3. **Edge-to-Edge System Bars**:
   - Implemented `enableEdgeToEdge()` in `MainActivity` with Material 3 insets.
4. **Room exportSchema warning**:
   - Set `exportSchema = false` in `SrDatabase.kt` to streamline builds.

---

## 5. Development Roadmap (Next Milestones)

- [x] **Milestone 1: Build & Infrastructure Stabilization** (Gradle 8.10.2, Kotlin 2.0.21, KSP).
- [x] **Milestone 2: Serverless Firebase Integration** (Auth, Firestore bidirectional sync, UID: `MReyTtl9ewUfjVHUZHl7Y8x2D5S2`).
- [x] **Milestone 3: Modern Gesture UI & Panic De-escalation** (Slide-to-panic, 4-7-8 breathing circle, sync badges).
- [ ] **Milestone 4: Cloud Firestore Accountability Partner Linking** (Pairing two devices via QR/UID for relapse alerts).
- [ ] **Milestone 5: Device Admin & Anti-Tamper Enforcement** (Preventing app uninstallation during active strictness).
- [ ] **Milestone 6: WorkManager Midnight Streak Evaluation** (Automatic daily streak incrementation and habit reset).
