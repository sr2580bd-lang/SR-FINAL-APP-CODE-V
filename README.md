# SR App — Phase 1 + Full UI Pass

This includes Phase 1 (core blocking) **plus a complete, navigable UI layer**
across every top-level screen, sharing one design system and one motion
language throughout.

## Screens included

- **Onboarding** (`onboarding/OnboardingScreens.kt`) — 5-page swipeable flow:
  welcome, why-are-you-here (multi-select), goals, strictness preview,
  permissions explainer. Animated progress dots, page transitions.
- **Dashboard / Home** (`dashboard/DashboardScreen.kt`) — hero streak card
  with count-up animation, level chip, time-of-day-aware greeting, quick
  actions (Start Focus / Manage Apps), and the panic button.
- **Focus** (`focus/FocusScreen.kt`) — 6-category picker grid → active
  session with a gradient progress ring, live countdown, pause/end controls.
- **Habits** (`habits/HabitsScreen.kt`) — animated checklist with a
  gradient progress bar and a GitHub-style 13-week contribution heatmap
  drawn with Canvas.
- **Stats** (`stats/StatsScreen.kt`) — time-reclaimed hero card, animated
  weekly focus-hours bar chart, AI-insight cards.
- **Settings** (`settings/SettingsScreen.kt`) — strictness-level selector
  (Standard/Hardcore/Nuclear) with color-coded risk, general settings rows.
- **Intervention** (`blocking/ui/InterventionScreen.kt`) — from Phase 1,
  restyled to match.

All five main screens live under one bottom-nav shell
(`navigation/SrNavRoot.kt`) with an animated selection pill.

## Shared design system (use these everywhere — don't invent per-screen)

- **`core/ui/theme/`** — colors, type scale, shapes (unchanged from Phase 1).
- **`core/ui/motion/Motion.kt`** — one set of animation curves/springs
  (`SrMotion.emphasized()`, `.springy()`, `.snappy()`) so every screen moves
  the same way. This one file is what stops the app from feeling like a
  pile of unrelated screens.
- **`core/ui/components/SrComponents.kt`** — reusable primitives:
  - `PressScale` — wrap any tappable card/tile in this for a tactile
    press-down effect instead of a flat Material ripple.
  - `GradientCard` / `GlassChip` — the hero-card and overlay-badge look
    used on Dashboard, Stats, and (later) boss-battle cards.
  - `CountUpText` — animated number reveal for streaks, XP, hours reclaimed.
    Never render a big stat number as a static jump-cut.
  - `GradientProgressRing` — custom Canvas ring (gradient stroke) used for
    the focus-session timer; swap in for habit-completion rings later.
  - `SectionHeader` — consistent "Title + optional action" row.

## UI/UX ideas layered in beyond your original spec

1. **Time-of-day-aware dashboard greeting** — "Late night. This is the
   danger zone." after 10pm nudges self-awareness at the exact moment
   Module 7's AI would flag as highest-risk, without needing the AI call.
2. **Press-scale on every tappable surface** — small (4%) scale-down on
   press. This single detail is most of what makes an app feel expensive
   vs. templated; most Compose apps skip it entirely.
3. **The panic button lives on the dashboard, not buried in a menu** — per
   your Module 12, but placed where you'll actually see it in a bad moment.
4. **Onboarding "why" step uses multi-select chips, not radio buttons** —
   most people quitting porn are also fighting gaming/social media/discipline
   simultaneously; forcing a single choice would misrepresent their reality
   from message one.
5. **Strictness levels are color-coded** (green/amber/red) everywhere they
   appear (Settings, onboarding preview) — reinforces the "Nuclear = point
   of no return" feeling visually, not just in copy.

## What's still a stub (by design, not oversight)

- Onboarding doesn't persist `hasOnboarded` yet — wire to DataStore
  (Module 15) so it isn't in-memory state that resets on process death.
- Focus session doesn't yet call into the AccessibilityService to actually
  restrict apps during the session — Phase 1's blocker enforces the
  permanent blocklist; wiring focus-session-scoped blocking is a short
  follow-up (swap the in-memory `blockedPackages` set based on session state).
- Stats/heatmap use seeded random demo data — swap for real Room queries
  once `habits`/`focus_sessions` tables exist (Phase 2).
- Habit list state is local `remember` — needs a Room-backed ViewModel to
  persist across app restarts.

## To build it

Same as before — Android Studio, real device recommended (accessibility
services and pager gestures behave inconsistently on some emulators). New
Gradle deps added for this pass: `material-icons-extended`, explicit
`foundation` (for `HorizontalPager`), `core-ktx`.

