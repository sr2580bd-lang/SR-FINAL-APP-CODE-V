package com.srapp.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * SR APP — "Voidforge" palette.
 * Design intent: near-black void background, electric violet as the
 * "discipline" primary, neon cyan as the "focus" accent, and a hot
 * signal-red reserved ONLY for relapse/danger states so it always reads
 * as an alarm, never decoration.
 */

// Base / surfaces — true near-black, not muddy grey, for OLED + "void" feel
val VoidBlack = Color(0xFF060608)
val VoidSurface = Color(0xFF111118)
val VoidSurfaceRaised = Color(0xFF171722)
val VoidSurfaceHigh = Color(0xFF1F1F2C)
val VoidOutline = Color(0xFF2C2C3A)

// Primary — electric violet (discipline / core brand)
val ElectricViolet = Color(0xFF8B5CF6)
val ElectricVioletBright = Color(0xFFA78BFA)
val ElectricVioletDim = Color(0xFF5B21B6)
val OnElectricViolet = Color(0xFFF5F3FF)

// Secondary — neon cyan (focus sessions / progress)
val NeonCyan = Color(0xFF22D3EE)
val NeonCyanBright = Color(0xFF67E8F9)
val NeonCyanDim = Color(0xFF0E7490)
val OnNeonCyan = Color(0xFF04121A)

// Tertiary — signal amber (streaks / XP / gamification)
val SignalAmber = Color(0xFFFBBF24)
val SignalAmberBright = Color(0xFFFDE68A)

// Danger — reserved exclusively for relapse, uninstall attempts, nuclear mode
val DangerRed = Color(0xFFFF3B5C)
val DangerRedDim = Color(0xFF7F1D2E)
val OnDanger = Color(0xFFFFF1F3)

// Success — habit complete, streak alive, safe state
val SuccessGreen = Color(0xFF34D399)
val SuccessGreenDim = Color(0xFF065F46)

// Text
val TextPrimary = Color(0xFFF4F4F7)
val TextSecondary = Color(0xFFA6A6B8)
val TextDisabled = Color(0xFF5C5C6E)

// Gradient stops used for the streak-flame hero and boss-battle cards
val GradientVioletStart = Color(0xFF8B5CF6)
val GradientVioletEnd = Color(0xFF3B0764)
val GradientDangerStart = Color(0xFFFF3B5C)
val GradientDangerEnd = Color(0xFF450A18)

// Centralized design tokens to match the product spec exactly.
object SRColors {
    val SurfaceBase: Color = com.srapp.core.ui.theme.VoidBlack
    val SurfaceRaised: Color = com.srapp.core.ui.theme.VoidSurface
    val SurfaceRaisedHigh: Color = com.srapp.core.ui.theme.VoidSurfaceHigh
    val SurfaceOverlay: Color = com.srapp.core.ui.theme.VoidBlack.copy(alpha = 0.92f)

    val AccentPrimary: Color = com.srapp.core.ui.theme.ElectricViolet
    val AccentPrimaryDim: Color = com.srapp.core.ui.theme.ElectricViolet.copy(alpha = 0.16f)
    val OnAccent: Color = com.srapp.core.ui.theme.OnElectricViolet

    val TextPrimary: Color = com.srapp.core.ui.theme.TextPrimary
    val TextSecondary: Color = com.srapp.core.ui.theme.TextSecondary
    val TextDisabled: Color = com.srapp.core.ui.theme.TextDisabled

    val Success: Color = com.srapp.core.ui.theme.SuccessGreen
    val Neutral: Color = com.srapp.core.ui.theme.TextSecondary
    val Danger: Color = com.srapp.core.ui.theme.DangerRed

    val HeatmapEmpty: Color = com.srapp.core.ui.theme.VoidSurfaceHigh
    val HeatmapLow: Color = com.srapp.core.ui.theme.ElectricViolet.copy(alpha = 0.35f)
    val HeatmapHigh: Color = com.srapp.core.ui.theme.ElectricViolet
}
