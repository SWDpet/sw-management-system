package com.swmanager.system.service;

import com.swmanager.system.domain.workplan.PerformanceSummary;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelExportService {

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
            titleFont.setFontName("맑은 고딕");
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setFontName("맑은 고딕");
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 10);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            CellStyle dataStyle = workbook.createCellStyle();
            Font dataFont = workbook.createFont();
            dataFont.setFontName("맑은 고딕");
            dataFont.setFontHeightInPoints((short) 10);
            dataStyle.setFont(dataFont);
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            CellStyle totalStyle = workbook.createCellStyle();
            Font totalFont = workbook.createFont();
            totalFont.setFontName("맑은 고딕");
            totalFont.setBold(true);
            totalFont.setFontHeightInPoints((short) 10);
            totalStyle.setFont(totalFont);
            totalStyle.setAlignment(HorizontalAlignment.CENTER);
            totalStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            totalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalStyle.setBorderBottom(BorderStyle.THIN);
            totalStyle.setBorderTop(BorderStyle.THIN);
            totalStyle.setBorderLeft(BorderStyle.THIN);
            totalStyle.setBorderRight(BorderStyle.THIN);

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
}
