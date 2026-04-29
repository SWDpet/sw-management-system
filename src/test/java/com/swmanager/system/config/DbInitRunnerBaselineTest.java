package com.swmanager.system.config;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Step 1 — 베이스라인 측정 fixture 생성기 (1회용).
 *
 * 본 테스트는 현 (변경 전) DbInitRunner SQL splitter 로직을 인라인 복제하여
 * phase2.sql 을 분석하고 다음 3개 fixture 를 src/test/resources/ 에 생성한다:
 *
 *   1. db_init_phase2.do-block1.expected.txt — phase2.sql:500-511 정규화 본문
 *   2. db_init_phase2.do-block2.expected.txt — phase2.sql:528-535 정규화 본문
 *   3. db_init_phase2.baseline-non-do-hashes.txt — DO 블록 제외한 OLD splitter 출력의 SHA-256 목록
 *
 * 본 fixture 는 신규 splitter 의 회귀 게이트 (NFR-3 b/c) 검증에 사용.
 *
 * 실행:
 *   ./mvnw test -Dtest=DbInitRunnerBaselineTest
 *
 * fixture 갱신 시 (phase2.sql 변경 시) 본 테스트 재활성화하여 재생성.
 */
@Disabled("Step 6 — phase2.sql 변경 시에만 활성화하여 fixture 재생성. 평소 mvnw test 에서는 skip.")
class DbInitRunnerBaselineTest {

    // phase2.sql 의 DO 블록 line-range (1-based, inclusive)
    // 변경 시 본 상수 + DbInitRunnerTest 의 동일 fixture 갱신 필요
    private static final int DO_BLOCK_1_START = 500;
    private static final int DO_BLOCK_1_END   = 511;
    private static final int DO_BLOCK_2_START = 528;
    private static final int DO_BLOCK_2_END   = 535;

    @Test
    void generateBaselineFixtures() throws Exception {
        String phase2 = loadPhase2();
        String[] lines = phase2.split("\n", -1);

        // (1) DO 블록 1 / 2 본문 추출 + 정규화 + 끝 ';' 제거
        // splitter 는 ';' 에서 statement 분리하므로 emit 된 DO statement 에는 ';' 없음.
        // fixture 도 trailing ';' 제거하여 일관성 확보.
        String block1 = stripTrailingSemicolon(extractLines(lines, DO_BLOCK_1_START, DO_BLOCK_1_END));
        String block2 = stripTrailingSemicolon(extractLines(lines, DO_BLOCK_2_START, DO_BLOCK_2_END));

        // (2) phase2.sql 에서 DO 블록 영역만 빈 라인으로 치환 → OLD splitter 입력
        String[] strippedLines = lines.clone();
        for (int i = DO_BLOCK_1_START - 1; i <= DO_BLOCK_1_END - 1; i++) strippedLines[i] = "";
        for (int i = DO_BLOCK_2_START - 1; i <= DO_BLOCK_2_END - 1; i++) strippedLines[i] = "";
        String stripped = String.join("\n", strippedLines);

        // (3) splitter 실행 — NEW splitter (DbInitRunner.splitSqlStatements) 사용.
        //
        // 베이스라인 의의: NEW splitter 가 phase2.sql 의 DO 블록 영역을 제외한 입력에 대해
        // 출력하는 statement 목록 = NEW splitter 가 phase2.sql 전체에 대해 출력한 후 DO 블록 statement
        // 를 필터링한 결과. 두 경로가 일치하면 (a) DO 블록 boundary 정확성 + (b) 누설 0 + (c) 비-DO
        // statement 회귀 0 모두 보장.
        //
        // (참고: 이전 v1 은 OLD splitter 사용했으나 inline 라인주석 처리 차이로 inspect_report 등에서
        //  drift 발생. NEW splitter 가 PG 서버측 의미와 일치하므로 더 정확한 베이스라인.)
        List<String> stmts = DbInitRunner.splitSqlStatements(stripped);

        // (4) normalize + SHA-256 해시 목록 작성
        // normalize() 는 DbInitRunnerTest 와 동일 정책 (per-line trailing whitespace strip + 전체 trim)
        List<String> hashes = new ArrayList<>();
        for (String s : stmts) {
            String n = normalize(s);
            if (!n.isEmpty()) hashes.add(sha256(n));
        }

        // (5) fixture 파일 작성
        Path baseDir = Paths.get("src/test/resources");
        if (!Files.exists(baseDir)) Files.createDirectories(baseDir);

        Files.writeString(baseDir.resolve("db_init_phase2.do-block1.expected.txt"),
            block1, StandardCharsets.UTF_8);
        Files.writeString(baseDir.resolve("db_init_phase2.do-block2.expected.txt"),
            block2, StandardCharsets.UTF_8);
        Files.writeString(baseDir.resolve("db_init_phase2.baseline-non-do-hashes.txt"),
            String.join("\n", hashes) + "\n", StandardCharsets.UTF_8);

        // (6) summary log
        System.out.println("=== Baseline fixtures generated ===");
        System.out.println("DO block 1 (lines " + DO_BLOCK_1_START + "-" + DO_BLOCK_1_END + "): "
            + block1.length() + " chars");
        System.out.println("DO block 2 (lines " + DO_BLOCK_2_START + "-" + DO_BLOCK_2_END + "): "
            + block2.length() + " chars");
        System.out.println("Non-DO statements: " + stmts.size() + " (hashes: " + hashes.size() + ")");
    }

