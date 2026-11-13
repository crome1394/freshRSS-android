package com.crome.freshrss.util

/**
 * Where a feed comes from, for the Source filter. Worked out from the feed's
 * own URL first (YouTube and Reddit feeds have telltale addresses), then its
 * site URL, then a sample article link.
 */
enum class Provider(val label: String) {
    YOUTUBE("YouTube"),
    REDDIT("Reddit"),
    GITHUB("GitHub"),
    MASTODON("Mastodon"),
    RUMBLE("Rumble"),
    ODYSEE("Odysee"),
    SUBSTACK("Substack"),
    MEDIUM("Medium"),
    WEB("Websites"),
}

object Providers {
    private val rules: List<Pair<Provider, Regex>> = listOf(
        Provider.YOUTUBE to Regex("""(^|\.)(youtube\.com|youtu\.be|youtube-nocookie\.com)$"""),
        Provider.REDDIT to Regex("""(^|\.)(reddit\.com|redd\.it)$"""),
        Provider.GITHUB to Regex("""(^|\.)(github\.com|githubusercontent\.com)$"""),
        Provider.RUMBLE to Regex("""(^|\.)rumble\.com$"""),
        Provider.ODYSEE to Regex("""(^|\.)(odysee\.com|lbry\.tv)$"""),
        Provider.SUBSTACK to Regex("""(^|\.)substack\.com$"""),
        Provider.MEDIUM to Regex("""(^|\.)medium\.com$"""),
    )

    /** Mastodon and other fediverse profiles: https://host/@user(.rss). */
    private val fediverseProfile = Regex("""^https?://[^/]+/@[A-Za-z0-9_.]+(\.rss)?/?$""", RegexOption.IGNORE_CASE)

    private fun host(url: String): String =
        Regex("""^https?://([^/:?#]+)""", RegexOption.IGNORE_CASE)
            .find(url.trim())?.groupValues?.get(1)?.lowercase()?.removePrefix("www.").orEmpty()

    /** Provider for one URL, or null when the URL says nothing special. */
    fun of(url: String?): Provider? {
        val u = url?.trim().orEmpty()
        if (u.isEmpty()) return null
        val h = host(u)
        if (h.isEmpty()) return null
        rules.firstOrNull { it.second.containsMatchIn(h) }?.let { return it.first }
        if (fediverseProfile.matches(u)) return Provider.MASTODON
        return null
    }

    /** First URL that names a provider wins; otherwise a plain website. */
    fun classify(vararg urls: String?): Provider =
        urls.firstNotNullOfOrNull { of(it) } ?: Provider.WEB
}
