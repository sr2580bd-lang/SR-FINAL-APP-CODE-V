package com.srapp.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SrDarkColorScheme = darkColorScheme(
    primary = ElectricViolet,
    onPrimary = OnElectricViolet,
    primaryContainer = ElectricVioletDim,
    onPrimaryContainer = ElectricVioletBright,
    secondary = NeonCyan,
    onSecondary = OnNeonCyan,
    secondaryContainer = NeonCyanDim,
    onSecondaryContainer = NeonCyanBright,
    tertiary = SignalAmber,
    onTertiary = Color(0xFF3B2900),
    background = VoidBlack,
    onBackground = TextPrimary,
    surface = VoidSurface,
    onSurface = TextPrimary,
    surfaceVariant = VoidSurfaceRaised,
    onSurfaceVariant = TextSecondary,
    surfaceContainerHighest = VoidSurfaceHigh,
    outline = VoidOutline,
    error = DangerRed,
    onError = OnDanger,
    errorContainer = DangerRedDim,
    onErrorContainer = OnDanger
)

// SR App is dark-first by design (it's the "night vision" tool for late-night
// urge moments) but we still provide a light scheme for the OS toggle.
private val SrLightColorScheme = lightColorScheme(
    primary = ElectricVioletDim,
    onPrimary = Color.White,
    secondary = NeonCyanDim,
    onSecondary = Color.White,
    tertiary = Color(0xFFB45309),
    background = Color(0xFFFAFAFC),
    onBackground = Color(0xFF16161F),
    surface = Color.White,
    onSurface = Color(0xFF16161F),
    error = DangerRed,
    onError = Color.White
)

/** Extra brand colors that don't map cleanly onto Material's color roles. */
data class SrExtendedColors(
    val success: Color,
    val successContainer: Color,
    val danger: Color,
    val dangerContainer: Color,
    val streakFlame: Color,
    val xpGold: Color
)

private val LocalSrExtendedColors = staticCompositionLocalOf {
    SrExtendedColors(
        success = SuccessGreen,
        successContainer = SuccessGreenDim,
        danger = DangerRed,
        dangerContainer = DangerRedDim,
        streakFlame = SignalAmber,
        xpGold = SignalAmberBright
    )
}

object SrTheme {
    val extended: SrExtendedColors
        @Composable get() = LocalSrExtendedColors.current
}

@Composable
fun SrAppTheme(
    darkTheme: Boolean = true, // default dark: matches the brief's "futuristic/void" direction
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SrDarkColorScheme else SrLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as? android.app.Activity)?.window
        window?.let {
            it.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    val extendedColors = SrExtendedColors(
        success = SuccessGreen,
        successContainer = SuccessGreenDim,
        danger = DangerRed,
        dangerContainer = DangerRedDim,
        streakFlame = SignalAmber,
        xpGold = SignalAmberBright
    )

    CompositionLocalProvider(LocalSrExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SrTypography,
            shapes = SrShapes,
            content = content
        )
    }
}
