# Building and releasing

## Build

Needs JDK 17+ and the Android SDK (platform 35).

```bash
echo "sdk.dir=$HOME/Android/Sdk" > local.properties
./gradlew :app:assembleDebug          # app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:installDebug           # to a phone with USB debugging
./gradlew :app:testDebugUnitTest      # unit tests
```

## Testing on emulators

`tools/mock-freshrss.py` is a small fake FreshRSS (made-up feeds, any login) so emulators
can be tested without a real server:

```bash
python3 -I tools/mock-freshrss.py              # listens on 127.0.0.1:8089
~/Android/Sdk/emulator/emulator -avd Tablet_API35 &
```

In the app: Server URL `http://10.0.2.2:8089` (the emulator's name for this machine), any
user and password, and turn on **Allow insecure HTTP**.

Useful virtual devices (`avdmanager create avd`): `pixel_tablet` and `pixel_fold` on
Android 15, and `pixel_4` on Android 9 (API 28) and 8.0 (API 26), the oldest the app supports.
`adb emu fold` / `adb emu unfold` switch the Fold's screens.

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
