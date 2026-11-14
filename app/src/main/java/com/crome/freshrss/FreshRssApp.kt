package com.crome.freshrss

import android.app.Application
import com.crome.freshrss.data.prefs.SettingsRepository
import com.crome.freshrss.data.remote.FreshRssClient
import com.crome.freshrss.data.remote.FreshRssClientImpl
import com.crome.freshrss.data.remote.AutoTailscale
import com.crome.freshrss.data.secure.EncryptedAuthTokenStore

class FreshRssApp : Application() {
    lateinit var settings: SettingsRepository
        private set
    lateinit var client: FreshRssClient
        private set

    /** Tailscale when the server is out of reach, shared by the list and Settings. */
    lateinit var tailscale: AutoTailscale
        private set

    override fun onCreate() {
        super.onCreate()
        settings = SettingsRepository(this)
        // Move any pre-0.6.4 plaintext API password into EncryptedSharedPreferences.
        settings.migrateSecretsBlocking()
        client = FreshRssClientImpl(
            authTokenStore = EncryptedAuthTokenStore(this),
        )
        tailscale = AutoTailscale(this, settings) { client.config.baseUrl }
    }
}

val Application.freshRssApp: FreshRssApp
    get() = this as FreshRssApp
