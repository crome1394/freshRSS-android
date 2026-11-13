package com.crome.freshrss.util

import com.crome.freshrss.data.model.ReaderDefaults

/**
 * Article HTML helpers, ported from the quickshell FreshRSS service
 * (`services/FreshRssService.qml`).
 */
object HtmlUtils {

    private val ignoreCase = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)

    fun stripHtml(html: String?): String {
        if (html.isNullOrBlank()) return ""
        var t = html
        t = t.replace(Regex("(?is)<script[^>]*>.*?</script>"), " ")
        t = t.replace(Regex("(?is)<style[^>]*>.*?</style>"), " ")
        t = t.replace(Regex("(?is)<br\\s*/?>"), "\n")
        t = t.replace(Regex("(?is)</p>"), "\n\n")
        t = t.replace(Regex("(?is)<[^>]+>"), " ")
        t = decodeEntities(t)
        t = t.replace(Regex("[ \\t]{2,}"), " ")
        t = t.replace(Regex("\n{3,}"), "\n\n")
        return t.trim()
    }

    /** Tags removed and whitespace collapsed to single spaces. */
    fun plainText(html: String?): String =
        decodeEntities(html.orEmpty().replace(Regex("<[^>]*>"), " "))
            .replace(Regex("\\s+"), " ")
            .trim()

    private fun decodeEntities(s: String): String = s
        .replace("&nbsp;", " ")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&amp;", "&")

    /**
     * Article HTML for the reader: no scripts, frames, forms, or styles, and
     * images become `[image]` links so nothing loads from the network. (The
     * article's lead picture is the one exception; see [leadImage].)
     */
    fun cleanHtml(html: String?): String {
        var s = html.orEmpty()
        s = s.replace(Regex("<!--.*?-->", ignoreCase), "")
        s = s.replace(
            Regex(
                "<(script|style|iframe|object|embed|form|noscript|svg|video|audio)\\b.*?</\\1\\s*>",
                ignoreCase,
            ),
            "",
        )
        s = s.replace(
            Regex(
                "<(script|style|iframe|object|embed|form|input|button|link|meta|base|source|track)\\b[^>]*>",
                RegexOption.IGNORE_CASE,
            ),
            "",
        )
        s = s.replace(
            Regex("<img\\b[^>]*?\\bsrc\\s*=\\s*[\"']?(https?:[^\"'\\s>]+)[^>]*>", RegexOption.IGNORE_CASE),
            "<a href=\"$1\">[image]</a>",
        )
        s = s.replace(Regex("<img\\b[^>]*>", RegexOption.IGNORE_CASE), "")
        s = s.replace(
            Regex("\\s(on[a-z]+|style|class|id)\\s*=\\s*(\"[^\"]*\"|'[^']*'|[^\\s>]+)", RegexOption.IGNORE_CASE),
            "",
        )
        s = s.replace(
            Regex("href\\s*=\\s*[\"']?\\s*(javascript|data|vbscript):[^\"'\\s>]*[\"']?", RegexOption.IGNORE_CASE),
            "href=\"#\"",
        )
        return s
    }

    /** The YouTube video id in a link, or "". */
    fun youtubeId(url: String?): String {
        val m = Regex(
            "^https?://(?:www\\.|m\\.)?(?:youtube\\.com/(?:watch\\?(?:[^#]*&)?v=|shorts/|live/|embed/)|youtu\\.be/)([A-Za-z0-9_-]{11})",
            RegexOption.IGNORE_CASE,
        ).find(url.orEmpty())
        return m?.groupValues?.get(1).orEmpty()
    }

    /**
     * The article's lead picture: a YouTube still frame, else the first image
     * in its HTML, else "". Only the open article's one is loaded.
     */
    fun leadImage(rawHtml: String?, mediaUrl: String?, url: String?): String {
        val yt = youtubeId(mediaUrl).ifBlank { youtubeId(url) }
        if (yt.isNotBlank()) return "https://i.ytimg.com/vi/$yt/hqdefault.jpg"
        val img = Regex(
            "<img\\b[^>]*?\\bsrc\\s*=\\s*[\"']?(https?:[^\"'\\s>]+)",
            RegexOption.IGNORE_CASE,
        ).find(rawHtml.orEmpty())
        return img?.groupValues?.get(1)?.replace("&amp;", "&").orEmpty()
    }

    /**
     * Cleaned, size-capped reader HTML. The lead picture shows above the text,
     * so its `[image]` link is dropped (and YouTube stills for a YouTube lead).
     */
    fun readerHtml(rawHtml: String?, leadImage: String): String {
        var html = cleanHtml(rawHtml)
        if (leadImage.isNotBlank()) {
            html = html
                .replace("<a href=\"${leadImage.replace("&", "&amp;")}\">[image]</a>", "")
                .replace("<a href=\"$leadImage\">[image]</a>", "")
            if (leadImage.startsWith("https://i.ytimg.com/")) {
                html = html.replace(
                    Regex("<a href=\"https?://[a-z0-9]+\\.ytimg\\.com/[^\"]*\">\\[image]</a>", RegexOption.IGNORE_CASE),
                    "",
                )
            }
        }
        return truncateHtml(html)
    }

    fun summary(plain: String, max: Int = ReaderDefaults.SUMMARY_MAX): String =
        if (plain.length <= max) plain else plain.take(max) + "…"

    fun truncateHtml(html: String, max: Int = ReaderDefaults.HTML_MAX): String =
        if (html.length <= max) html else html.take(max) + "…"

    fun truncateText(text: String, max: Int = ReaderDefaults.TEXT_MAX): String =
        if (text.length <= max) text else text.take(max) + "…"
}
