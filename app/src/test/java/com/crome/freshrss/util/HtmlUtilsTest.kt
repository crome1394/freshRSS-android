package com.crome.freshrss.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HtmlUtilsTest {

    @Test
    fun cleanHtml_dropsScriptsAndTurnsImagesIntoLinks() {
        val out = HtmlUtils.cleanHtml(
            "<p onclick=\"x()\" class=\"a\">Hi</p><script>evil()</script>" +
                "<img src=\"https://ex.com/a.jpg\" alt=x><iframe src=\"https://y\"></iframe>" +
                "<a href=\"javascript:alert(1)\">bad</a>",
        )
        assertEquals(
            "<p>Hi</p><a href=\"https://ex.com/a.jpg\">[image]</a><a href=\"#\">bad</a>",
            out,
        )
    }

    @Test
    fun youtubeId_handlesCommonForms() {
        assertEquals("dQw4w9WgXcQ", HtmlUtils.youtubeId("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
        assertEquals("dQw4w9WgXcQ", HtmlUtils.youtubeId("https://youtu.be/dQw4w9WgXcQ?t=3"))
        assertEquals("dQw4w9WgXcQ", HtmlUtils.youtubeId("https://m.youtube.com/shorts/dQw4w9WgXcQ"))
        assertEquals("dQw4w9WgXcQ", HtmlUtils.youtubeId("https://www.youtube.com/watch?feature=x&v=dQw4w9WgXcQ"))
        assertEquals("", HtmlUtils.youtubeId("https://example.com/watch?v=dQw4w9WgXcQ"))
    }

    @Test
    fun leadImage_prefersYoutubeStillThenFirstImage() {
        assertEquals(
            "https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg",
            HtmlUtils.leadImage("<img src=\"https://ex.com/a.jpg\">", "", "https://youtu.be/dQw4w9WgXcQ"),
        )
        assertEquals(
            "https://ex.com/a.jpg?x=1&y=2",
            HtmlUtils.leadImage("<p>t</p><img src=\"https://ex.com/a.jpg?x=1&amp;y=2\">", "", "https://ex.com/post"),
        )
        assertEquals("", HtmlUtils.leadImage("<p>none</p>", "", "https://ex.com/post"))
    }

    @Test
    fun readerHtml_dropsTheLeadImageLink() {
        val raw = "<img src=\"https://ex.com/a.jpg\"><p>Body</p><img src=\"https://ex.com/b.jpg\">"
        val lead = HtmlUtils.leadImage(raw, "", "")
        val out = HtmlUtils.readerHtml(raw, lead)
        assertFalse("a.jpg" in out)
        assertTrue("<a href=\"https://ex.com/b.jpg\">[image]</a>" in out)
    }

    @Test
    fun plainText_collapsesWhitespaceAndEntities() {
        assertEquals("a & b <c>", HtmlUtils.plainText("<p>a &amp; b</p>\n\n <i>&lt;c&gt;</i>"))
    }
}
