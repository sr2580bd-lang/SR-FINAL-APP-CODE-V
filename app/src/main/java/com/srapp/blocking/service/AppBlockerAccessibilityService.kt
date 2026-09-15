package com.srapp.blocking.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.srapp.SrApplication
import com.srapp.blocking.ui.InterventionActivity

/**
 * PHASE 1 CORE: watches for foreground-app changes system-wide and, if the
 * new foreground app is on the blocklist, immediately launches the
 * intervention screen instead of letting the user proceed into the app.
 *
 * Notes on why it's built this way:
 *  - We only react to TYPE_WINDOW_STATE_CHANGED (declared in the service's
 *    config xml) which fires once per app/activity switch — cheap, not a
 *    per-frame flood like TYPE_VIEW_* events would be.
 *  - We never touch Room here (see SrApplication.blockedPackages) — reading
 *    disk on this callback would cause dropped events / jank.
 *  - We de-dupe consecutive events for the same package so re-entering the
 *    intervention doesn't spam-launch itself.
 *  - This does NOT try to force-close the blocked app (that's not reliably
 *    possible without root). Instead it draws the intervention screen on
 *    top in its own task and, on completion, sends the user home. This
 *    matches how BlockerX/Forest actually behave under Android's model.
 */
class AppBlockerAccessibilityService : AccessibilityService() {

    private var lastHandledPackage: String? = null
    private var lastHandledAtMs: Long = 0L

    // Packages that should never trigger the intervention loop even if
    // somehow added to the blocklist by mistake.
    private val alwaysAllowed = setOf(
        "com.srapp",
        "com.android.systemui",
        "android"
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        if (packageName in alwaysAllowed) return

        val app = application as? SrApplication ?: return
        val blocked = app.blockedPackages.value
        if (packageName !in blocked) return

        val now = System.currentTimeMillis()
        // Debounce: don't re-launch the intervention more than once per 2s
        // for the same package (window-state events can double-fire).
        if (packageName == lastHandledPackage && now - lastHandledAtMs < 2000) return
        lastHandledPackage = packageName
        lastHandledAtMs = now

        Log.d(TAG, "Blocked app detected in foreground: $packageName")
        launchIntervention(packageName)
    }

    private fun launchIntervention(packageName: String) {
        val intent = Intent(this, InterventionActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(InterventionActivity.EXTRA_PACKAGE_NAME, packageName)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {
        Log.w(TAG, "Accessibility service interrupted by system")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "SR App blocker connected")
        (application as? SrApplication)?.refreshBlocklistCache()
    }

    companion object {
        private const val TAG = "SrAppBlocker"
    }
}
