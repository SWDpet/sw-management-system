package com.swmanager.system.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ExcelExportService.generatePerformanceReport 출력 골든 테스트 (S4-b).
 *
 * generatePerformanceReport 는 인자(details/totals)만으로 동작(@Autowired 필드 미사용)하므로
 * DB·컨텍스트 없이 직접 호출 가능. 셀 스타일 팩토리 추출(FONT 상수 + setBorders 재사용)
 * 리팩토링 전/후 헤더·합계 스타일이 동일함을 고정한다.
 */
class ExcelExportServicePerfStyleTest {

    /** 헤더 행(인덱스 4)·합계 셀의 폰트/채움/보더가 기대값과 동일한지 검증. */
    @Test
    void perfReport_headerAndTotalStyles_areStable() throws Exception {
        ExcelExportService svc = new ExcelExportService();
        byte[] xlsx = svc.generatePerformanceReport("tester", 2026, 1, 2026, 3, null, null);

        assertThat(xlsx).isNotEmpty();

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(xlsx))) {
            Sheet sheet = wb.getSheet("성과 리포트");
            assertThat(sheet).isNotNull();

            // 헤더 행 = index 4 (title 0, 빈 1, info 2, 빈 3, header 4)
            Row headerRow = sheet.getRow(4);
            assertThat(headerRow).isNotNull();
            Cell h0 = headerRow.getCell(0);
            assertThat(h0.getStringCellValue()).isEqualTo("기간");

            CellStyle hs = h0.getCellStyle();
            Font hf = wb.getFontAt(hs.getFontIndex());
            assertThat(hf.getFontName()).isEqualTo("맑은 고딕");
            assertThat(hf.getBold()).isTrue();
            assertThat(hf.getColor()).isEqualTo(IndexedColors.WHITE.getIndex());
            assertThat(hs.getFillForegroundColor()).isEqualTo(IndexedColors.DARK_BLUE.getIndex());
            assertThat(hs.getFillPattern()).isEqualTo(FillPatternType.SOLID_FOREGROUND);
            assertThat(hs.getAlignment()).isEqualTo(HorizontalAlignment.CENTER);
            // THIN 4면 보더 (setBorders 추출 후에도 동일해야 함)
            assertThat(hs.getBorderTop()).isEqualTo(BorderStyle.THIN);
            assertThat(hs.getBorderBottom()).isEqualTo(BorderStyle.THIN);
            assertThat(hs.getBorderLeft()).isEqualTo(BorderStyle.THIN);
            assertThat(hs.getBorderRight()).isEqualTo(BorderStyle.THIN);

            // 합계 행: "합계" 셀 = totalStyle (LIGHT_YELLOW 채움 + bold + THIN 보더)
            Cell totalCell = null;
            for (int r = 5; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                Cell c0 = row.getCell(0);
                if (c0 != null && "합계".equals(c0.getStringCellValue())) { totalCell = c0; break; }
            }
            assertThat(totalCell).as("합계 행").isNotNull();
            CellStyle ts = totalCell.getCellStyle();
            assertThat(wb.getFontAt(ts.getFontIndex()).getBold()).isTrue();
            assertThat(ts.getFillForegroundColor()).isEqualTo(IndexedColors.LIGHT_YELLOW.getIndex());
            assertThat(ts.getBorderTop()).isEqualTo(BorderStyle.THIN);
            assertThat(ts.getBorderRight()).isEqualTo(BorderStyle.THIN);
        }
    }
}
