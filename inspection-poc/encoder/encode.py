"""
QR encoder PoC (server-side simulation)
점검 페이로드(JSON) → gzip → base45 → QR PNG (다중 청크)

Usage:
  python encode.py --in ../sample_payload.json --out-dir ../out --max-bytes 1800
"""
from __future__ import annotations

import argparse
import base45
import binascii
import gzip
import hashlib
import json
import os
import sys
from pathlib import Path

import qrcode
from qrcode.constants import ERROR_CORRECT_L


def compress_payload(payload_json: str) -> bytes:
    return gzip.compress(payload_json.encode("utf-8"), compresslevel=9)


def chunk_base45(text: str, chunk_size: int) -> list[str]:
    return [text[i : i + chunk_size] for i in range(0, len(text), chunk_size)]


def build_frames(payload: dict, max_chunk_chars: int) -> list[dict]:
    payload_json = json.dumps(payload, ensure_ascii=False, separators=(",", ":"))
    compressed = compress_payload(payload_json)
    sha = hashlib.sha1(compressed).hexdigest()[:6]
    encoded = base45.b45encode(compressed).decode("ascii")
    chunks = chunk_base45(encoded, max_chunk_chars)

    frames = [
        {
            "v": 1,
            "id": payload["id"],
            "seq": 0,
            "total": len(chunks),
            "site": payload.get("site"),
            "round": payload.get("round"),
            "ts": payload.get("ts"),
            "hash": sha,
            "raw_bytes": len(payload_json),
            "gz_bytes": len(compressed),
            "b45_chars": len(encoded),
        }
    ]
    for i, chunk in enumerate(chunks, start=1):
        crc = binascii.crc32(chunk.encode("ascii")) & 0xFFFF
        frames.append(
            {
                "v": 1,
                "id": payload["id"],
                "seq": i,
                "total": len(chunks),
                "chk": f"{crc:04x}",
                "d": chunk,
            }
        )
    return frames


def render_qr(text: str, path: Path, version: int | None = None) -> int:
    qr = qrcode.QRCode(
        version=version,
        error_correction=ERROR_CORRECT_L,
        box_size=10,
        border=4,
    )
    qr.add_data(text)
    qr.make(fit=True)
    img = qr.make_image(fill_color="black", back_color="white")
    img.save(path)
    return qr.version


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--in", dest="inp", required=True)
    ap.add_argument("--out-dir", dest="out_dir", required=True)
    ap.add_argument(
        "--max-bytes",
        type=int,
        default=1800,
        help="Max base45 chars per chunk (Ver35 alphanumeric ≤ 3289, conservative default 1800)",
    )
    args = ap.parse_args()

    out_dir = Path(args.out_dir)
    out_dir.mkdir(parents=True, exist_ok=True)

    with open(args.inp, "r", encoding="utf-8") as f:
        payload = json.load(f)

    frames = build_frames(payload, args.max_bytes)
    header = frames[0]
    data_frames = frames[1:]

    print("─" * 60)
    print(f"Payload id      : {payload['id']}")
    print(f"Raw JSON bytes  : {header['raw_bytes']}")
    print(f"gzip bytes      : {header['gz_bytes']}")
    print(f"base45 chars    : {header['b45_chars']}")
    print(f"Chunks          : {header['total']}")
    print(f"Total QR frames : {len(frames)} (1 header + {len(data_frames)} data)")
    print(f"SHA-1 (gz)      : {header['hash']}")
    print("─" * 60)

    manifest = []
    for frame in frames:
        frame_json = json.dumps(frame, ensure_ascii=False, separators=(",", ":"))
        seq = frame["seq"]
        kind = "header" if seq == 0 else "data"
        filename = f"qr_{seq:02d}_{kind}.png"
        path = out_dir / filename
        version = render_qr(frame_json, path)
        size = path.stat().st_size
        manifest.append(
            {
                "seq": seq,
                "kind": kind,
                "filename": filename,
                "qr_version": version,
                "chars": len(frame_json),
                "png_bytes": size,
            }
        )
        print(f"  [{seq:02d}/{header['total']}] {filename:<24} QR-v{version:2d} {len(frame_json):>5} chars  {size:>6} B")

    with open(out_dir / "manifest.json", "w", encoding="utf-8") as f:
        json.dump({"header": header, "frames": manifest}, f, ensure_ascii=False, indent=2)

    with open(out_dir / "frames.json", "w", encoding="utf-8") as f:
        json.dump(frames, f, ensure_ascii=False, indent=2)

    print("─" * 60)
    print(f"Output dir      : {out_dir}")
    print(f"Manifest        : {out_dir / 'manifest.json'}")
    print("Done.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
