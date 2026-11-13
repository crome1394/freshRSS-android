# Features

## List

| Feature | How |
|---|---|
| Scopes | Chips: ● Unread · All · ✓ Read · ★ Starred |
| Media filters | 🎥 Videos · 🔊 Sound (tap again to clear) |
| Date filter | Filters → All · Today · -1 · -7 · -14 · -21 |
| Search | Filters → search box: title, feed, author, article text |
| Grouping | Feeds A–Z (site icon, or a letter badge) → days → articles |
| Open a feed or day | Tap the header. It scrolls to the top of the list |
| Feed all read ↔ unread | Button on the feed header, or long-press the header |
| Day all read ↔ unread | Long-press the day header |
| Article read / unread | Swipe left = read, swipe right = unread |
| Star | Star button on the row, or long-press the row |
| Collapse / expand all | Funnel button on the Filters bar |
| Download size | Settings → History days (default 30), or Filters → per feed / max articles when history is 0 |

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

## Title bar

| Button | Tap | Long-press |
|---|---|---|
| 🔑 Tailscale | Connect / disconnect the VPN. Lit key = connected | Open the Tailscale app |
| ⟳ Refresh | Reload from the server | — |
| ⚙ Settings | Open Settings | — |

The list reloads by itself once Tailscale connects.

## Appearance

Settings → Appearance:

| Setting | Options |
|---|---|
| Theme | System · Light · Dark |
| Colors | Tokyo Night · Catppuccin · Nord · Gruvbox · Rosé Pine · Solarized · Wallpaper (Android 12+) |
| Layout | Title bar, filter chips, and Filters panel can each sit at the top or bottom |

On a tablet in landscape (≥ 840 dp wide) the list and the article sit side by side.

## Offline and widget

- The last loaded list is saved and shown when the server can't be reached ("offline" in the title).
- Home-screen widget: unread count and last update. The app updates it when you read, mark, or refresh (no background polling).
