package com.swmanager.system.service;

import com.swmanager.system.domain.SwProject;
import com.swmanager.system.domain.workplan.Document;
import com.swmanager.system.domain.workplan.DocumentDetail;
import com.swmanager.system.i18n.MessageResolver;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static com.swmanager.system.service.ExcelStyleSupport.FONT;
import static com.swmanager.system.service.ExcelStyleSupport.str;
import static com.swmanager.system.service.ExcelStyleSupport.toDouble;
import static com.swmanager.system.service.ExcelStyleSupport.toLong;
import static com.swmanager.system.service.ExcelStyleSupport.setStringDirect;
import static com.swmanager.system.service.ExcelStyleSupport.setNumericDirect;

/**
 * 기성내역서(중간) Excel 생성 — ExcelExportService 에서 분리(excel-service-split §6-5 C).
 *
 * INTERIM 문서의 detail_sheet 섹션을 받아 강릉시 형식 4-sheet xlsx 를 만든다. 공용 셀 헬퍼는
 * {@link ExcelStyleSupport}(static import). 진입점 generateInterimReport 는 ExcelExportService 에
 * facade wrapper 로도 남아 있다(public API 유지). 절사정책 static 은 본 양식에서 미사용.
 */
@Service
public class InterimReportExcelService {

    private final DocumentService documentService;
    private final MessageResolver messages;

    public InterimReportExcelService(DocumentService documentService, MessageResolver messages) {
        this.documentService = documentService;
        this.messages = messages;
    }

    // =====================================================
    // 기성내역서 Excel (강릉시 형식: 4 sheets)
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
                if (truncDigit < 1 || truncDigit > 5) truncDigit = 3;
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

            boolean fullBidRate = bidRate >= 100.0;
            int truncDigit = (int) toDouble(data.get("truncDigit"));
            if (truncDigit < 1 || truncDigit > 5) truncDigit = 3;
            String truncLabel = switch (truncDigit) {
                case 1 -> "일단위 절사";
                case 2 -> "십단위 절사";
                case 4 -> "천단위 절사";
                case 5 -> "만단위 절사";
                default -> "백단위 절사";
            };

            // ── [0-b] 표지: A5 사업명 셀 폭 초과 시 자동 축소
            if (cover != null) {
                Row r5 = cover.getRow(4);
                Cell a5 = r5 != null ? r5.getCell(0) : null;
                if (a5 != null) {
                    CellStyle src = a5.getCellStyle();
                    CellStyle cs = wb.createCellStyle();
                    if (src != null) cs.cloneStyleFrom(src);
                    cs.setShrinkToFit(true);
                    a5.setCellStyle(cs);
                }
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
                // row 18 (B/E/G/I): 100% → row 17 직접 참조 / 미만 → 선택 절사 단위 적용
                Row row18 = detail.getRow(17);
                if (row18 == null) row18 = detail.createRow(17);
                for (int col : new int[]{1, 4, 6, 8}) {  // B18, E18, G18, I18
                    Cell c = row18.getCell(col);
                    if (c == null) c = row18.createCell(col);
                    String letter = String.valueOf((char) ('A' + col));
                    if (fullBidRate) {
                        c.setCellFormula(letter + "17");
                    } else {
                        c.setCellFormula("ROUNDDOWN(" + letter + "17,-" + truncDigit + ")");
                    }
                }
            }

            // ── [3] GeoNURIS for KRAS 1.0 유지관리 ──
            Sheet geo = wb.getSheet("GeoNURIS for KRAS 1.0 유지관리");
            // SW요율(%): 폼 입력 > proj.swRt > 12 fallback
            double swRtPct;
            Object swRtForm = data.get("swRt");
            if (swRtForm != null && !String.valueOf(swRtForm).isBlank() && toDouble(swRtForm) > 0) {
                swRtPct = toDouble(swRtForm);
            } else if (proj != null && proj.getSwRt() != null && proj.getSwRt() > 0) {
                swRtPct = proj.getSwRt();
            } else {
                swRtPct = 12.0;
            }
            double swRate = swRtPct / 100.0;
            if (geo != null) {
                // G2 header: 낙찰율 100% 면 라벨에서 (낙찰율 %) 생략
                String g2Text = fullBidRate
                        ? "연간 유지관리비"
                        : "연간 유지관리비\n(낙찰율 " + trimPct(bidRate) + "%)";
                setStringDirect(geo, 1, 6, g2Text);
                // D4: 도입가(VAT포함) 총액
                setNumericDirect(geo, 3, 3, totalUnitPrice);
                // E4: VAT 제외 단가 직접 기재(수식 대체)
                double e4 = totalUnitPrice / 1.1;
                setNumericDirect(geo, 3, 4, Math.round(e4));
                // F4: SW 요율 동적 주입 (소수 비율)
                setNumericDirect(geo, 3, 5, swRate);
                // H4: 100% 면 formula `=E4*F4` (낙찰율 곱 제거), 미만이면 numeric 으로 낙찰율 반영
                double h4;
                if (fullBidRate) {
                    Row r4 = geo.getRow(3);
                    if (r4 == null) r4 = geo.createRow(3);
                    Cell h4cell = r4.getCell(7);
                    if (h4cell == null) h4cell = r4.createCell(7);
                    h4cell.setCellFormula("E4*F4");
                    h4 = Math.round(e4 * swRate);
                } else {
                    h4 = Math.round(e4 * swRate * (bidRate / 100.0));
                    setNumericDirect(geo, 3, 7, h4);
                }
                // I4: 기성기간(개월), K4: 준공기간(개월)
                setNumericDirect(geo, 3, 8, interimMonths);
                setNumericDirect(geo, 3, 10, completionMonths);
                // J4: 기성금 = =H4/(G4/I4) — formula 로 설정
                Row r4j = geo.getRow(3);
                if (r4j == null) r4j = geo.createRow(3);
                Cell j4cell = r4j.getCell(9);
                if (j4cell == null) j4cell = r4j.createCell(9);
                j4cell.setCellFormula("H4/(G4/I4)");
                // L4: 준공금 = =H4-J4 — formula 로 설정
                Cell l4cell = r4j.getCell(11);
                if (l4cell == null) l4cell = r4j.createCell(11);
                l4cell.setCellFormula("H4-J4");
            }

            // ── [4] 총괄표: bidRate=100 시 ROUNDDOWN 제거 / 미만이면 선택된 절사 단위 적용
            Sheet summary = wb.getSheet("총괄표");
            if (summary != null) {
                Row row8 = summary.getRow(7);
                if (row8 == null) row8 = summary.createRow(7);
                if (fullBidRate) {
                    Cell f8 = row8.getCell(5);
                    if (f8 == null) f8 = row8.createCell(5);
                    f8.setCellFormula("F7");
                    Cell g8 = row8.getCell(6);
                    if (g8 == null) g8 = row8.createCell(6);
                    g8.setCellFormula("G7");
                    setStringDirect(summary, 7, 8, "");
                } else {
                    Cell f8 = row8.getCell(5);
                    if (f8 == null) f8 = row8.createCell(5);
                    f8.setCellFormula("ROUNDDOWN(F7,-" + truncDigit + ")");
                    Cell g8 = row8.getCell(6);
                    if (g8 == null) g8 = row8.createCell(6);
                    g8.setCellFormula("ROUNDDOWN(G7,-" + truncDigit + ")");
                    setStringDirect(summary, 7, 8, truncLabel + "(낙찰율 " + trimPct(bidRate) + "%)");
                }
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
        f.setFontName(FONT);
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

}
