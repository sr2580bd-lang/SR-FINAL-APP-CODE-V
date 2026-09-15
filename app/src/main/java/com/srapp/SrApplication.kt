package com.srapp

import android.app.Application
import com.srapp.core.data.SrDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SrApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var database: SrDatabase
        private set

    /**
     * The AccessibilityService fires on EVERY window change across the whole
     * OS — it must never touch Room/disk directly on that hot path or you'll
     * introduce jank and dropped events. Instead we keep a plain in-memory
     * set here, refreshed whenever the blocklist changes, and the service
     * only ever reads this.
     */
    private val _blockedPackages = MutableStateFlow<Set<String>>(emptySet())
    val blockedPackages: StateFlow<Set<String>> = _blockedPackages

    override fun onCreate() {
        super.onCreate()
        database = SrDatabase.get(this)
        refreshBlocklistCache()
    }

    fun refreshBlocklistCache() {
        applicationScope.launch {
            val names = database.blockingDao().getBlockedPackageNames().toSet()
            _blockedPackages.value = names
        }
    }
}
