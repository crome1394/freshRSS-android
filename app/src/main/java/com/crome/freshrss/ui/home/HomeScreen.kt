package com.crome.freshrss.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.VerticalDivider
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import android.widget.Toast
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.VpnKeyOff
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import com.crome.freshrss.util.Tailscale
import androidx.compose.foundation.shape.RoundedCornerShape
import coil.compose.AsyncImage
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import com.crome.freshrss.ui.theme.CardShape
import com.crome.freshrss.ui.theme.PillShape
import com.crome.freshrss.ui.theme.canvas
import com.crome.freshrss.ui.theme.canvasBackground
import com.crome.freshrss.ui.theme.cardSurface
import com.crome.freshrss.ui.theme.LocalLook
import com.crome.freshrss.ui.theme.card
import com.crome.freshrss.ui.theme.ink
import com.crome.freshrss.ui.theme.searchPill
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.DoneAll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.crome.freshrss.BuildConfig
import com.crome.freshrss.data.model.Article
import com.crome.freshrss.data.model.ClientMode
import com.crome.freshrss.data.model.ReadScope
import com.crome.freshrss.ui.article.ArticleContent
import com.crome.freshrss.ui.article.ArticleEmptyPane
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Width ≥ this (dp) → list + reading pane (tablet landscape). */
const val DUAL_PANE_MIN_WIDTH_DP = 840

