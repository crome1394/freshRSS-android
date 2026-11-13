# Building and releasing

## Build

Needs JDK 17+ and the Android SDK (platform 35).

```bash
echo "sdk.dir=$HOME/Android/Sdk" > local.properties
./gradlew :app:assembleDebug          # app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:installDebug           # to a phone with USB debugging
./gradlew :app:testDebugUnitTest      # unit tests
```

## Release signing

`app/build.gradle.kts` signs the release build when these are set, either in
`local.properties` (gitignored) or in the environment:

| Variable | Meaning |
|---|---|
| `RELEASE_STORE_FILE` | Path to the `.jks` keystore |
| `RELEASE_STORE_PASSWORD` | Keystore password |
| `RELEASE_KEY_ALIAS` | Key alias |
| `RELEASE_KEY_PASSWORD` | Key password |

The maintainer's key and an env file setting these live outside the repo:

```bash
set -a; . ~/.local/share/freshrss-android/credentials.env; set +a
./gradlew :app:assembleRelease        # app/build/outputs/apk/release/app-release.apk
```

Keep a backup of the keystore. Phones only accept updates signed with the same key.

## Making a release

1. Bump `versionCode` and `versionName` in `app/build.gradle.kts`; add a `CHANGELOG.md` entry.
2. Build the signed release (above) and check it:
   `apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk`
3. Tag and publish with the APK attached:

```bash
cp app/build/outputs/apk/release/app-release.apk FreshRSS-Android-0.8.0.apk
git tag v0.8.0 && git push origin main v0.8.0
gh release create v0.8.0 FreshRSS-Android-0.8.0.apk --title "0.8.0" --notes-file notes.md
```
