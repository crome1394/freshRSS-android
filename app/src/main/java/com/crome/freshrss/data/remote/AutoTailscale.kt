package com.crome.freshrss.data.remote

import android.content.Context
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
 * waits for it before the call. When no call has used the server for
 * [LINGER_MS], it disconnects again, but only if it was the one to connect.
 * A counter keeps parallel calls from disconnecting each other.
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
    private var disconnectJob: Job? = null

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
                disconnectJob = scope.launch {
                    delay(LINGER_MS)
                    if (users == 0 && ours) disconnectNow()
                }
            }
        }
    }

    /** Disconnect now if the app connected Tailscale (e.g. when the app closes). */
    fun disconnectNow() {
        if (!ours) return
        Tailscale.setConnected(appContext, connect = false)
        ours = false
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
        const val LINGER_MS = 30_000L
    }
}
