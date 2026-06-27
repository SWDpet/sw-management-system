package com.swmanager.system.service;

import lombok.extern.slf4j.Slf4j;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static com.swmanager.system.service.ExcelStyleSupport.str;
import static com.swmanager.system.service.ExcelStyleSupport.toDouble;
import static com.swmanager.system.service.ExcelStyleSupport.toLong;
import static com.swmanager.system.service.ExcelStyleSupport.setStringDirect;
import static com.swmanager.system.service.ExcelStyleSupport.setNumericDirect;
import static com.swmanager.system.service.ExcelExportService.toRoundDigits;
import static com.swmanager.system.service.ExcelExportService.roundLabel;

/**
 * 설계내역서(견적) Excel 의 TYPE별 생성 로직 — DesignEstimateExcelService 에서 분리(S4 거대클래스 #4).
 *
 * design_estimate 파싱 결과(projNm/items 등)를 인자로 받아 TYPE_A/B/C/D 양식 xlsx 를 만드는
 * 순수 POI 함수군(필드/서비스 의존 0). 데이터 조회·dispatch 는 DesignEstimateExcelService 잔류.
 * 공용 셀 헬퍼는 {@link ExcelStyleSupport}, 절사정책 static 은 {@link ExcelExportService}.
 *
 * 기획서: docs/product-specs/refactor-design-estimate-writer.md
 * 개발계획서: docs/exec-plans/refactor-design-estimate-writer.md
 */
@Slf4j
final class DesignEstimateWriter {

    private DesignEstimateWriter() {}

    /** 필수 시트 — 없으면 fail-fast (§8-2). */
    private static Sheet requireSheet(XSSFWorkbook wb, String name) {
        Sheet s = wb.getSheet(name);
        if (s == null) throw new IllegalStateException("설계내역서 템플릿 시트 누락: " + name);
        return s;
    }

