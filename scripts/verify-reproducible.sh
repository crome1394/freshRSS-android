#!/bin/sh
# Check that a published, signed release APK matches an unsigned build of its tag,
# the way F-Droid's reproducible-build check does.
#
#   sh scripts/verify-reproducible.sh v0.8.1 path/to/FreshRSS-Android-0.8.1.apk
#
# Clones the tag into a temporary directory (a different path catches path
# leaks), builds the unsigned release there with no signing settings, strips
# the APK Signing Block from the published APK, and compares byte for byte.
set -eu

tag=${1:?usage: verify-reproducible.sh <tag> <signed.apk>}
signed=${2:?usage: verify-reproducible.sh <tag> <signed.apk>}
root=$(CDPATH= cd -- "$(dirname "$0")/.." && pwd)
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT

git clone -q --branch "$tag" "$root" "$work/src"
sdk=${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}}
printf 'sdk.dir=%s\n' "$sdk" > "$work/src/local.properties"
(cd "$work/src" && env -u RELEASE_STORE_FILE ./gradlew --no-daemon -q :app:assembleRelease)

unsigned="$work/src/app/build/outputs/apk/release/app-release-unsigned.apk"
[ -f "$unsigned" ] || { echo "no unsigned APK built (is signing still configured?)" >&2; exit 1; }
python3 -I "$root/scripts/strip-apk-signature.py" "$signed" "$work/stripped.apk"

if cmp -s "$work/stripped.apk" "$unsigned"; then
  echo "Reproducible: $(sha256sum "$unsigned" | cut -d' ' -f1)"
else
  echo "MISMATCH between $signed and a rebuild of $tag" >&2
  echo "Diff with: diffoscope (see https://f-droid.org/docs/Reproducible_Builds/)" >&2
  exit 1
fi
