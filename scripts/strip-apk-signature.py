#!/usr/bin/env python3
"""Remove the APK Signing Block (v2/v3 signatures) from a signed APK.

    python3 -I scripts/strip-apk-signature.py signed.apk out-unsigned.apk

F-Droid's reproducible-build check copies the upstream signature onto its own
unsigned build; equivalently, an upstream APK with its signing block removed
must be byte-identical to an unsigned build of the same source. Only for APKs
without a v1 (JAR) signature, which is the case for minSdk >= 24 builds.
"""
import struct
import sys

EOCD_SIG = b"PK\x05\x06"
BLOCK_MAGIC = b"APK Sig Block 42"


def strip(data: bytes) -> bytes:
    eocd = data.rfind(EOCD_SIG, max(0, len(data) - 65557))
    if eocd < 0:
        raise SystemExit("not a zip: no end of central directory")
    cd_offset = struct.unpack_from("<I", data, eocd + 16)[0]
    if data[cd_offset - 16:cd_offset] != BLOCK_MAGIC:
        return data  # already unsigned (or v1-only)
    block_size = struct.unpack_from("<Q", data, cd_offset - 24)[0]
    start = cd_offset - (block_size + 8)
    if struct.unpack_from("<Q", data, start)[0] != block_size:
        raise SystemExit("malformed APK Signing Block")
    out = bytearray(data[:start] + data[cd_offset:])
    struct.pack_into("<I", out, (eocd - (cd_offset - start)) + 16, start)
    return bytes(out)


if __name__ == "__main__":
    if len(sys.argv) != 3:
        raise SystemExit(__doc__)
    with open(sys.argv[1], "rb") as f:
        signed = f.read()
    if b"META-INF/MANIFEST.MF" in signed[-65557:] or b"META-INF/MANIFEST.MF" in signed:
        print("warning: APK may carry a v1 (JAR) signature; this check assumes v2/v3 only", file=sys.stderr)
    with open(sys.argv[2], "wb") as f:
        f.write(strip(signed))
