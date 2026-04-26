package com.swmanager.system.service;

import com.swmanager.system.domain.workplan.Document;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 설계내역서 엑셀 — TYPE_A/B/D 셀 수식/표시값 POI 검증.
 *
 * 기획서 v6 §7-5/7-6/7-7, 개발계획서 v6 T5~T7 대응.
 * Spring context 없이 {@link ExcelExportService} 내부 메서드(package-private)를 직접 호출.
 * 원본 xlsx 템플릿은 test classpath 에서도 `templates/excel/*.xlsx` 로 접근 가능.
 */
class ExcelExportDesignEstimateTest {

    private final ExcelExportService service = new ExcelExportService();

    // ---------- 테스트용 항목 데이터 ----------

    private static List<Map<String, Object>> sampleHwItems() {
        return List.of(
            Map.of("type", "HW", "name", "DB서버", "rate", 8, "unitPrice", 32552885L, "remark", "OS포함"),
            Map.of("type", "HW", "name", "APP서버", "rate", 8, "unitPrice", 26394231L, "remark", "OS포함")
        );
    }

    private static List<Map<String, Object>> sampleSwItems() {
        return List.of(
            Map.of("type", "SW", "name", "DBMS", "rate", 16, "unitPrice", 8622115L, "remark", ""),
            Map.of("type", "SW", "name", "GIS 엔진", "rate", 16, "unitPrice", 69768751L, "remark", "")
        );
    }

    // ---------- POI 유틸 ----------

