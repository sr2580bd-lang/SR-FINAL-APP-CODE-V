package com.srapp.core.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object SRSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}

object SRShapes {
    val chip = RoundedCornerShape(100)
    val listRow = RoundedCornerShape(12.dp)
    val card = RoundedCornerShape(20.dp)
    val sheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
    val fullCircle = CircleShape
}

object SRHaptics {
    // Placeholder haptic API for future use. Keep the hooks centralized.
    // Actual haptic invocation can be attached at the view/interaction layer when needed.
}
