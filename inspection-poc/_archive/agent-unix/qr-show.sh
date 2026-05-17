#!/bin/sh
# ─────────────────────────────────────────────────────────────────────
# qr-show.sh — Unix 점검 대상 서버의 터미널에 ASCII QR 표시
# 사용법:
#   ./qr-show.sh                 # 직전 회차 out/snapshot.json 사용
#   ./qr-show.sh path/to/snap.json
#
# 의존성:
#   - perl (코어 모듈 IO::Compress::Gzip + Digest::SHA + Compress::Zlib::crc32) — 모든 Unix 표준
#   - qrencode — 시스템 패키지 (Linux: apt/yum install qrencode / SLES: zypper)
#     AIX/HP-UX 는 별도 빌드 필요 — 미지원 시 옵션 A (snapshot.json USB 전송) 사용 권장.
#
# 동작:
#   snapshot.json → 축약 qr-payload → gzip → base45 → 1800자 청크 분할
#   → 첫 프레임: 헤더 (id/total/hash/raw/gz/b45)
#   → N 프레임: 각 청크 + CRC32 (PWA decoder.mjs 와 동일 contract)
#   → qrencode -t ANSIUTF8 로 한 프레임씩 터미널 출력. ENTER 누르면 다음 프레임.
# ─────────────────────────────────────────────────────────────────────
set -u
ROOT=$(cd "$(dirname "$0")" && pwd)

SNAP=${1:-"$ROOT/out/snapshot.json"}
[ -f "$SNAP" ] || { echo "ERR: snapshot 파일 없음: $SNAP" >&2; exit 2; }

command -v perl     >/dev/null 2>&1 || { echo "ERR: perl 미설치" >&2; exit 3; }
command -v qrencode >/dev/null 2>&1 || {
    echo "ERR: qrencode 미설치." >&2
    echo "    Linux: sudo apt/yum install qrencode" >&2
    echo "    AIX/HP-UX/Solaris 는 별도 빌드 또는 옵션 A (snapshot.json USB 전송) 사용." >&2
    exit 3
}

FRAMES="$ROOT/out/frames.json"
mkdir -p "$ROOT/out"

# ── snapshot → frames.json (Perl 외부 스크립트) ──────────────────────
perl "$ROOT/lib/qr-encode.pl" "$SNAP" "$FRAMES"
ENC_RC=$?
if [ $ENC_RC -ne 0 ]; then
    echo "ERR: perl encoder 실패 (rc=$ENC_RC)" >&2
    exit $ENC_RC
fi

# ── 프레임 개수 추출 ─────────────────────────────────────────────
FRAME_COUNT=$(perl -ne 'while(/"seq":(\d+)/g){$c++} END{print $c||0}' "$FRAMES")
echo "─────────────────────────────────────────────────────────"
echo "  QR 프레임 총 $FRAME_COUNT 개 — 갤럭시탭 PWA 스캐너로 한 장씩 캡처"
echo "─────────────────────────────────────────────────────────"

# ── 각 프레임을 ANSIUTF8 QR 로 표출 ─────────────────────────────
i=0
while [ "$i" -lt "$FRAME_COUNT" ]; do
    # 프레임 JSON 추출 (i 번째 객체)
    FRAME_JSON=$(perl -e '
        use strict; use warnings;
        open my $fh, "<:raw", $ARGV[0] or die;
        local $/; my $raw = <$fh>; close $fh;
        my $depth = 0; my $start = -1; my $count = 0;
        my $target = $ARGV[1] + 0;
        for (my $i = 0; $i < length($raw); $i++) {
            my $c = substr($raw, $i, 1);
            if ($c eq "{") { $start = $i if $depth == 0; $depth++ }
            elsif ($c eq "}") {
                $depth--;
                if ($depth == 0) {
                    if ($count == $target) { print substr($raw, $start, $i - $start + 1); last }
                    $count++;
                }
            }
        }
    ' "$FRAMES" "$i")
    clear 2>/dev/null
    echo "═══════════════════════════════════════════════════════════"
    echo "  Frame $((i+1)) / $FRAME_COUNT"
    echo "═══════════════════════════════════════════════════════════"
    printf '%s' "$FRAME_JSON" | qrencode -t ANSIUTF8 -m 1
    echo ""
    if [ $((i+1)) -lt "$FRAME_COUNT" ]; then
        printf "  ENTER ↵ — 다음 프레임 / Ctrl+C — 종료 : "
        read _dummy
    else
        echo "  ✓ 마지막 프레임 — 점검 종료"
    fi
    i=$((i + 1))
done