    private static Cell getCell(byte[] xlsx, int sheetIdx, int row, int col) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(xlsx))) {
            Sheet sheet = wb.getSheetAt(sheetIdx);
            Row r = sheet.getRow(row);
            return r != null ? r.getCell(col) : null;
        }
    }

    /** 이름 기반 sheet 접근 (TYPE_B/D 의 "총괄표" 시트 인덱스가 고정값이 아니므로). */
    private static Cell getCellByName(byte[] xlsx, String sheetName, int row, int col) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(xlsx))) {
            Sheet sheet = wb.getSheet(sheetName);
            if (sheet == null) {
                throw new AssertionError("Sheet '" + sheetName + "' not found. Available: " + listSheetNames(wb));
            }
            Row r = sheet.getRow(row);
            return r != null ? r.getCell(col) : null;
        }
    }

    private static String listSheetNames(XSSFWorkbook wb) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(wb.getSheetName(i));
        }
        return sb.append("]").toString();
    }

    // =====================================================
    // T5 — TYPE_A fillSummarySheet (총괄표 시트 index 2)
    // =====================================================

    @Test
    void typeA_91pct_cheonUnit_producesExpectedH11AndI11() throws Exception {
        byte[] xlsx = service.generateFromTemplate(
            new Document(), "테스트사업", "밀양시", 2026, "2026. 2.", "경상남도 밀양시 전역",
            0.91, 1000, /*vatSeparate*/ false,
            sampleHwItems(), sampleSwItems()
        );
        Cell h11 = getCell(xlsx, 2, 10, 7);
        Cell i11 = getCell(xlsx, 2, 10, 8);
        assertThat(h11.getCellFormula()).isEqualTo("ROUNDDOWN((H4+H8)*0.91,-3)");
        assertThat(i11.getStringCellValue()).isEqualTo("낙찰율 적용 91%\n천단위 절사");
    }

    @Test
    void typeA_85pct_manUnit_producesExpectedCells() throws Exception {
        byte[] xlsx = service.generateFromTemplate(
            new Document(), "P", "D", 2026, "2026. 3.", "L",
            0.85, 10000, false, sampleHwItems(), sampleSwItems()
        );
        Cell h11 = getCell(xlsx, 2, 10, 7);
        Cell i11 = getCell(xlsx, 2, 10, 8);
        assertThat(h11.getCellFormula()).isEqualTo("ROUNDDOWN((H4+H8)*0.85,-4)");
        assertThat(i11.getStringCellValue()).isEqualTo("낙찰율 적용 85%\n만단위 절사");
    }

    @Test
    void typeA_91pct_noRound_noLabel() throws Exception {
        byte[] xlsx = service.generateFromTemplate(
            new Document(), "P", "D", 2026, "2026. 3.", "L",
            0.91, 1, false, sampleHwItems(), sampleSwItems()
        );
        Cell h11 = getCell(xlsx, 2, 10, 7);
        Cell i11 = getCell(xlsx, 2, 10, 8);
        assertThat(h11.getCellFormula()).isEqualTo("ROUNDDOWN((H4+H8)*0.91,0)");
        assertThat(i11.getStringCellValue()).isEqualTo("낙찰율 적용 91%");
    }

    @Test
    void typeA_100pct_cheonUnit_dropsMultiplier() throws Exception {
        byte[] xlsx = service.generateFromTemplate(
            new Document(), "P", "D", 2026, "2026. 3.", "L",
            1.0, 1000, false, sampleHwItems(), sampleSwItems()
        );
        Cell h11 = getCell(xlsx, 2, 10, 7);
        Cell i11 = getCell(xlsx, 2, 10, 8);
        // FR-20: 낙찰 미적용 수식
        assertThat(h11.getCellFormula()).isEqualTo("ROUNDDOWN((H4+H8),-3)");
        assertThat(i11.getStringCellValue()).isEqualTo("천단위 절사");
    }

    @Test
    void typeA_100pct_noRound_emptyI11() throws Exception {
        byte[] xlsx = service.generateFromTemplate(
            new Document(), "P", "D", 2026, "2026. 3.", "L",
            1.0, 1, false, sampleHwItems(), sampleSwItems()
        );
        Cell h11 = getCell(xlsx, 2, 10, 7);
        Cell i11 = getCell(xlsx, 2, 10, 8);
        assertThat(h11.getCellFormula()).isEqualTo("ROUNDDOWN((H4+H8),0)");
        assertThat(i11.getStringCellValue()).isEmpty();
    }

    // =====================================================
    // T6 — TYPE_B
    // =====================================================

    @Test
    void typeB_91pct_cheonUnit_unifiesNakchalLyulAndRemovesBaekDan() throws Exception {
        byte[] xlsx = service.generateFromTypeBTemplate(
            "P", "D", 2026, "2026. 2.",
            0.91, 1000, sampleHwItems(), sampleSwItems()
        );
        // TYPE_B 는 이름으로 "총괄표" 시트 접근 (코드가 wb.getSheet("총괄표") 사용)
        Cell h11 = getCellByName(xlsx, "총괄표", 10, 7);
        Cell i11 = getCellByName(xlsx, "총괄표", 10, 8);
        assertThat(h11.getCellFormula()).isEqualTo("ROUNDDOWN((H10+H9)*0.91,-3)");
        // FR-18: 낙찰"률"→"율", §4-7: 백단위절사→천단위 절사
        String i11Val = i11.getStringCellValue();
        assertThat(i11Val).isEqualTo("낙찰율 적용 91%\n천단위 절사");
        assertThat(i11Val).doesNotContain("낙찰률");
        assertThat(i11Val).doesNotContain("백단위절사");
    }

    // =====================================================
    // T7 — TYPE_D
    // =====================================================

    @Test
    void typeD_91pct_sipwonUnit_producesExpectedCells() throws Exception {
        byte[] xlsx = service.generateFromSwTemplate(
            "P", "D", 2026, "2026. 2.",
            0.91, 10, sampleSwItems()
        );
        // TYPE_D 총괄표 시트 이름 기반 접근 (코드가 wb.getSheet("총괄표") 사용)
        Cell h8 = getCellByName(xlsx, "총괄표", 7, 7);
        Cell i8 = getCellByName(xlsx, "총괄표", 7, 8);
        assertThat(h8.getCellFormula()).isEqualTo("ROUNDDOWN((H6+H7)*0.91,-1)");
        assertThat(i8.getStringCellValue()).isEqualTo("낙찰율 적용 91%\n십원단위 절사");
    }
}
