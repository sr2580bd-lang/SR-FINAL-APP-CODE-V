# SR App Implementation Plan

This plan translates the master product spec into a buildable, dependency-ordered roadmap for the Android app.

## 1. Current verified baseline

- Android project path: [srapp-full-ui](.)
- Apk generated successfully in debug mode:
  - [srapp-full-ui/app/build/outputs/apk/debug/app-debug.apk](app/build/outputs/apk/debug/app-debug.apk)
- Current app package is `com.srapp`.
- The app was build-blocked by several real issues and they were fixed during verification:
  - missing Java/SDK path setup
  - missing wrapper jar issue bypassed by using cached Gradle 8.10.2
  - missing launcher icon resource
  - Build Tools mismatch / compileSdk mismatch
  - Kotlin syntax error in SettingsScreen
  - missing Compose imports in shared components and stats screen

This means the project is currently buildable as a debug APK, but the full product spec still needs staged implementation.

## 2. Product direction to implement

Goal: Ship the app as a premium, offline-first blockers + recovery companion with privacy-first local enforcement and a single-source-of-truth blocking engine.

## 3. Delivery roadmap

### Phase 1 — App foundation and quality gates
1. Standardize package identity and Android config
   - Keep one package namespace across manifest, Gradle, and code.
   - Use `com.simanto.sr_app` only if the product requirement is formalized; otherwise continue `com.srapp` until a deliberate migration decision is made.
2. Stabilize build environment
   - Android Studio JDK = Embedded JDK / Java 21
   - Gradle 8.10.2
   - Android SDK with platform 35 and Build Tools 36.0.0
3. Create build verification workflow
   - `assembleDebug` must be green before feature work continues.
   - `assembleRelease` later after signing config is ready.
4. Security/UX baseline
   - Accessibility target compliance
   - 48dp touch targets
   - proper content descriptions
   - high-contrast colors

### Phase 2 — Data layer and single source of truth
1. Room schema for:
   - `blocked_apps`
   - `usage_today`
   - `streaks`
   - `habits`
   - `focus_sessions`
   - `character_stats`
   - `achievements`
   - `block_attempts`
   - `sos_events`
   - `daily_rituals`
   - `voice_notes`
2. Implement repository layer and local DAO queries.
3. Implement `isAppBlockedNow()` as the central enforcement function.
4. Add DataStore flags for onboarding/tooltips and permanent settings.
5. Add WorkManager reset job for daily counters.

### Phase 3 — Design system and navigation
1. Build the core token system:
   - colors
   - typography
   - spacing
   - shapes
   - haptics
2. Create consistent component library:
   - PressScale
   - GradientCard
   - GlassChip
   - CountUpText
   - GradientProgressRing
   - SectionHeader
3. Add bottom navigation shell and predictive back behaviors.
4. Maintain single theme instead of multiple theme files.

### Phase 4 — Onboarding and permission flow
1. Implement onboarding screens in correct order.
2. Capture user goal text and store it for reuse in SOS and intervention flows.
3. Request permissions in the required order.
4. Write tooltip and onboarding flags to DataStore.
5. Avoid duplicate prompt flows and repeated onboarding resets.

### Phase 5 — Enforcement, blocking, and friction systems
1. Add global + scheduled + session block logic.
2. Add time-limit enforcement and daily counter reset.
3. Implement strictness tiers.
4. Build the challenge screen grid and anti-uninstall flow.
5. Build Device Admin / accessibility flows behind the app UX.

### Phase 6 — Core screens
1. Home
   - streak ring
   - danger-window banner
   - daily ritual prompts
2. Blocking screen
   - grouped rows
   - long-press explanation
   - deactivation challenge entry
3. Habits
   - swipe-to-complete rows
   - heatmap
   - streak freeze logic
4. Focus screen
   - radial dial
   - timer ring
   - ambient sound selection
5. Insights screen
   - pattern detection
   - auto-tighten toggle
   - weekly recap card
6. Profile/settings screen
   - account state
   - AMOLED mode
   - partner management
   - voice notes

### Phase 7 — God-tier features
1. SOS panic flow
2. daily intention + reflection ritual
3. voice-note milestone playback
4. widget implementation
5. weekly chapter recap
6. companion avatar stage progression

### Phase 8 — Polish and release
1. Accessibility pass against the spec baseline
2. Empty/loading/error patterns on all screens
3. Device real-world validation
4. release signing
5. final QA on physical Android device

## 4. Implementation rule for this project

Work in one checklist item at a time. Do not mix unrelated features into one change. After each unit of work:

- build
- test the changed behavior
- fix bugs immediately
- keep only the relevant patch
- move to the next item

## 5. High-priority bug watchlist for this app

The following are the most likely issues to hit during implementation:

1. Gradle/JDK mismatch
   - Use Java 21 and Gradle 8.10.2.
2. SDK path mismatch
   - set `ANDROID_HOME` / `ANDROID_SDK_ROOT` in terminal or Android Studio settings.
3. android-37 platform not installed
   - use supported installed platform, or lower `compileSdk` to a local SDK version.
4. resource linking errors
   - check icon resource references and always provide a valid drawable/mipmap.
5. Compose import errors
   - ensure imports match Compose BOM versions.
6. Room schema generation warnings
   - not fatal, but eventually resolve by setting export schema policy deliberately.
7. permission security gaps
   - do not add broad or unnecessary permissions without matching UX and policy logic.
8. blocking logic bugs
   - centralize decision logic; never duplicate blocking checks across screens/services.

## 6. Recommended next immediate tasks

1. Confirm final package identity: `com.srapp` vs `com.simanto.sr_app`.
2. Finalize the Single Source of Truth blocking function and data model.
3. Implement Room schema and repository layer.
4. Build the Home and Blocking screen foundations.
5. Then add SOS + daily ritual and insights.

This roadmap keeps the app moving without mixing architecture and UI work in one large uncontrolled patch.
