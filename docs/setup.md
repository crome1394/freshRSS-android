# Setup

## FreshRSS server

1. In FreshRSS: **Settings → Authentication → Allow API access**.
2. **Profile → API password**: set one. It is not your login password.

## The app

1. Install the APK from [Releases](https://github.com/crome1394/freshRSS-android/releases/latest)
   (allow "Install unknown apps" for your browser or file manager).
2. On first start enter:

   | Field | Example |
   |---|---|
   | Server URL | `https://rss.example.com` (a bare host gets `https://`) |
   | User | your FreshRSS user name |
   | API password | from Profile → API password |

3. **Test connection**, then **Save & continue**.

`http://` addresses are blocked unless you turn on **Allow insecure HTTP**. Use it only on a
LAN or VPN you trust. The API password is stored in Android's Keystore-backed encrypted storage.

## Tailscale

If your server is only reachable over Tailscale, the 🔑 button in the footer connects
and disconnects it, and when the server is out of reach (mobile data, or Wi-Fi away from
home) the app connects it by itself for each refresh (Settings → Tailscale).

- Tailscale must be installed and logged in, and you must have accepted its VPN prompt
  once. If not, Tailscale opens its own screen to finish.
- Hide the button or turn off the automatic connect under **Settings → Tailscale**.

## Widget

Long-press the home screen → **Widgets** → **FreshRSS**. It shows what the app last saw;
open the app to refresh it.

## Updating from 0.7.x

0.8.0 is signed with a new key, so Android won't install it over 0.7.x. Uninstall the old
app first, then install 0.8.0 and enter the server again. Later versions update in place.
