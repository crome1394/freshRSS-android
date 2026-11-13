package com.crome.freshrss.data.remote

import com.crome.freshrss.util.PageExtractor
import com.crome.freshrss.util.SafeUrls
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/** Result of fetching an article's web page for the reader view. */
sealed class PageResult {
    data class Ok(val html: String) : PageResult()
    data class Failed(val message: String) : PageResult()
}

/**
 * Fetches an article's page and keeps its main text ([PageExtractor]).
 * Only http(s) links; the body is capped at [PageExtractor.PAGE_MAX] chars.
 */
class PageFetcher(private val http: OkHttpClient = HttpSupport.defaultClient()) {

    suspend fun fetch(url: String): PageResult = withContext(Dispatchers.IO) {
        if (!SafeUrls.isSafeHttpUrl(url)) return@withContext PageResult.Failed("This article has no web page.")
        val req = Request.Builder()
            .url(url)
            .header("User-Agent", PAGE_USER_AGENT)
            .header("Accept", "text/html,application/xhtml+xml")
            .get()
            .build()
        try {
            http.newCall(req).execute().use { r ->
                if (r.code == 403 || r.code == 503) {
                    return@withContext PageResult.Failed(
                        "The site only lets a full browser in (HTTP ${r.code}). Open it in the browser.",
                    )
                }
                if (!r.isSuccessful) {
                    return@withContext PageResult.Failed("The page did not load (HTTP ${r.code}).")
                }
                val body = r.body ?: return@withContext PageResult.Failed("The page did not load.")
                val source = body.source()
                source.request(PageExtractor.PAGE_MAX.toLong())
                val page = source.buffer.readString(
                    minOf(source.buffer.size, PageExtractor.PAGE_MAX.toLong()),
                    body.contentType()?.charset() ?: Charsets.UTF_8,
                )
                val html = PageExtractor.extract(page, r.request.url.toString())
                if (html.isBlank()) {
                    PageResult.Failed("No article text was found on the page. Open it in the browser.")
                } else {
                    PageResult.Ok(html)
                }
            }
        } catch (e: Exception) {
            PageResult.Failed("The page did not load (${e.message ?: "network error"}).")
        }
    }

    companion object {
        private const val PAGE_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Mobile Safari/537.36"
    }
}
