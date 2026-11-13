# Features

## List

| Feature | How |
|---|---|
| Scopes | Chips: ● Unread · All · ✓ Read · ★ Starred |
| Media filters | 🎥 Videos · 🔊 Sound (tap again to clear) |
| Date filter | Filters → All · Today · -1 · -7 · -14 · -21 |
| Search | Pill at the top: title, feed, author, article text |
| Grouping | Feed cards A–Z (site icon, newest article, unread dot) → days → article cards |
| Open a feed or day | Tap the header. It scrolls to the top of the list |
| Feed all read ↔ unread | Button on the feed header, or long-press the header |
| Day all read ↔ unread | Long-press the day header |
| Article read / unread | Swipe left = read, swipe right = unread |
| Star | Star button on the row, or long-press the row |
| Collapse / expand all | ⇕ in the footer |
| Date and download filters | Funnel in the footer |
| Download size | Settings → Reading → History days (default 30), or the per feed / max articles limits when history is 0 |

## Article

| Feature | How |
|---|---|
| Text | Full article with paragraphs and links. Images show as `[image]` links, so nothing loads unless you tap it |
| Lead picture | YouTube still frame or the article's first image, shown above the text. Tap to play or open |
| Web page | **Web page** fetches the article's page and shows its main text (no comments, share bars, or scripts). **Feed text** goes back |
| Always web page | Tick **Always web page for this feed** to open every article of that feed this way (saved) |
| Browser · Share · Copy link | Buttons under the title |
| Play / Listen | Opens an **Open with** chooser that prefers YouTube, NewPipe, Spotify, SoundCloud, Vimeo, Twitch |
| Read ↔ unread | ✓ / ✉ in the top bar (opening an article marks it read) |

Sites behind a browser check (for example Cloudflare) refuse the web page fetch. Use **Browser** for those.

## Footer

The left side shows the unread count and when the list last updated (or "Connecting Tailscale…", "offline").

| Button | Tap | Long-press |
|---|---|---|
| Funnel | Show / hide the date and download filters | — |
| ⇕ | Collapse / expand all feeds | — |
| 🔑 Tailscale | Connect / disconnect the VPN. Lit key = connected | Open the Tailscale app |
| ⟳ Refresh | Reload from the server | — |
| ⚙ Settings | Open the settings sheet | — |

The list reloads by itself once Tailscale connects.

## Tailscale on mobile data

With **Settings → Tailscale → Use Tailscale on mobile data** on (default), refreshing or marking
articles off Wi-Fi connects Tailscale first, waits for it (up to 15 s), and disconnects again
after 30 seconds with no server calls. If you connected Tailscale yourself, it is left alone.

## Settings

The ⚙ button opens a sheet over the list. Drag its handle to resize it (half ↔ full height) or
down to close. Swipe sideways between tabs; **Test** and **Save** stay at the top.

| Tab | Settings |
|---|---|
| Server | URL, user, API password, allow insecure HTTP |
| Reading | History days, download limits, open Filters on start |
| Look | Theme, colors, where the scope chips and filters sit |
| Tailscale | Show the button, use Tailscale on mobile data |

## Appearance

Settings → Look:

| Setting | Options |
|---|---|
| Theme | System · Light · Dark |
| Colors | Tokyo Night · Catppuccin · Nord · Gruvbox · Rosé Pine · Solarized · Wallpaper (Android 12+) |
| Layout | Scope chips and filters can sit at the top or just above the footer |

On a tablet in landscape (≥ 840 dp wide) the list and the article sit side by side.

## Offline and widget

- The last loaded list is saved and shown when the server can't be reached ("offline" in the title).
- Home-screen widget: unread count and last update. The app updates it when you read, mark, or refresh (no background polling).
