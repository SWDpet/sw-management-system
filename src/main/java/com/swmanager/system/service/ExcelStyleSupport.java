package com.swmanager.system.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * 엑셀 생성 공용 무상태 헬퍼 (excel-service-split §6-5).
 *
 * ExcelExportService 가 거대화되어 문서타입별 서비스(DesignEstimateExcelService,
 * InterimReportExcelService)로 분리되면서, 설계/기성 클러스터가 공유하던 폰트 상수와
 * 원시 셀 헬퍼를 한 곳에 모은다. 인스턴스 상태가 없어 {@code static} 유틸로 둔다
 * (주입 불필요 → {@code new ExcelExportService()} 단위테스트 NPE 차단).
 *
 * 사용처는 {@code import static ...ExcelStyleSupport.*} 로 무프리픽스 호출 → 분리 시
 * 메서드 본문을 바이트 그대로 옮길 수 있다(동작 보존). package-private 으로 두어 외부 노출 0.
 */
final class ExcelStyleSupport {

    private ExcelStyleSupport() {}

    /** 엑셀 기본 폰트. */
    static final String FONT = "맑은 고딕";

    static String str(Object obj) {
        return obj != null ? obj.toString() : "";
    }

    static double toDouble(Object obj) {
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        if (obj instanceof String) {
            try { return Double.parseDouble((String) obj); } catch (NumberFormatException e) { return 0; }
        }
        return 0;
    }

    static long toLong(Object obj) {
        if (obj instanceof Number) return ((Number) obj).longValue();
        if (obj instanceof String) {
            try { return Long.parseLong((String) obj); } catch (NumberFormatException e) { return 0; }
        }
        return 0;
    }

    static void setCellValue(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    /** 문자열 값을 셀에 기록하되 기존 셀 스타일(서식)은 그대로 보존. */
    static void setStringDirect(Sheet sheet, int rowIdx, int colIdx, String val) {
        if (sheet == null) return;
        Row row = sheet.getRow(rowIdx);
        if (row == null) row = sheet.createRow(rowIdx);
        Cell oldCell = row.getCell(colIdx);
        CellStyle keepStyle = (oldCell != null) ? oldCell.getCellStyle() : null;
        if (oldCell != null) row.removeCell(oldCell);
        Cell cell = row.createCell(colIdx, CellType.STRING);
        if (keepStyle != null) cell.setCellStyle(keepStyle);
        cell.setCellValue(val == null ? "" : val);
    }

    /** 숫자 값을 셀에 기록하되 기존 셀 스타일(서식)은 그대로 보존. */
    static void setNumericDirect(Sheet sheet, int rowIdx, int colIdx, double val) {
        if (sheet == null) return;
        Row row = sheet.getRow(rowIdx);
        if (row == null) row = sheet.createRow(rowIdx);
        Cell oldCell = row.getCell(colIdx);
        CellStyle keepStyle = (oldCell != null) ? oldCell.getCellStyle() : null;
        if (oldCell != null) row.removeCell(oldCell);
        Cell cell = row.createCell(colIdx, CellType.NUMERIC);
        if (keepStyle != null) cell.setCellStyle(keepStyle);
        cell.setCellValue(val);
    }
}
