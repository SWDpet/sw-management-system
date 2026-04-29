package com.swmanager.system.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DbInitRunner.splitSqlStatements 단위테스트.
 *
 * <p>스프린트: dbinitrunner-dollar-quote-aware (개발계획서 v2 §2 매트릭스 18 케이스).
 * <ul>
 *   <li>UT-1 ~ UT-11 / UT-13 ~ UT-15 — splitter 기본 동작 14건</li>
 *   <li>UT-12 b1/b2/b3/c — phase2.sql 회귀 게이트 (의미적 동치, NFR-3 b/c) 4건</li>
 * </ul>
 * 합계 18 PASS 기대.
 */
class DbInitRunnerTest {

    // ===== UT-1 ~ UT-11 / UT-13 ~ UT-15: splitter 기본 동작 =====

    @Test void ut1_simpleTwoStatements() {
        List<String> stmts = DbInitRunner.splitSqlStatements(
            "CREATE TABLE foo(); CREATE TABLE bar();");
        assertEquals(2, stmts.size());
    }

    @Test void ut2_lineCommentStripped() {
        List<String> stmts = DbInitRunner.splitSqlStatements(
            "-- comment\nCREATE TABLE foo();");
        assertEquals(1, stmts.size());
        assertFalse(stmts.get(0).contains("comment"),
            "라인 주석 본문이 statement 에 포함되면 안됨");
    }

    @Test void ut3_singleQuoteSemicolonProtected() {
        List<String> stmts = DbInitRunner.splitSqlStatements(
            "INSERT INTO t VALUES ('a;b');");
        assertEquals(1, stmts.size());
    }

    @Test void ut4_singleQuoteEscape() {
        List<String> stmts = DbInitRunner.splitSqlStatements(
            "INSERT INTO t VALUES ('it''s');");
        assertEquals(1, stmts.size());
        assertTrue(stmts.get(0).contains("''"),
            "단일 인용 escape '' 가 본문에 보존되어야 함");
    }

    @Test void ut5_doDollarQuoteSingle() {
        List<String> stmts = DbInitRunner.splitSqlStatements(
            "DO $$ BEGIN PERFORM 1; PERFORM 2; END $$;");
        assertEquals(1, stmts.size(), "DO $$ ... $$ 블록은 단일 statement");
    }

    @Test void ut6_taggedDollarQuote() {
        List<String> stmts = DbInitRunner.splitSqlStatements(
            "DO $plpgsql$ BEGIN PERFORM 1; END $plpgsql$;");
        assertEquals(1, stmts.size(), "$plpgsql$ ... $plpgsql$ 블록은 단일 statement");
    }

    @Test void ut7_doBlockWithSingleQuote() {
        List<String> stmts = DbInitRunner.splitSqlStatements(
            "DO $$ BEGIN RAISE NOTICE 'a;b;c'; END $$;");
        assertEquals(1, stmts.size(), "DO 블록 내 단일 인용 안의 ; 보호");
    }

    @Test void ut8_doBlockBetweenStatements() {
        List<String> stmts = DbInitRunner.splitSqlStatements(
            "CREATE TABLE x(); DO $$ BEGIN PERFORM 1; END $$; CREATE TABLE y();");
        assertEquals(3, stmts.size());
    }

    @Test void ut9_dollarQuoteLiteralInsideSingleQuote() {
        List<String> stmts = DbInitRunner.splitSqlStatements(
            "INSERT INTO t VALUES ('contains $$ literal');");
        assertEquals(1, stmts.size(), "단일 인용 안의 $$ 는 dollar-quote 진입 안함");
    }

    @Test void ut10_unterminatedDollarQuote() {
        List<String> stmts = DbInitRunner.splitSqlStatements(
            "DO $$ BEGIN PERFORM 1");
        assertEquals(1, stmts.size(), "미종료 dollar-quote — panic 없이 단일 statement 로 종료");
    }

    @Test void ut11_emptyStatementsIgnored() {
        List<String> stmts = DbInitRunner.splitSqlStatements(";;;");
        assertEquals(0, stmts.size(), "빈 statement 는 출력에서 제외");
    }

    @Test void ut13_doBlockPreservesLineComment() {
        String input = "DO $$ BEGIN -- 주석 한국어\n  PERFORM 1;\n  -- 다른 주석\n  PERFORM 2;\nEND $$;";
        List<String> stmts = DbInitRunner.splitSqlStatements(input);
        assertEquals(1, stmts.size(), "DO 블록 단일 statement");
        assertTrue(stmts.get(0).contains("-- 주석 한국어"),
            "DO 블록 내 라인 주석 (한국어) 본문에 보존");
        assertTrue(stmts.get(0).contains("-- 다른 주석"),
            "DO 블록 내 두 번째 라인 주석 본문에 보존");
    }

    @Test void ut14_caseSensitiveDollarTag() {
        String input = "DO $TAG$ BEGIN PERFORM 1; END $tag$ ; SELECT 1;";
        List<String> stmts = DbInitRunner.splitSqlStatements(input);
        assertEquals(1, stmts.size(),
            "$tag$ 가 닫힘 토큰으로 인식 안됨 (대소문자 민감) → 끝까지 IN_DOLLAR_QUOTE → 단일 statement");
    }