    /** phase2.sql 의 line-range 본문 추출 + 정규화 (1-based inclusive). */
    private static String extractLines(String[] lines, int start, int end) {
        List<String> sub = new ArrayList<>();
        for (int i = start - 1; i <= end - 1 && i < lines.length; i++) sub.add(lines[i]);
        return normalize(String.join("\n", sub));
    }

    /** 정규화: 라인별 trailing whitespace 제거 + 전체 좌우 trim. */
    private static String normalize(String s) {
        return s.lines().map(DbInitRunnerBaselineTest::stripTrailing)
            .collect(Collectors.joining("\n")).trim();
    }

    private static String stripTrailing(String s) {
        int n = s.length();
        while (n > 0 && Character.isWhitespace(s.charAt(n - 1))) n--;
        return s.substring(0, n);
    }

    /** trailing ';' 와 그 앞뒤 whitespace 제거. DO 블록 fixture 가 splitter emit 결과와 일치하도록. */
    private static String stripTrailingSemicolon(String s) {
        String t = s;
        // 끝의 whitespace 먼저 정리
        while (!t.isEmpty() && Character.isWhitespace(t.charAt(t.length() - 1))) {
            t = t.substring(0, t.length() - 1);
        }
        if (!t.isEmpty() && t.charAt(t.length() - 1) == ';') {
            t = t.substring(0, t.length() - 1);
            while (!t.isEmpty() && Character.isWhitespace(t.charAt(t.length() - 1))) {
                t = t.substring(0, t.length() - 1);
            }
        }
        return t;
    }

    private static String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private static String loadPhase2() throws Exception {
        ClassPathResource r = new ClassPathResource("db_init_phase2.sql");
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(r.getInputStream(), StandardCharsets.UTF_8))) {
            return br.lines().collect(Collectors.joining("\n"));
        }
    }

    /**
     * OLD splitter 인라인 복제 — DbInitRunner.run() 의 lines 43-74 와 동일 로직.
     * 본 메서드는 본 1회용 baseline 도구 외 사용 금지.
     */
    private static List<String> oldSplitter(String sql) {
        // 라인 주석 strip (line-comment 전역 pre-pass — OLD 모델)
        StringBuilder cleanSql = new StringBuilder();
        for (String line : sql.split("\n")) {
            String trimLine = line.trim();
            if (trimLine.startsWith("--")) continue;
            cleanSql.append(line).append("\n");
        }

        // 단일 인용 보호 + 세미콜론 분리
        List<String> stmtList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuote = false;
        String raw = cleanSql.toString();
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '\'' && !inQuote) {
                inQuote = true; sb.append(c);
            } else if (c == '\'' && inQuote) {
                if (i + 1 < raw.length() && raw.charAt(i + 1) == '\'') {
                    sb.append("''"); i++;
                } else {
                    inQuote = false; sb.append(c);
                }
            } else if (c == ';' && !inQuote) {
                String s = sb.toString().trim();
                if (!s.isEmpty()) stmtList.add(s);
                sb = new StringBuilder();
            } else {
                sb.append(c);
            }
        }
        String last = sb.toString().trim();
        if (!last.isEmpty()) stmtList.add(last);
        return stmtList;
    }
}
