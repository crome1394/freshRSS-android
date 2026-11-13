package com.crome.freshrss.ui.article

import android.content.Context
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Checkbox
import com.crome.freshrss.ui.home.PageState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crome.freshrss.data.model.Article
import com.crome.freshrss.util.MediaUtils
import com.crome.freshrss.util.SafeUrls
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun shareArticle(context: Context, title: String, url: String) {
    // Only include the URL in the share body if it is a safe http(s) link.
    val safeUrl = SafeUrls.normalizeHttpUrl(url).orEmpty()
    val text = buildString {
        if (title.isNotBlank()) append(title)
        if (safeUrl.isNotBlank()) {
            if (isNotEmpty()) append("\n")
            append(safeUrl)
        }
    }
    if (text.isBlank()) return
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, "Share article"))
}

/**
 * Shared article body used by full-screen [ArticleScreen] (phone) and the
 * tablet dual-pane reading column.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArticleContent(
    article: Article,
    writable: Boolean,
    onToggleStar: () -> Unit,
    /** Read ↔ unread. */
    onToggleRead: () -> Unit,
    modifier: Modifier = Modifier,
    /** Show the article's web page instead of the feed text. */
    webPage: Boolean = false,
    page: PageState? = null,
    /** This feed always opens as the web page (saved). */
    alwaysWebPage: Boolean = false,
    onToggleWebPage: () -> Unit = {},
    onAlwaysWebPage: (Boolean) -> Unit = {},
    /** When true, show star/mark-read in a top action row (tablet pane). */
    showInlineActions: Boolean = false,
) {
    val context = LocalContext.current
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        if (showInlineActions) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    article.feedTitle.ifBlank { "Article" },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                )
                IconButton(
                    onClick = { shareArticle(context, article.title, article.url) },
                    enabled = article.url.isNotBlank() || article.title.isNotBlank(),
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
                if (writable) {
                    IconButton(onClick = onToggleStar) {
                        Icon(
                            if (article.isSaved) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Star",
                        )
                    }
                    ReadToggleButton(isRead = article.isRead, onClick = onToggleRead)
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        Text(
            article.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            buildString {
                if (article.author.isNotBlank()) append(article.author).append(" · ")
                if (article.createdOnTime > 0) {
                    append(
                        SimpleDateFormat("EEE, MMM d  HH:mm", Locale.getDefault())
                            .format(Date(article.createdOnTime * 1000)),
                    )
                }
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))

        val browserUrl = article.url
        val playUrl = MediaUtils.playableUrl(article.mediaUrl, article.url)
        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilledTonalButton(
                onClick = { SafeUrls.openInBrowser(context, browserUrl) },
                enabled = SafeUrls.isSafeHttpUrl(browserUrl),
            ) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                Text("  Browser", modifier = Modifier.padding(start = 4.dp))
            }
            FilledTonalButton(
                onClick = { shareArticle(context, article.title, article.url) },
                enabled = article.title.isNotBlank() || SafeUrls.isSafeHttpUrl(article.url),
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Text("  Share", modifier = Modifier.padding(start = 4.dp))
            }
            FilledTonalButton(
                onClick = { copyLink(context, browserUrl) },
                enabled = SafeUrls.isSafeHttpUrl(browserUrl),
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null)
                Text("  Copy link", modifier = Modifier.padding(start = 4.dp))
            }
            FilledTonalButton(
                onClick = onToggleWebPage,
                enabled = SafeUrls.isSafeHttpUrl(browserUrl),
            ) {
                Icon(
                    if (webPage) Icons.AutoMirrored.Filled.Article else Icons.Default.Language,
                    contentDescription = null,
                )
                Text(
                    if (webPage) "  Feed text" else "  Web page",
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
            if (article.isVideo || article.isAudio) {
                FilledTonalButton(
                    onClick = { SafeUrls.openWithChooser(context, playUrl, title = "Open with") },
                    enabled = SafeUrls.isSafeHttpUrl(playUrl),
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Text(
                        if (article.isAudio && !article.isVideo) "  Listen" else "  Play",
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        if (article.leadImage.isNotBlank()) {
            LeadPicture(
                imageUrl = article.leadImage,
                isMedia = article.isVideo || article.isAudio,
                onClick = {
                    if (article.isVideo || article.isAudio) {
                        SafeUrls.openWithChooser(context, playUrl, title = "Open with")
                    } else {
                        SafeUrls.openInBrowser(context, browserUrl)
                    }
                },
            )
        }

        if (webPage || alwaysWebPage) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onAlwaysWebPage(!alwaysWebPage) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(checked = alwaysWebPage, onCheckedChange = onAlwaysWebPage)
                Text("Always web page for this feed", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(8.dp))
        }

        val pageNote = when {
            !webPage -> null
            page is PageState.Loading || page == null -> "Loading the web page…"
            page is PageState.Failed -> page.message + " Showing the feed text."
            else -> null
        }
        if (pageNote != null) {
            Text(
                pageNote,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }
        val pageHtml = (page as? PageState.Ready)?.html.takeIf { webPage }
        ArticleBody(article = article, html = pageHtml ?: article.html)
    }
}

@Composable
fun ArticleEmptyPane(modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Select an article",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Choose a feed and article from the list",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Done icon marks read; on a read article the envelope marks it unread again. */
@Composable
fun ReadToggleButton(isRead: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        if (isRead) {
            Icon(Icons.Default.MarkEmailUnread, contentDescription = "Mark unread")
        } else {
            Icon(Icons.Default.Done, contentDescription = "Mark read")
        }
    }
}

fun copyLink(context: Context, url: String) {
    val safe = SafeUrls.normalizeHttpUrl(url) ?: return
    val cm = context.getSystemService(ClipboardManager::class.java) ?: return
    cm.setPrimaryClip(ClipData.newPlainText("Article link", safe))
    // Android 13+ shows its own clipboard confirmation.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        Toast.makeText(context, "Link copied", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Article text from its cleaned HTML: paragraphs, headings, lists, and links
 * (images are `[image]` links). Links open through [SafeUrls] (http/https only).
 * Falls back to the plain text when there is no HTML.
 */
@Composable
private fun ArticleBody(article: Article, html: String) {
    val context = LocalContext.current
    val linkColor = MaterialTheme.colorScheme.primary
    if (html.isBlank()) {
        Text(
            article.text.ifBlank { article.summary }.ifBlank { "(no content — open in browser)" },
            style = MaterialTheme.typography.bodyLarge,
        )
        return
    }
    val annotated = remember(html, linkColor) {
        AnnotatedString.fromHtml(
            htmlString = html,
            linkStyles = TextLinkStyles(
                style = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline),
            ),
            linkInteractionListener = { link ->
                if (link is LinkAnnotation.Url) SafeUrls.openInBrowser(context, link.url)
            },
        )
    }
    Text(
        annotated,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

/**
 * The article's lead picture (YouTube still frame or first image). Hidden
 * until it loads, and hidden for good if it fails. Tap: play or open.
 */
@Composable
private fun LeadPicture(imageUrl: String, isMedia: Boolean, onClick: () -> Unit) {
    var failed by remember(imageUrl) { mutableStateOf(false) }
    var loaded by remember(imageUrl) { mutableStateOf(false) }
    if (failed || !SafeUrls.isSafeHttpUrl(imageUrl)) return
    Box(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = if (isMedia) "Play" else "Open article",
            contentScale = ContentScale.FillWidth,
            onSuccess = { loaded = true },
            onError = { failed = true },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 360.dp),
        )
        if (isMedia && loaded) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.55f),
                modifier = Modifier.size(64.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp),
                    )
                }
            }
        }
    }
}
