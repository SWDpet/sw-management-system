/**
 * Fault injection test — verifies the decoder rejects:
 *   (1) CRC mismatch on a chunk
 *   (2) Missing chunk
 *   (3) SHA-1 mismatch on full payload
 */
import { readFileSync, writeFileSync, mkdirSync } from "node:fs";
import { execSync } from "node:child_process";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const root = join(__dirname, "..");
const framesPath = join(root, "out_multichunk", "frames.json");

const original = JSON.parse(readFileSync(framesPath, "utf8"));

function runDecoder(frames, label) {
  const tmpDir = join(root, "out_fault");
  mkdirSync(tmpDir, { recursive: true });
  const tmpFile = join(tmpDir, `${label}.json`);
  writeFileSync(tmpFile, JSON.stringify(frames, null, 2));
  console.log(`\n══ ${label} ══`);
  try {
    execSync(`node ${join(__dirname, "decode.js")} ${tmpFile}`, { stdio: "inherit" });
    console.log(`✗ EXPECTED FAILURE BUT PASSED: ${label}`);
    return false;
  } catch (err) {
    console.log(`✓ rejected as expected (exit ${err.status})`);
    return true;
  }
}

const results = [];

// (1) CRC corruption — flip a byte in chunk 2
{
  const tampered = JSON.parse(JSON.stringify(original));
  const c2 = tampered.find((f) => f.seq === 2);
  c2.d = c2.d.slice(0, -1) + (c2.d.endsWith("A") ? "B" : "A");
  results.push(["CRC corruption (chunk 2)", runDecoder(tampered, "crc_tampered")]);
}

// (2) Missing chunk — remove seq=3
{
  const missing = original.filter((f) => f.seq !== 3);
  results.push(["Missing chunk (seq 3)", runDecoder(missing, "missing_chunk")]);
}

// (3) SHA-1 mismatch — substitute another chunk with all-zero base45-valid string
{
  const shaCorrupt = JSON.parse(JSON.stringify(original));
  const c1 = shaCorrupt.find((f) => f.seq === 1);
  c1.d = "0".repeat(c1.d.length);
  // re-compute CRC so the chunk passes CRC but fails SHA
  function crc32(buf) {
    let crc = 0xffffffff;
    for (let i = 0; i < buf.length; i++) {
      crc ^= buf[i];
      for (let j = 0; j < 8; j++) crc = crc & 1 ? (crc >>> 1) ^ 0xedb88320 : crc >>> 1;
    }
    return (crc ^ 0xffffffff) >>> 0;
  }
  const crc16 = (crc32(Buffer.from(c1.d, "ascii")) & 0xffff).toString(16).padStart(4, "0");
  c1.chk = crc16;
  results.push(["SHA-1 mismatch (full assembly)", runDecoder(shaCorrupt, "sha_corrupt")]);
}

console.log("\n══════════════════════════════════════");
console.log("Summary");
console.log("══════════════════════════════════════");
for (const [name, ok] of results) {
  console.log(`  ${ok ? "✓" : "✗"} ${name}`);
}
const allOk = results.every(([, ok]) => ok);
console.log(allOk ? "\nAll fault scenarios rejected correctly." : "\nSome fault scenarios were NOT rejected.");
process.exit(allOk ? 0 : 1);
