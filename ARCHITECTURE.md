# Architecture

A single-module Jetpack Compose app. One `Application` holds the settings and
the FreshRSS client; one `HomeViewModel` holds the reader state shared by the
list, the article screen, and the tablet reading pane.

```
MainActivity ── FreshRssTheme(mode, palette) ── FreshRssNav
                                                  ├─ home     HomeScreen ─┐
                                                  ├─ article  ArticleScreen ├─ HomeViewModel
                                                  ├─ settings SettingsScreen ── SettingsViewModel
                                                  └─ setup    (first run)
HomeViewModel ── FreshRssClient (Fever + Google Reader APIs) ── your FreshRSS server
              ├─ PageFetcher (web page view) ── article sites
              ├─ OfflineCache (last list, JSON in app storage)
              └─ FreshRssWidgetUpdater ── home-screen widget
```

## Packages

`app/src/main/java/com/crome/freshrss/`

| Package | Files | Role |
|---|---|---|
| (root) | `FreshRssApp`, `MainActivity` | Creates `SettingsRepository` and `FreshRssClientImpl`; applies the theme |
| `data/model` | `Models.kt` | `Article`, `FeedRef`, `ReadScope`, results, `ReaderDefaults` |
| `data/remote` | `FreshRssClient(Impl)`, `GReaderAuth`, `HttpSupport`, `PageFetcher` | All network: FreshRSS APIs and article pages (OkHttp) |
| `data/prefs` | `SettingsRepository` | DataStore preferences (server, layout, theme, web-page feeds) |
| `data/secure` | `SecureSecrets`, `AuthTokenStore` | API password and GReader token in Keystore-backed encrypted prefs |
| `data/offline` | `OfflineCache` | Last loaded list, shown at start and when the server is unreachable |
| `ui/home` | `HomeScreen`, `HomeViewModel` | Feed → day → article list, filters, actions, dual pane |
| `ui/article` | `ArticleScreen`, `ArticleContent` | Article view: lead picture, HTML body, web page view, buttons |
| `ui/settings` | `SettingsScreen`, `SettingsViewModel` | Server, security, appearance, layout |
| `ui/theme` | `Theme.kt`, `Palettes.kt` | Light/dark mode and color palettes |
| `util` | `HtmlUtils`, `PageExtractor`, `MediaUtils`, `MediaAppLinks`, `SafeUrls`, `ServerUrl`, `Tailscale`, `FeverAuth` | Pure helpers (most have unit tests) |
| `widget` | Glance widget, updater, state store | Unread count + last updated; pushed by the app, no polling |

## Talking to FreshRSS

The client mirrors the old quickshell `freshrss-api.sh` command set:

| Need | API |
|---|---|
| Unread count, per-feed counts, feed icons | Google Reader `unread-count` + `subscription/list` |
| **All** / **Read** lists | Google Reader `stream/contents/feed/N`, one request per feed, 12 in parallel |
| **Unread** / **Starred** lists | Fever `unread_item_ids` / `saved_item_ids`, then `items&with_ids` |
| Mark read/unread, star | Fever `mark=item` |
| Mark feed read / unread | Google Reader `mark-all-as-read` / `edit-tag` |
| No API password | Public RSS `/i/?a=rss` (read-only) |

The GReader auth token is cached in encrypted storage and refreshed once on failure.

## Article HTML

Ported from the quickshell reader (`services/FreshRssService.qml`, `freshrss/PageReader.qml`):

1. **Ingest** (`HtmlUtils`): `leadImage()` picks a YouTube still frame or the
   first `<img>`. `cleanHtml()` strips scripts, frames, forms, styles, and event
   attributes, and turns every image into an `[image]` link so the body loads
   nothing from the network. `readerHtml()` removes the lead picture's own link
   and caps the size (60 kB). Plain text (4 kB) is kept for search.
2. **Display** (`ArticleContent`): `AnnotatedString.fromHtml` renders
   paragraphs, headings, lists, and links. Links open via `SafeUrls` (http/https only).
   Coil loads the one lead picture.
3. **Web page view** (`PageFetcher` + `PageExtractor`): fetches the article URL,
   drops `head/nav/header/footer/aside/form` and comment/share/related blocks,
   keeps the `<article>` / `<main>` / post-content element with the most paragraph
   text (else all paragraphs ≥ 40 chars), keeps only text tags, makes links
   absolute, then runs `cleanHtml()`. One fetch at a time; the last 20 pages stay in memory.
   Feeds set to "Always web page" are stored by feed title.

## State

`HomeUiState` is one immutable data class in a `StateFlow`. `listRows` and
`filtered` are computed from it, so the scope, media, date, and search filters
never get out of step with the counts. Read/unread and star changes are applied
optimistically, sent to the server, and reverted on failure. Each change also
updates the offline cache and the widget.

## Tailscale

`util/Tailscale.kt` sends `com.tailscale.ipn.CONNECT_VPN` / `DISCONNECT_VPN`,
first to Tailscale's `AutomationActivity` (newer versions, wakes a stopped app),
then to `IPNReceiver` as a broadcast (older versions). A `ConnectivityManager`
callback on VPN transport drives the icon. The manifest declares
`<queries><package android:name="com.tailscale.ipn"/>` so the app can see Tailscale.

## Tests

`./gradlew :app:testDebugUnitTest`. JVM unit tests cover the helpers in `util/`
(HTML cleaning, lead picture, page extraction, URLs, media mapping, Fever auth)
and the date filter.
