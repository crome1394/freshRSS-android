package com.crome.freshrss.util

/**
 * Reader view for an article's web page, ported from the quickshell
 * `freshrss/PageReader.qml`: drop comment sections and share bars and keep the
 * main text (the `<article>`, `<main>`, or post-content block with the most
 * paragraph text, else every real paragraph), cleaned like feed text so no
 * scripts run and images stay links.
 */
object PageExtractor {
    const val PAGE_MAX = 1_500_000
    private const val MIN_TEXT = 200
    private const val MIN_PARAGRAPH = 40

    private val keepTags = listOf(
        "p", "h1", "h2", "h3", "h4", "ul", "ol", "li", "blockquote", "a", "b", "strong",
        "i", "em", "br", "pre", "code", "img", "figcaption",
    )

    private val dotAll = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
    private val paragraph = Regex("<p\\b[^>]*>.*?</p>", dotAll)
    private val openTag = Regex("<([a-z][a-z0-9]*)\\b([^>]*)>", RegexOption.IGNORE_CASE)

    /** Comment sections, share bars, and related-post lists, by class or id. */
    private val junkName = Regex(
        "\\b(?:class|id)\\s*=\\s*[\"'][^\"']*(?:comment|share|social|related|newsletter|subscribe|sidebar|promo|advert)",
        RegexOption.IGNORE_CASE,
    )

    /** Blocks that usually hold the story. */
    private val storyName = Regex(
        "\\b(?:class|id|itemprop)\\s*=\\s*[\"'][^\"']*(?:(?:post|entry|article|story)[-_]?(?:content|body|text)|articleBody)",
        RegexOption.IGNORE_CASE,
    )

    private data class Element(val start: Int, val end: Int, val html: String)

    /** Text inside `<p>` tags, as a rough score for how much article a block holds. */
    private fun paragraphText(block: String): Int =
        paragraph.findAll(block).sumOf { HtmlUtils.plainText(it.value).length }

    /**
     * End of the element whose opening tag starts at [at]: the matching closing
     * tag, counting nested ones of the same name. With no closing tag it ends
     * after the opening tag.
     */
    private fun elementEnd(page: String, at: Int): Int {
        val open = openTag.matchAt(page, at) ?: return at + 1
        val tag = open.groupValues[1].lowercase()
        val re = Regex("<(/)?$tag\\b[^>]*>", RegexOption.IGNORE_CASE)
        var depth = 1
        var m = re.find(page, at + open.value.length)
        while (m != null) {
            depth += if (m.groupValues[1].isNotEmpty()) -1 else 1
            if (depth == 0) return m.range.last + 1
            m = m.next()
        }
        return at + open.value.length
    }

    /** Every whole element whose opening tag matches [test] (tag, attrs). */
    private fun elements(page: String, test: (String, String) -> Boolean): List<Element> {
        val out = mutableListOf<Element>()
        var from = 0
        while (from < page.length) {
            val m = openTag.find(page, from) ?: break
            if (!test(m.groupValues[1].lowercase(), m.groupValues[2])) {
                from = m.range.last + 1
                continue
            }
            val end = elementEnd(page, m.range.first)
            out += Element(m.range.first, end, page.substring(m.range.first, end))
            from = end
        }
        return out
    }

    private fun withoutJunk(page: String): String {
        val junk = elements(page) { _, attrs -> junkName.containsMatchIn(attrs) }
        if (junk.isEmpty()) return page
        val sb = StringBuilder()
        var last = 0
        for (e in junk) {
            sb.append(page, last, e.start)
            last = e.end
        }
        sb.append(page, last, page.length)
        return sb.toString()
    }

    /** Links and images relative to the page made absolute. */
    private fun absolute(html: String, pageUrl: String): String {
        val origin = Regex("^https?://[^/]+", RegexOption.IGNORE_CASE).find(pageUrl)?.value
            ?: return html
        return html
            .replace(Regex("\\b(href|src)\\s*=\\s*([\"'])/(?!/)", RegexOption.IGNORE_CASE), "$1=$2$origin/")
            .replace(Regex("\\b(href|src)\\s*=\\s*([\"'])//", RegexOption.IGNORE_CASE), "$1=$2https://")
    }

    /** The page's main text as reader HTML, or "" when none was found. */
    fun extract(page: String, pageUrl: String): String {
        var s = if (page.length > PAGE_MAX) page.substring(0, PAGE_MAX) else page
        s = s.replace(Regex("<!--.*?-->", dotAll), "")
        s = s.replace(
            Regex(
                "<(head|script|style|noscript|svg|nav|header|footer|aside|form|iframe|template|button|select)\\b.*?</\\1\\s*>",
                dotAll,
            ),
            "",
        )
        s = withoutJunk(s)
        var best = ""
        var bestScore = 0
        val candidates = elements(s) { tag, attrs ->
            tag == "article" || tag == "main" || storyName.containsMatchIn(attrs)
        }
        for (c in candidates) {
            val score = paragraphText(c.html)
            if (score > bestScore) {
                best = c.html
                bestScore = score
            }
        }
        if (bestScore < MIN_TEXT) {
            best = paragraph.findAll(s)
                .map { it.value }
                .filter { HtmlUtils.plainText(it).length >= MIN_PARAGRAPH }
                .joinToString("\n")
        }
        if (HtmlUtils.plainText(best).length < MIN_TEXT) return ""
        val keep = Regex("<(?!/?(?:${keepTags.joinToString("|")})\\b)/?[a-z][^>]*>", RegexOption.IGNORE_CASE)
        best = best.replace(keep, "")
        best = absolute(best, pageUrl)
        return HtmlUtils.cleanHtml(best).replace(Regex("\\n\\s*\\n+"), "\n").trim()
    }
}
