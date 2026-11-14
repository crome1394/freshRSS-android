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
 * Tailscale for server calls when the server is out of reach. One per app,
 * shared by the list (refresh, mark, star) and Settings (Test).
 *
 * When the setting is on, Tailscale is installed, the VPN is down, and the
 * server can't be reached directly, [run] connects Tailscale and waits for it
 * before the call. "Can't be reached" means: on mobile data, always; on Wi-Fi
 * or anything else, a quick TCP connect to the server's host and port
 * ([ServerProbe]) fails, so home Wi-Fi stays direct while any other Wi-Fi
 * uses Tailscale. Only a connection this class made is ever disconnected,
 * and it is disconnected:
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
    /** The URL calls are about to use (the client's current config). */
    private val serverUrl: () -> String,
) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val connectLock = Mutex()

    private var users = 0
    private var ours = false
    private var inBackground = false
    private var disconnectJob: Job? = null
    private val alarms = appContext.getSystemService(AlarmManager::class.java)

    // Last failed probe: skip re-probing (and its timeout) on the same network
    // and URL for a while, so marks after the linger don't each wait again.
    private var unreachableKey: String? = null
    private var unreachableAt = 0L

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

    /** True when [run] would connect Tailscale right now. May probe the server (up to ~2 s). */
    suspend fun willConnect(): Boolean {
        if (Tailscale.isVpnActive(appContext)) return false
        if (!settings.autoTailscale.first() || !Tailscale.isInstalled(appContext)) return false
        val network = Tailscale.activeNetwork(appContext) ?: return false
        if (Tailscale.isOnMobileData(appContext)) return true
        val url = serverUrl().ifBlank { settings.config.first().baseUrl }
        if (url.isBlank()) return false
        val key = "$network|$url"
        val now = SystemClock.elapsedRealtime()
        if (key == unreachableKey && now - unreachableAt < UNREACHABLE_CACHE_MS) return true
        if (ServerProbe.reachable(network, url)) {
            unreachableKey = null
            return false
        }
        unreachableKey = key
        unreachableAt = now
        return true
    }

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

        /** How long a failed probe on one network is trusted. */
        private const val UNREACHABLE_CACHE_MS = 10 * 60_000L
    }
}
