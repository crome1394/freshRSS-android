# Changelog

## 0.8.0

New, from the quickshell FreshRSS reader:

- **Web page view**: fetch an article's page and read its main text in the app (comments, share bars, and scripts removed). Switch per article, or tick **Always web page for this feed**
- **Lead picture**: YouTube still frame or the first image above the article; tap to play or open
- **Full article text** with paragraphs and tappable links (was 1,200 characters of plain text). Images show as `[image]` links, so the body loads nothing from the network
- **Feed icons** from your FreshRSS server, with a letter badge when there is none
- Feed header button and long-press: whole feed read ↔ unread. Long-press a day: that day read ↔ unread
- **Copy link**; the ✓ button in an article now toggles read ↔ unread
- Opening a feed or day scrolls it to the top; titles wrap to three lines; search covers article text

Improved:

- **Tailscale key toggles the VPN** (connect / disconnect) instead of only opening Tailscale. The icon shows the state, long-press opens the app, and the list reloads after connecting
- **New look**, modeled on BlurtText: feeds and articles as rounded cards on a canvas, a search pill at the top, and a footer with the unread count and actions
- **Settings sheet**: opens from the bottom, resizes (half ↔ full), with swipeable Server / Reading / Look / Tailscale tabs. "Title bar at bottom" is gone (actions live in the footer)
- **Tailscale on mobile data**: off Wi-Fi, refresh connects Tailscale, updates, and disconnects after 30 s idle (only if the app connected it)
- **New app icon** with a themed (monochrome) layer
- **Color themes**: from the quickshell bar, Liquid amber, Liquid glass, Liquid ice, Ink, Solid dark, and Deep ocean (glass becomes an accent glow behind translucent cards); plus Tokyo Night, Catppuccin, Nord, Gruvbox, Rosé Pine, Solarized, and Wallpaper (Material You). Each has light and dark versions
- Settings **Test** result is bright green when OK, bright red on failure, and Test connects Tailscale on mobile data too
- Status bar icons follow the app's light/dark theme

Release:

- APKs are now published on GitHub Releases, signed with a release key. **Uninstall 0.7.x before installing**; later versions update in place

## 0.7.3

- **Play / Listen** opens an **Open with** chooser and prefers installed media apps (YouTube, Spotify, SoundCloud, Vimeo, Twitch, NewPipe when present)
- Declares Android package visibility so those apps appear in the chooser (not only the browser)
- Unit tests for media-host → package mapping

## 0.7.2

- Home-screen widget: unread count + last updated
- Widget refreshes on mark-read, category mark-read, and pull-to-refresh
- Downloadable APK naming: `FreshRSS_Personal_Client-<version>.apk`

## 0.7.1 and earlier

- Personal FreshRSS client (Fever + Google Reader APIs)
- Unread / all / read / starred scopes; video & audio filters; date filters
- Star, mark read, share, browser open; offline snapshot; theme & layout options
- Optional Tailscale shortcut; first-run setup wizard
