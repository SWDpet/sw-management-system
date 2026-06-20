package com.swmanager.system.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * excel-service-split (§6-5) 골든 캡처 — ExcelExportService 분리 前/後 출력 동일성 안전망.
 *
 * 운영DB 의존(generateDesignEstimate/InterimReport 는 docId→documentService.getDocumentById).
 * RUN_DB_TESTS=true + DB_URL 로만 실행(회사 PC 내부망).
 *
 * 용도: 각 Step(0/A/B/C) 후 실행해 stdout 의 sha256 + 구조 digest 가 Step 0 기록과
 * 동일한지 사람이 대조한다(바이트 동일 이동의 보조 검증, codex 권고).
 *
 *  - INTERIM docId=47 (기성내역서, generateInterimReport) — 실데이터 존재, 결정적(bytesEqual=true).
 *
 * ※ 설계(generateDesignEstimate) 골든은 제외: 운영DB에 design_estimate 섹션 보유 문서 0건
 *   (COMMENCE 문서 없음). 설계 경로는 "바이트 동일 코드 이동"(git diff 무변경) + 컴파일 +
 *   전체 테스트로 보장하고, 공용 헬퍼층(스타일/setStringDirect/Numeric/rounddown)은 본 INTERIM
 *   골든이 설계와 공유 코드를 함께 행사하므로 보조 커버한다.
 */
@SpringBootTest
@Transactional  // 웹 OSIV 처럼 lazy 컬렉션(Document.details) 접근 동안 세션 유지
@EnabledIfEnvironmentVariable(named = "RUN_DB_TESTS", matches = "true",
        disabledReason = "Live DB required; set RUN_DB_TESTS=true to run.")
class ExcelSplitGoldenIT {

    @Autowired ExcelExportService svc;

    static final int INTERIM_ID = 47;

    @Test
    void interim_determinism_and_digest() throws Exception {
        capture("INTERIM", svc.generateInterimReport(INTERIM_ID), svc.generateInterimReport(INTERIM_ID));
    }

    private void capture(String label, byte[] a, byte[] b) throws Exception {
        System.out.println("[GOLDEN] " + label + " bytesEqual=" + Arrays.equals(a, b)
                + " lenA=" + a.length + " lenB=" + b.length
                + " sha256A=" + sha(a) + " sha256B=" + sha(b));
        System.out.println("[GOLDEN] " + label + " structure=" + structure(a));
    }

    /** 시트별 시트명·(rows,maxCol)·머지수·셀값/수식/스타일핵심 해시 — 바이트 비결정 대비 구조 digest. */
    private static String structure(byte[] xlsx) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(xlsx))) {
            for (int s = 0; s < wb.getNumberOfSheets(); s++) {
                Sheet sh = wb.getSheetAt(s);
                StringBuilder cells = new StringBuilder();
                int maxCol = 0;
                for (Row row : sh) {
                    for (Cell c : row) {
                        maxCol = Math.max(maxCol, c.getColumnIndex());
                        cells.append(row.getRowNum()).append(',').append(c.getColumnIndex()).append('=');
                        switch (c.getCellType()) {
                            case STRING:  cells.append('S').append(c.getStringCellValue()); break;
                            case NUMERIC: cells.append('N').append(c.getNumericCellValue()); break;
                            case BOOLEAN: cells.append('B').append(c.getBooleanCellValue()); break;
                            case FORMULA: cells.append('F').append(c.getCellFormula()); break;
                            default: cells.append('_');
                        }
                        CellStyle cs = c.getCellStyle();
                        cells.append('@').append(cs.getFontIndexAsInt())
                             .append('/').append(cs.getFillForegroundColor())
                             .append('/').append(cs.getBorderBottom().getCode())
                             .append('/').append(cs.getAlignment().getCode())
                             .append(';');
                    }
                }
                sb.append('[').append(sh.getSheetName()).append(" rows=").append(sh.getPhysicalNumberOfRows())
                  .append(" maxCol=").append(maxCol)
                  .append(" merged=").append(sh.getNumMergedRegions())
                  .append(" cellsHash=").append(sha(cells.toString().getBytes("UTF-8"))).append("] ");
            }
        }
        return sb.toString();
    }

    private static String sha(byte[] data) throws Exception {
        byte[] h = MessageDigest.getInstance("SHA-256").digest(data);
        StringBuilder sb = new StringBuilder();
        for (byte x : h) sb.append(String.format("%02x", x));
        return sb.toString();
    }
}