    /** 필수 행 — 없으면 fail-fast (§8-1). */
    private static Row requireRow(Sheet sheet, int rowIdx) {
        Row r = sheet.getRow(rowIdx);
        if (r == null) throw new IllegalStateException(
                "설계내역서 템플릿 행 누락: " + sheet.getSheetName() + " row " + (rowIdx + 1));
        return r;
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
    static byte[] generateFromTemplate(String projNm, String distNm, int year,
                                         String designDate, String location,
                                         double bidRate, int rounddownUnit,
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
    private static void fillCoverSheet(XSSFWorkbook wb, String projNm, String distNm, int year, String designDate) {
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
    private static void fillGapjiSheet(XSSFWorkbook wb, String designDate, String location) {
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
    private static void fillSummarySheet(XSSFWorkbook wb, List<Map<String, Object>> hwItems,
                                   List<Map<String, Object>> swItems, double bidRate, int rounddownUnit) {
        Sheet sheet = wb.getSheetAt(2); // 총괄표

        // HW 항목 채우기 (row4=index4, row5=index5 ... 템플릿에 2행 있음)
        int hwDataStartRow = 4; // 0-based (Excel row 5)
        int hwTemplateRows = 2; // 원본 템플릿의 HW 데이터 행 수
        if (hwItems.size() > hwTemplateRows) {
            log.warn("설계내역서(TYPE_A) 총괄표 HW 항목 {}개 중 템플릿 슬롯 {} 초과분 누락(행침범 방지)",
                    hwItems.size(), hwTemplateRows);
        }
        for (int i = 0; i < Math.min(hwItems.size(), hwTemplateRows); i++) {
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
        for (int i = hwItems.size(); i < hwTemplateRows; i++) {
            int rowIdx = hwDataStartRow + i;
            clearDataRow(sheet, rowIdx);
        }

        // SW 항목 채우기 (row8=index8, row9=index9 ... 템플릿에 2행 있음)
        int swDataStartRow = 8; // 0-based (Excel row 9)
        int swTemplateRows = 2; // 원본 템플릿의 SW 데이터 행 수
        if (swItems.size() > swTemplateRows) {
            log.warn("설계내역서(TYPE_A) 총괄표 SW 항목 {}개 중 템플릿 슬롯 {} 초과분 누락(행침범 방지)",
                    swItems.size(), swTemplateRows);
        }
        for (int i = 0; i < Math.min(swItems.size(), swTemplateRows); i++) {
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
    private static void simplifyMaintFormula(Sheet sheet, int rowIdx) {
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
    private static void setCellKeepStyle(Sheet sheet, int rowIdx, int colIdx, String value) {
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
    private static void setCellKeepStyle(Sheet sheet, int rowIdx, int colIdx, double value) {
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
    private static void clearDataRow(Sheet sheet, int rowIdx) {
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

    // =====================================================
    // TYPE_C: 복합형 (김포시 등) - 김포시 템플릿 기반
    // 14개 시트 그대로 보존, 표지/갑지 핵심 셀만 동적 채움
    // =====================================================
    static byte[] generateFromTypeCTemplate(String projNm, String distNm, int year,
                                             String designDate, double bidRate,
                                             List<Map<String, Object>> hwItems,
                                             List<Map<String, Object>> swItems) throws IOException {
        ClassPathResource res = new ClassPathResource("templates/excel/design_estimate_c_template.xlsx");
        try (InputStream is = res.getInputStream();
             XSSFWorkbook wb = new XSSFWorkbook(is)) {

            // 표지: B4(사업명), B8(설계년월), F12(지자체)
            Sheet cover = requireSheet(wb, "표지");
            setStringDirect(cover, 3, 1, projNm);
            setStringDirect(cover, 7, 1, designDate);
            setStringDirect(cover, 11, 5, distNm);

            // 갑지: A1(설계일자), A3(연도), A5(사업명), E10(HW), E11(SW)
            Sheet gapji = requireSheet(wb, "갑지");
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
    static byte[] generateFromTypeBTemplate(String projNm, String distNm, int year,
                                             String designDate, double bidRate, int rounddownUnit,
                                             List<Map<String, Object>> hwItems,
                                             List<Map<String, Object>> swItems) throws IOException {
        ClassPathResource res = new ClassPathResource("templates/excel/design_estimate_b_template.xlsx");
        try (InputStream is = res.getInputStream();
             XSSFWorkbook wb = new XSSFWorkbook(is)) {

            // 표지
            Sheet cover = requireSheet(wb, "표지");
            setStringDirect(cover, 4, 0, projNm);          // A5
            setStringDirect(cover, 14, 0, designDate);      // A15
            setStringDirect(cover, 21, 0, distNm);          // A22

            // 설계갑지
            Sheet gapji = requireSheet(wb, "설계갑지");
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
            Sheet summary = requireSheet(wb, "총괄표");
            // HW 항목: 행 4-5 (0-based 3-4)
            fillTypeBItemRows(summary, hwItems, 3);
            // SW 항목: 행 7-8 (0-based 6-7)
            fillTypeBItemRows(summary, swItems, 6);

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

    /** TYPE_B 총괄표의 항목 행을 채움. 템플릿엔 항목당 2행이 기본. 초과분은 행침범 방지를 위해 누락(§8-3). */
    private static void fillTypeBItemRows(Sheet summary, List<Map<String, Object>> items, int startRow0) {
        int templateSlots = 2;
        if (items.size() > templateSlots) {
            log.warn("설계내역서(TYPE_B) 총괄표 항목 {}개 중 템플릿 슬롯 {} 초과분 누락(행침범 방지)",
                    items.size(), templateSlots);
        }
        for (int i = 0; i < templateSlots; i++) {
            int r = startRow0 + i;
            requireRow(summary, r);   // 필수 항목 행 fail-fast (§8-1, fillTypeB 일관성)
            if (i < items.size()) {
                Map<String, Object> it = items.get(i);
                double rate = toDouble(it.get("rate")) / 100.0;
                long price = toLong(it.get("unitPrice"));
                // A:번호 / B:품명 / D:적용율 / E:도입가 — create-if-absent 통일(§8-5)
                setNumericDirect(summary, r, 0, i + 1);
                setStringDirect(summary, r, 1, str(it.get("name")));
                setNumericDirect(summary, r, 3, rate);
                setNumericDirect(summary, r, 4, price);
                // I열: 비고
                String remark = str(it.get("remark"));
                if (!remark.isEmpty()) setStringDirect(summary, r, 8, remark);
            } else {
                // 빈 행 처리: A(번호),B,D,E,I 비움 (수식은 그대로 두면 0 출력)
                setStringDirect(summary, r, 0, "");
                setStringDirect(summary, r, 1, "");
                setNumericDirect(summary, r, 3, 0);
                setNumericDirect(summary, r, 4, 0);
                setStringDirect(summary, r, 8, "");
            }
        }
    }

    // =====================================================
    // TYPE_D: SW전용 (김해시/강릉시 등) - 템플릿 기반
    // =====================================================
    static byte[] generateFromSwTemplate(String projNm, String distNm, int year,
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
            Sheet cover = requireSheet(wb, "표지");
            setStringDirect(cover, 4, 0, projNm);
            setStringDirect(cover, 14, 0, designDate);
            setStringDirect(cover, 21, 0, distNm);

            // 설계갑지: A5(서기 ~ 년도), A8(사업명), D10(사업명), D12(SW 항목)
            Sheet gapji = requireSheet(wb, "설계갑지");
            setStringDirect(gapji, 4, 0, "   서기 " + year + " 년도 ");
            setStringDirect(gapji, 7, 0, projNm);
            setStringDirect(gapji, 9, 3, projNm);
            setStringDirect(gapji, 11, 3, " 1. S/W :    " + swName + "  1식");

            // 총괄표
            Sheet summary = requireSheet(wb, "총괄표");
            // B5: SW 품명 / D5: 적용율 / E5: 도입가 — create-if-absent 통일(§8-5)
            requireRow(summary, 4);
            setStringDirect(summary, 4, 1, swName);
            setNumericDirect(summary, 4, 3, swRate);
            setNumericDirect(summary, 4, 4, swPrice);

            // H8: 총용역비 수식 (낙찰율 적용)
            Row r8 = requireRow(summary, 7);
            Cell h8 = r8.getCell(7);
            if (h8 != null) {
                if (bidRate > 0 && bidRate < 1.0) {
                    h8.setCellFormula("ROUNDDOWN((H6+H7)*" + bidRate + ",-1)");
                } else {
                    h8.setCellFormula("ROUNDDOWN(H6+H7,0)");
                }
            }
            // I8: 낙찰율 라벨
            Cell i8 = r8.getCell(8);
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
}
