/**
 * QR decoder PoC (PWA-side simulation)
 *
 * Takes a list of QR-frame JSON strings (as a PWA would obtain from BarcodeDetector),
 * verifies sequence + CRC + SHA-1, then base45-decodes and gunzips back to JSON.
 *
 * Usage:
 *   node decode.js ../out/frames.json
 */

import { readFileSync } from "node:fs";
import { gunzipSync } from "node:zlib";
import { createHash } from "node:crypto";

// ── base45 (RFC 9285) — minimal implementation ──────────────────────────────
const B45 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:";
const B45_MAP = Object.fromEntries([...B45].map((c, i) => [c, i]));

function b45decode(str) {
  if (str.length === 0) return Buffer.alloc(0);
  const out = [];
  for (let i = 0; i < str.length; i += 3) {
    const seg = str.slice(i, i + 3);
    if (seg.length === 3) {
      const n = B45_MAP[seg[0]] + B45_MAP[seg[1]] * 45 + B45_MAP[seg[2]] * 45 * 45;
      if (n > 0xffff) throw new Error("base45 overflow");
      out.push((n >> 8) & 0xff, n & 0xff);
    } else if (seg.length === 2) {
      const n = B45_MAP[seg[0]] + B45_MAP[seg[1]] * 45;
      if (n > 0xff) throw new Error("base45 overflow (tail)");
      out.push(n);
    } else {
      throw new Error("base45: invalid length");
    }
  }
  return Buffer.from(out);
}

// ── CRC32 → 16-bit (matches Python encoder) ─────────────────────────────────
function crc32(buf) {
  let crc = 0xffffffff;
  for (let i = 0; i < buf.length; i++) {
    crc ^= buf[i];
    for (let j = 0; j < 8; j++) {
      crc = crc & 1 ? (crc >>> 1) ^ 0xedb88320 : crc >>> 1;
    }
  }
  return (crc ^ 0xffffffff) >>> 0;
}

function crc16ofText(s) {
  const v = crc32(Buffer.from(s, "ascii"));
  return v & 0xffff;
}

// ── main ─────────────────────────────────────────────────────────────────────
function decodeFrames(framesPath) {
  let raw = readFileSync(framesPath, "utf8");
  if (raw.charCodeAt(0) === 0xfeff) raw = raw.slice(1); // strip UTF-8 BOM
  const frames = JSON.parse(raw);
  if (frames.length === 0) throw new Error("no frames");

  const header = frames.find((f) => f.seq === 0);
  if (!header) throw new Error("header frame (seq=0) missing");
  const dataFrames = frames.filter((f) => f.seq > 0);

  console.log("─".repeat(60));
  console.log(`Header           : id=${header.id}  total=${header.total}  hash=${header.hash}`);
  console.log(`Data frames recv : ${dataFrames.length}`);
  console.log("─".repeat(60));

  // CRC verify per chunk
  const collected = new Map();
  for (const f of dataFrames) {
    const expected = crc16ofText(f.d).toString(16).padStart(4, "0");
    const ok = expected === f.chk;
    console.log(`  [${String(f.seq).padStart(2, "0")}/${f.total}] chk=${f.chk} computed=${expected} ${ok ? "✓" : "✗"}`);
    if (!ok) throw new Error(`CRC mismatch at seq ${f.seq}`);
    collected.set(f.seq, f.d);
  }

  // Order + assemble
  if (collected.size !== header.total) {
    const missing = [];
    for (let i = 1; i <= header.total; i++) if (!collected.has(i)) missing.push(i);
    throw new Error(`missing chunks: ${missing.join(", ")}`);
  }
  const ordered = [];
  for (let i = 1; i <= header.total; i++) ordered.push(collected.get(i));
  const b45 = ordered.join("");

  // base45 → gzip bytes
  const gz = b45decode(b45);
  const sha = createHash("sha1").update(gz).digest("hex").slice(0, 6);
  console.log("─".repeat(60));
  console.log(`Reassembled b45  : ${b45.length} chars`);
  console.log(`Decoded gzip     : ${gz.length} bytes`);
  console.log(`SHA-1 (gz)       : ${sha} (header=${header.hash}) ${sha === header.hash ? "✓" : "✗"}`);
  if (sha !== header.hash) throw new Error("SHA-1 mismatch");

  // gunzip
  const json = gunzipSync(gz).toString("utf8");
  const parsed = JSON.parse(json);

  console.log("─".repeat(60));
  console.log("Decoded payload preview:");
  console.log(`  id        : ${parsed.id}`);
  console.log(`  site      : ${parsed.site}`);
  console.log(`  round     : ${parsed.round}`);
  console.log(`  inspector : ${parsed.inspector}`);
  if (parsed.tiers) {
    for (const [tier, body] of Object.entries(parsed.tiers)) {
      const total = body.i?.length ?? 0;
      const warn = (body.i ?? []).filter((x) => x[1] === "warn").length;
      const manual = (body.i ?? []).filter((x) => x[1] === "M").length;
      console.log(`  ${tier.padEnd(4)} (${body.h}) : ${total} items, warn=${warn}, manual=${manual}`);
    }
  }
  console.log("─".repeat(60));
  console.log("Decode OK ✓");
}

const path = process.argv[2] ?? "../out/frames.json";
decodeFrames(path);
