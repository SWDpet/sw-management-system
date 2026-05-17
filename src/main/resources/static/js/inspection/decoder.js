/**
 * Browser-side decoder — mirrored from inspection-poc/pwa/decoder.mjs
 * for inline use from /document/doc-inspect.html STEP 2 카메라.
 *
 * Pure functions only; no DOM, no globals. Requires modern browser
 * (Chrome 80+, Safari 16.4+) — TextEncoder, crypto.subtle, DecompressionStream.
 */

// ── base45 (RFC 9285) ────────────────────────────────────────────────────────
const B45 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:";
const B45_MAP = (() => {
  const m = new Int16Array(128).fill(-1);
  for (let i = 0; i < B45.length; i++) m[B45.charCodeAt(i)] = i;
  return m;
})();

export function b45decode(str) {
  if (str.length === 0) return new Uint8Array(0);
  const out = [];
  for (let i = 0; i < str.length; i += 3) {
    const seg = str.slice(i, i + 3);
    if (seg.length === 3) {
      const a = B45_MAP[seg.charCodeAt(0)];
      const b = B45_MAP[seg.charCodeAt(1)];
      const c = B45_MAP[seg.charCodeAt(2)];
      if (a < 0 || b < 0 || c < 0) throw new Error("base45: invalid char");
      const n = a + b * 45 + c * 45 * 45;
      if (n > 0xffff) throw new Error("base45 overflow");
      out.push((n >> 8) & 0xff, n & 0xff);
    } else if (seg.length === 2) {
      const a = B45_MAP[seg.charCodeAt(0)];
      const b = B45_MAP[seg.charCodeAt(1)];
      if (a < 0 || b < 0) throw new Error("base45: invalid char (tail)");
      const n = a + b * 45;
      if (n > 0xff) throw new Error("base45 overflow (tail)");
      out.push(n);
    } else {
      throw new Error("base45: invalid length");
    }
  }
  return Uint8Array.from(out);
}

// ── CRC32 → 16-bit ───────────────────────────────────────────────────────────
const CRC_TABLE = (() => {
  const t = new Uint32Array(256);
  for (let i = 0; i < 256; i++) {
    let c = i;
    for (let j = 0; j < 8; j++) c = c & 1 ? (c >>> 1) ^ 0xedb88320 : c >>> 1;
    t[i] = c >>> 0;
  }
  return t;
})();

export function crc32Ascii(s) {
  let crc = 0xffffffff;
  for (let i = 0; i < s.length; i++) {
    crc = (crc >>> 8) ^ CRC_TABLE[(crc ^ s.charCodeAt(i)) & 0xff];
  }
  return (crc ^ 0xffffffff) >>> 0;
}

export function crc16ofText(s) {
  return crc32Ascii(s) & 0xffff;
}

async function sha1Hex6(bytes) {
  const hash = await crypto.subtle.digest("SHA-1", bytes);
  const view = new Uint8Array(hash);
  let hex = "";
  for (let i = 0; i < 3; i++) hex += view[i].toString(16).padStart(2, "0");
  return hex;
}

async function gunzip(bytes) {
  const stream = new Response(bytes).body.pipeThrough(new DecompressionStream("gzip"));
  const buf = await new Response(stream).arrayBuffer();
  return new Uint8Array(buf);
}

export class FrameAccumulator {
  constructor(onEvent) {
    this.onEvent = onEvent ?? (() => {});
    this.reset();
  }

  reset() {
    this.header = null;
    this.chunks = new Map();
    this.seenRaw = new Set();
    this.errors = [];
  }

  async ingest(text) {
    if (!text || this.seenRaw.has(text)) return null;
    let frame;
    try { frame = JSON.parse(text); }
    catch (e) {
      this.errors.push(`JSON parse failed: ${e.message}`);
      this.onEvent({ kind: "error", message: "JSON parse failed", text });
      return null;
    }
    if (typeof frame !== "object" || frame === null || typeof frame.seq !== "number") {
      this.errors.push("Not a valid frame object");
      this.onEvent({ kind: "error", message: "Not a frame object" });
      return null;
    }
    this.seenRaw.add(text);

    if (frame.seq === 0) {
      if (this.header && this.header.id !== frame.id) {
        this.onEvent({ kind: "error", message: `다른 페이로드의 헤더가 섞였습니다 (id ${frame.id} vs ${this.header.id})` });
        return frame;
      }
      this.header = frame;
      this.onEvent({ kind: "header", frame, progress: this.progress() });
      await this.tryComplete();
      return frame;
    }

    const expected = crc16ofText(frame.d).toString(16).padStart(4, "0");
    if (expected !== frame.chk) {
      this.errors.push(`CRC mismatch at seq=${frame.seq}: ${frame.chk} vs ${expected}`);
      this.onEvent({ kind: "error", message: `CRC 불일치 (seq=${frame.seq})` });
      return frame;
    }
    if (this.chunks.has(frame.seq)) return frame;
    this.chunks.set(frame.seq, frame.d);
    this.onEvent({ kind: "chunk", frame, progress: this.progress() });
    await this.tryComplete();
    return frame;
  }

  progress() {
    const total = this.header?.total;
    return {
      total,
      received: this.chunks.size,
      hasHeader: !!this.header,
      missing: total ? this.missingSeqs() : null,
    };
  }

  missingSeqs() {
    if (!this.header) return null;
    const miss = [];
    for (let i = 1; i <= this.header.total; i++) if (!this.chunks.has(i)) miss.push(i);
    return miss;
  }

  isComplete() {
    return !!this.header && this.chunks.size === this.header.total;
  }

  async tryComplete() {
    if (!this.isComplete()) return;
    try {
      const result = await this.decode();
      this.onEvent({ kind: "complete", ...result });
    } catch (e) {
      this.errors.push(e.message);
      this.onEvent({ kind: "error", message: e.message });
    }
  }

  async decode() {
    if (!this.header) throw new Error("헤더가 없습니다");
    const total = this.header.total;
    const ordered = [];
    for (let i = 1; i <= total; i++) {
      const d = this.chunks.get(i);
      if (d === undefined) throw new Error(`청크 ${i} 누락`);
      ordered.push(d);
    }
    const b45 = ordered.join("");
    const gz = b45decode(b45);
    const sha = await sha1Hex6(gz);
    if (sha !== this.header.hash) throw new Error(`SHA-1 불일치 (header=${this.header.hash} actual=${sha})`);
    const jsonBytes = await gunzip(gz);
    const json = new TextDecoder("utf-8").decode(jsonBytes);
    const payload = JSON.parse(json);
    return {
      payload,
      header: this.header,
      stats: { b45Chars: b45.length, gzBytes: gz.length, jsonBytes: jsonBytes.length, sha },
    };
  }
}