    @Test void ut15_identifierBoundaryDoesNotEnterDollarQuote() {
        String input = "SELECT foo$$ FROM t; CREATE TABLE x();";
        List<String> stmts = DbInitRunner.splitSqlStatements(input);
        assertEquals(2, stmts.size(),
            "식별자 직후의 $$ 는 dollar-quote 진입 안함 (보수적 boundary)");
    }

    // UT-16 (codex 6차 권고 lock-in): 현 splitter 는 block comment ('/* ... */') 를 별도 상태로
    // 처리하지 않음. phase2.sql 이 block comment 를 사용 안 한다는 invariant 를 본 테스트로
    // 명시하여, 미래 phase2.sql 에 block comment 추가 시 본 테스트가 FAIL 해 splitter 측
    // IN_BLOCK_COMMENT state 추가가 선행되도록 강제.
    @Test void ut16_phase2DoesNotUseBlockComments() throws Exception {
        String phase2 = loadPhase2();
        assertFalse(phase2.contains("/*"),
            "phase2.sql 에 block comment ('/* ... */') 사용 금지 — 현 splitter 미처리. "
                + "추가 필요 시 splitter 의 IN_BLOCK_COMMENT state 선행 구현 필수");
    }

    // ===== UT-12: phase2.sql 회귀 게이트 (NFR-3 b/c) =====

    @Test void ut12_b1_doBlocksMatchLineRangeFixture() throws Exception {
        String phase2 = loadPhase2();
        List<String> stmts = DbInitRunner.splitSqlStatements(phase2);

        String block1 = loadFixture("db_init_phase2.do-block1.expected.txt");
        String block2 = loadFixture("db_init_phase2.do-block2.expected.txt");

        long matches1 = stmts.stream().filter(s -> normalize(s).equals(normalize(block1))).count();
        long matches2 = stmts.stream().filter(s -> normalize(s).equals(normalize(block2))).count();

        assertEquals(1, matches1,
            "DO 블록 1 (lines 500-511) 가 정확히 1 statement 로 보존");
        assertEquals(1, matches2,
            "DO 블록 2 (lines 528-535) 가 정확히 1 statement 로 보존");
    }

    @Test void ut12_b2_doBlocksHaveDoEndPrefixSuffix() throws Exception {
        String phase2 = loadPhase2();
        List<String> stmts = DbInitRunner.splitSqlStatements(phase2);

        long doStmts = stmts.stream()
            .filter(s -> s.startsWith("DO $$"))
            .filter(s -> s.endsWith("END $$"))
            .count();
        assertEquals(2, doStmts,
            "phase2.sql 의 DO 블록 2건 모두 'DO $$' 시작 + 'END $$' 종료");
    }

    @Test void ut12_b3_noLeakageOfDoBodyToOtherStatements() throws Exception {
        String phase2 = loadPhase2();
        List<String> stmts = DbInitRunner.splitSqlStatements(phase2);

        String[] doBodyKeywords = {
            "fk_tb_document_org_unit",
            "ck_tb_document_support_target_type",
            "ck_tb_document_environment",
            "fk_tb_document_region_code"
        };
        for (String kw : doBodyKeywords) {
            long leaks = stmts.stream()
                .filter(s -> !s.startsWith("DO $$"))
                .filter(s -> s.contains(kw))
                .count();
            assertEquals(0, leaks,
                "DO 블록 본문 키워드 '" + kw + "' 가 DO 외 statement 로 누설되면 안됨");
        }
    }

    @Test void ut12_c_nonDoStatementsUnchanged() throws Exception {
        String phase2 = loadPhase2();
        List<String> stmts = DbInitRunner.splitSqlStatements(phase2);

        List<String> hashes = stmts.stream()
            .filter(s -> !s.startsWith("DO $$"))
            .map(DbInitRunnerTest::normalize)
            .filter(s -> !s.isEmpty())
            .map(DbInitRunnerTest::sha256)
            .collect(Collectors.toList());

        List<String> baseline = loadBaselineHashes();
        assertEquals(baseline, hashes,
            "DO 블록 외 statement 의 SHA-256 목록이 베이스라인과 동일 (회귀 0)");
    }

    // ===== helpers =====

    private static String loadPhase2() throws Exception {
        ClassPathResource r = new ClassPathResource("db_init_phase2.sql");
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(r.getInputStream(), StandardCharsets.UTF_8))) {
            return br.lines().collect(Collectors.joining("\n"));
        }
    }

    private static String loadFixture(String name) throws Exception {
        ClassPathResource r = new ClassPathResource(name);
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(r.getInputStream(), StandardCharsets.UTF_8))) {
            return br.lines().collect(Collectors.joining("\n"));
        }
    }

    private static List<String> loadBaselineHashes() throws Exception {
        ClassPathResource r = new ClassPathResource("db_init_phase2.baseline-non-do-hashes.txt");
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(r.getInputStream(), StandardCharsets.UTF_8))) {
            return br.lines().filter(l -> !l.isBlank()).collect(Collectors.toList());
        }
    }

    private static String normalize(String s) {
        return s.lines().map(DbInitRunnerTest::stripTrailing)
            .collect(Collectors.joining("\n")).trim();
    }

    private static String stripTrailing(String s) {
        int n = s.length();
        while (n > 0 && Character.isWhitespace(s.charAt(n - 1))) n--;
        return s.substring(0, n);
    }

    private static String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
