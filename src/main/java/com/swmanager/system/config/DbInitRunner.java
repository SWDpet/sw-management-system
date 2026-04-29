package com.swmanager.system.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 서버 시작 시 필요한 DB 테이블 자동 생성.
 * CREATE TABLE IF NOT EXISTS 사용으로 멱등성 보장.
 *
 * <p>SQL 분리 모델 — 상태 기반 단일 패스 (sprint dbinitrunner-dollar-quote-aware):
 * <ul>
 *   <li>NORMAL — {@code --}, {@code '}, {@code $tag$}, {@code ;} 처리</li>
 *   <li>IN_LINE_COMMENT — {@code \n} 만 NORMAL 복귀 (본문 strip)</li>
 *   <li>IN_SINGLE_QUOTE — {@code ''} escape 포함 {@code '} 만 검사</li>
 *   <li>IN_DOLLAR_QUOTE(tag) — 동일 tag 닫힘 토큰만 검사 ({@code ;} / {@code '} / {@code --} 모두 raw 보존)</li>
 * </ul>
 *
 * @see com.swmanager.system.config.DbInitRunner#splitSqlStatements
 */
@Slf4j
@Component
public class DbInitRunner implements ApplicationRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            ClassPathResource resource = new ClassPathResource("db_init_phase2.sql");
            if (!resource.exists()) {
                log.info("db_init_phase2.sql 파일 없음 - 스킵");
                return;
            }

            String sql;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                sql = reader.lines().collect(Collectors.joining("\n"));
            }

            List<String> stmtList = splitSqlStatements(sql);

            int executed = 0;
            for (String stmt : stmtList) {
                try {
                    jdbcTemplate.execute(stmt);
                    executed++;
                } catch (Exception e) {
                    log.debug("SQL 실행 스킵 (이미 존재하거나 에러): {}", e.getMessage());
                }
            }
            log.info("DB 초기화 완료: {}개 SQL 실행", executed);
        } catch (Exception e) {
            log.warn("DB 초기화 실패 (무시): {}", e.getMessage());
        }
    }

    /**
     * SQL 텍스트를 statement 단위로 분리한다 — 상태 기반 단일 패스.
     *
     * <p>처리 규칙:
     * <ul>
     *   <li>라인 주석 ({@code -- ...}) — NORMAL 상태에서만 strip. dollar-quote 본문 안의 {@code --} 는 보존.</li>
     *   <li>단일 인용 ({@code '...'}) — 내부 {@code ;} / {@code --} / {@code $tag$} 보호. {@code ''} escape 처리.</li>
     *   <li>dollar-quote ({@code $$...$$} / {@code $tag$...$tag$}) — 내부 {@code ;} / {@code '} / {@code --} 보호.
     *       tag 는 ASCII subset {@code [A-Za-z_][A-Za-z0-9_]*}, 대소문자 민감.</li>
     *   <li>식별자 직후의 {@code $tag$} 는 dollar-quote 시작으로 간주하지 않음 (보수적 boundary).</li>
     *   <li>미종료 dollar-quote / 단일 인용은 마지막 statement 로 그대로 묶어 반환 — panic 없음.</li>
     * </ul>
     *
     * @param rawSql 입력 SQL 텍스트 (개행 {@code \n})
     * @return trim 된 statement 리스트 (빈 statement 제외)
     */
    static List<String> splitSqlStatements(String rawSql) {
        List<String> stmts = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        SplitterState state = SplitterState.NORMAL;
        String dollarTag = null;  // IN_DOLLAR_QUOTE 진입 시 토큰 (예: "$$", "$plpgsql$")
        char prevChar = '\0';     // boundary 검사용 — 마지막 소비 문자

        int n = rawSql.length();
        for (int i = 0; i < n; i++) {
            char c = rawSql.charAt(i);
            switch (state) {
                case NORMAL: {
                    if (c == '-' && i + 1 < n && rawSql.charAt(i + 1) == '-') {
                        // 라인 주석 진입 — strip 시작 (본문에 -- 미포함)
                        state = SplitterState.IN_LINE_COMMENT;
                        i++; // skip second '-'
                        prevChar = '-';
                    } else if (c == '\'') {
                        state = SplitterState.IN_SINGLE_QUOTE;
                        sb.append(c);
                        prevChar = c;
                    } else if (c == '$' && !isIdentifierChar(prevChar)) {
                        String tag = tryReadDollarTag(rawSql, i);
                        if (tag != null) {
                            state = SplitterState.IN_DOLLAR_QUOTE;
                            dollarTag = tag;
                            sb.append(tag);
                            i += tag.length() - 1;
                            prevChar = '$';
                        } else {
                            sb.append(c);
                            prevChar = c;
                        }
                    } else if (c == ';') {
                        String s = sb.toString().trim();
                        if (!s.isEmpty()) stmts.add(s);
                        sb = new StringBuilder();
                        prevChar = c;
                    } else {
                        sb.append(c);
                        prevChar = c;
                    }
                    break;
                }
                case IN_LINE_COMMENT: {
                    if (c == '\n') {
                        state = SplitterState.NORMAL;
                        sb.append(c);
                        prevChar = c;
                    }
                    // 그 외 char 는 strip — prevChar 갱신 안함 (본문 미포함)
                    break;
                }
                case IN_SINGLE_QUOTE: {
                    if (c == '\'') {
                        if (i + 1 < n && rawSql.charAt(i + 1) == '\'') {
                            // escape '': 두 ' 모두 보존
                            sb.append("''");
                            i++;
                            prevChar = '\'';
                        } else {
                            state = SplitterState.NORMAL;
                            sb.append(c);
                            prevChar = c;
                        }
                    } else {
                        sb.append(c);
                        prevChar = c;
                    }
                    break;
                }
                case IN_DOLLAR_QUOTE: {
                    if (c == '$' && rawSql.startsWith(dollarTag, i)) {
                        // 닫힘 토큰 정확 매칭 (대소문자 민감)
                        state = SplitterState.NORMAL;
                        sb.append(dollarTag);
                        i += dollarTag.length() - 1;
                        dollarTag = null;
                        prevChar = '$';
                    } else {
                        sb.append(c);
                        prevChar = c;
                    }
                    break;
                }
            }
        }
        // EOF — 잔여 sb 처리 (미종료 단일인용 / dollar-quote 도 그대로 묶음)
        String last = sb.toString().trim();
        if (!last.isEmpty()) stmts.add(last);
        return stmts;
    }

    private enum SplitterState {
        NORMAL, IN_LINE_COMMENT, IN_SINGLE_QUOTE, IN_DOLLAR_QUOTE
    }

    /** ASCII 식별자 시작 문자: A-Z, a-z, _. */
    private static boolean isAsciiIdStart(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_';
    }

    /** ASCII 식별자 후속 문자: A-Z, a-z, 0-9, _. */
    private static boolean isAsciiIdCont(char c) {
        return isAsciiIdStart(c) || (c >= '0' && c <= '9');
    }

    /**
     * boundary 검사 — NORMAL 상태에서 {@code $} 직전 문자가 식별자 문자였는지.
     * 식별자 문자 ({@code A-Z}, {@code a-z}, {@code 0-9}, {@code _}) 또는 {@code $} 면
     * dollar-quote 시작 후보 아님 — 일반 {@code $} 문자로 통과.
     */
    private static boolean isIdentifierChar(char c) {
        return isAsciiIdCont(c) || c == '$';
    }

    /**
     * raw[i] 가 {@code $} 일 때 dollar-quote 시작 토큰을 시도 매칭.
     * 매칭 성공 시 토큰 문자열 (예: {@code "$$"}, {@code "$plpgsql$"}) 반환.
     * 매칭 실패 시 {@code null} 반환.
     *
     * <p>매칭 규칙 (ASCII subset, codex 1차 권고):
     * {@code $tag$} where {@code tag = [A-Za-z_][A-Za-z0-9_]*} (선택, 빈 tag = {@code $$})
     */
    private static String tryReadDollarTag(String raw, int i) {
        if (i >= raw.length() || raw.charAt(i) != '$') return null;
        int j = i + 1;
        if (j < raw.length() && isAsciiIdStart(raw.charAt(j))) {
            while (j < raw.length() && isAsciiIdCont(raw.charAt(j))) j++;
        }
        if (j < raw.length() && raw.charAt(j) == '$') {
            return raw.substring(i, j + 1);
        }
        return null;
    }
}
