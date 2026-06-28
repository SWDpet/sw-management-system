package com.swmanager.system.service;

import com.swmanager.system.domain.workplan.PerformanceSummary;
import com.swmanager.system.dto.DocumentDTO;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import static com.swmanager.system.service.ExcelStyleSupport.FONT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ExcelExportService {

    private static final Logger log = LoggerFactory.getLogger(ExcelExportService.class);

    // FONT 상수 + 공용 셀 헬퍼(str/toDouble/toLong/setCellValue/setStringDirect/setNumericDirect)는
    // ExcelStyleSupport 로 이동(§6-5 A) — 위 static import 로 무프리픽스 호출 유지.

    // documentService/messages 는 설계·기성 분리(§6-5 B/C)로 잔존 미사용 → 제거.
    // 데이터 조회·메시지는 각 분리 서비스가 자체 주입.
    @Autowired private DesignEstimateExcelService designEstimateExcelService;   // §6-5 B facade 위임
    @Autowired private InterimReportExcelService interimReportExcelService;     // §6-5 C facade 위임

    // ===== 절사 단위(rounddownUnit) 정책 — quotation 모듈과 동일 매핑 =====
    // 1000=백단위(-3), 10000=천단위(-4), 100000=만단위(-5), 1000000=십만단위(-6)

    /** sectionData 등에서 가져온 raw 값 → 정규화.
     *  null 또는 빈 값 → 0 (절사 안 함). 허용외 값 → 1000(백단위) fallback + WARN.
     *  허용 값: 1000(백)/10000(천)/100000(만)/1000000(십만) */
    static int normalizeRounddownUnit(Object raw) {  // [S3] package-private: 단위 테스트 대상
        if (raw == null || raw.toString().trim().isEmpty()) return 0;
        try {
            int v = (raw instanceof Number) ? ((Number) raw).intValue()
                    : Integer.parseInt(raw.toString().trim());
            if (v == 0) return 0;
            if (v == 1000 || v == 10000 || v == 100000 || v == 1000000) return v;
            log.warn("rounddownUnit 허용외 값({}), 1000(백단위)로 fallback", v);
        } catch (NumberFormatException e) {
            log.warn("rounddownUnit 파싱 실패 raw='{}', 1000(백단위)로 fallback", raw);
        }
        return 1000;
    }

    /** 절사 단위 → ROUNDDOWN 두번째 인자(digits). 1000→-3, 10000→-4, 100000→-5, 1000000→-6 */
    static int toRoundDigits(int unit) {  // [S3] package-private: 단위 테스트 대상
        return switch (unit) {
            case 10000 -> -4;
            case 100000 -> -5;
            case 1000000 -> -6;
            default -> -3; // 1000 fallback
        };
    }

    /** 절사 단위 → 비고 셀 라벨. */
    static String roundLabel(int unit) {  // [S3] package-private: 단위 테스트 대상
        return switch (unit) {
            case 10000 -> "천단위 절사";
            case 100000 -> "만단위 절사";
            case 1000000 -> "십만단위 절사";
            default -> "백단위 절사"; // 1000 fallback
        };
    }

    /**
     * 성과 리포트 엑셀 생성
     */
    public byte[] generatePerformanceReport(String userName, int fromYear, int fromMonth,
                                             int toYear, int toMonth,
                                             List<PerformanceSummary> details,
                                             java.util.Map<String, Object> totals) throws IOException {

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("성과 리포트");
            sheet.setDefaultColumnWidth(14);

            // 스타일 정의
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setFontName(FONT);
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setFontName(FONT);
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 10);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            setBorders(headerStyle);  // [S4-b] THIN 4면 — setBorders 헬퍼 재사용

            CellStyle dataStyle = workbook.createCellStyle();
            Font dataFont = workbook.createFont();
            dataFont.setFontName(FONT);
            dataFont.setFontHeightInPoints((short) 10);
            dataStyle.setFont(dataFont);
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            setBorders(dataStyle);  // [S4-b]

            CellStyle totalStyle = workbook.createCellStyle();
            Font totalFont = workbook.createFont();
            totalFont.setFontName(FONT);
            totalFont.setBold(true);
            totalFont.setFontHeightInPoints((short) 10);
            totalStyle.setFont(totalFont);
            totalStyle.setAlignment(HorizontalAlignment.CENTER);
            totalStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            totalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorders(totalStyle);  // [S4-b]

            int rowIdx = 0;

            // 제목
            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("(주)정도UIT SW지원부 성과 리포트");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

            rowIdx++; // 빈줄

            // 기본 정보
            Row infoRow1 = sheet.createRow(rowIdx++);
            infoRow1.createCell(0).setCellValue("직원명");
            infoRow1.createCell(1).setCellValue(userName != null ? userName : "-");
            infoRow1.createCell(3).setCellValue("조회기간");
            infoRow1.createCell(4).setCellValue(fromYear + "년 " + fromMonth + "월 ~ " + toYear + "년 " + toMonth + "월");

            rowIdx++; // 빈줄

            // 헤더
            Row headerRow = sheet.createRow(rowIdx++);
            String[] headers = {"기간", "설치", "패치", "장애", "업무지원", "기성", "준공", "점검계획", "점검완료", "일정준수율"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 데이터
            if (details != null) {
                for (PerformanceSummary ps : details) {
                    Row dataRow = sheet.createRow(rowIdx++);
                    createDataCell(dataRow, 0, ps.getPeriodYear() + "년 " + ps.getPeriodMonth() + "월", dataStyle);
                    createDataCell(dataRow, 1, safe(ps.getInstallCount()), dataStyle);
                    createDataCell(dataRow, 2, safe(ps.getPatchCount()), dataStyle);
                    createDataCell(dataRow, 3, safe(ps.getFaultCount()), dataStyle);
                    createDataCell(dataRow, 4, safe(ps.getSupportCount()), dataStyle);
                    createDataCell(dataRow, 5, safe(ps.getInterimCount()), dataStyle);
                    createDataCell(dataRow, 6, safe(ps.getCompletionCount()), dataStyle);
                    createDataCell(dataRow, 7, safe(ps.getInspectPlanCount()), dataStyle);
                    createDataCell(dataRow, 8, safe(ps.getInspectDoneCount()), dataStyle);

                    int planTotal = safe(ps.getPlanTotalCount());
                    int planOntime = safe(ps.getPlanOntimeCount());
                    String rate = planTotal > 0 ? Math.round(planOntime * 100.0 / planTotal) + "%" : "-";
                    createDataCell(dataRow, 9, rate, dataStyle);
                }
            }

            // 합계 행
            Row totalRow = sheet.createRow(rowIdx++);
            createDataCell(totalRow, 0, "합계", totalStyle);
            if (totals != null) {
                createDataCell(totalRow, 1, toInt(totals.get("installTotal")), totalStyle);
                createDataCell(totalRow, 2, toInt(totals.get("patchTotal")), totalStyle);
                createDataCell(totalRow, 3, toInt(totals.get("faultTotal")), totalStyle);
                createDataCell(totalRow, 4, toInt(totals.get("supportTotal")), totalStyle);
                createDataCell(totalRow, 5, toInt(totals.get("interimTotal")), totalStyle);
                createDataCell(totalRow, 6, toInt(totals.get("completionTotal")), totalStyle);
                createDataCell(totalRow, 7, "-", totalStyle);
                createDataCell(totalRow, 8, "-", totalStyle);
                createDataCell(totalRow, 9, totals.get("ontimeRate") + "%", totalStyle);
            }

            rowIdx += 2;

            // 수행율 요약
            Row rateHeader = sheet.createRow(rowIdx++);
            rateHeader.createCell(0).setCellValue("정기점검 수행율");
            rateHeader.createCell(1).setCellValue(totals != null ? totals.get("inspectRate") + "%" : "-");
            rateHeader.createCell(3).setCellValue("일정 준수율");
            rateHeader.createCell(4).setCellValue(totals != null ? totals.get("ontimeRate") + "%" : "-");

            // 열 너비 조정
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                if (sheet.getColumnWidth(i) < 3500) sheet.setColumnWidth(i, 3500);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    // =====================================================
    // 설계내역서 Excel → DesignEstimateExcelService 분리(§6-5 B).
    // public API 보존 위해 facade 위임(호출처 DocumentController 무수정).
    // =====================================================
    public byte[] generateDesignEstimate(Integer docId) throws IOException {
        return designEstimateExcelService.generateDesignEstimate(docId);
    }

    // =====================================================
    // 기성내역서 Excel → InterimReportExcelService 분리(§6-5 C).
    // public API 보존 위해 facade 위임(호출처 DocumentController 무수정).
    // =====================================================
    public byte[] generateInterimReport(Integer docId) throws IOException {
        return interimReportExcelService.generateInterimReport(docId);
    }

    // --- 스타일 헬퍼 ---
    // createTitle/Header/Body/TotalStyle 4종은 §6-5 split 후 호출처 0(성과/목록 빌더는 인라인 스타일 사용,
    // 설계/기성은 facade 위임)이라 삭제. setBorders 는 generatePerformanceReport 가 live 호출하므로 유지.
    private void setBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    // str/toDouble/toLong/setCellValue 는 ExcelStyleSupport 로 이동(§6-5 A, static import).

    // =====================================================
    // 성과 리포트 Excel (기존)
    // =====================================================

    private void createDataCell(Row row, int col, Object value, CellStyle style) {
        Cell cell = row.createCell(col);
        if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else {
            cell.setCellValue(value != null ? value.toString() : "-");
        }
        cell.setCellStyle(style);
    }

    private int safe(Integer val) {
        return val != null ? val : 0;
    }

    private int toInt(Object obj) {
        if (obj instanceof Number) return ((Number) obj).intValue();
        return 0;
    }

    // =====================================================
    // 사업문서 목록 Excel (D-01 검색 결과 다운로드)
    // =====================================================

    /** 문서 목록(검색 결과) 을 화면 컬럼 구성 그대로 xlsx 로 출력. */
    public byte[] generateDocumentList(List<DocumentDTO> docs) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("사업문서 목록");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setFontName(FONT);
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            CellStyle bodyStyle = workbook.createCellStyle();
            Font bodyFont = workbook.createFont();
            bodyFont.setFontName(FONT);
            bodyStyle.setFont(bodyFont);
            bodyStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            bodyStyle.setBorderBottom(BorderStyle.THIN);
            bodyStyle.setBorderLeft(BorderStyle.THIN);
            bodyStyle.setBorderRight(BorderStyle.THIN);

            String[] headers = {"No", "문서번호", "문서유형", "시도", "시군구", "시스템", "제목", "작성자", "작성일"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            int total = docs.size();
            int rowIdx = 1;
            for (int i = 0; i < docs.size(); i++) {
                DocumentDTO d = docs.get(i);
                Row row = sheet.createRow(rowIdx++);
                createDataCell(row, 0, total - i, bodyStyle);
                createDataCell(row, 1, d.getDocNo(), bodyStyle);
                createDataCell(row, 2, DocumentDTO.getDocTypeLabel(d.getDocType()), bodyStyle);
                createDataCell(row, 3, d.getCityNm(), bodyStyle);
                createDataCell(row, 4, d.getDistNm(), bodyStyle);
                createDataCell(row, 5, d.getSysNm(), bodyStyle);
                createDataCell(row, 6, d.getTitle(), bodyStyle);
                createDataCell(row, 7, d.getAuthorName(), bodyStyle);
                String created = d.getCreatedAt();
                createDataCell(row, 8, (created != null && created.length() >= 10) ? created.substring(0, 10) : created, bodyStyle);
            }

            int[] widths = {1500, 5500, 3000, 3000, 3500, 6000, 12000, 3000, 3500};
            for (int i = 0; i < widths.length; i++) sheet.setColumnWidth(i, widths[i]);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        }
    }
}
