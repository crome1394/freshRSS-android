package com.crome.freshrss.util

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Connect / disconnect the Tailscale VPN from the title-bar key button.
 *
 * Tailscale's Android app accepts `com.tailscale.ipn.CONNECT_VPN` and
 * `DISCONNECT_VPN` (the same intents Tasker uses). Newer versions (1.9x+) handle
 * them in `AutomationActivity`, a no-UI trampoline that also wakes a stopped
 * app; older ones only have the `IPNReceiver` broadcast receiver. We try the
 * activity first and fall back to the broadcast.
 *
 * Connect needs Tailscale to be logged in with VPN consent already granted;
 * otherwise Tailscale opens its own screen to finish that.
 */
object Tailscale {
    const val PACKAGE = "com.tailscale.ipn"
    private const val ACTION_CONNECT = "com.tailscale.ipn.CONNECT_VPN"
    private const val ACTION_DISCONNECT = "com.tailscale.ipn.DISCONNECT_VPN"
    private const val AUTOMATION_ACTIVITY = "com.tailscale.ipn.AutomationActivity"
    private const val RECEIVER = "com.tailscale.ipn.IPNReceiver"

    fun isInstalled(context: Context): Boolean =
        context.packageManager.getLaunchIntentForPackage(PACKAGE) != null

    /** True while any VPN network is up. Tailscale is the only VPN this button controls. */
    fun isVpnActive(context: Context): Boolean {
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return false
        @Suppress("DEPRECATION")
        return cm.allNetworks.any { n ->
            cm.getNetworkCapabilities(n)?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
        }
    }

    /**
     * True when the phone is online over mobile data and not Wi-Fi or Ethernet
     * (checked before any VPN is up, so the active network is the real one).
     */
    fun isOnMobileData(context: Context): Boolean {
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return false
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        ) {
            return false
        }
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    /** Emits [isVpnActive] now and whenever a VPN network comes or goes. */
    fun vpnActiveFlow(context: Context): Flow<Boolean> = callbackFlow {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        trySend(isVpnActive(context))
        if (cm == null) {
            awaitClose { }
            return@callbackFlow
        }
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
            .build()
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(isVpnActive(context))
            }
        }
        cm.registerNetworkCallback(request, callback)
        awaitClose { cm.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

    /**
     * Send connect (when [connect]) or disconnect to Tailscale.
     * @return false when Tailscale is not installed or refused both intents.
     */
    fun setConnected(context: Context, connect: Boolean): Boolean {
        val action = if (connect) ACTION_CONNECT else ACTION_DISCONNECT
        val activity = Intent(action)
            .setComponent(ComponentName(PACKAGE, AUTOMATION_ACTIVITY))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
        try {
            context.startActivity(activity)
            return true
        } catch (_: ActivityNotFoundException) {
            // Older Tailscale: broadcast receiver only.
        } catch (_: SecurityException) {
        }
        return try {
            // FLAG_INCLUDE_STOPPED_PACKAGES: reach Tailscale even after the
            // system force-stopped it (Samsung "deep sleeping apps").
            context.sendBroadcast(
                Intent(action)
                    .setComponent(ComponentName(PACKAGE, RECEIVER))
                    .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES),
            )
            isInstalled(context)
        } catch (_: SecurityException) {
            false
        }
    }

    /** Open the Tailscale app (long-press on the key button). */
    fun open(context: Context): Boolean {
        val launch = context.packageManager.getLaunchIntentForPackage(PACKAGE) ?: return false
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launch)
        return true
    }
}
