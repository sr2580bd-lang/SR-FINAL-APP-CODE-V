package com.srapp.core.ui.motion

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/**
 * One motion language for the whole app. Every screen should reach for these
 * instead of inventing its own tween — that consistency is 80% of what makes
 * an app feel "premium" rather than "a pile of screens."
 */
object SrMotion {
    // Emphasized easing (Material 3's expressive curve) — snappy start, soft landing.
    val EmphasizedEasing = CubicBezierEasing(0.2f, 0.0f, 0f, 1.0f)

    fun <T> emphasized(durationMs: Int = 400): FiniteAnimationSpec<T> =
        tween(durationMs, easing = EmphasizedEasing)

    // Bouncy spring for anything that should feel alive: streak counters,
    // celebration states, button presses, card entrances.
    fun <T> springy(): FiniteAnimationSpec<T> =
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)

    fun <T> snappy(): FiniteAnimationSpec<T> =
        spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh)

    const val QUICK = 150
    const val STANDARD = 300
    const val SLOW = 500
}
