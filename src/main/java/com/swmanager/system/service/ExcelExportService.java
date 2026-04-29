package com.swmanager.system.service;

import com.swmanager.system.domain.workplan.Document;
import com.swmanager.system.domain.workplan.DocumentDetail;
import com.swmanager.system.domain.workplan.PerformanceSummary;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Service
public class ExcelExportService {

    private static final Logger log = LoggerFactory.getLogger(ExcelExportService.class);

    @Autowired private DocumentService documentService;
    @Autowired private com.swmanager.system.i18n.MessageResolver messages;

    // ===== 절사 단위(rounddownUnit) 정책 — quotation 모듈과 동일 매핑 =====
    // 1000=백단위(-3), 10000=천단위(-4), 100000=만단위(-5), 1000000=십만단위(-6)

    /** sectionData 등에서 가져온 raw 값 → 정규화.
     *  null 또는 빈 값 → 0 (절사 안 함). 허용외 값 → 1000(백단위) fallback + WARN.
     *  허용 값: 1000(백)/10000(천)/100000(만)/1000000(십만) */
    private static int normalizeRounddownUnit(Object raw) {
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
    private static int toRoundDigits(int unit) {
        return switch (unit) {
            case 10000 -> -4;
            case 100000 -> -5;
            case 1000000 -> -6;
            default -> -3; // 1000 fallback
        };
    }

    /** 절사 단위 → 비고 셀 라벨. */
    private static String roundLabel(int unit) {
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

    // =====================================================
    // 설계내역서 TYPE_A (4 sheets: 표지, 갑지, 총괄표, 유지보수등급측정표)
    // =====================================================

    @SuppressWarnings("unchecked")
    public byte[] generateDesignEstimate(Integer docId) throws IOException {
        Document doc = documentService.getDocumentById(docId);

        // design_estimate 섹션 데이터 조회
        Map<String, Object> estData = doc.getDetails().stream()
                .filter(d -> "design_estimate".equals(d.getSectionKey()))
                .findFirst()
                .map(DocumentDetail::getSectionData)
                .orElseThrow(() -> new IllegalStateException(messages.get("error.export.design_data_empty")));

        String estimateType = (String) estData.getOrDefault("estimateType", "TYPE_A");
        String designDate = (String) estData.getOrDefault("designDate", "");
        String location = (String) estData.getOrDefault("location", "");
        double bidRate = toDouble(estData.get("bidRate")) / 100.0;
        int rounddownUnit = normalizeRounddownUnit(estData.get("rounddownUnit"));
        boolean vatSeparate = Boolean.TRUE.equals(estData.get("vatSeparate"));
        List<Map<String, Object>> items = (List<Map<String, Object>>) estData.getOrDefault("items", List.of());

        // 프로젝트명
        String projNm = doc.getProject() != null ? doc.getProject().getProjNm() : "사업명";
        String orgNm = doc.getProject() != null && doc.getProject().getOrgNm() != null ?
                doc.getProject().getOrgNm() : "";

        // HW/SW 항목 분리
        List<Map<String, Object>> hwItems = items.stream()
                .filter(i -> "HW".equals(i.get("type"))).toList();
        List<Map<String, Object>> swItems = items.stream()
                .filter(i -> "SW".equals(i.get("type"))).toList();

        // 프로젝트 추가 정보
        String distNm = doc.getProject() != null && doc.getProject().getDistNm() != null ?
                doc.getProject().getDistNm() : "";
        int year = doc.getProject() != null && doc.getProject().getYear() != null ?
                doc.getProject().getYear() : java.time.LocalDate.now().getYear();

        if ("TYPE_D".equals(estimateType)) {
            // TYPE_D: SW전용 (김해시/강릉시 등) - 템플릿 기반 생성
            return generateFromSwTemplate(projNm, distNm, year, designDate, bidRate, swItems);
        }
        if ("TYPE_B".equals(estimateType)) {
            // TYPE_B: 중간형 (단양군 등) - 단양군 템플릿 기반
            return generateFromTypeBTemplate(projNm, distNm, year, designDate, bidRate, rounddownUnit, hwItems, swItems);
        }
        if ("TYPE_C".equals(estimateType)) {
            // TYPE_C: 복합형 (김포시 등) - 김포시 템플릿 기반
            return generateFromTypeCTemplate(projNm, distNm, year, designDate, bidRate, hwItems, swItems);
        }

        // TYPE_A: 기존 템플릿 기반 생성 (밀양시 형식)
        return generateFromTemplate(doc, projNm, distNm, year, designDate, location,
                bidRate, rounddownUnit, vatSeparate, hwItems, swItems);
    }

    /**
     * 원본 Excel 템플릿(design_estimate_template.xlsx)을 로드하여
     * DB 데이터만 채워넣는 방식으로 설계내역서 생성 (TYPE_A/B/C)
     *
     * 템플릿 시트 구조:
     *   표지 - A4(연도), A5(사업명), A15(날짜), F20(지자체명)
     *   갑지 - B1(날짜), A5(사업명 참조), C6(위치), C8(용역개요), C13(총용역비 한글금액)
     *   총괄표 - HW/SW 항목별 품명, 적용율, 도입가
     *   유지보수등급측정표(전산장비) - 비어있음 (그대로 유지)
     */
    private byte[] generateFromTemplate(Document doc, String projNm, String distNm, int year,
                                         String designDate, String location,
                                         double bidRate, int rounddownUnit, boolean vatSeparate,
                                         List<Map<String, Object>> hwItems,
                                         List<Map<String, Object>> swItems) throws IOException {

        ClassPathResource templateResource = new ClassPathResource("templates/excel/design_estimate_template.xlsx");
        try (InputStream is = templateResource.getInputStream();
             XSSFWorkbook wb = new XSSFWorkbook(is)) {

            // === 시트0: 표지 ===
            fillCoverSheet(wb, projNm, distNm, year, designDate);

            // === 시트1: 갑지 ===
            fillGapjiSheet(wb, designDate, location);

            // === 시트2: 총괄표 ===
            fillSummarySheet(wb, hwItems, swItems, bidRate, rounddownUnit);

            // === 시트3: 유지보수등급측정표(전산장비) - 비어있으므로 그대로 유지 ===

            // 수식 재계산 강제
            wb.setForceFormulaRecalculation(true);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    /**
     * 표지 시트: 연도, 사업명, 날짜, 지자체명만 교체
     */
    private void fillCoverSheet(XSSFWorkbook wb, String projNm, String distNm, int year, String designDate) {
        Sheet sheet = wb.getSheetAt(0); // 표지

        // A4 (row3): 연도 (예: "2026년도")
        setCellKeepStyle(sheet, 3, 0, year + "년도");

        // A5 (row4): 사업명 (병합 A5:L5)
        setCellKeepStyle(sheet, 4, 0, projNm);

        // A15 (row14): 날짜
        if (designDate != null && !designDate.isEmpty()) {
            setCellKeepStyle(sheet, 14, 0, designDate);
        }

        // F20 (row19, col5): 지자체명
        if (distNm != null && !distNm.isEmpty()) {
            setCellKeepStyle(sheet, 19, 5, distNm);
        }
    }

    /**
     * 갑지 시트: 날짜, 위치, 용역개요 교체
     * B1(날짜), C6(위치), C8(용역개요)
     */
    private void fillGapjiSheet(XSSFWorkbook wb, String designDate, String location) {
        Sheet sheet = wb.getSheetAt(1); // 갑지

        // B1 (row0, col1): 설계년월일 날짜
        if (designDate != null && !designDate.isEmpty()) {
            setCellKeepStyle(sheet, 0, 1, designDate);
        }

        // C6 (row5, col2): 위치
        if (location != null && !location.isEmpty()) {
            setCellKeepStyle(sheet, 5, 2, location);
        }
    }

    /**
     * 총괄표 시트: HW/SW 항목의 품명(B열), 적용율(D열), 도입가(E열) 교체
     *
     * 원본 구조:
     *   Row2 (A3): "1. 하드웨어 유지보수" 헤더
     *   Row3 (A4): 열 헤더 (구분/산출내역/적용율/도입가/...)
     *   Row4 (A5): HW 항목1 (1.0 / DB서버 / 수식 / 0.08 / 39201925 ...)
     *   Row5 (A6): HW 항목2 (2.0 / AP서버 / 수식 / 0.08 / 29352195 ...)
     *   Row6 (A7): "2. 소프트웨어 유지보수" 헤더
     *   Row7 (A8): 열 헤더
     *   Row8 (A9): SW 항목1 (1.0 / DBMS / 수식 / 0.16 / 9160249 ...)
     *   Row9 (A10): SW 항목2 (2.0 / GIS엔진 / 수식 / 0.16 / 50233623 ...)
     *   Row10 (A11): "총계(부가세포함)" (수식)
     */
    private void fillSummarySheet(XSSFWorkbook wb, List<Map<String, Object>> hwItems,
                                   List<Map<String, Object>> swItems, double bidRate, int rounddownUnit) {
        Sheet sheet = wb.getSheetAt(2); // 총괄표

        // HW 항목 채우기 (row4=index4, row5=index5 ... 템플릿에 2행 있음)
        int hwDataStartRow = 4; // 0-based (Excel row 5)
        for (int i = 0; i < hwItems.size(); i++) {
            int rowIdx = hwDataStartRow + i;
            Map<String, Object> item = hwItems.get(i);

            // A열: 번호
            setCellKeepStyle(sheet, rowIdx, 0, (double)(i + 1));
            // B열: 품명(산출내역)
            setCellKeepStyle(sheet, rowIdx, 1, str(item.get("name")));
            // D열: 적용율 (예: 0.08)
            double rate = toDouble(item.get("rate")) / 100.0;
            setCellKeepStyle(sheet, rowIdx, 3, rate);
            // E열: 도입가
            long unitPrice = toLong(item.get("unitPrice"));
            setCellKeepStyle(sheet, rowIdx, 4, (double) unitPrice);
            // H열(유지관리소계): 템플릿 default `=SUM(F:G)/12*12` → 단순 `=F+G` 로 정리
            simplifyMaintFormula(sheet, rowIdx);
        }
        // 템플릿보다 항목이 적으면 빈 행 클리어
        int hwTemplateRows = 2; // 원본 템플릿의 HW 데이터 행 수
        for (int i = hwItems.size(); i < hwTemplateRows; i++) {
            int rowIdx = hwDataStartRow + i;
            clearDataRow(sheet, rowIdx);
        }

        // SW 항목 채우기 (row8=index8, row9=index9 ... 템플릿에 2행 있음)
        int swDataStartRow = 8; // 0-based (Excel row 9)
        for (int i = 0; i < swItems.size(); i++) {
            int rowIdx = swDataStartRow + i;
            Map<String, Object> item = swItems.get(i);

            setCellKeepStyle(sheet, rowIdx, 0, (double)(i + 1));
            setCellKeepStyle(sheet, rowIdx, 1, str(item.get("name")));
            double rate = toDouble(item.get("rate")) / 100.0;
            setCellKeepStyle(sheet, rowIdx, 3, rate);
            long unitPrice = toLong(item.get("unitPrice"));
            setCellKeepStyle(sheet, rowIdx, 4, (double) unitPrice);
            simplifyMaintFormula(sheet, rowIdx);
        }
        int swTemplateRows = 2; // 원본 템플릿의 SW 데이터 행 수
        for (int i = swItems.size(); i < swTemplateRows; i++) {
            int rowIdx = swDataStartRow + i;
            clearDataRow(sheet, rowIdx);
        }

        // H11 (총계 수식) + I11 (비고 라벨) — 사용자 입력 bidRate · rounddownUnit 동적 적용
        // 템플릿 default (예: 0.9696, -3, "백단위 절사 / 낙찰율 적용 97%") 는 무조건 덮어쓰기.
        // rounddownUnit == 0 (UI 미선택) 인 경우 ROUNDDOWN 미적용 + 라벨에서 절사 표기 제거.
        Row totalRow = sheet.getRow(10);
        if (totalRow == null) totalRow = sheet.createRow(10);
        boolean applyRound = rounddownUnit > 0;
        Cell h11 = totalRow.getCell(7);
        if (h11 == null) h11 = totalRow.createCell(7);
        if (applyRound) {
            int digits = toRoundDigits(rounddownUnit);
            if (bidRate > 0 && bidRate < 1.0) {
                h11.setCellFormula("ROUNDDOWN((H4+H8)*" + bidRate + "," + digits + ")");
            } else {
                h11.setCellFormula("ROUNDDOWN(H4+H8," + digits + ")");
            }
        } else {
            if (bidRate > 0 && bidRate < 1.0) {
                h11.setCellFormula("(H4+H8)*" + bidRate);
            } else {
                h11.setCellFormula("H4+H8");
            }
        }
        Cell i11 = totalRow.getCell(8);
        if (i11 == null) i11 = totalRow.createCell(8);
        StringBuilder remark = new StringBuilder();
        if (applyRound) remark.append(roundLabel(rounddownUnit));
        if (bidRate > 0 && bidRate < 1.0) {
            if (remark.length() > 0) remark.append("\n");
            remark.append("낙찰율 적용 ").append(Math.round(bidRate * 100)).append("%");
        }
        i11.setCellValue(remark.toString());
    }

    /** H열(col7) 유지관리소계 수식을 단순 `=F{n}+G{n}` 으로 정리 (템플릿 default `=SUM(F:G)/12*12` 가독성 개선). */
    private void simplifyMaintFormula(Sheet sheet, int rowIdx) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) return;
        Cell cell = row.getCell(7);
        if (cell == null) cell = row.createCell(7);
        int excelRow = rowIdx + 1; // 0-based → Excel 1-based
        cell.setCellFormula("F" + excelRow + "+G" + excelRow);
    }

    /**
     * 셀 값을 교체하되 기존 스타일은 유지
     */
    private void setCellKeepStyle(Sheet sheet, int rowIdx, int colIdx, String value) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) row = sheet.createRow(rowIdx);
        Cell cell = row.getCell(colIdx);
        if (cell == null) cell = row.createCell(colIdx);
        CellStyle existingStyle = cell.getCellStyle();
        cell.setCellValue(value != null ? value : "");
        if (existingStyle != null) cell.setCellStyle(existingStyle);
    }

