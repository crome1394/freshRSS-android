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

FEEDS = [
    ("Ars Technica", "https://arstechnica.com", [
        ("The quiet return of the home server", "Self-hosting is having a moment as people move their photos, notes, and feeds back onto hardware they own."),
        ("Inside the chip that runs your thermostat", "A teardown of the tiny controller that decides when your heat comes on."),
        ("Why your next laptop might have a repairable battery", "New rules and a few stubborn engineers are changing how laptops are put together."),
    ]),
    ("CachyOS", "https://cachyos.org", [
        ("November release: faster boot and a new installer", "The installer gains a guided partitioning screen and the kernel picks up new scheduler work."),
        ("Community spotlight: tiling setups", "Readers share their Hyprland and Sway configurations."),
    ]),
    ("Hacker News", "https://news.ycombinator.com", [
        ("Show HN: A reader for my FreshRSS server", "I wanted something that felt like my messaging app, so I built one."),
        ("The case for boring technology, ten years on", "Revisiting the essay that told us to spend innovation tokens carefully."),
        ("Ask HN: How do you read the news?", "RSS, newsletters, or nothing at all?"),
    ]),
    ("NASA Image of the Day", "https://www.nasa.gov", [
        ("Aurora over the Gulf of Alaska", "Astronauts captured a ribbon of green light from the International Space Station."),
        ("A new look at the Pillars of Creation", "Combined infrared views reveal stars forming inside the dust."),
    ]),
    ("The Verge", "https://www.theverge.com", [
        ("The best e-readers to buy right now", "From budget picks to color screens, here is what we recommend."),
        ("Tablets are good again", "Bigger screens, better keyboards, and software that finally uses them."),
    ]),
    ("Linux Weekly", "https://lwn.net", [
        ("Kernel 6.x merge window highlights", "Memory management, new drivers, and a long-awaited scheduler change."),
    ]),
]


def stream_id(i):
    return f"feed/{i + 1}"


def items_for(i):
    title, site, posts = FEEDS[i]
    out = []
    for j, (t, body) in enumerate(posts):
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
            "canonical": [{"href": f"{site}/posts/{j + 1}"}],
            "alternate": [{"href": f"{site}/posts/{j + 1}"}],
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
            subs = [{"id": stream_id(i), "title": t, "htmlUrl": site, "url": site + "/feed",
                     "categories": [{"id": "user/-/label/News", "label": "News"}]}
                    for i, (t, site, _) in enumerate(FEEDS)]
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
