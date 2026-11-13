# F-Droid

Prepared, **not submitted**. The setup matches Point Forecast, which is live on F-Droid:
**exclusive upstream-signed reproducible builds**. F-Droid rebuilds each tagged release
from source, checks it matches our GitHub APK apart from the signature, and then
publishes **our** APK. Users can switch between GitHub and F-Droid updates without
reinstalling. If a rebuild doesn't match, F-Droid skips that version; it never falls
back to its own key.

## Files

| File | Purpose |
|---|---|
| [`docs/fdroid/com.crome.freshrss.yml`](docs/fdroid/com.crome.freshrss.yml) | Recipe for the fdroiddata merge request |
| `fastlane/metadata/android/en-US/` | Store title, descriptions, changelogs (`changelogs/<versionCode>.txt`), icon, feature graphic, screenshots |
| [`scripts/verify-reproducible.sh`](scripts/verify-reproducible.sh) | Rebuild a tag elsewhere and compare it with the published APK |
| [`scripts/strip-apk-signature.py`](scripts/strip-apk-signature.py) | Remove the signing block from an APK (used by the script) |

F-Droid reads the store text and graphics from the source repo at the commit it builds,
so they only appear for releases tagged after they were added.

## Release contract

| | |
|---|---|
| Tag | `v<versionName>`, e.g. `v0.8.1` |
| Asset | `FreshRSS-Android-<versionName>.apk` |
| Signing key | `~/.local/share/freshrss-android/freshrss-release.jks` (never change it) |
| Cert SHA-256 | `953f79964111328b397d00fb2cd4188c63ff023e78cd6d9f4a33185015904837` |
| Build | Gradle, `subdir: app`; no signing settings in F-Droid's build, so it is unsigned |

Each release: bump `versionCode`/`versionName`, add `fastlane/.../changelogs/<versionCode>.txt`,
build and sign (see [docs/building.md](docs/building.md)), publish the asset, then run:

```bash
sh scripts/verify-reproducible.sh v0.8.1 FreshRSS-Android-0.8.1.apk
```

## Verified

v0.8.1: the published APK with its signing block removed is byte-identical to an
unsigned build of the tag from a fresh clone in another directory
(`ff3047d5…736c`, OpenJDK 21, Gradle 8.11.1, AGP 8.7.3).

## Submitting

1. Fork https://gitlab.com/fdroid/fdroiddata and add the recipe as `metadata/com.crome.freshrss.yml`
   (point `Builds`, `CurrentVersion`, and `CurrentVersionCode` at the newest tag).
2. Open the merge request with the "App inclusion" template.
3. Reviewers may add **NonFreeNet** (they did for Point Forecast): the app loads YouTube
   thumbnails for YouTube feeds and can talk to Tailscale.
