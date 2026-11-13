package com.crome.freshrss.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PageExtractorTest {
    private val para = "This is a sentence of the real story that goes on for a while. "

    @Test
    fun keepsTheArticleAndDropsJunk() {
        val page = """
            <html><head><title>x</title><script>bad()</script></head><body>
            <nav><a href="/">Home</a></nav>
            <div class="sidebar"><p>${para.repeat(10)}</p></div>
            <article><div class="post-content">
              <p>${para.repeat(3)}</p>
              <div class="share-bar"><p>Share this on social media please</p></div>
              <p>Second <a href="/more">paragraph</a> ${para.repeat(2)}</p>
              <img src="/pic.jpg">
            </div></article>
            <section id="comments"><p>${para.repeat(5)} comment</p></section>
            </body></html>
        """.trimIndent()
        val out = PageExtractor.extract(page, "https://news.example.com/a/story")
        assertTrue(out.startsWith("<p>This is a sentence"))
        assertTrue("href=\"https://news.example.com/more\"" in out)
        assertTrue("<a href=\"https://news.example.com/pic.jpg\">[image]</a>" in out)
        assertFalse("Share this" in out)
        assertFalse("comment" in out)
        assertFalse("<div" in out)
        assertFalse("bad()" in out)
    }

    @Test
    fun fallsBackToLongParagraphs() {
        val page = "<div><p>short</p><p>${para.repeat(4)}</p></div>"
        val out = PageExtractor.extract(page, "https://ex.com/")
        assertEquals("<p>${para.repeat(4)}</p>", out)
    }

    @Test
    fun emptyWhenNoText() {
        assertEquals("", PageExtractor.extract("<html><body><p>hi</p></body></html>", "https://ex.com"))
    }
}
