package com.crome.freshrss.data.remote

import android.net.Network
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeoutOrNull
import java.net.InetSocketAddress
import java.net.URI

/**
 * Can the FreshRSS server be reached directly (without Tailscale)?
 *
 * Opens a TCP connection to the URL's host and port on [Network] and closes it
 * at once. On home Wi-Fi this answers in milliseconds; on another network the
 * LAN address or home name fails to resolve or times out.
 */
object ServerProbe {
    private const val CONNECT_TIMEOUT_MS = 2_000
    private const val TOTAL_TIMEOUT_MS = 3_000L

    // DNS lookups can't be interrupted; run them here so a slow one is
    // abandoned at the timeout instead of holding up the caller.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    suspend fun reachable(network: Network, url: String): Boolean {
        val uri = runCatching { URI(url.trim()) }.getOrNull() ?: return false
        val host = uri.host ?: return false
        val port = when {
            uri.port > 0 -> uri.port
            uri.scheme.equals("http", ignoreCase = true) -> 80
            else -> 443
        }
        val attempt = scope.async {
            runCatching {
                val address = network.getByName(host)
                network.socketFactory.createSocket().use { socket ->
                    socket.connect(InetSocketAddress(address, port), CONNECT_TIMEOUT_MS)
                }
                true
            }.getOrDefault(false)
        }
        return withTimeoutOrNull(TOTAL_TIMEOUT_MS) { attempt.await() } ?: run {
            attempt.cancel()
            false
        }
    }
}
