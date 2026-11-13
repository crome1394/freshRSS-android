package com.crome.freshrss.data.remote

import android.app.AlarmManager
import android.content.Context
import android.os.SystemClock
import com.crome.freshrss.data.prefs.SettingsRepository
import com.crome.freshrss.util.Tailscale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Tailscale for server calls on mobile data. One per app, shared by the list
 * (refresh, mark, star) and Settings (Test).
 *
 * When the phone is on mobile data (not Wi-Fi/Ethernet), the setting is on,
 * Tailscale is installed, and the VPN is down, [run] connects Tailscale and
 * waits for it before the call. Only a connection this class made is ever
 * disconnected, and it is disconnected:
 *
 * - [LINGER_MS] after the last server call (quick marks share one connection),
 * - at once when the app goes to the background ([onAppBackground]), or as
 *   soon as a call still running then finishes,
 * - by a system alarm [FAILSAFE_MS] later if the app is killed first. The
 *   alarm sends DISCONNECT_VPN straight to Tailscale, so it needs no process.
 */
class AutoTailscale(
    context: Context,
    private val settings: SettingsRepository,
) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val connectLock = Mutex()

    private var users = 0
    private var ours = false
    private var inBackground = false
    private var disconnectJob: Job? = null
    private val alarms = appContext.getSystemService(AlarmManager::class.java)

    private val _connecting = MutableStateFlow(false)

    /** True while waiting for Tailscale to come up. */
    val connecting: StateFlow<Boolean> = _connecting.asStateFlow()

    suspend fun <T> run(block: suspend () -> T): T {
        disconnectJob?.cancel()
        users++
        try {
            connectLock.withLock { connectIfNeeded() }
            return block()
        } finally {
            users--
            if (users == 0 && ours) {
                if (inBackground) {
                    disconnectNow()
                } else {
                    scheduleFailsafe()
                    disconnectJob = scope.launch {
                        delay(LINGER_MS)
                        if (users == 0 && ours) disconnectNow()
                    }
                }
            }
        }
    }

    /** Disconnect now if the app connected Tailscale. */
    fun disconnectNow() {
        disconnectJob?.cancel()
        cancelFailsafe()
        if (!ours) return
        Tailscale.setConnected(appContext, connect = false)
        ours = false
    }

    /** App left the screen: drop our connection now, or when the running call ends. */
    fun onAppBackground() {
        inBackground = true
        if (users == 0 && ours) disconnectNow()
    }

    fun onAppForeground() {
        inBackground = false
    }

    // Inexact alarm (no exact-alarm permission needed): may fire a little late
    // under Doze, but fires even when this process is gone.
    private fun scheduleFailsafe() {
        alarms?.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + FAILSAFE_MS,
            Tailscale.disconnectPendingIntent(appContext),
        )
    }

    private fun cancelFailsafe() {
        alarms?.cancel(Tailscale.disconnectPendingIntent(appContext))
    }

    /** True when [run] would connect Tailscale right now. */
    suspend fun willConnect(): Boolean =
        !Tailscale.isVpnActive(appContext) &&
            settings.autoTailscale.first() &&
            Tailscale.isInstalled(appContext) &&
            Tailscale.isOnMobileData(appContext)

    private suspend fun connectIfNeeded() {
        if (!willConnect()) return
        if (!Tailscale.setConnected(appContext, connect = true)) return
        ours = true
        scheduleFailsafe()
        _connecting.value = true
        try {
            withTimeoutOrNull(CONNECT_TIMEOUT_MS) {
                Tailscale.vpnActiveFlow(appContext).first { it }
            }
            // Give MagicDNS and routes a moment after the interface appears.
            delay(SETTLE_MS)
        } finally {
            _connecting.value = false
        }
    }

    companion object {
        private const val CONNECT_TIMEOUT_MS = 15_000L
        private const val SETTLE_MS = 1_000L

        /** Idle time before an auto-connected Tailscale is disconnected. */
        const val LINGER_MS = 5_000L

        /** Backstop alarm if the app is killed before it can disconnect. */
        private const val FAILSAFE_MS = 60_000L
    }
}