    /**
     * 셀 값을 숫자로 교체하되 기존 스타일 유지
     */
    private void setCellKeepStyle(Sheet sheet, int rowIdx, int colIdx, double value) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) row = sheet.createRow(rowIdx);
        Cell cell = row.getCell(colIdx);
        if (cell == null) cell = row.createCell(colIdx);
        CellStyle existingStyle = cell.getCellStyle();
        cell.setCellValue(value);
        if (existingStyle != null) cell.setCellStyle(existingStyle);
    }

    /**
     * 총괄표 데이터 행 클리어 (템플릿보다 항목이 적을 때)
     */
    private void clearDataRow(Sheet sheet, int rowIdx) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) return;
        // A, B, D, E 열만 클리어 (수식이 있는 C, F, G, H열은 유지)
        for (int col : new int[]{0, 1, 3, 4}) {
            Cell cell = row.getCell(col);
            if (cell != null) {
                CellStyle style = cell.getCellStyle();
                cell.setBlank();
                cell.setCellStyle(style);
            }
        }
    }

    // --- Sheet 1: 표지 ---
    private void createCoverSheet(XSSFWorkbook wb, String projNm, String designDate,
                                   String location, CellStyle titleStyle, CellStyle subTitleStyle, CellStyle bodyStyle) {
        Sheet sheet = wb.createSheet("표지");
        sheet.setDefaultColumnWidth(15);

        // 빈 공간
        int r = 8;
        Row row = sheet.createRow(r);
        Cell c = row.createCell(1);
        c.setCellValue("설 계 내 역 서");
        c.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 1, 5));

        r += 3;
        row = sheet.createRow(r);
        c = row.createCell(1); c.setCellValue("사 업 명 :  " + projNm); c.setCellStyle(subTitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 1, 5));

        r += 2;
        row = sheet.createRow(r);
        c = row.createCell(1); c.setCellValue("위      치 :  " + location); c.setCellStyle(bodyStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 1, 5));

        r += 2;
        row = sheet.createRow(r);
        c = row.createCell(1); c.setCellValue("설 계 일 :  " + designDate); c.setCellStyle(bodyStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 1, 5));

        r += 5;
        row = sheet.createRow(r);
        c = row.createCell(1); c.setCellValue("(주) 정 도 U I T"); c.setCellStyle(subTitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 1, 5));

        sheet.setColumnWidth(0, 2000);
        for (int i = 1; i <= 5; i++) sheet.setColumnWidth(i, 4000);
    }

    // --- Sheet 2: 갑지 (총괄 요약) ---
    private void createSummaryCoverSheet(XSSFWorkbook wb, String projNm,
                                          List<Map<String, Object>> hwItems, List<Map<String, Object>> swItems,
                                          double bidRate, boolean vatSeparate,
                                          CellStyle titleStyle, CellStyle headerStyle, CellStyle bodyStyle,
                                          CellStyle bodyBoldStyle, CellStyle numberStyle,
                                          CellStyle totalStyle, CellStyle totalNumberStyle) {
        Sheet sheet = wb.createSheet("갑지");
        sheet.setColumnWidth(0, 1500);
        sheet.setColumnWidth(1, 6000);
        sheet.setColumnWidth(2, 5500);
        sheet.setColumnWidth(3, 5500);
        sheet.setColumnWidth(4, 5500);

        int r = 1;
        Row row = sheet.createRow(r);
        Cell c = row.createCell(1);
        c.setCellValue(projNm + " 설계내역서");
        c.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 1, 4));

        r += 2;
        // 헤더
        row = sheet.createRow(r);
        setCellValue(row, 1, "구분", headerStyle);
        setCellValue(row, 2, "공급가액", headerStyle);
        setCellValue(row, 3, "부가세", headerStyle);
        setCellValue(row, 4, "합계", headerStyle);

        // HW 항목 합산 (각 항목: unitPrice * rate / 100)
        // 수식은 총괄표 시트를 참조하도록 설계
        // 갑지에서는 총괄표!합계셀을 참조하는 것이 이상적이나, 직접 계산
        int hwStartRow = r + 1;
        r++;
        row = sheet.createRow(r);
        setCellValue(row, 1, "1. 하드웨어 유지보수", bodyBoldStyle);
        // 공급가액 = SUM(각 HW 항목의 도입가 × 적용율)
        // 여기서는 총괄표 시트의 합계를 참조하는 수식 사용
        String hwSupplyRef = "'총괄표'!E" + (hwItems.size() + 5); // 총괄표 시트의 HW 소계행
        row.createCell(2).setCellFormula("'총괄표'!E" + (hwItems.size() + 5));
        row.getCell(2).setCellStyle(numberStyle);
        // 부가세 = 공급가 * 10%
        row.createCell(3).setCellFormula("C" + (r + 1) + "*0.1");
        row.getCell(3).setCellStyle(numberStyle);
        // 합계 = 공급가 + 부가세
        row.createCell(4).setCellFormula("C" + (r + 1) + "+D" + (r + 1));
        row.getCell(4).setCellStyle(numberStyle);

        int swDataRow = r + 1;
        r++;
        row = sheet.createRow(r);
        setCellValue(row, 1, "2. 소프트웨어 유지보수", bodyBoldStyle);
        int swSummaryRow = hwItems.size() + 5 + swItems.size() + 2; // 총괄표의 SW 소계행
        row.createCell(2).setCellFormula("'총괄표'!E" + swSummaryRow);
        row.getCell(2).setCellStyle(numberStyle);
        row.createCell(3).setCellFormula("C" + (r + 1) + "*0.1");
        row.getCell(3).setCellStyle(numberStyle);
        row.createCell(4).setCellFormula("C" + (r + 1) + "+D" + (r + 1));
        row.getCell(4).setCellStyle(numberStyle);

        // 소계
        r++;
        row = sheet.createRow(r);
        setCellValue(row, 1, "소계", totalStyle);
        row.createCell(2).setCellFormula("C" + hwStartRow + "+C" + swDataRow);
        row.getCell(2).setCellStyle(totalNumberStyle);
        row.createCell(3).setCellFormula("D" + hwStartRow + "+D" + swDataRow);
        row.getCell(3).setCellStyle(totalNumberStyle);
        row.createCell(4).setCellFormula("E" + hwStartRow + "+E" + swDataRow);
        row.getCell(4).setCellStyle(totalNumberStyle);

        // 낙찰율 적용
        r++;
        row = sheet.createRow(r);
        setCellValue(row, 1, "낙찰율 적용(" + Math.round(bidRate * 100) + "%)", bodyBoldStyle);
        // 낙찰금액 = ROUNDDOWN(소계 * 낙찰율, -3) (천원 단위 절사)
        row.createCell(2).setCellFormula("ROUNDDOWN(C" + (r) + "*" + bidRate + ",-3)");
        row.getCell(2).setCellStyle(numberStyle);
        row.createCell(3).setCellFormula("ROUNDDOWN(D" + (r) + "*" + bidRate + ",-3)");
        row.getCell(3).setCellStyle(numberStyle);
        row.createCell(4).setCellFormula("C" + (r + 1) + "+D" + (r + 1));
        row.getCell(4).setCellStyle(numberStyle);

        // 최종 합계
        r++;
        row = sheet.createRow(r);
        setCellValue(row, 1, "계약금액", totalStyle);
        row.createCell(2).setCellFormula("C" + r);
        row.getCell(2).setCellStyle(totalNumberStyle);
        row.createCell(3).setCellFormula("D" + r);
        row.getCell(3).setCellStyle(totalNumberStyle);
        row.createCell(4).setCellFormula("E" + r);
        row.getCell(4).setCellStyle(totalNumberStyle);
    }

    // --- Sheet 3: 총괄표 (상세 내역) ---
    private void createSummaryTableSheet(XSSFWorkbook wb,
                                          List<Map<String, Object>> hwItems, List<Map<String, Object>> swItems,
                                          double bidRate, boolean vatSeparate,
                                          CellStyle headerStyle, CellStyle bodyStyle, CellStyle numberStyle,
                                          CellStyle percentStyle, CellStyle totalStyle, CellStyle totalNumberStyle,
                                          CellStyle bodyBoldStyle) {
        Sheet sheet = wb.createSheet("총괄표");
        sheet.setColumnWidth(0, 1500);  // No
        sheet.setColumnWidth(1, 2000);  // 구분
        sheet.setColumnWidth(2, 8000);  // 품명
        sheet.setColumnWidth(3, 4500);  // 도입가(부가세포함)
        sheet.setColumnWidth(4, 4500);  // 유지보수대가
        sheet.setColumnWidth(5, 3000);  // 적용율
        sheet.setColumnWidth(6, 4000);  // 비고

        int r = 1;
        Row row = sheet.createRow(r);
        setCellValue(row, 0, "유지보수 총괄표", headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 6));

        r += 2;
        // 헤더
        row = sheet.createRow(r);
        setCellValue(row, 0, "No", headerStyle);
        setCellValue(row, 1, "구분", headerStyle);
        setCellValue(row, 2, "품명", headerStyle);
        setCellValue(row, 3, "도입가(부가세포함)", headerStyle);
        setCellValue(row, 4, "유지보수대가", headerStyle);
        setCellValue(row, 5, "적용율", headerStyle);
        setCellValue(row, 6, "비고", headerStyle);
        int headerRow = r;

        // HW 항목
        int hwStart = r + 1;
        for (int i = 0; i < hwItems.size(); i++) {
            r++;
            row = sheet.createRow(r);
            Map<String, Object> item = hwItems.get(i);
            setCellValue(row, 0, String.valueOf(i + 1), bodyStyle);
            setCellValue(row, 1, "HW", bodyStyle);
            setCellValue(row, 2, str(item.get("name")), bodyStyle);

            long price = toLong(item.get("unitPrice"));
            double rate = toDouble(item.get("rate")) / 100.0;

            Cell priceCell = row.createCell(3);
            priceCell.setCellValue(price);
            priceCell.setCellStyle(numberStyle);

            // 유지보수대가 = 도입가 × 적용율 (수식)
            Cell maintCell = row.createCell(4);
            maintCell.setCellFormula("D" + (r + 1) + "*F" + (r + 1));
            maintCell.setCellStyle(numberStyle);

            Cell rateCell = row.createCell(5);
            rateCell.setCellValue(rate);
            rateCell.setCellStyle(percentStyle);

            setCellValue(row, 6, str(item.get("remark")), bodyStyle);
        }

        // HW 소계
        r++;
        int hwEndRow = r; // 0-indexed, Excel row = r+1
        row = sheet.createRow(r);
        setCellValue(row, 0, "", totalStyle);
        setCellValue(row, 1, "", totalStyle);
        setCellValue(row, 2, "하드웨어 소계", totalStyle);
        Cell hwTotalPrice = row.createCell(3);
        if (!hwItems.isEmpty()) {
            hwTotalPrice.setCellFormula("SUM(D" + (hwStart + 1) + ":D" + r + ")");
        } else {
            hwTotalPrice.setCellValue(0);
        }
        hwTotalPrice.setCellStyle(totalNumberStyle);
        Cell hwTotalMaint = row.createCell(4);
        if (!hwItems.isEmpty()) {
            hwTotalMaint.setCellFormula("SUM(E" + (hwStart + 1) + ":E" + r + ")");
        } else {
            hwTotalMaint.setCellValue(0);
        }
        hwTotalMaint.setCellStyle(totalNumberStyle);
        setCellValue(row, 5, "", totalStyle);
        setCellValue(row, 6, "", totalStyle);

        // SW 항목
        int swStart = r + 1;
        for (int i = 0; i < swItems.size(); i++) {
            r++;
            row = sheet.createRow(r);
            Map<String, Object> item = swItems.get(i);
            setCellValue(row, 0, String.valueOf(i + 1), bodyStyle);
            setCellValue(row, 1, "SW", bodyStyle);
            setCellValue(row, 2, str(item.get("name")), bodyStyle);

            long price = toLong(item.get("unitPrice"));
            double rate = toDouble(item.get("rate")) / 100.0;

            Cell priceCell = row.createCell(3);
            priceCell.setCellValue(price);
            priceCell.setCellStyle(numberStyle);

            Cell maintCell = row.createCell(4);
            maintCell.setCellFormula("D" + (r + 1) + "*F" + (r + 1));
            maintCell.setCellStyle(numberStyle);

            Cell rateCell = row.createCell(5);
            rateCell.setCellValue(rate);
            rateCell.setCellStyle(percentStyle);

            setCellValue(row, 6, str(item.get("remark")), bodyStyle);
        }

        // SW 소계
        r++;
        row = sheet.createRow(r);
        setCellValue(row, 0, "", totalStyle);
        setCellValue(row, 1, "", totalStyle);
        setCellValue(row, 2, "소프트웨어 소계", totalStyle);
        Cell swTotalPrice = row.createCell(3);
        if (!swItems.isEmpty()) {
            swTotalPrice.setCellFormula("SUM(D" + (swStart + 1) + ":D" + r + ")");
        } else {
            swTotalPrice.setCellValue(0);
        }
        swTotalPrice.setCellStyle(totalNumberStyle);
        Cell swTotalMaint = row.createCell(4);
        if (!swItems.isEmpty()) {
            swTotalMaint.setCellFormula("SUM(E" + (swStart + 1) + ":E" + r + ")");
        } else {
            swTotalMaint.setCellValue(0);
        }
        swTotalMaint.setCellStyle(totalNumberStyle);
        setCellValue(row, 5, "", totalStyle);
        setCellValue(row, 6, "", totalStyle);

        // 합계
        r++;
        row = sheet.createRow(r);
        setCellValue(row, 0, "", totalStyle);
        setCellValue(row, 1, "", totalStyle);
        setCellValue(row, 2, "합  계", totalStyle);
        Cell grandTotalPrice = row.createCell(3);
        grandTotalPrice.setCellFormula("D" + (hwEndRow + 1) + "+D" + r);
        grandTotalPrice.setCellStyle(totalNumberStyle);
        Cell grandTotalMaint = row.createCell(4);
        grandTotalMaint.setCellFormula("E" + (hwEndRow + 1) + "+E" + r);
        grandTotalMaint.setCellStyle(totalNumberStyle);
        setCellValue(row, 5, "", totalStyle);
        setCellValue(row, 6, "", totalStyle);

        // 낙찰율 적용
        r++;
        row = sheet.createRow(r);
        setCellValue(row, 0, "", bodyBoldStyle);
        setCellValue(row, 1, "", bodyBoldStyle);
        setCellValue(row, 2, "낙찰율 적용(" + Math.round(bidRate * 100) + "%)", bodyBoldStyle);
        Cell bidApplyPrice = row.createCell(3);
        bidApplyPrice.setCellFormula("ROUNDDOWN(D" + r + "*" + bidRate + ",-3)");
        bidApplyPrice.setCellStyle(numberStyle);
        Cell bidApplyMaint = row.createCell(4);
        bidApplyMaint.setCellFormula("ROUNDDOWN(E" + r + "*" + bidRate + ",-3)");
        bidApplyMaint.setCellStyle(numberStyle);
        setCellValue(row, 5, "", bodyStyle);
        setCellValue(row, 6, "", bodyStyle);
    }

    // --- Sheet 4: 유지보수등급측정표 ---
    private void createGradeSheet(XSSFWorkbook wb,
                                   List<Map<String, Object>> hwItems, List<Map<String, Object>> swItems,
                                   CellStyle headerStyle, CellStyle bodyStyle, CellStyle numberStyle,
                                   CellStyle percentStyle, CellStyle totalStyle, CellStyle totalNumberStyle,
                                   CellStyle bodyBoldStyle) {
        Sheet sheet = wb.createSheet("유지보수등급측정표");
        sheet.setColumnWidth(0, 1500);
        sheet.setColumnWidth(1, 2000);
        sheet.setColumnWidth(2, 8000);
        sheet.setColumnWidth(3, 4500);
        sheet.setColumnWidth(4, 3500);
        sheet.setColumnWidth(5, 5000);
        sheet.setColumnWidth(6, 4000);

        int r = 1;
        Row row = sheet.createRow(r);
        setCellValue(row, 0, "유지보수 등급측정표", headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 6));

        r += 2;
        // 헤더
        row = sheet.createRow(r);
        setCellValue(row, 0, "No", headerStyle);
        setCellValue(row, 1, "구분", headerStyle);
        setCellValue(row, 2, "품명", headerStyle);
        setCellValue(row, 3, "도입가", headerStyle);
        setCellValue(row, 4, "적용율", headerStyle);
        setCellValue(row, 5, "유지보수대가", headerStyle);
        setCellValue(row, 6, "등급", headerStyle);

        // HW
        int hwStart = r + 1;
        for (int i = 0; i < hwItems.size(); i++) {
            r++;
            row = sheet.createRow(r);
            Map<String, Object> item = hwItems.get(i);
            setCellValue(row, 0, String.valueOf(i + 1), bodyStyle);
            setCellValue(row, 1, "HW", bodyStyle);
            setCellValue(row, 2, str(item.get("name")), bodyStyle);

            long price = toLong(item.get("unitPrice"));
            double rate = toDouble(item.get("rate")) / 100.0;

            Cell priceCell = row.createCell(3);
            priceCell.setCellValue(price);
            priceCell.setCellStyle(numberStyle);

            Cell rateCell = row.createCell(4);
            rateCell.setCellValue(rate);
            rateCell.setCellStyle(percentStyle);

            Cell maintCell = row.createCell(5);
            maintCell.setCellFormula("D" + (r + 1) + "*E" + (r + 1));
            maintCell.setCellStyle(numberStyle);

            // 등급: 적용율 기반 (8% 이하=A, 10%=B, 12%이상=C)
            String grade = rate <= 0.08 ? "A" : (rate <= 0.10 ? "B" : "C");
            setCellValue(row, 6, grade, bodyStyle);
        }

        // HW 소계
        r++;
        row = sheet.createRow(r);
        setCellValue(row, 0, "", totalStyle);
        setCellValue(row, 1, "", totalStyle);
        setCellValue(row, 2, "하드웨어 소계", totalStyle);
        Cell hwTotal = row.createCell(3);
        if (!hwItems.isEmpty()) {
            hwTotal.setCellFormula("SUM(D" + (hwStart + 1) + ":D" + r + ")");
        } else { hwTotal.setCellValue(0); }
        hwTotal.setCellStyle(totalNumberStyle);
        setCellValue(row, 4, "", totalStyle);
        Cell hwMaintTotal = row.createCell(5);
        if (!hwItems.isEmpty()) {
            hwMaintTotal.setCellFormula("SUM(F" + (hwStart + 1) + ":F" + r + ")");
        } else { hwMaintTotal.setCellValue(0); }
        hwMaintTotal.setCellStyle(totalNumberStyle);
        setCellValue(row, 6, "", totalStyle);

        // SW
        int swStart = r + 1;
        for (int i = 0; i < swItems.size(); i++) {
            r++;
            row = sheet.createRow(r);
            Map<String, Object> item = swItems.get(i);
            setCellValue(row, 0, String.valueOf(i + 1), bodyStyle);
            setCellValue(row, 1, "SW", bodyStyle);
            setCellValue(row, 2, str(item.get("name")), bodyStyle);

            long price = toLong(item.get("unitPrice"));
            double rate = toDouble(item.get("rate")) / 100.0;

            Cell priceCell = row.createCell(3);
            priceCell.setCellValue(price);
            priceCell.setCellStyle(numberStyle);

            Cell rateCell = row.createCell(4);
            rateCell.setCellValue(rate);
            rateCell.setCellStyle(percentStyle);

            Cell maintCell = row.createCell(5);
            maintCell.setCellFormula("D" + (r + 1) + "*E" + (r + 1));
            maintCell.setCellStyle(numberStyle);

            String grade = rate <= 0.12 ? "A" : (rate <= 0.16 ? "B" : "C");
            setCellValue(row, 6, grade, bodyStyle);
        }

        // SW 소계
        r++;
        row = sheet.createRow(r);
        setCellValue(row, 0, "", totalStyle);
        setCellValue(row, 1, "", totalStyle);
        setCellValue(row, 2, "소프트웨어 소계", totalStyle);
        Cell swTotal = row.createCell(3);
        if (!swItems.isEmpty()) {
            swTotal.setCellFormula("SUM(D" + (swStart + 1) + ":D" + r + ")");
        } else { swTotal.setCellValue(0); }
        swTotal.setCellStyle(totalNumberStyle);
        setCellValue(row, 4, "", totalStyle);
        Cell swMaintTotal = row.createCell(5);
        if (!swItems.isEmpty()) {
            swMaintTotal.setCellFormula("SUM(F" + (swStart + 1) + ":F" + r + ")");
        } else { swMaintTotal.setCellValue(0); }
        swMaintTotal.setCellStyle(totalNumberStyle);
        setCellValue(row, 6, "", totalStyle);
    }

    // =====================================================
    // TYPE_C: 복합형 (김포시 등) - 김포시 템플릿 기반
    // 14개 시트 그대로 보존, 표지/갑지 핵심 셀만 동적 채움
    // =====================================================
    private byte[] generateFromTypeCTemplate(String projNm, String distNm, int year,
                                             String designDate, double bidRate,
                                             List<Map<String, Object>> hwItems,
                                             List<Map<String, Object>> swItems) throws IOException {
        ClassPathResource res = new ClassPathResource("templates/excel/design_estimate_c_template.xlsx");
        try (InputStream is = res.getInputStream();
             XSSFWorkbook wb = new XSSFWorkbook(is)) {

            // 표지: B4(사업명), B8(설계년월), F12(지자체)
            Sheet cover = wb.getSheet("표지");
            setStringDirect(cover, 3, 1, projNm);
            setStringDirect(cover, 7, 1, designDate);
            setStringDirect(cover, 11, 5, distNm);

            // 갑지: A1(설계일자), A3(연도), A5(사업명), E10(HW), E11(SW)
            Sheet gapji = wb.getSheet("갑지");
            setStringDirect(gapji, 0, 0, designDate + "     설계");
            setStringDirect(gapji, 2, 0, "  " + year + "년도");
            setStringDirect(gapji, 4, 0, projNm);
            String hwLine = hwItems.isEmpty() ? "" :
                " 1. H/W :    " + hwItems.stream().map(i -> str(i.get("name"))).reduce((a,b) -> a+", "+b).orElse("") + "   1식";
            String swLine = swItems.isEmpty() ? "" :
                " 2. S/W :    " + swItems.stream().map(i -> str(i.get("name"))).reduce((a,b) -> a+", "+b).orElse("") + "    1식";
            setStringDirect(gapji, 9, 4, hwLine);
            setStringDirect(gapji, 10, 4, swLine);

            // HW등급측정: 행 16/17(0-idx 15/16) B열=품명, E열=도입비 (템플릿 서식/수식 보존)
            Sheet hwGrade = wb.getSheet("HW등급측정");
            if (hwGrade != null) {
                int[] rowIdx = {15, 16}; // 템플릿에 2행만 존재
                for (int i = 0; i < Math.min(hwItems.size(), rowIdx.length); i++) {
                    Map<String, Object> it = hwItems.get(i);
                    String nm = str(it.get("name"));
                    double price = toDouble(it.get("unitPrice"));
                    // (참고: 템플릿 E16/E17 서식 유지, 수식 F16=ROUNDDOWN(E16*G16*0.01,-1) 자동 재계산)
                    setStringDirect(hwGrade, rowIdx[i], 1, nm);            // B열
                    setNumericDirect(hwGrade, rowIdx[i], 4, price);         // E열 도입비
                }
            }

            // 설계서(상용소프트웨어): J8(0-idx row=7, col=9) = "3개 지자체 평균 도입금액"
            // SW 항목 중 품명에 GIS엔진/GIS SW/GeoNURIS 포함되면 해당 도입가로 덮어씀
            Sheet swDesign = wb.getSheet("설계서(상용소프트웨어)");
            if (swDesign != null) {
                for (Map<String, Object> it : swItems) {
                    String nm = str(it.get("name")).toUpperCase().replace(" ", "");
                    if (nm.contains("GIS엔진") || nm.contains("GISSW") || nm.contains("GEONURIS")) {
                        setNumericDirect(swDesign, 7, 9, toDouble(it.get("unitPrice")));
                        break;
                    }
                }
            }

            // 산출내역(장비유지보수): G6(0-idx row=5, col=6) DBMS 단가
            // SW 항목 중 품명에 DBMS 포함되면 해당 도입가로 덮어씀
            Sheet calc = wb.getSheet("산출내역(장비유지보수)");
            if (calc != null) {
                for (Map<String, Object> it : swItems) {
                    String nm = str(it.get("name")).toUpperCase();
                    if (nm.contains("DBMS")) {
                        setNumericDirect(calc, 5, 6, toDouble(it.get("unitPrice")));
                        break;
                    }
                }
            }

            wb.setForceFormulaRecalculation(true);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    // =====================================================
    // TYPE_B: 중간형 (단양군 등) - 템플릿 기반
    // 템플릿 기본 행: HW 행 4-5 (2개), SW 행 7-8 (2개)
    // =====================================================
    private byte[] generateFromTypeBTemplate(String projNm, String distNm, int year,
                                             String designDate, double bidRate, int rounddownUnit,
                                             List<Map<String, Object>> hwItems,
                                             List<Map<String, Object>> swItems) throws IOException {
        ClassPathResource res = new ClassPathResource("templates/excel/design_estimate_b_template.xlsx");
        try (InputStream is = res.getInputStream();
             XSSFWorkbook wb = new XSSFWorkbook(is)) {

            // 표지
            Sheet cover = wb.getSheet("표지");
            setStringDirect(cover, 4, 0, projNm);          // A5
            setStringDirect(cover, 14, 0, designDate);      // A15
            setStringDirect(cover, 21, 0, distNm);          // A22

            // 설계갑지
            Sheet gapji = wb.getSheet("설계갑지");
            setStringDirect(gapji, 2, 0, "   " + year + " 년도 ");                       // A3
            setStringDirect(gapji, 5, 0, projNm);                                        // A6
            setStringDirect(gapji, 7, 3, " " + projNm);                                  // D8
            // HW/SW 항목 요약 (D10/D11)
            String hwLine = hwItems.isEmpty() ? "" :
                " 1. H/W :    " + hwItems.stream().map(i -> str(i.get("name"))).reduce((a,b) -> a+", "+b).orElse("") + "   1식";
            String swLine = swItems.isEmpty() ? "" :
                " 2. S/W :    " + swItems.stream().map(i -> str(i.get("name"))).reduce((a,b) -> a+", "+b).orElse("") + "    1식";
            setStringDirect(gapji, 9, 3, hwLine);   // D10
            setStringDirect(gapji, 10, 3, swLine);  // D11

            // 총괄표
            Sheet summary = wb.getSheet("총괄표");
            // HW 항목: 행 4-5 (0-based 3-4)
            fillTypeBItemRows(summary, hwItems, 3, "HW");
            // SW 항목: 행 7-8 (0-based 6-7)
            fillTypeBItemRows(summary, swItems, 6, "SW");

            // 낙찰율·절사 단위 적용 (H11 = row 10, col 7 / I11 = col 8)
            // rounddownUnit == 0 인 경우 절사 안 함 (사용자 미선택)
            boolean applyRound = rounddownUnit > 0;
            Row row11 = summary.getRow(10);
            if (row11 == null) row11 = summary.createRow(10);
            Cell h11 = row11.getCell(7);
            if (h11 == null) h11 = row11.createCell(7);
            if (applyRound) {
                int digits = toRoundDigits(rounddownUnit);
                if (bidRate > 0 && bidRate < 1.0) {
                    h11.setCellFormula("ROUNDDOWN((H10+H9)*" + bidRate + "," + digits + ")");
                } else {
                    h11.setCellFormula("ROUNDDOWN(H10+H9," + digits + ")");
                }
            } else {
                if (bidRate > 0 && bidRate < 1.0) {
                    h11.setCellFormula("(H10+H9)*" + bidRate);
                } else {
                    h11.setCellFormula("H10+H9");
                }
            }
            Cell i11 = row11.getCell(8);
            if (i11 == null) i11 = row11.createCell(8);
            StringBuilder remark = new StringBuilder();
            if (applyRound) remark.append(roundLabel(rounddownUnit));
            if (bidRate > 0 && bidRate < 1.0) {
                if (remark.length() > 0) remark.append("\n");
                remark.append("낙찰율 적용 ").append(Math.round(bidRate * 100)).append("%");
            }
            i11.setCellValue(remark.toString());

            wb.setForceFormulaRecalculation(true);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    /** TYPE_B 총괄표의 항목 행을 채움. 템플릿엔 항목당 2행이 기본이며, 항목 개수에 맞게 비우거나 채움. */
    private void fillTypeBItemRows(Sheet summary, List<Map<String, Object>> items, int startRow0, String type) {
        // 템플릿 기본 2행 가정. 항목이 많으면 시작 2행만 채움(추후 확장)
        int templateSlots = 2;
        for (int i = 0; i < templateSlots; i++) {
            int r = startRow0 + i;
            Row row = summary.getRow(r);
            if (row == null) continue;
            if (i < items.size()) {
                Map<String, Object> it = items.get(i);
                double rate = toDouble(it.get("rate")) / 100.0;
                long price = toLong(it.get("unitPrice"));
                // A열: 번호
                if (row.getCell(0) != null) row.getCell(0).setCellValue(i + 1);
                // B열: 품명
                setStringDirect(summary, r, 1, str(it.get("name")));
                // D열: 적용율 (숫자)
                if (row.getCell(3) != null) row.getCell(3).setCellValue(rate);
                // E열: 도입가
                if (row.getCell(4) != null) row.getCell(4).setCellValue(price);
                // I열: 비고
                String remark = str(it.get("remark"));
                if (!remark.isEmpty()) setStringDirect(summary, r, 8, remark);
            } else {
                // 빈 행 처리: B,D,E,I 비움 (수식은 그대로 두면 0 출력)
                setStringDirect(summary, r, 1, "");
                if (row.getCell(3) != null) row.getCell(3).setCellValue(0);
                if (row.getCell(4) != null) row.getCell(4).setCellValue(0);
                setStringDirect(summary, r, 8, "");
            }
        }
    }

    // =====================================================
    // TYPE_D: SW전용 (김해시/강릉시 등) - 템플릿 기반
    // =====================================================
    private byte[] generateFromSwTemplate(String projNm, String distNm, int year,
                                          String designDate, double bidRate,
                                          List<Map<String, Object>> swItems) throws IOException {
        ClassPathResource res = new ClassPathResource("templates/excel/design_estimate_sw_template.xlsx");
        try (InputStream is = res.getInputStream();
             XSSFWorkbook wb = new XSSFWorkbook(is)) {

            // SW 항목 첫 번째 (단일 SW만 지원, 복수면 첫 번째 사용)
            String swName = swItems.isEmpty() ? "" : str(swItems.get(0).get("name"));
            double swRate = swItems.isEmpty() ? 0 : toDouble(swItems.get(0).get("rate")) / 100.0;
            long swPrice = swItems.isEmpty() ? 0 : toLong(swItems.get(0).get("unitPrice"));

            // 표지: A5(사업명), A15(설계년월), A22(지자체)
            Sheet cover = wb.getSheet("표지");
            setStringDirect(cover, 4, 0, projNm);
            setStringDirect(cover, 14, 0, designDate);
            setStringDirect(cover, 21, 0, distNm);

            // 설계갑지: A5(서기 ~ 년도), A8(사업명), D10(사업명), D12(SW 항목)
            Sheet gapji = wb.getSheet("설계갑지");
            setStringDirect(gapji, 4, 0, "   서기 " + year + " 년도 ");
            setStringDirect(gapji, 7, 0, projNm);
            setStringDirect(gapji, 9, 3, projNm);
            setStringDirect(gapji, 11, 3, " 1. S/W :    " + swName + "  1식");

            // 총괄표
            Sheet summary = wb.getSheet("총괄표");
            System.err.println("[SW_TEMPLATE] sheets=" + wb.getNumberOfSheets() + " cover=" + (cover!=null) + " gapji=" + (gapji!=null) + " summary=" + (summary!=null));
            System.err.println("[SW_TEMPLATE] B5 before=" + (summary.getRow(4) != null && summary.getRow(4).getCell(1) != null ? summary.getRow(4).getCell(1).toString() : "null"));
            // B5: SW 품명
            setStringDirect(summary, 4, 1, swName);
            System.err.println("[SW_TEMPLATE] B5 after=" + summary.getRow(4).getCell(1).toString());
            // D5: 적용율 (숫자)
            Cell d5 = summary.getRow(4).getCell(3);
            if (d5 != null) d5.setCellValue(swRate);
            // E5: 도입가 (숫자)
            Cell e5 = summary.getRow(4).getCell(4);
            if (e5 != null) e5.setCellValue(swPrice);

            // H8: 총용역비 수식 (낙찰율 적용)
            Cell h8 = summary.getRow(7).getCell(7);
            if (h8 != null) {
                if (bidRate > 0 && bidRate < 1.0) {
                    h8.setCellFormula("ROUNDDOWN((H6+H7)*" + bidRate + ",-1)");
                } else {
                    h8.setCellFormula("ROUNDDOWN(H6+H7,0)");
                }
            }
            // I8: 낙찰율 라벨
            Cell i8 = summary.getRow(7).getCell(8);
            if (i8 != null) {
                if (bidRate > 0 && bidRate < 1.0) {
                    i8.setCellValue("낙찰율 적용(" + Math.round(bidRate * 100) + "%)");
                } else {
                    i8.setCellValue("");
                }
            }

            wb.setForceFormulaRecalculation(true);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    private void setStringDirect(Sheet sheet, int rowIdx, int colIdx, String val) {
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

    /** 숫자 값을 셀에 기록하되 기존 셀 스타일(서식)은 그대로 보존 */
    private void setNumericDirect(Sheet sheet, int rowIdx, int colIdx, double val) {
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

    private void replacePlaceholder(Sheet sheet, String key, String value) {
        if (sheet == null) return;
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell.getCellType() == CellType.STRING) {
                    String v = cell.getStringCellValue();
                    if (v != null && v.contains(key)) {
                        cell.setCellValue(v.replace(key, value == null ? "" : value));
                    }
                }
            }
        }
    }

    // =====================================================
    // (구) TYPE_D POI 직접 생성 - 템플릿 방식으로 대체됨, 미사용
    // =====================================================
    @SuppressWarnings("unused")
    private void createTypeDCoverSheet_OLD(XSSFWorkbook wb, String projNm, String designDate,
                                        String distNm, CellStyle titleStyle, CellStyle subTitleStyle, CellStyle bodyStyle) {
        Sheet sheet = wb.createSheet("표지");
        sheet.setDefaultColumnWidth(12);
        for (int i = 0; i < 16; i++) sheet.setColumnWidth(i, 2500);

        Row row = sheet.createRow(4);
        Cell c = row.createCell(0);
        c.setCellValue(projNm);
        c.setCellStyle(subTitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 15));

        row = sheet.createRow(5);
        c = row.createCell(0);
        c.setCellValue("산  출  내  역  서");
        c.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(5, 5, 0, 15));

        row = sheet.createRow(14);
        c = row.createCell(0);
        c.setCellValue(designDate);
        c.setCellStyle(subTitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(14, 14, 0, 15));

        row = sheet.createRow(21);
        c = row.createCell(0);
        c.setCellValue(distNm);
        c.setCellStyle(subTitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(21, 21, 0, 15));
    }

    // --- TYPE_D Sheet 2: 설계갑지 ---
    private void createTypeDGapjiSheet(XSSFWorkbook wb, String projNm,
                                        List<Map<String, Object>> swItems, int year,
                                        CellStyle titleStyle, CellStyle subTitleStyle,
                                        CellStyle bodyStyle, CellStyle numberStyle) {
        Sheet sheet = wb.createSheet("설계갑지");
        sheet.setColumnWidth(0, 3000);
        sheet.setColumnWidth(1, 3000);
        sheet.setColumnWidth(2, 4000);
        sheet.setColumnWidth(3, 8000);
        for (int i = 4; i <= 7; i++) sheet.setColumnWidth(i, 4000);

        int r = 0;
        Row row = sheet.createRow(r);
        Cell c = row.createCell(0);
        c.setCellValue("설 계 서 용 지 (갑 지)");
        c.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 7));

        r = 4;
        row = sheet.createRow(r);
        c = row.createCell(0);
        c.setCellValue("   서기 " + year + " 년도 ");
        c.setCellStyle(bodyStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 7));

        r = 7;
        row = sheet.createRow(r);
        c = row.createCell(0);
        c.setCellValue(projNm);
        c.setCellStyle(subTitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 7));

        // 용역 개요
        r = 9;
        row = sheet.createRow(r);
        setCellValue(row, 2, "용 역 개 요 :", bodyStyle);
        c = row.createCell(3);
        c.setCellValue((year + 1) + "년 " + projNm);
        c.setCellStyle(bodyStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 3, 7));

        // SW 목록
        r = 11;
        for (int i = 0; i < swItems.size(); i++) {
            row = sheet.createRow(r + i);
            c = row.createCell(3);
            c.setCellValue(" " + (i + 1) + ". S/W :    " + str(swItems.get(i).get("name")) + "  1식");
            c.setCellStyle(bodyStyle);
            sheet.addMergedRegion(new CellRangeAddress(r + i, r + i, 3, 7));
        }

        // 총 용역비
        r = 16;
        row = sheet.createRow(r);
        setCellValue(row, 2, "   총 용 역 비  :", bodyStyle);
        // 한글 금액 - NUMBERSTRING 수식
        Cell amtKr = row.createCell(4);
        amtKr.setCellFormula("\"금\"&NUMBERSTRING(H" + (r + 1) + ",1)&\"원정\"");
        amtKr.setCellStyle(bodyStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 4, 6));
        // 금액 = 총괄표!H8 참조
        Cell amtNum = row.createCell(7);
        amtNum.setCellFormula("총괄표!H8");
        amtNum.setCellStyle(numberStyle);
    }

    // --- TYPE_D Sheet 3: 총괄표 ---
    private void createTypeDSummarySheet(XSSFWorkbook wb, List<Map<String, Object>> swItems, double bidRate,
                                          CellStyle headerStyle, CellStyle bodyStyle, CellStyle numberStyle,
                                          CellStyle percentStyle, CellStyle totalStyle, CellStyle totalNumberStyle,
                                          CellStyle bodyBoldStyle) {
        Sheet sheet = wb.createSheet("총괄표");
        sheet.setColumnWidth(0, 1500);  // No
        sheet.setColumnWidth(1, 7000);  // 품명
        sheet.setColumnWidth(2, 5500);  // 산출내역
        sheet.setColumnWidth(3, 3500);  // 적용율
        sheet.setColumnWidth(4, 5000);  // 도입가
        sheet.setColumnWidth(5, 4000);  // 직접인건비
        sheet.setColumnWidth(6, 5000);  // 유지관리(월단위)
        sheet.setColumnWidth(7, 5000);  // 소계(12개월분)
        sheet.setColumnWidth(8, 4000);  // 비고

        int r = 0;
        Row row = sheet.createRow(r);
        setCellValue(row, 0, "총괄표", headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 8));

        // 헤더
        r = 1;
        row = sheet.createRow(r);
        setCellValue(row, 0, "구 분", headerStyle);
        setCellValue(row, 1, "", headerStyle); // 품명 (구분과 합쳐짐)
        setCellValue(row, 2, "산출내역", headerStyle);
        setCellValue(row, 3, "적용율\n(낙찰율 적용)", headerStyle);
        setCellValue(row, 4, "도입가", headerStyle);
        setCellValue(row, 5, "직접인건비", headerStyle);
        setCellValue(row, 6, "유지관리(월 단위)", headerStyle);
        setCellValue(row, 7, "소계(12개월분)", headerStyle);
        setCellValue(row, 8, "비  고", headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 1));

        // 소프트웨어 유지보수 제목
        r = 2;
        row = sheet.createRow(r);
        setCellValue(row, 0, "1. 소프트웨어 유지보수", bodyBoldStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 8));

        // 유지보수대가 소계 행
        r = 3;
        row = sheet.createRow(r);
        setCellValue(row, 0, "I. 유지보수대가", bodyBoldStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 1));
        setCellValue(row, 2, "", bodyStyle);
        setCellValue(row, 3, "", bodyStyle);
        // 도입가 합계 수식
        int itemStartRow = r + 2; // Excel row (1-based)
        int itemEndRow = itemStartRow + swItems.size() - 1;
        if (!swItems.isEmpty()) {
            Cell sumE = row.createCell(4);
            sumE.setCellFormula("SUM(E" + itemStartRow + ":E" + itemEndRow + ")");
            sumE.setCellStyle(numberStyle);
            Cell sumF = row.createCell(5);
            sumF.setCellFormula("SUM(F" + itemStartRow + ":F" + itemEndRow + ")");
            sumF.setCellStyle(numberStyle);
            Cell sumG = row.createCell(6);
            sumG.setCellFormula("SUM(G" + itemStartRow + ":G" + itemEndRow + ")");
            sumG.setCellStyle(numberStyle);
            Cell sumH = row.createCell(7);
            sumH.setCellFormula("SUM(H" + itemStartRow + ":H" + itemEndRow + ")");
            sumH.setCellStyle(numberStyle);
        } else {
            row.createCell(4).setCellValue(0); row.getCell(4).setCellStyle(numberStyle);
            row.createCell(5).setCellValue(0); row.getCell(5).setCellStyle(numberStyle);
            row.createCell(6).setCellValue(0); row.getCell(6).setCellStyle(numberStyle);
            row.createCell(7).setCellValue(0); row.getCell(7).setCellStyle(numberStyle);
        }
        setCellValue(row, 8, "", bodyStyle);

        // SW 항목
        for (int i = 0; i < swItems.size(); i++) {
            r++;
            row = sheet.createRow(r);
            Map<String, Object> item = swItems.get(i);
            double rate = toDouble(item.get("rate")) / 100.0;
            long price = toLong(item.get("unitPrice"));

            setCellValue(row, 0, String.valueOf(i + 1), bodyStyle);
            setCellValue(row, 1, str(item.get("name")), bodyStyle);
            // 산출내역: " 기준단가의 XX%적용" (수식)
            Cell descCell = row.createCell(2);
            descCell.setCellFormula("\" 기준단가의 \"&TEXT(D" + (r + 1) + ",\"0%\")&\"적용\"");
            descCell.setCellStyle(bodyStyle);
            // 적용율
            Cell rateCell = row.createCell(3);
            rateCell.setCellValue(rate);
            rateCell.setCellStyle(percentStyle);
            // 도입가
            Cell priceCell = row.createCell(4);
            priceCell.setCellValue(price);
            priceCell.setCellStyle(numberStyle);
            // 직접인건비 (0)
            Cell laborCell = row.createCell(5);
            laborCell.setCellValue(0);
            laborCell.setCellStyle(numberStyle);
            // 유지관리(월단위) = 도입가 × 적용율
            Cell maintCell = row.createCell(6);
            maintCell.setCellFormula("E" + (r + 1) + "*D" + (r + 1));
            maintCell.setCellStyle(numberStyle);
            // 소계(12개월분) = (직접인건비 + 유지관리) / 12 * 12
            Cell subtotalCell = row.createCell(7);
            subtotalCell.setCellFormula("SUM(F" + (r + 1) + ":G" + (r + 1) + ")/12*12");
            subtotalCell.setCellStyle(numberStyle);
            // 비고
            setCellValue(row, 8, str(item.get("remark")).isEmpty() ? "참고_도입가 참조" : str(item.get("remark")), bodyStyle);
        }

        // 합계
        r++;
        row = sheet.createRow(r);
        setCellValue(row, 0, "합계", totalStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 1));
        setCellValue(row, 1, "", totalStyle);
        setCellValue(row, 2, "", totalStyle);
        setCellValue(row, 3, "", totalStyle);
        Cell totalE = row.createCell(4);
        totalE.setCellFormula("E4"); // I.유지보수대가 행의 도입가
        totalE.setCellStyle(totalNumberStyle);
        setCellValue(row, 5, "", totalStyle);
        Cell totalG = row.createCell(6);
        totalG.setCellFormula("G4");
        totalG.setCellStyle(totalNumberStyle);
        Cell totalH = row.createCell(7);
        totalH.setCellFormula("H4");
        totalH.setCellStyle(totalNumberStyle);
        setCellValue(row, 8, "", totalStyle);

        // 부가세
        r++;
        int vatRow = r;
        row = sheet.createRow(r);
        setCellValue(row, 0, "부가세", bodyBoldStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 1));
        setCellValue(row, 1, "", bodyStyle);
        setCellValue(row, 2, "", bodyStyle);
        setCellValue(row, 3, "", bodyStyle);
        setCellValue(row, 4, "", bodyStyle);
        setCellValue(row, 5, "", bodyStyle);
        setCellValue(row, 6, "", bodyStyle);
        Cell vatCell = row.createCell(7);
        vatCell.setCellFormula("H" + r + "*0.1"); // 합계의 H열 × 10%
        vatCell.setCellStyle(numberStyle);
        setCellValue(row, 8, "", bodyStyle);

        // 총용역비
        r++;
        row = sheet.createRow(r);
        setCellValue(row, 0, "총용역비", totalStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 1));
        setCellValue(row, 1, "", totalStyle);
        setCellValue(row, 2, "", totalStyle);
        setCellValue(row, 3, "", totalStyle);
        setCellValue(row, 4, "", totalStyle);
        setCellValue(row, 5, "", totalStyle);
        setCellValue(row, 6, "", totalStyle);
        Cell grandTotal = row.createCell(7);
        if (bidRate > 0 && bidRate < 1.0) {
            grandTotal.setCellFormula("ROUNDDOWN((H" + vatRow + "+H" + (vatRow + 1) + ")*" + bidRate + ",-1)");
        } else {
            grandTotal.setCellFormula("ROUNDDOWN(H" + vatRow + "+H" + (vatRow + 1) + ",0)");
        }
        grandTotal.setCellStyle(totalNumberStyle);
        if (bidRate > 0 && bidRate < 1.0) {
            setCellValue(row, 8, "낙찰율 적용(" + Math.round(bidRate * 100) + "%)", bodyStyle);
        } else {
            setCellValue(row, 8, "", totalStyle);
        }
    }

    // =====================================================
    // 기성내��서 Excel (강릉시 형식: 4 sheets)
    // =====================================================

    @SuppressWarnings("unchecked")
    public byte[] generateInterimReport(Integer docId) throws IOException {
        Document doc = documentService.getDocumentById(docId);

        // detail_sheet 섹션 (기성내역서 폼 입력)
        Map<String, Object> data = doc.getDetails().stream()
                .filter(d -> "detail_sheet".equals(d.getSectionKey()))
                .findFirst()
                .map(DocumentDetail::getSectionData)
                .orElseThrow(() -> new IllegalStateException(messages.get("error.export.performance_data_empty")));

        // inspector 섹션 (금회기성금액 등 기성검사원 폼 입력)
        Map<String, Object> inspData = doc.getDetails().stream()
                .filter(d -> "inspector".equals(d.getSectionKey()))
                .findFirst()
                .map(DocumentDetail::getSectionData)
                .orElse(java.util.Collections.emptyMap());

        double bidRate = toDouble(data.get("bidRate"));          // % 단위 (예: 94)
        if (bidRate == 0) bidRate = 100;
        int interimMonths = (int) toDouble(data.get("interimMonths"));
        int completionMonths = (int) toDouble(data.get("completionMonths"));
        if (interimMonths == 0) interimMonths = 6;
        if (completionMonths == 0) completionMonths = 6;

        List<Map<String, Object>> items = (List<Map<String, Object>>) data.getOrDefault("items", List.of());
        // 도입가(VAT포함) 총액
        long totalUnitPrice = 0L;
        for (Map<String, Object> it : items) totalUnitPrice += toLong(it.get("unitPrice"));

        // DB에서 가져오는 값
        var proj = doc.getProject();
        String projNm = proj != null ? proj.getProjNm() : "";
        String distNm = (proj != null && proj.getDistNm() != null) ? proj.getDistNm() : "";
        int year = (proj != null && proj.getYear() != null) ? proj.getYear() : java.time.LocalDate.now().getYear();
        long contAmt = (proj != null && proj.getContAmt() != null) ? proj.getContAmt() : 0L;
        // 수행기간: 폼 입력값 우선, 없으면 사업 기간으로 자동 생성. 개월수는 기간 문자열에서 자동 계산
        String formPeriod = String.valueOf(data.getOrDefault("periodText", "")).trim();
        String periodText;
        if (!formPeriod.isEmpty()) {
            String base = formPeriod.replaceAll("\\s*\\([^)]*개월\\)\\s*$", "").trim();
            int months = monthsFromPeriodText(base);
            if (months <= 0) months = interimMonths + completionMonths;
            periodText = base + "(" + months + "개월)";
        } else {
            periodText = buildPeriodText(proj, interimMonths + completionMonths);
        }

        // 금회기성금액 (inspector.paymentAmount)
        long paymentAmt = toLong(inspData.get("paymentAmount"));
        // 금회 기성율: 기성검사원 > inspector.paymentRate (% 단위, 예: 50)
        double currRate = toDouble(inspData.get("paymentRate"));
        // paymentAmt 가 비어있고 비율이 있으면 계약금액×비율/100 으로 보정
        if (paymentAmt == 0 && currRate > 0 && contAmt > 0) {
            paymentAmt = Math.round(contAmt * currRate / 100.0);
        }
        // 전회 기성율: 기성내역서 폼 입력 (% 단위)
        double prevRate = toDouble(data.get("prevRate"));
        double cumRate = prevRate + currRate;              // 기성누계 비율
        if (cumRate > 100) cumRate = 100;
        double completionRate = 100 - cumRate;             // 준공금 비율

        // maint_type 별 템플릿 분기: HS/DHS = HW 포함 → 운영장비용 / 그 외 = SW용
        String maintType = proj != null ? proj.getMaintType() : null;
        boolean isHwTemplate = "HS".equals(maintType) || "DHS".equals(maintType);
        String tmplPath = isHwTemplate
                ? "templates/excel/interim_template_hw.xlsx"
                : "templates/excel/interim_template.xlsx";
        // 회차 (기성검사원 입력 paymentRound, 기본 1)
        int round = (int) toDouble(inspData.get("paymentRound"));
        if (round == 0) round = 1;

        ClassPathResource res = new ClassPathResource(tmplPath);
        try (InputStream is = res.getInputStream();
             XSSFWorkbook wb = new XSSFWorkbook(is)) {

            if (isHwTemplate) {
                int truncDigit = (int) toDouble(data.get("truncDigit"));
                if (truncDigit < 3 || truncDigit > 5) truncDigit = 3;
                populateHwInterimTemplate(wb, projNm, distNm, year, contAmt, paymentAmt,
                        currRate, prevRate, round, items, inspData, truncDigit, periodText);
            } else {

            // ── [0] 표지 ──
            Sheet cover = wb.getSheet("표지");
            if (cover != null) {
                setStringDirect(cover, 3, 1, "      " + year + "년도");   // B4
                setStringDirect(cover, 4, 0, projNm);                      // A5
                setStringDirect(cover, 11, 0, distNm);                     // A12
            }

            // ── [1] 기성 내역서 ──
            Sheet detail = wb.getSheet("기성 내역서");
            if (detail != null) {
                setStringDirect(detail, 2, 0, "가. 용    역     명 : " + projNm);
                setStringDirect(detail, 3, 0, "나. 계  약  금  액 : 금" + String.format("%,d", contAmt)
                        + "원(금" + HwpxExportService.convertToKoreanAmount(contAmt) + "원)");
                setStringDirect(detail, 7, 0, "바. 금회기성신청금액 : 금" + String.format("%,d", paymentAmt)
                        + "원(금" + HwpxExportService.convertToKoreanAmount(paymentAmt) + "원)");
                setStringDirect(detail, 8, 0, "사. 수행기간: " + periodText);
            }

            // ── [3] GeoNURIS for KRAS 1.0 유지관리 ──
            Sheet geo = wb.getSheet("GeoNURIS for KRAS 1.0 유지관리");
            if (geo != null) {
                // G2 header: 낙찰율 %
                setStringDirect(geo, 1, 6, "연간 유지관리비\n(낙찰율 " + trimPct(bidRate) + "%)");
                // D4: 도입가(VAT포함) 총액
                setNumericDirect(geo, 3, 3, totalUnitPrice);
                // E4: VAT 제외 단가 직접 기재(수식 대체)
                double e4 = totalUnitPrice / 1.1;
                setNumericDirect(geo, 3, 4, Math.round(e4));
                // H4: 연간 유지관리비 = E4 * 0.12 * 낙찰율 (기존 수식 제거, 직접 값)
                double h4 = Math.round(e4 * 0.12 * (bidRate / 100.0));
                setNumericDirect(geo, 3, 7, h4);
                // I4: 기성기간(개월), J4: 기성누계 금액(전회+금회 비율)
                setNumericDirect(geo, 3, 8, interimMonths);
                setNumericDirect(geo, 3, 9, Math.round(h4 * cumRate / 100.0));
                // K4: 준공기간(개월), L4: 준공금 금액(잔여 비율)
                setNumericDirect(geo, 3, 10, completionMonths);
                setNumericDirect(geo, 3, 11, Math.round(h4 * completionRate / 100.0));
            }
            } // end else (SW용)

            wb.setForceFormulaRecalculation(true);
            org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator.evaluateAllFormulaCells(wb);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    /**
     * 운영장비용(HW+SW) 기성내역서 템플릿 채우기.
     * 시트 구성: 표지 / 기성내역서 / 설계서 / 참고
     * 항목별 금액은 설계서 시트의 기존 수식이 자동 계산하므로
     * 여기서는 표지·헤더 텍스트와 금회/전회 기성율(H12/F12)만 주입한다.
     */
    private void populateHwInterimTemplate(XSSFWorkbook wb, String projNm, String distNm,
            int year, long contAmt, long paymentAmt,
            double currRate, double prevRate, int round,
            List<Map<String, Object>> items, Map<String, Object> inspData,
            int truncDigit, String periodText) {

        Sheet cover = wb.getSheet("표지");
        if (cover != null) {
            setStringDirect(cover, 4, 0, projNm);
            setStringDirect(cover, 5, 0, "기성(" + round + "회) 내역서");
            setStringDirect(cover, 14, 0, year + ". " + currentMonthLabel(inspData));
            setStringDirect(cover, 21, 0, "  " + (distNm == null ? "" : distNm));
        }

        Sheet detail = wb.getSheet("기성내역서");
        if (detail != null) {
            // 행 9 에 "사. 수행기간" 새로 삽입 — 기존 9~18 행 한 줄씩 아래로(머지/수식 자동 시프트)
            detail.shiftRows(8, 17, 1);
            setStringDirect(detail, 0, 0, "기성(제" + round + "회) 내역서");
            setStringDirect(detail, 2, 0, "가. 용    역     명 : " + projNm);
            setStringDirect(detail, 3, 0, "나. 계  약  금  액 : 금"
                    + HwpxExportService.convertToKoreanAmount(contAmt)
                    + "원(\\" + String.format("%,d", contAmt) + ")");
            setStringDirect(detail, 7, 0, "바. 금회기성신청금액 : 금"
                    + HwpxExportService.convertToKoreanAmount(paymentAmt)
                    + "원(\\" + String.format("%,d", paymentAmt) + ")");
            // 옛 템플릿이 L8 에 잔여 숫자(8,190,000) 가 남아있어 보임 — 명시적으로 비움
            Row r8 = detail.getRow(7);
            if (r8 != null) {
                Cell oldL8 = r8.getCell(11);
                if (oldL8 != null) r8.removeCell(oldL8);
            }
            setStringDirect(detail, 8, 0, "사. 수행기간 : " + (periodText == null ? "" : periodText));
            applyFontSize(wb, detail, 8, 0, (short) 12);
            setStringDirect(detail, 9, 0, "아. 내       역(부가가치세 포함)");
        }

        Sheet plan = wb.getSheet("설계서");
        if (plan != null) {
            // 항목 매핑: items[0..3] → HW 2개(B6/B7,D6/D7) + SW 2개(B10/B11,D10/D11)
            int[] itemRows = { 5, 6, 9, 10 }; // POI 0-index → B6/D6, B7/D7, B10/D10, B11/D11
            for (int i = 0; i < itemRows.length && i < items.size(); i++) {
                Map<String, Object> it = items.get(i);
                String name = str(it.get("name"));
                long unitPrice = toLong(it.get("unitPrice"));
                if (!name.isBlank()) setStringDirect(plan, itemRows[i], 1, name);
                setNumericDirect(plan, itemRows[i], 3, unitPrice);
            }

            // 비율(0~1) 변환
            double prev = prevRate / 100.0;
            double curr = currRate / 100.0;
            double cum = prev + curr;
            if (cum > 1.0) cum = 1.0;

            // 전회기성 비율: F5,F6,F7,F9,F10,F11,F12  (행 index: 4,5,6,8,9,10,11)
            int[] rateRows = { 4, 5, 6, 8, 9, 10, 11 };
            for (int r : rateRows) setNumericDirect(plan, r, 5, prev);   // F열
            for (int r : rateRows) setNumericDirect(plan, r, 7, curr);   // H열
            for (int r : rateRows) setNumericDirect(plan, r, 9, cum);    // J열 (누계)

            // D12 절사 단위 (백/천/만 → -3/-4/-5)
            Row d12row = plan.getRow(11);
            if (d12row == null) d12row = plan.createRow(11);
            Cell d12 = d12row.getCell(3);
            if (d12 == null) d12 = d12row.createCell(3, CellType.FORMULA);
            d12.setCellFormula("ROUNDDOWN((D5+D9),-" + truncDigit + ")");
        }

        // 기성내역서 시트 합계 행(행 삽입 후 B19/E19/G19)도 같은 절사 단위 적용
        Sheet detailSh = wb.getSheet("기성내역서");
        if (detailSh != null) {
            Row r19 = detailSh.getRow(18);
            if (r19 == null) r19 = detailSh.createRow(18);
            setSumFormula(r19, 1, "ROUNDDOWN((SUM(B13+B16)),-" + truncDigit + ")");
            setSumFormula(r19, 4, "ROUNDDOWN((E13+E16),-" + truncDigit + ")");
            setSumFormula(r19, 6, "ROUNDDOWN((G13+G16),-" + truncDigit + ")");

            // 항목 이름: items[0..3] → A14/A15/A17/A18 (행 삽입 후, 2-space 들여쓰기)
            int[] nameRows = { 13, 14, 16, 17 };
            for (int i = 0; i < nameRows.length && i < items.size(); i++) {
                String name = str(items.get(i).get("name"));
                if (!name.isBlank()) setStringDirect(detailSh, nameRows[i], 0, "  " + name);
            }
        }
    }

    private void applyFontSize(XSSFWorkbook wb, Sheet sheet, int rowIdx, int colIdx, short pts) {
        if (sheet == null) return;
        Row row = sheet.getRow(rowIdx);
        if (row == null) return;
        Cell cell = row.getCell(colIdx);
        if (cell == null) return;
        org.apache.poi.ss.usermodel.Font f = wb.createFont();
        f.setFontName("맑은 고딕");
        f.setFontHeightInPoints(pts);
        CellStyle cs = wb.createCellStyle();
        CellStyle existing = cell.getCellStyle();
        if (existing != null) cs.cloneStyleFrom(existing);
        cs.setFont(f);
        cell.setCellStyle(cs);
    }

    private void setSumFormula(Row row, int colIdx, String formula) {
        Cell c = row.getCell(colIdx);
        CellStyle keep = (c != null) ? c.getCellStyle() : null;
        if (c != null) row.removeCell(c);
        c = row.createCell(colIdx, CellType.FORMULA);
        if (keep != null) c.setCellStyle(keep);
        c.setCellFormula(formula);
    }

    private String currentMonthLabel(Map<String, Object> inspData) {
        Object m = inspData != null ? inspData.get("month") : null;
        if (m != null && !String.valueOf(m).isBlank()) {
            try { return String.format("%02d", Integer.parseInt(String.valueOf(m).trim())); }
            catch (NumberFormatException ignore) { return String.valueOf(m); }
        }
        return String.format("%02d", java.time.LocalDate.now().getMonthValue());
    }

    /** 소수점 2자리까지만 표기하되 불필요한 0 제거 */
    private String trimPct(double v) {
        if (v == Math.floor(v)) return String.valueOf((long) v);
        return String.format("%.2f", v).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    /** "2026.01~2026.06" 형태 문자열에서 개월수 계산 (양끝 포함) */
    private int monthsFromPeriodText(String s) {
        if (s == null) return 0;
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(\\d{4})[.\\-/](\\d{1,2}).*?[~∼\\-].*?(\\d{4})[.\\-/](\\d{1,2})")
                .matcher(s);
        if (!m.find()) return 0;
        int y1 = Integer.parseInt(m.group(1)), mo1 = Integer.parseInt(m.group(2));
        int y2 = Integer.parseInt(m.group(3)), mo2 = Integer.parseInt(m.group(4));
        return (y2 - y1) * 12 + (mo2 - mo1) + 1;
    }

    private String buildPeriodText(com.swmanager.system.domain.SwProject proj, int totalMonths) {
        if (proj == null || proj.getStartDt() == null || proj.getEndDt() == null) return "";
        java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern("yyyy.MM");
        return proj.getStartDt().format(f) + "~" + proj.getEndDt().format(f)
                + "(" + totalMonths + "개월)";
    }

    // --- 기성 Sheet 1: 표지 ---
    private void createInterimCoverSheet(XSSFWorkbook wb, String projNm, int year, String distNm,
                                          CellStyle titleStyle, CellStyle subTitleStyle) {
        Sheet sheet = wb.createSheet("표지");
        for (int i = 0; i < 7; i++) sheet.setColumnWidth(i, 3500);

        Row row = sheet.createRow(3);
        Cell c = row.createCell(1); c.setCellValue("      " + year + "년도"); c.setCellStyle(subTitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(3, 3, 1, 5));

        row = sheet.createRow(4);
        c = row.createCell(0); c.setCellValue(projNm); c.setCellStyle(subTitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 6));

        row = sheet.createRow(5);
        c = row.createCell(2); c.setCellValue("기성내역서"); c.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(5, 5, 2, 4));

        row = sheet.createRow(11);
        c = row.createCell(0); c.setCellValue(distNm); c.setCellStyle(subTitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(11, 11, 0, 6));
    }

    // --- 기성 Sheet 2: 기성 내역서 ---
    private void createInterimDetailSheet(XSSFWorkbook wb, String projNm, long contAmt, long prePay, long prevAmt,
                                           String periodText, List<Map<String, Object>> items,
                                           double bidRate, int interimMonths, int completionMonths, int totalMonths,
                                           CellStyle titleStyle, CellStyle headerStyle, CellStyle bodyStyle,
                                           CellStyle bodyBoldStyle, CellStyle numberStyle, CellStyle percentStyle,
                                           CellStyle totalStyle, CellStyle totalNumberStyle) {
        Sheet sheet = wb.createSheet("기성 내역서");
        sheet.setColumnWidth(0, 6000);
        for (int i = 1; i <= 9; i++) sheet.setColumnWidth(i, 4000);

        int r = 0;
        Row row = sheet.createRow(r);
        setCellValue(row, 0, "기 성 내 역 서", titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 9));

        // 가~사 항목
        r = 2;
        row = sheet.createRow(r);
        setCellValue(row, 0, "���. 용    역     명 : " + projNm, bodyStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 9));

        r++;
        row = sheet.createRow(r);
        Cell amtCell = row.createCell(0);
        // 계약금액은 수식으로 한글 변환 불가 → 직접 텍스트
        String contAmtKr = "금" + String.format("%,d", contAmt) + "원";
        amtCell.setCellValue("나. 계  약  금  액 : " + contAmtKr);
        amtCell.setCellStyle(bodyStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 9));

        r++;
        row = sheet.createRow(r);
        setCellValue(row, 0, "다. 선  금  금  액 : " + (prePay > 0 ? "금" + String.format("%,d", prePay) + "원" : "-"), bodyStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 9));

        r++;
        row = sheet.createRow(r);
        setCellValue(row, 0, "라. 전회기성금액 : " + (prevAmt > 0 ? "금" + String.format("%,d", prevAmt) + "원" : "-"), bodyStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 9));

        r++;
        row = sheet.createRow(r);
        setCellValue(row, 0, "마. 선급금 정산금액 : -", bodyStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 9));

        // 금회기성신청금액 = 총괄표의 기성금 총계 참조
        r++;
        row = sheet.createRow(r);
        Cell currentAmtCell = row.createCell(0);
        currentAmtCell.setCellFormula("\"바. 금회기성신청금액 : 금\"&TEXT(총괄표!F8,\"#,##0\")&\"원\"");
        currentAmtCell.setCellStyle(bodyStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 9));

        r++;
        row = sheet.createRow(r);
        setCellValue(row, 0, "사. 수행기간: " + periodText + "(" + interimMonths + "개월)", bodyStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 9));

        r++;
        row = sheet.createRow(r);
        setCellValue(row, 0, "아. 내     역(부가가치세 포함)", bodyBoldStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 9));

        // 내역 테이블 헤더
        r += 2;
        row = sheet.createRow(r);
        setCellValue(row, 0, "구분", headerStyle);
        setCellValue(row, 1, "도급액", headerStyle);
        setCellValue(row, 2, "전회 기성부문", headerStyle);
        setCellValue(row, 3, "", headerStyle);
        setCellValue(row, 4, "금회 기성부문", headerStyle);
        setCellValue(row, 5, "", headerStyle);
        setCellValue(row, 6, "기성 누계", headerStyle);
        setCellValue(row, 7, "", headerStyle);
        setCellValue(row, 8, "준공금", headerStyle);
        setCellValue(row, 9, "", headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 2, 3));
        sheet.addMergedRegion(new CellRangeAddress(r, r, 4, 5));
        sheet.addMergedRegion(new CellRangeAddress(r, r, 6, 7));
        sheet.addMergedRegion(new CellRangeAddress(r, r, 8, 9));

        r++;
        row = sheet.createRow(r);
        setCellValue(row, 0, "", headerStyle);
        setCellValue(row, 1, "금액(원)", headerStyle);
        setCellValue(row, 2, "금액(원)", headerStyle);
        setCellValue(row, 3, "비율(%)", headerStyle);
        setCellValue(row, 4, "금액(원)", headerStyle);
        setCellValue(row, 5, "비율(%)", headerStyle);
        setCellValue(row, 6, "금액(원)", headerStyle);
        setCellValue(row, 7, "비율(%)", headerStyle);
        setCellValue(row, 8, "금액(원)", headerStyle);
        setCellValue(row, 9, "비율(%)", headerStyle);
        int dataStartRow = r + 2; // Excel 1-based

        // 데이터 행: 각 SW 항목
        for (int i = 0; i < items.size(); i++) {
            r++;
            row = sheet.createRow(r);
            Map<String, Object> item = items.get(i);
            long cost = toLong(item.get("cost"));
            double rate = toDouble(item.get("rate")) / 100.0;
            // 연간유지관리비(낙찰율, VAT별도)
            long annualMaint = Math.round(cost * rate * bidRate);
            long interimAmt = Math.round(annualMaint * interimMonths / (double) totalMonths);
            long completionAmt = annualMaint - interimAmt;

            setCellValue(row, 0, str(item.get("name")), bodyStyle);
            // 도급액 = 연간유지관리비
            Cell c1 = row.createCell(1); c1.setCellValue(annualMaint); c1.setCellStyle(numberStyle);
            // 전회 기성 = prevAmt 비율분배 (첫 기성이면 0)
            Cell c2 = row.createCell(2); c2.setCellValue(0); c2.setCellStyle(numberStyle);
            Cell c3 = row.createCell(3); c3.setCellValue(0); c3.setCellStyle(numberStyle);
            // 금회 기성
            Cell c4 = row.createCell(4); c4.setCellValue(interimAmt); c4.setCellStyle(numberStyle);
            Cell c5 = row.createCell(5); c5.setCellValue(0.5); c5.setCellStyle(percentStyle);
            // 기성 누계 = 전회 + 금회
            Cell c6 = row.createCell(6); c6.setCellFormula("C" + (r + 1) + "+E" + (r + 1)); c6.setCellStyle(numberStyle);
            Cell c7 = row.createCell(7); c7.setCellValue(0.5); c7.setCellStyle(percentStyle);
            // 준공금
            Cell c8 = row.createCell(8); c8.setCellValue(completionAmt); c8.setCellStyle(numberStyle);
            Cell c9 = row.createCell(9); c9.setCellValue(0.5); c9.setCellStyle(percentStyle);
        }

        // 소계
        int itemEndRow = r + 1;
        r++;
        row = sheet.createRow(r);
        setCellValue(row, 0, "소계", totalStyle);
        for (int col = 1; col <= 9; col += 2) {
            if (items.size() > 0) {
                Cell sc = row.createCell(col);
                String colLetter = String.valueOf((char) ('B' + col - 1));
                sc.setCellFormula("SUM(" + colLetter + dataStartRow + ":" + colLetter + itemEndRow + ")");
                sc.setCellStyle(totalNumberStyle);
            }
            if (col + 1 <= 9) setCellValue(row, col + 1, "", totalStyle);
        }

        // 부가세
        r++;
        int vatRow = r + 1;
        row = sheet.createRow(r);
        setCellValue(row, 0, "부가가치세", bodyBoldStyle);
        Cell vatB = row.createCell(1); vatB.setCellFormula("B" + (r) + "*0.1"); vatB.setCellStyle(numberStyle);
        setCellValue(row, 2, "", bodyStyle); setCellValue(row, 3, "", bodyStyle);
        Cell vatE = row.createCell(4); vatE.setCellFormula("E" + (r) + "*0.1"); vatE.setCellStyle(numberStyle);
        setCellValue(row, 5, "", bodyStyle);
        Cell vatG = row.createCell(6); vatG.setCellFormula("G" + (r) + "*0.1"); vatG.setCellStyle(numberStyle);
        setCellValue(row, 7, "", bodyStyle);
        Cell vatI = row.createCell(8); vatI.setCellFormula("I" + (r) + "*0.1"); vatI.setCellStyle(numberStyle);
        setCellValue(row, 9, "", bodyStyle);

        // 총합계
        r++;
        row = sheet.createRow(r);
        setCellValue(row, 0, "총합계", totalStyle);
        for (int col : new int[]{1, 4, 6, 8}) {
            Cell tc = row.createCell(col);
            String colLetter = String.valueOf((char) ('B' + col - 1));
            tc.setCellFormula(colLetter + (r) + "+" + colLetter + (r + 1 - 1));
            tc.setCellStyle(totalNumberStyle);
        }

        // 총계 (백단위 절사)
        r++;
        row = sheet.createRow(r);
        setCellValue(row, 0, "총계", totalStyle);
        for (int col : new int[]{1, 4, 6, 8}) {
            Cell tc = row.createCell(col);
            String colLetter = String.valueOf((char) ('B' + col - 1));
            tc.setCellFormula("ROUNDDOWN(" + colLetter + r + ",-3)");
            tc.setCellStyle(totalNumberStyle);
        }
    }

    // --- 기성 Sheet 3: 총괄표 ---
    private void createInterimSummarySheet(XSSFWorkbook wb, List<Map<String, Object>> items,
                                            double bidRate, int interimMonths, int completionMonths, int totalMonths,
                                            CellStyle headerStyle, CellStyle bodyStyle, CellStyle bodyBoldStyle,
                                            CellStyle numberStyle, CellStyle percentStyle,
                                            CellStyle totalStyle, CellStyle totalNumberStyle) {
        Sheet sheet = wb.createSheet("총괄표");
        sheet.setColumnWidth(0, 2000);
        sheet.setColumnWidth(1, 7000);
        sheet.setColumnWidth(2, 5000);
        sheet.setColumnWidth(3, 4500);
        sheet.setColumnWidth(4, 4500);
        sheet.setColumnWidth(5, 4500);
        sheet.setColumnWidth(6, 4500);
        sheet.setColumnWidth(7, 4500);
        sheet.setColumnWidth(8, 4000);

        int r = 0;
        Row row = sheet.createRow(r);
        setCellValue(row, 0, "총        괄        표", headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 8));

        r = 1;
        row = sheet.createRow(r);
        setCellValue(row, 0, "구 분", headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 1));
        setCellValue(row, 2, "산출내역", headerStyle);
        setCellValue(row, 3, "직접인건비", headerStyle);
        setCellValue(row, 4, "경비 및 재료비", headerStyle);
        setCellValue(row, 5, "기성금(" + interimMonths + "개월)", headerStyle);
        setCellValue(row, 6, "준공금(" + completionMonths + "개월)", headerStyle);
        setCellValue(row, 7, "소계", headerStyle);
        setCellValue(row, 8, "비  고", headerStyle);

        // 카테고리 행
        r = 2;
        row = sheet.createRow(r);
        setCellValue(row, 1, "1. 상용소프트웨어 유지관리", bodyBoldStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 1, 8));

        // 항목별
        for (int i = 0; i < items.size(); i++) {
            r++;
            row = sheet.createRow(r);
            Map<String, Object> item = items.get(i);
            long cost = toLong(item.get("cost"));
            double rate = toDouble(item.get("rate")) / 100.0;
            long annualMaint = Math.round(cost * rate * bidRate);
            long interimAmt = Math.round(annualMaint * interimMonths / (double) totalMonths);
            long completionAmt = annualMaint - interimAmt;

            setCellValue(row, 0, String.valueOf(i + 1), bodyStyle);
            setCellValue(row, 1, str(item.get("name")), bodyStyle);
            setCellValue(row, 2, "", bodyStyle);
            Cell cD = row.createCell(3); cD.setCellValue(annualMaint); cD.setCellStyle(numberStyle);
            setCellValue(row, 4, "", bodyStyle);
            Cell cF = row.createCell(5); cF.setCellValue(interimAmt); cF.setCellStyle(numberStyle);
            Cell cG = row.createCell(6); cG.setCellValue(completionAmt); cG.setCellStyle(numberStyle);
            Cell cH = row.createCell(7); cH.setCellFormula("D" + (r + 1)); cH.setCellStyle(numberStyle);
            setCellValue(row, 8, "", bodyStyle);
        }

        // 합계
        r++;
        int sumRow = r + 1;
        row = sheet.createRow(r);
        setCellValue(row, 0, "합         계", totalStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 1));
        for (int col : new int[]{5, 6, 7}) {
            Cell sc = row.createCell(col);
            String cl = String.valueOf((char) ('A' + col));
            sc.setCellFormula("SUM(" + cl + "4:" + cl + r + ")");
            sc.setCellStyle(totalNumberStyle);
        }

        // 부가세
        r++;
        row = sheet.createRow(r);
        setCellValue(row, 0, "부   가   세", bodyBoldStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 1));
        for (int col : new int[]{5, 6, 7}) {
            Cell vc = row.createCell(col);
            String cl = String.valueOf((char) ('A' + col));
            vc.setCellFormula(cl + sumRow + "*0.1");
            vc.setCellStyle(numberStyle);
        }

        // 총합계
        r++;
        row = sheet.createRow(r);
        setCellValue(row, 0, "총합계", totalStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 1));
        for (int col : new int[]{5, 6, 7}) {
            Cell tc = row.createCell(col);
            String cl = String.valueOf((char) ('A' + col));
            tc.setCellFormula(cl + sumRow + "+" + cl + (r));
            tc.setCellStyle(totalNumberStyle);
        }

        // 총용역비 (백단위 절사)
        r++;
        row = sheet.createRow(r);
        setCellValue(row, 0, "총 용 역 비", totalStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 1));
        for (int col : new int[]{5, 6, 7}) {
            Cell fc = row.createCell(col);
            String cl = String.valueOf((char) ('A' + col));
            fc.setCellFormula("ROUNDDOWN(" + cl + r + ",-3)");
            fc.setCellStyle(totalNumberStyle);
        }
        setCellValue(row, 8, "백단위 절사(낙찰율 " + Math.round(bidRate * 100) + "%)", bodyStyle);
    }

    // --- 기성 Sheet 4: 상세 내역 ---
    private void createInterimItemDetailSheet(XSSFWorkbook wb, String projNm, List<Map<String, Object>> items,
                                               double bidRate, int interimMonths, int completionMonths, int totalMonths,
                                               CellStyle titleStyle, CellStyle headerStyle, CellStyle bodyStyle,
                                               CellStyle numberStyle, CellStyle percentStyle,
                                               CellStyle totalStyle, CellStyle totalNumberStyle) {
        String sheetName = items.size() > 0 ? str(items.get(0).get("name")) + " 유지관리" : "상세내역";
        if (sheetName.length() > 31) sheetName = sheetName.substring(0, 31);
        Sheet sheet = wb.createSheet(sheetName);
        for (int i = 0; i < 16; i++) sheet.setColumnWidth(i, 3200);

        int r = 0;
        Row row = sheet.createRow(r);
        setCellValue(row, 0, projNm + " 내역", headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 15));

        // 헤더
        r = 1;
        row = sheet.createRow(r);
        setCellValue(row, 0, "시스템목록", headerStyle);
        setCellValue(row, 1, "구분", headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r + 1, 1, 2));
        setCellValue(row, 3, "총액\n(VAT 포함)", headerStyle);
        setCellValue(row, 4, "취득원가(원)\n(VAT별도)", headerStyle);
        setCellValue(row, 5, "요율", headerStyle);
        setCellValue(row, 6, "연간 유지관리비\n(낙찰율 " + Math.round(bidRate * 100) + "%)", headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 6, 7));
        setCellValue(row, 8, "기성금", headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 8, 9));
        setCellValue(row, 10, "준공금", headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 10, 11));
        setCellValue(row, 12, "장비설명", headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 12, 13));
        setCellValue(row, 14, "비고", headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 14, 15));

        r = 2;
        row = sheet.createRow(r);
        setCellValue(row, 0, "", headerStyle);
        setCellValue(row, 6, "기간", headerStyle);
        setCellValue(row, 7, "금액\n(VAT별도)", headerStyle);
        setCellValue(row, 8, "기간", headerStyle);
        setCellValue(row, 9, "금액\n(VAT별도)", headerStyle);
        setCellValue(row, 10, "기간", headerStyle);
        setCellValue(row, 11, "금액\n(VAT별도)", headerStyle);
        setCellValue(row, 15, "낙찰율", headerStyle);

        // 항목
        for (int i = 0; i < items.size(); i++) {
            r++;
            row = sheet.createRow(r);
            Map<String, Object> item = items.get(i);
            long price = toLong(item.get("unitPrice"));
            long cost = toLong(item.get("cost"));
            double rate = toDouble(item.get("rate")) / 100.0;
            long annualMaint = Math.round(cost * rate * bidRate);
            long interimAmt = Math.round(annualMaint * interimMonths / (double) totalMonths);
            long completionAmt = annualMaint - interimAmt;

            setCellValue(row, 0, str(item.get("name")), bodyStyle);
            setCellValue(row, 1, "S/W", bodyStyle);
            setCellValue(row, 2, "라이선스", bodyStyle);
            Cell c3 = row.createCell(3); c3.setCellValue(price); c3.setCellStyle(numberStyle);
            Cell c4 = row.createCell(4); c4.setCellValue(cost); c4.setCellStyle(numberStyle);
            Cell c5 = row.createCell(5); c5.setCellValue(rate); c5.setCellStyle(percentStyle);
            Cell c6 = row.createCell(6); c6.setCellValue(12); c6.setCellStyle(bodyStyle);
            Cell c7 = row.createCell(7); c7.setCellValue(annualMaint); c7.setCellStyle(numberStyle);
            setCellValue(row, 8, interimMonths + "개월", bodyStyle);
            Cell c9 = row.createCell(9); c9.setCellValue(interimAmt); c9.setCellStyle(numberStyle);
            setCellValue(row, 10, completionMonths + "개월", bodyStyle);
            Cell c11 = row.createCell(11); c11.setCellValue(completionAmt); c11.setCellStyle(numberStyle);
            setCellValue(row, 12, "", bodyStyle);
            setCellValue(row, 14, "", bodyStyle);
            Cell c15 = row.createCell(15); c15.setCellValue(bidRate); c15.setCellStyle(percentStyle);
        }

        // 소계
        r++;
        row = sheet.createRow(r);
        setCellValue(row, 0, "소    계", totalStyle);
        for (int col : new int[]{7, 9, 11}) {
            Cell sc = row.createCell(col);
            String cl = String.valueOf((char) ('A' + col));
            sc.setCellFormula("SUM(" + cl + "4:" + cl + r + ")");
            sc.setCellStyle(totalNumberStyle);
        }
    }

    // --- 스타일 헬퍼 ---
    private CellStyle createTitleStyle(XSSFWorkbook wb, int fontSize) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("맑은 고딕");
        font.setBold(true);
        font.setFontHeightInPoints((short) fontSize);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(XSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("맑은 고딕");
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorders(style);
        return style;
    }

    private CellStyle createBodyStyle(XSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("맑은 고딕");
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style);
        return style;
    }

    private CellStyle createTotalStyle(XSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("맑은 고딕");
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorders(style);
        return style;
    }

    private void setBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    private void setCellValue(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private double toDouble(Object obj) {
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        if (obj instanceof String) {
            try { return Double.parseDouble((String) obj); } catch (NumberFormatException e) { return 0; }
        }
        return 0;
    }

    private long toLong(Object obj) {
        if (obj instanceof Number) return ((Number) obj).longValue();
        if (obj instanceof String) {
            try { return Long.parseLong((String) obj); } catch (NumberFormatException e) { return 0; }
        }
        return 0;
    }

    private String str(Object obj) {
        return obj != null ? obj.toString() : "";
    }

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
}
