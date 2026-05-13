/**
 * Smoke test — runs decoder.mjs in Node (which exposes the same web APIs
 * we use in the browser: crypto.subtle, DecompressionStream, TextDecoder).
 *
 *   node pwa/_smoke.mjs
 *
 * Not shipped — exists only to prove the browser decoder matches decode.js.
 */
import { readFileSync } from "node:fs";
import { decodeFrames } from "./decoder.mjs";

const cases = [
  "../out/frames.json",
  "../out_stress/frames.json",
  "../out_multichunk/frames.json",
];

let ok = 0;
let fail = 0;
for (const rel of cases) {
  const path = new URL(rel, import.meta.url);
  const raw = readFileSync(path, "utf8");
  const frames = JSON.parse(raw.charCodeAt(0) === 0xfeff ? raw.slice(1) : raw);
  try {
    const { payload, header, stats } = await decodeFrames(frames);
    console.log(
      `✓ ${rel}  id=${header.id}  total=${header.total}  ` +
        `gz=${stats.gzBytes}B  json=${stats.jsonBytes}B  sha=${stats.sha}  ` +
        `keys=[${Object.keys(payload).join(",")}]`,
    );
    ok++;
  } catch (e) {
    console.log(`✗ ${rel}  ${e.message}`);
    fail++;
  }
}

// fault tests — decoder must reject
const faultCases = [
  ["CRC corruption", () => {
    const f = JSON.parse(readFileSync(new URL("../out_multichunk/frames.json", import.meta.url), "utf8"));
    const c = f.find((x) => x.seq === 2);
    c.d = c.d.slice(0, -1) + (c.d.endsWith("A") ? "B" : "A");
    return f;
  }],
  ["missing chunk", () => {
    const f = JSON.parse(readFileSync(new URL("../out_multichunk/frames.json", import.meta.url), "utf8"));
    return f.filter((x) => x.seq !== 3);
  }],
  ["SHA-1 mismatch", () => {
    const f = JSON.parse(readFileSync(new URL("../out_multichunk/frames.json", import.meta.url), "utf8"));
    const c1 = f.find((x) => x.seq === 1);
    c1.d = "0".repeat(c1.d.length);
    // re-stamp CRC so SHA is the only failure
    let crc = 0xffffffff;
    for (let i = 0; i < c1.d.length; i++) {
      crc ^= c1.d.charCodeAt(i);
      for (let j = 0; j < 8; j++) crc = crc & 1 ? (crc >>> 1) ^ 0xedb88320 : crc >>> 1;
    }
    c1.chk = ((crc ^ 0xffffffff) & 0xffff).toString(16).padStart(4, "0");
    return f;
  }],
];

console.log("\n— fault rejection —");
for (const [name, build] of faultCases) {
  try {
    await decodeFrames(build());
    console.log(`✗ ${name} — should have been rejected`);
    fail++;
  } catch (e) {
    console.log(`✓ ${name} rejected: ${e.message}`);
    ok++;
  }
}

console.log(`\n${ok} ok, ${fail} failed`);
process.exit(fail === 0 ? 0 : 1);
