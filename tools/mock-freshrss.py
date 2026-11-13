#!/usr/bin/env python3
"""Tiny fake FreshRSS (Google Reader API subset) for emulator tests and screenshots.

    python3 -I tools/mock-freshrss.py [port]      # default 8089

In the app: Server URL http://10.0.2.2:8089 (the emulator's name for this
machine), any user and password, and turn on "Allow insecure HTTP".
Only the calls the app makes for the All / Read scopes are answered; the
feeds and articles are made up.
"""
import json
import sys
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import urlparse

NOW = int(time.time())
H = 3600

# (title, site URL, feed URL, [(title, summary, link or None)]) — all made up.
FEEDS = [
    ("Byte Review", "https://bytereview.example", "https://bytereview.example/feed", [
        ("The quiet return of the home server", "Self-hosting is having a moment as people move their photos, notes, and feeds back onto hardware they own.", None),
        ("Why your next laptop might have a repairable battery", "New rules and a few stubborn engineers are changing how laptops are put together.", None),
        ("Hands-on with a tablet that finally replaces my laptop", "Bigger screens, better keyboards, and software that uses them.", None),
    ]),
    ("Linux Desk", "https://linuxdesk.example", "https://linuxdesk.example/rss", [
        ("November release: faster boot and a new installer", "The installer gains a guided partitioning screen and the kernel picks up scheduler work.", None),
        ("Tiling window managers, explained", "Readers share their Hyprland and Sway setups.", None),
    ]),
    ("Night Sky Daily", "https://nightsky.example", "https://nightsky.example/feed.xml", [
        ("Aurora over the Gulf of Alaska", "A ribbon of green light captured from orbit.", None),
        ("How to photograph the Geminids", "Settings, lenses, and where to point your camera.", None),
    ]),
    ("r/selfhosted", "https://www.reddit.com/r/selfhosted/", "https://www.reddit.com/r/selfhosted/.rss", [
        ("My FreshRSS + Tailscale setup after a year", "Everything runs on a mini PC in the closet.", "https://www.reddit.com/r/selfhosted/comments/abc123/"),
        ("What do you self-host that you can't live without?", "Weekly thread.", "https://www.reddit.com/r/selfhosted/comments/abc124/"),
    ]),
    ("r/androiddev", "https://www.reddit.com/r/androiddev/", "https://www.reddit.com/r/androiddev/.rss", [
        ("Compose bottom sheets with swipeable tabs", "A small pattern that works well for settings screens.", "https://www.reddit.com/r/androiddev/comments/abc125/"),
    ]),
    ("Orbit Explained", "https://www.youtube.com/channel/UCorbit", "https://www.youtube.com/feeds/videos.xml?channel_id=UCorbit", [
        ("How do satellites stay up?", "A twelve-minute tour of orbital mechanics.", "https://www.youtube.com/watch?v=aaaaaaaaaaa"),
        ("Building a backyard radio telescope", "Parts, software, and first light.", "https://www.youtube.com/watch?v=bbbbbbbbbbb"),
    ]),
    ("Workshop Notes", "https://www.youtube.com/channel/UCworkshop", "https://www.youtube.com/feeds/videos.xml?channel_id=UCworkshop", [
        ("Restoring a 1970s bench drill", "Rust, paint, and new bearings.", "https://www.youtube.com/watch?v=ccccccccccc"),
    ]),
    ("Release Notes: reader-app", "https://github.com/example/reader-app", "https://github.com/example/reader-app/releases.atom", [
        ("v2.4.0: offline mode and widgets", "Full changelog in the release.", "https://github.com/example/reader-app/releases/tag/v2.4.0"),
    ]),
]


def stream_id(i):
    return f"feed/{i + 1}"


def items_for(i):
    title, site, _feed, posts = FEEDS[i]
    out = []
    for j, (t, body, link) in enumerate(posts):
        link = link or f"{site}/posts/{j + 1}"
        age = (i * 5 + j * 7 + 1) * H // 2
        read = (i + j) % 3 == 2
        cats = ["user/-/state/com.google/reading-list"]
        if read:
            cats.append("user/-/state/com.google/read")
        if (i + j) % 5 == 0:
            cats.append("user/-/state/com.google/starred")
        out.append({
            "id": f"tag:google.com,2005:reader/item/{(i + 1) * 1000 + j:016x}",
            "title": t,
            "published": NOW - age,
            "author": "Staff",
            "categories": cats,
            "canonical": [{"href": link}],
            "alternate": [{"href": link}],
            "summary": {"content": f"<p>{body}</p><p>{body} More of the story continues on the site, "
                                   f"with details, quotes, and links to related coverage.</p>"},
            "origin": {"streamId": stream_id(i), "title": title},
        })
    return out


class Handler(BaseHTTPRequestHandler):
    def send(self, body, ctype="application/json"):
        data = body.encode() if isinstance(body, str) else body
        self.send_response(200)
        self.send_header("Content-Type", ctype)
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def do_POST(self):
        self.do_GET()

    def do_GET(self):
        path = urlparse(self.path).path
        if path.endswith("/accounts/ClientLogin"):
            return self.send("SID=x\nLSID=x\nAuth=mocktoken\n", "text/plain")
        if path.endswith("/reader/api/0/token"):
            return self.send("mocktoken", "text/plain")
        if path.endswith("/subscription/list"):
            subs = [{"id": stream_id(i), "title": t, "htmlUrl": site, "url": feed,
                     "categories": [{"id": "user/-/label/News", "label": "News"}]}
                    for i, (t, site, feed, _) in enumerate(FEEDS)]
            return self.send(json.dumps({"subscriptions": subs}))
        if path.endswith("/unread-count"):
            counts, total = [], 0
            for i in range(len(FEEDS)):
                n = sum(1 for it in items_for(i) if "user/-/state/com.google/read" not in it["categories"])
                total += n
                counts.append({"id": stream_id(i), "count": n})
            counts.append({"id": "user/-/state/com.google/reading-list", "count": total})
            return self.send(json.dumps({"max": total, "unreadcounts": counts}))
        if "/stream/contents/feed/" in path:
            i = int(path.rsplit("/", 1)[1]) - 1
            return self.send(json.dumps({"items": items_for(i) if 0 <= i < len(FEEDS) else []}))
        if path in ("", "/", "/api/greader.php"):
            return self.send("OK", "text/plain")
        # Writes (mark read, star, ...) just succeed.
        if "/edit-tag" in path or "/mark-all-as-read" in path or "fever.php" in self.path:
            return self.send('{"api_version":3,"auth":1}' if "fever" in self.path else "OK")
        self.send_response(404)
        self.end_headers()

    def log_message(self, fmt, *args):
        pass


if __name__ == "__main__":
    port = int(sys.argv[1]) if len(sys.argv) > 1 else 8089
    print(f"mock FreshRSS on http://127.0.0.1:{port} (emulator: http://10.0.2.2:{port})", flush=True)
    ThreadingHTTPServer(("127.0.0.1", port), Handler).serve_forever()
