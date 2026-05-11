"""
Generate a stress test payload that forces multi-chunk QR sequence.
Simulates 5 sites × 4 tiers worth of inspection data.
"""
import json
from pathlib import Path

def make_tier(host, n_items):
    return {
        "h": host,
        "i": [
            [f"item.metric_{i:03d}", "ok" if i % 7 else "warn", round(i * 1.37, 2)]
            for i in range(n_items)
        ],
    }

payload = {
    "s": "snapshot/qr1",
    "id": "stress-2026-05",
    "site": "stress",
    "round": "2026-05",
    "ts": 1778461321,
    "inspector": "박욱진",
    "tiers": {
        f"site{s}_{t}": make_tier(f"HOST-S{s}-{t.upper()}", 50)
        for s in range(1, 6)
        for t in ("ap", "db", "gis", "app")
    },
}

out = Path(__file__).parent / "stress_payload.json"
out.write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")
print(f"wrote {out} (raw {len(json.dumps(payload, ensure_ascii=False, separators=(',',':')))} bytes)")
