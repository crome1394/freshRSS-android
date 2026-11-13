package com.crome.freshrss.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ServerUrlTest {

    @Test
    fun defaultsBareHostToHttps() {
        val r = ServerUrl.normalize("freshrss.example.com", allowCleartext = false)
        assertTrue(r.ok)
        assertEquals("https://freshrss.example.com", r.normalized)
    }

    @Test
    fun allowsHttpsAlways() {
        val r = ServerUrl.normalize("https://news.local/fr", allowCleartext = false)
        assertTrue(r.ok)
        assertEquals("https://news.local/fr", r.normalized)
    }

    @Test
    fun blocksHttpWhenCleartextDisallowed() {
        val r = ServerUrl.normalize("http://10.0.0.5", allowCleartext = false)
        assertFalse(r.ok)
        assertTrue(r.error!!.contains("HTTP", ignoreCase = true))
    }

    @Test
    fun allowsHttpWhenCleartextEnabled() {
        val r = ServerUrl.normalize("http://10.0.0.5/", allowCleartext = true)
        assertTrue(r.ok)
        assertEquals("http://10.0.0.5", r.normalized)
        assertTrue(r.isCleartext)
    }

    @Test
    fun rejectsBlank() {
        assertFalse(ServerUrl.normalize("", allowCleartext = true).ok)
        assertFalse(ServerUrl.normalize("   ", allowCleartext = true).ok)
    }

    @Test
    fun stripsTrailingSlash() {
        val r = ServerUrl.normalize("https://example.com/freshrss/", allowCleartext = false)
        assertEquals("https://example.com/freshrss", r.normalized)
    }

    @Test
    fun iconUrl_resolvesPathsAgainstServerOrigin() {
        org.junit.Assert.assertEquals(
            "https://rss.example.com/f.php?h=abc",
            ServerUrl.iconUrl("/f.php?h=abc", "https://rss.example.com/freshrss"),
        )
        org.junit.Assert.assertEquals(
            "http://10.0.0.5/f.php?h=1",
            ServerUrl.iconUrl("http://10.0.0.5/f.php?h=1", "https://other"),
        )
        org.junit.Assert.assertEquals("", ServerUrl.iconUrl("javascript:x", "https://rss.example.com"))
        org.junit.Assert.assertEquals("", ServerUrl.iconUrl("", "https://rss.example.com"))
    }
}
