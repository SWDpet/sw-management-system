package com.swmanager.system.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DesignEstimateWriter 단위테스트 (거대클래스 #4 견고화).
 *
 * Writer 의 package-private static 생성기를 직접 호출하고 실제 템플릿 리소스(design_estimate_*.xlsx)로
 * 생성된 xlsx 를 POI 로 다시 열어 핵심 셀·수식·항목 경계를 검증한다(골든 역할 + 커버리지).
 * 좌표·값은 템플릿과 1:1 결합(템플릿 변경 시 동반 갱신).
 */
class DesignEstimateWriterTest {

    private static Map<String, Object> item(String name, double rate, long unitPrice) {
        Map<String, Object> m = new HashMap<>();
        m.put("name", name);
        m.put("rate", rate);
        m.put("unitPrice", unitPrice);
        return m;
    }

    private Workbook open(byte[] xlsx) throws IOException {
        return new XSSFWorkbook(new ByteArrayInputStream(xlsx));
    }

    private String strCell(Sheet s, int row, int col) {
        Row r = s.getRow(row);
        if (r == null) return "";
        Cell c = r.getCell(col);
        return (c != null && c.getCellType() == CellType.STRING) ? c.getStringCellValue() : "";
    }

    // ===== TYPE_A =====

    @Test
    void typeA_fillsCoverAndSummary_andTotalFormula() throws IOException {
        List<Map<String, Object>> hw = List.of(item("DB서버", 8.0, 39201925L), item("AP서버", 8.0, 29352195L));
        List<Map<String, Object>> sw = List.of(item("DBMS", 16.0, 9160249L), item("GIS엔진", 16.0, 50233623L));

        byte[] xlsx = DesignEstimateWriter.generateFromTemplate(
                "밀양시 GIS 유지보수", "밀양시", 2026, "2026-06-01", "경상남도 밀양시",
                0.97, 1000, hw, sw);

        try (Workbook wb = open(xlsx)) {
            assertThat(wb.getNumberOfSheets()).isEqualTo(4);
            Sheet cover = wb.getSheetAt(0);
            assertThat(strCell(cover, 3, 0)).isEqualTo("2026년도");          // A4 연도
            assertThat(strCell(cover, 4, 0)).isEqualTo("밀양시 GIS 유지보수"); // A5 사업명
            assertThat(strCell(cover, 19, 5)).isEqualTo("밀양시");            // F20 지자체

            Sheet summary = wb.getSheetAt(2);
            assertThat(strCell(summary, 4, 1)).isEqualTo("DB서버");           // B5 HW1 품명
            assertThat(summary.getRow(4).getCell(3).getNumericCellValue()).isEqualTo(0.08); // D5 적용율
            assertThat(summary.getRow(4).getCell(4).getNumericCellValue()).isEqualTo(39201925.0); // E5 도입가
            assertThat(strCell(summary, 8, 1)).isEqualTo("DBMS");            // B9 SW1 품명

            // H11(10,7) 총계 수식: rounddownUnit>0 + bidRate<1 → ROUNDDOWN
            Cell h11 = summary.getRow(10).getCell(7);
            assertThat(h11.getCellType()).isEqualTo(CellType.FORMULA);
            assertThat(h11.getCellFormula()).startsWith("ROUNDDOWN").contains("0.97");
        }
    }

    @Test
    void typeA_singleItem_clearsSecondRow() throws IOException {
        List<Map<String, Object>> hw = List.of(item("DB서버", 8.0, 39201925L));
        byte[] xlsx = DesignEstimateWriter.generateFromTemplate(
                "단건사업", "군위군", 2026, "2026-06-01", "위치", 0.0, 0, hw, List.of());

        try (Workbook wb = open(xlsx)) {
            Sheet summary = wb.getSheetAt(2);
            assertThat(strCell(summary, 4, 1)).isEqualTo("DB서버");  // 1행만 채움
            assertThat(strCell(summary, 5, 1)).isEmpty();            // 2행 클리어
        }
    }

    @Test
    void typeA_hwOverflow_doesNotInvadeSwSection() throws IOException {
        // §8-3: HW 3개 → 템플릿 슬롯 2 초과분(3번째)은 누락, SW헤더(row6)·SW항목(row8) 침범 없음
        List<Map<String, Object>> hw = List.of(
                item("HW-A", 8.0, 100L), item("HW-B", 8.0, 200L), item("HW-C", 8.0, 300L));
        List<Map<String, Object>> sw = List.of(item("SW-X", 16.0, 400L));

        byte[] xlsx = DesignEstimateWriter.generateFromTemplate(
                "초과사업", "구미시", 2026, "2026-06-01", "위치", 0.0, 0, hw, sw);

        try (Workbook wb = open(xlsx)) {
            Sheet summary = wb.getSheetAt(2);
            assertThat(strCell(summary, 4, 1)).isEqualTo("HW-A");
            assertThat(strCell(summary, 5, 1)).isEqualTo("HW-B");
            assertThat(strCell(summary, 6, 1)).isNotEqualTo("HW-C"); // 3번째는 SW헤더 영역(row6) 침범 안 함
            assertThat(strCell(summary, 8, 1)).isEqualTo("SW-X");    // SW는 정상 위치
        }
    }

    // ===== TYPE_B / C / D 디스패치 + 핵심 셀 =====

    @Test
    void typeB_fillsCoverAndSummary() throws IOException {
        List<Map<String, Object>> hw = List.of(item("DB서버", 8.0, 39201925L));
        List<Map<String, Object>> sw = List.of(item("DBMS", 16.0, 9160249L));
        byte[] xlsx = DesignEstimateWriter.generateFromTypeBTemplate(
                "단양군 사업", "단양군", 2026, "2026-06-01", 0.97, 1000, hw, sw);

        try (Workbook wb = open(xlsx)) {
            Sheet cover = wb.getSheet("표지");
            assertThat(strCell(cover, 4, 0)).isEqualTo("단양군 사업");   // A5
            Sheet summary = wb.getSheet("총괄표");
            assertThat(strCell(summary, 3, 1)).isEqualTo("DB서버");      // B4 HW1
            assertThat(strCell(summary, 6, 1)).isEqualTo("DBMS");        // B7 SW1
        }
    }

    @Test
    void typeC_fillsCover() throws IOException {
        List<Map<String, Object>> hw = List.of(item("DB서버", 8.0, 39201925L));
        List<Map<String, Object>> sw = List.of(item("GIS엔진", 16.0, 50233623L));
        byte[] xlsx = DesignEstimateWriter.generateFromTypeCTemplate(
                "김포시 사업", "김포시", 2026, "2026-06-01", 0.97, hw, sw);

        try (Workbook wb = open(xlsx)) {
            assertThat(strCell(wb.getSheet("표지"), 3, 1)).isEqualTo("김포시 사업"); // B4
        }
    }

    @Test
    void typeD_fillsSwSummaryAndFormula() throws IOException {
        List<Map<String, Object>> sw = List.of(item("GeoNURIS", 16.0, 50233623L));
        byte[] xlsx = DesignEstimateWriter.generateFromSwTemplate(
                "김해시 사업", "김해시", 2026, "2026-06-01", 0.97, sw);

        try (Workbook wb = open(xlsx)) {
            Sheet summary = wb.getSheet("총괄표");
            assertThat(strCell(summary, 4, 1)).isEqualTo("GeoNURIS");    // B5
            Cell h8 = summary.getRow(7).getCell(7);
            assertThat(h8.getCellType()).isEqualTo(CellType.FORMULA);
            assertThat(h8.getCellFormula()).startsWith("ROUNDDOWN");
        }
    }

    @Test
    void typeD_emptySw_producesWorkbook() throws IOException {
        byte[] xlsx = DesignEstimateWriter.generateFromSwTemplate(
                "빈사업", "거창군", 2026, "2026-06-01", 0.0, List.of());
        try (Workbook wb = open(xlsx)) {
            assertThat(wb.getSheet("총괄표")).isNotNull();
            assertThat(strCell(wb.getSheet("총괄표"), 4, 1)).isEmpty();  // SW 품명 빈값
        }
    }
}
