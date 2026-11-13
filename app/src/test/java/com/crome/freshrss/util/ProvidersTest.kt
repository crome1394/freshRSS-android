package com.crome.freshrss.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ProvidersTest {
    @Test
    fun feedUrlsOfCommonProviders() {
        assertEquals(Provider.YOUTUBE, Providers.classify("https://www.youtube.com/feeds/videos.xml?channel_id=UC123"))
        assertEquals(Provider.YOUTUBE, Providers.classify("https://youtu.be/abc"))
        assertEquals(Provider.REDDIT, Providers.classify("https://www.reddit.com/r/cachyos/.rss"))
        assertEquals(Provider.REDDIT, Providers.classify("https://old.reddit.com/r/linux/"))
        assertEquals(Provider.GITHUB, Providers.classify("https://github.com/owner/repo/releases.atom"))
        assertEquals(Provider.RUMBLE, Providers.classify("https://rumble.com/c/channel/feed"))
        assertEquals(Provider.ODYSEE, Providers.classify("https://odysee.com/\$/rss/@channel:1"))
        assertEquals(Provider.SUBSTACK, Providers.classify("https://someone.substack.com/feed"))
        assertEquals(Provider.MEDIUM, Providers.classify("https://medium.com/feed/@writer"))
        assertEquals(Provider.MASTODON, Providers.classify("https://mastodon.social/@someone.rss"))
    }

    @Test
    fun fallsBackThroughUrlsThenWeb() {
        // FreshRSS-proxied feed URL says nothing; the site URL does.
        assertEquals(Provider.YOUTUBE, Providers.classify("https://rss.example.com/f/1", "https://www.youtube.com/channel/UC1"))
        assertEquals(Provider.WEB, Providers.classify("https://arstechnica.com/feed/", "https://arstechnica.com"))
        assertEquals(Provider.WEB, Providers.classify("", null))
        // Look-alike hosts are not the provider.
        assertEquals(Provider.WEB, Providers.classify("https://notyoutube.com.evil.example/feed"))
        assertEquals(Provider.WEB, Providers.classify("https://myreddit.com/feed"))
    }
}