/** Widest the list and article text get on big screens (dp). */
const val MAX_CONTENT_WIDTH_DP = 720

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    vm: HomeViewModel,
    onOpenSettings: () -> Unit,
    onOpenArticle: (String) -> Unit,
    /** When true (wide window), show list + article side by side. */
    dualPane: Boolean = false,
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val listScope = rememberCoroutineScope()

    val scheme = MaterialTheme.colorScheme
    val ink = scheme.ink()

    Scaffold(
        modifier = Modifier.canvasBackground(),
        containerColor = Color.Transparent,
        contentColor = ink,
        topBar = {
            SearchPill(
                query = state.searchQuery,
                onQuery = vm::setSearch,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 4.dp),
            )
        },
        bottomBar = {
            HomeFooter(
                state = state,
                onFilters = vm::toggleFiltersExpanded,
                onCollapseExpand = {
                    if (state.collapsed.isEmpty()) vm.collapseAll() else vm.expandAll()
                },
                onRefresh = { vm.refresh() },
                onSettings = onOpenSettings,
                onTailscaleConnected = { vm.refresh() },
            )
        },
    ) { padding ->
        val scopeChips: @Composable () -> Unit = {
            ScopeChips(
                selected = state.scope,
                onSelect = vm::setScope,
                mediaFilter = state.mediaFilter,
                onMediaFilter = vm::setMediaFilter,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
            )
        }

        // Filters (footer funnel): date window and download size. Search is the top pill.
        val filtersPanel: @Composable () -> Unit = {
            AnimatedVisibility(visible = state.filtersExpanded) {
                Column(Modifier.padding(vertical = 4.dp)) {
                    DateFilterChips(
                        selected = state.dateFilter,
                        onSelect = vm::setDateFilter,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                    )
                    if (state.historyDays == 0) {
                        DownloadLimitBar(
                            scope = state.scope,
                            itemLimit = state.itemLimit,
                            perFeedLimit = state.perFeedLimit,
                            enabled = true,
                            onNudgePerFeed = vm::nudgePerFeed,
                            onNudgeItemLimit = vm::nudgeItemLimit,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    } else {
                        Text(
                            "Downloading the last ${state.historyDays} days (Settings).",
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }

        // Opening a feed or day scrolls it to the top of the list. Rows above
        // it do not change when it expands, so its current index stays right.
        val scrollToRow: (ListRow) -> Unit = { row ->
            val idx = state.listRows.indexOf(row)
            if (idx >= 0) listScope.launch { listState.animateScrollToItem(idx) }
        }

        val listColumn: @Composable (Modifier) -> Unit = { listMod ->
            Column(listMod) {
                Text(
                    state.activeChipLabel,
                    modifier = Modifier.padding(start = 20.dp, top = 8.dp, end = 16.dp, bottom = 2.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = ink,
                )
                if (!state.scopeChipsAtBottom) {
                    scopeChips()
                }

                if (!state.filtersAtBottom) {
                    filtersPanel()
                }

                if (state.error != null) {
                    Text(
                        text = state.error.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                PullToRefreshBox(
                    isRefreshing = state.loading,
                    onRefresh = { vm.refresh() },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    if (state.loading && state.items.isEmpty() && state.knownFeeds.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (state.listRows.isEmpty() && !state.loading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "No articles. Pull to refresh or check Settings.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(24.dp),
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(
                                items = state.listRows,
                                key = { row ->
                                    when (row) {
                                        is ListRow.Header -> "h:${row.category}"
                                        is ListRow.DateHeader -> "d:${row.category}\u001f${row.dateKey}"
                                        is ListRow.Item -> "i:${row.article.id}"
                                        is ListRow.EmptyFeed -> "e:${row.category}"
                                    }
                                },
                            ) { row ->
                                when (row) {
                                    is ListRow.Header -> FeedHeaderRow(
                                        row = row,
                                        onClick = {
                                            if (row.collapsed) scrollToRow(row)
                                            vm.toggleCategory(row.category)
                                        },
                                        onToggleRead = {
                                            vm.toggleFeedRead(row.category, row.feedId)
                                        },
                                    )
                                    is ListRow.DateHeader -> DateHeaderRow(
                                        row = row,
                                        onClick = {
                                            if (row.collapsed) scrollToRow(row)
                                            vm.toggleDateGroup(row.category, row.dateKey)
                                        },
                                        onLongClick = { vm.toggleDayRead(row.category, row.dateKey) },
                                    )
                                    is ListRow.Item -> SwipeableArticleRow(
                                        article = row.article,
                                        selected = dualPane &&
                                            row.article.id == state.selectedArticleId,
                                        onClick = {
                                            if (dualPane) {
                                                vm.selectArticle(row.article.id)
                                            } else {
                                                onOpenArticle(row.article.id)
                                            }
                                        },
                                        onToggleStar = {
                                            if (state.writable) vm.toggleStar(row.article.id)
                                        },
                                        onMarkRead = { vm.markRead(row.article.id) },
                                        onMarkUnread = { vm.markUnread(row.article.id) },
                                    )
                                    is ListRow.EmptyFeed -> Text(
                                        text = when (state.scope) {
                                            ReadScope.UNREAD -> "No unread in this feed — try All"
                                            ReadScope.SAVED -> "No starred items"
                                            ReadScope.READ -> "No read items loaded"
                                            ReadScope.ALL -> "No articles returned for this feed"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(
                                            start = 28.dp,
                                            end = 16.dp,
                                            top = 4.dp,
                                            bottom = 8.dp,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom docking order: article list → Filters panel → scope chips → footer
                if (state.filtersAtBottom) {
                    filtersPanel()
                }
                if (state.scopeChipsAtBottom) {
                    scopeChips()
                }
            }
        }

        if (dualPane) {
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                listColumn(
                    Modifier
                        .weight(0.38f)
                        .fillMaxHeight(),
                )
                VerticalDivider(Modifier.fillMaxHeight())
                Box(
                    Modifier
                        .weight(0.62f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surface),
                ) {
                    val selected = state.selectedArticleId?.let { id ->
                        state.items.firstOrNull { it.id == id }
                    }
                    if (selected != null) {
                        ArticleContent(
                            article = selected,
                            writable = state.writable,
                            onToggleStar = { vm.toggleStar(selected.id) },
                            onToggleRead = { vm.toggleRead(selected.id) },
                            showInlineActions = true,
                            webPage = state.wantsWebPage(selected),
                            page = state.pages[selected.id],
                            alwaysWebPage = selected.displayCategory in state.webPageFeeds,
                            onToggleWebPage = { vm.toggleWebPage(selected.id) },
                            onAlwaysWebPage = { vm.setWebPageForFeed(selected.id, it) },
                        )
                    } else {
                        ArticleEmptyPane()
                    }
                }
            }
        } else {
            // Portrait tablets, Fold cover screens, DeX windows: keep cards and
            // text at a readable width, centered.
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.TopCenter,
            ) {
                listColumn(
                    Modifier
                        .fillMaxHeight()
                        .widthIn(max = MAX_CONTENT_WIDTH_DP.dp)
                        .fillMaxWidth(),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableArticleRow(
    article: Article,
    selected: Boolean = false,
    onClick: () -> Unit,
    onToggleStar: () -> Unit,
    onMarkRead: () -> Unit,
    onMarkUnread: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    // Swipe left → mark read
                    if (!article.isRead) onMarkRead()
                    false // don't dismiss the row
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    // Swipe right → mark unread
                    if (article.isRead) onMarkUnread()
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        },
    )

    // Reset visual position after action
    LaunchedEffect(dismissState.currentValue, article.id, article.isRead) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    SwipeToDismissBox(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 8.dp, top = 3.dp, bottom = 3.dp)
            .cardSurface(elevation = 4.dp),
        state = dismissState,
        backgroundContent = {
            val dir = dismissState.dismissDirection
            val color = when (dir) {
                SwipeToDismissBoxValue.EndToStart -> Color(0xFF2E7D32) // mark read
                SwipeToDismissBoxValue.StartToEnd -> Color(0xFF1565C0) // mark unread
                else -> Color.Transparent
            }
            val icon = when (dir) {
                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Done
                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.MarkEmailUnread
                else -> null
            }
            val align = when (dir) {
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                else -> Alignment.Center
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = align,
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = Color.White)
                }
            }
        },
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
    ) {
        ArticleRow(
            article = article,
            selected = selected,
            onClick = onClick,
            onToggleStar = onToggleStar,
        )
    }
}

@Composable
private fun DownloadLimitBar(
    scope: ReadScope,
    itemLimit: Int,
    perFeedLimit: Int,
    enabled: Boolean = true,
    onNudgePerFeed: (Int) -> Unit,
    onNudgeItemLimit: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val perFeedMode = scope == ReadScope.ALL || scope == ReadScope.READ
    val label = if (perFeedMode) "Per feed" else "Max articles"
    val value = if (perFeedMode) perFeedLimit else itemLimit
    val onMinus = { if (perFeedMode) onNudgePerFeed(-1) else onNudgeItemLimit(-1) }
    val onPlus = { if (perFeedMode) onNudgePerFeed(+1) else onNudgeItemLimit(+1) }
    val muted = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.45f)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Download",
                style = MaterialTheme.typography.labelMedium,
                color = muted,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = muted,
                    modifier = Modifier.padding(end = 4.dp),
                )
                IconButton(onClick = onMinus, modifier = Modifier.width(36.dp), enabled = enabled) {
                    Icon(Icons.Default.Remove, contentDescription = "Fewer articles")
                }
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        muted
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(min = 36.dp),
                )
                IconButton(onClick = onPlus, modifier = Modifier.width(36.dp), enabled = enabled) {
                    Icon(Icons.Default.Add, contentDescription = "More articles")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScopeChips(
    selected: ReadScope,
    onSelect: (ReadScope) -> Unit,
    mediaFilter: MediaFilter,
    onMediaFilter: (MediaFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val order = listOf(ReadScope.UNREAD, ReadScope.ALL, ReadScope.READ, ReadScope.SAVED)
    val chipIconMod = Modifier.height(18.dp)
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (scope in order) {
            FilterChip(
                selected = selected == scope,
                onClick = { onSelect(scope) },
                label = {
                    when (scope) {
                        // Filled bullet = unread items (not an email glyph).
                        ReadScope.UNREAD -> Icon(
                            Icons.Default.FiberManualRecord,
                            contentDescription = "Unread",
                            modifier = chipIconMod,
                        )
                        ReadScope.ALL -> Text("All")
                        // Check circle = already read (not an email glyph).
                        ReadScope.READ -> Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Read",
                            modifier = chipIconMod,
                        )
                        ReadScope.SAVED -> Icon(
                            Icons.Default.Star,
                            contentDescription = "Starred",
                            modifier = chipIconMod,
                        )
                    }
                },
            )
        }
        // Icon-only media filters (client-side) sit next to Starred.
        FilterChip(
            selected = mediaFilter == MediaFilter.VIDEO,
            onClick = { onMediaFilter(MediaFilter.VIDEO) },
            label = {
                Icon(
                    Icons.Default.Videocam,
                    contentDescription = "Videos",
                    modifier = chipIconMod,
                )
            },
        )
        FilterChip(
            selected = mediaFilter == MediaFilter.SOUND,
            onClick = { onMediaFilter(MediaFilter.SOUND) },
            label = {
                Icon(
                    Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Sound",
                    modifier = chipIconMod,
                )
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DateFilterChips(
    selected: DateFilter,
    onSelect: (DateFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (f in DateFilter.entries) {
            FilterChip(
                selected = selected == f,
                onClick = { onSelect(f) },
                label = { Text(f.label) },
            )
        }
    }
}

/**
 * A feed as a card: round site icon, name, newest article as a preview line,
 * its time and an unread dot, and the read ↔ unread button where BlurtText
 * has its pin.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FeedHeaderRow(
    row: ListRow.Header,
    onClick: () -> Unit,
    /** All read ↔ all unread (also on long-press). */
    onToggleRead: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val hasUnread = row.unread > 0
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .cardSurface()
            .combinedClickable(onClick = onClick, onLongClick = onToggleRead)
            .padding(start = 14.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FeedIcon(title = row.category, iconUrl = row.iconUrl, size = 44.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = row.category,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.ink(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (row.latestEpoch > 0) {
                    Text(
                        formatShortTime(row.latestEpoch),
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                if (hasUnread) UnreadDot(Modifier.padding(start = 6.dp))
            }
            val preview = row.latestTitle.ifBlank { "No articles loaded" }
            Text(
                text = preview,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (hasUnread) FontWeight.Medium else FontWeight.Normal,
                color = if (hasUnread) scheme.ink() else scheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
            Text(
                text = buildString {
                    if (hasUnread) append("${row.unread} unread · ")
                    append("${row.shown} shown")
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (hasUnread) scheme.primary else scheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        IconButton(onClick = onToggleRead, modifier = Modifier.size(40.dp)) {
            Icon(
                if (hasUnread) Icons.Default.DoneAll else Icons.Default.MarkEmailUnread,
                contentDescription = if (hasUnread) "Mark feed read" else "Mark feed unread",
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/** Day heading inside an open feed, like BlurtText's "Unread" / "Read" headings. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DateHeaderRow(
    row: ListRow.DateHeader,
    onClick: () -> Unit,
    /** Long-press: the day's articles all read ↔ all unread. */
    onLongClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(start = 28.dp, end = 16.dp, top = 10.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = row.dateLabel,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = scheme.ink(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = if (row.unread > 0) "${row.unread} / ${row.shown}" else "${row.shown}",
            color = if (row.unread > 0) scheme.primary else scheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
        )
        Icon(
            if (row.collapsed) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
            contentDescription = if (row.collapsed) "Show day" else "Hide day",
            tint = scheme.onSurfaceVariant,
            modifier = Modifier
                .padding(start = 4.dp)
                .size(18.dp),
        )
    }
}

/** One article as a card: title, time and unread dot, preview, star. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ArticleRow(
    article: Article,
    selected: Boolean = false,
    onClick: () -> Unit,
    onToggleStar: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    // The card surface comes from the swipe box (cardSurface); only selection paints here.
    val bg = if (selected) scheme.primaryContainer else Color.Transparent
    Row(
        Modifier
            .fillMaxWidth()
            .background(bg)
            .combinedClickable(onClick = onClick, onLongClick = onToggleStar)
            .padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = article.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (article.isRead) FontWeight.Normal else FontWeight.SemiBold,
                    color = if (article.isRead) scheme.onSurfaceVariant else scheme.ink(),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    formatShortTime(article.createdOnTime),
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp),
                )
                if (!article.isRead) UnreadDot(Modifier.padding(start = 6.dp, top = 7.dp))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp),
            ) {
                if (article.isVideo || article.isAudio) {
                    Icon(
                        if (article.isVideo) Icons.Default.Videocam else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = if (article.isVideo) "Video" else "Audio",
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(16.dp),
                        tint = scheme.secondary,
                    )
                }
                if (article.summary.isNotBlank()) {
                    Text(
                        text = article.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        IconButton(onClick = onToggleStar, modifier = Modifier.size(40.dp)) {
            Icon(
                if (article.isSaved) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = if (article.isSaved) "Unstar" else "Star",
                tint = if (article.isSaved) scheme.secondary else scheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun UnreadDot(modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
    )
}

/** Pill search field at the top, like BlurtText's "Search messages". */
@Composable
private fun SearchPill(
    query: String,
    onQuery: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val focus = LocalFocusManager.current
    Surface(
        shape = PillShape,
        color = scheme.searchPill(),
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
    ) {
        Row(
            Modifier.padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = scheme.onSurfaceVariant)
            Box(
                Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (query.isEmpty()) {
                    Text(
                        "Search articles",
                        color = scheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQuery,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = scheme.ink()),
                    cursorBrush = SolidColor(scheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (query.isNotEmpty()) {
                IconButton(onClick = {
                    onQuery("")
                    focus.clearFocus()
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear search", tint = scheme.onSurfaceVariant)
                }
            }
        }
    }
}

/**
 * Footer like BlurtText's: unread count and status on the left, actions on
 * the right (Filters, collapse/expand, Tailscale, refresh, settings).
 */
@Composable
private fun HomeFooter(
    state: HomeUiState,
    onFilters: () -> Unit,
    onCollapseExpand: () -> Unit,
    onRefresh: () -> Unit,
    onSettings: () -> Unit,
    onTailscaleConnected: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        Modifier
            .background(if (LocalLook.current.glass) scheme.card().copy(alpha = 0.5f) else scheme.canvas())
            .navigationBarsPadding(),
    ) {
        HorizontalDivider(color = scheme.outlineVariant)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    if (state.unread > 0) "${state.unread} unread" else "All read",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.ink(),
                    maxLines = 1,
                )
                val caption = buildString {
                    if (state.tailscaleConnecting) {
                        append("Connecting Tailscale…")
                    } else if (state.loading) {
                        append("Updating…")
                    } else if (state.lastUpdatedEpochMs > 0) {
                        append("Updated ${formatLastUpdated(state.lastUpdatedEpochMs)}")
                    }
                    if (state.isOffline) append(" · offline")
                    if (state.dateFilter != DateFilter.ALL) append(" · ${state.dateFilter.label}")
                }
                if (caption.isNotBlank()) {
                    Text(
                        caption,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (state.isOffline) scheme.error else scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            IconButton(onClick = onFilters) {
                Icon(
                    Icons.Default.FilterAlt,
                    contentDescription = if (state.filtersExpanded) "Hide filters" else "Filters",
                    tint = if (state.filtersExpanded || state.dateFilter != DateFilter.ALL) {
                        scheme.primary
                    } else {
                        scheme.ink()
                    },
                )
            }
            IconButton(onClick = onCollapseExpand) {
                Icon(
                    if (state.collapsed.isEmpty()) Icons.Default.UnfoldLess else Icons.Default.UnfoldMore,
                    contentDescription = if (state.collapsed.isEmpty()) "Collapse all" else "Expand all",
                    tint = scheme.ink(),
                )
            }
            if (state.showTailscaleButton) {
                TailscaleButton(onConnected = onTailscaleConnected)
            }
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = scheme.ink())
            }
            IconButton(onClick = onSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = scheme.ink())
            }
        }
    }
}

/** Time today, "Yesterday", or a short date — like a message list. */
private fun formatShortTime(epoch: Long): String {
    if (epoch <= 0) return ""
    val key = dateKeyForEpoch(epoch)
    val today = dateKeyForEpoch(System.currentTimeMillis() / 1000)
    val yesterday = dateKeyForEpoch(System.currentTimeMillis() / 1000 - 86_400)
    val date = Date(epoch * 1000)
    return when (key) {
        today -> java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(date)
        yesterday -> "Yesterday"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
    }
}

private fun formatTime(epoch: Long): String {
    if (epoch <= 0) return ""
    val fmt = SimpleDateFormat("MMM d  HH:mm", Locale.getDefault())
    return fmt.format(Date(epoch * 1000))
}

private fun formatLastUpdated(epochMs: Long): String {
    if (epochMs <= 0) return ""
    val fmt = SimpleDateFormat("MMM d  HH:mm", Locale.getDefault())
    return fmt.format(Date(epochMs))
}

/**
 * Key button: tap connects or disconnects Tailscale, long-press opens the app.
 * Bright key = VPN up, crossed-out key = down. Reloads the list once the VPN
 * comes up after a tap, since the server is usually only reachable through it.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TailscaleButton(onConnected: () -> Unit) {
    val context = LocalContext.current
    val vpnFlow = remember { Tailscale.vpnActiveFlow(context.applicationContext) }
    val vpnActive by vpnFlow.collectAsStateWithLifecycle(
        initialValue = remember { Tailscale.isVpnActive(context) },
    )
    var waitingForConnect by remember { mutableStateOf(false) }
    LaunchedEffect(vpnActive) {
        if (vpnActive && waitingForConnect) {
            waitingForConnect = false
            onConnected()
        }
    }
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .combinedClickable(
                onClickLabel = if (vpnActive) "Disconnect Tailscale" else "Connect Tailscale",
                onLongClickLabel = "Open Tailscale",
                onClick = {
                    val connect = !vpnActive
                    if (Tailscale.setConnected(context, connect)) {
                        waitingForConnect = connect
                        Toast.makeText(
                            context,
                            if (connect) "Connecting Tailscale…" else "Disconnecting Tailscale…",
                            Toast.LENGTH_SHORT,
                        ).show()
                    } else {
                        Toast.makeText(context, "Tailscale not installed", Toast.LENGTH_SHORT).show()
                    }
                },
                onLongClick = {
                    if (!Tailscale.open(context)) {
                        Toast.makeText(context, "Tailscale not installed", Toast.LENGTH_SHORT).show()
                    }
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (vpnActive) Icons.Default.VpnKey else Icons.Default.VpnKeyOff,
            contentDescription = if (vpnActive) "Tailscale connected" else "Tailscale disconnected",
            tint = if (vpnActive) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.ink()
            },
        )
    }
}

/** Site icon from the FreshRSS server, or a letter badge when there is none. */
@Composable
private fun FeedIcon(title: String, iconUrl: String, size: Dp = 22.dp) {
    var failed by remember(iconUrl) { mutableStateOf(false) }
    val mod = Modifier
        .size(size)
        .clip(CircleShape)
    if (iconUrl.isNotBlank() && !failed) {
        // Favicons are small and often transparent: center them on a light disc.
        Box(mod.background(Color.White), contentAlignment = Alignment.Center) {
            AsyncImage(
                model = iconUrl,
                contentDescription = null,
                onError = { failed = true },
                modifier = Modifier.size(size * 0.62f),
            )
        }
    } else {
        Box(
            mod.background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = title.trim().firstOrNull { it.isLetterOrDigit() }?.uppercase() ?: "•",
                style = if (size >= 36.dp) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}
