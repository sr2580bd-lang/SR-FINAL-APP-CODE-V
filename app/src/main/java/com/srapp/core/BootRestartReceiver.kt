package com.srapp.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.srapp.SrApplication

/**
 * Accessibility services survive reboot on their own once enabled in
 * Settings, but our in-memory blocklist cache (SrApplication.blockedPackages)
 * does not — this just forces a fresh read from Room the first chance we get
 * after boot. Phase 3 will extend this to re-arm WorkManager schedules too.
 */
class BootRestartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        (context.applicationContext as? SrApplication)?.refreshBlocklistCache()
    }
}
