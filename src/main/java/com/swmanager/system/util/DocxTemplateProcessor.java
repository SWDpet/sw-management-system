package com.swmanager.system.util;

import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * docx 템플릿 placeholder 치환 헬퍼.
 *
 * <p>Word 가 같은 텍스트를 여러 {@code <w:r><w:t>} 로 split 저장하는 문제 (예: "${cover.x}"
 * 가 "${cover.", "x}", "" 로 쪼개짐) 를 해결하기 위해, paragraph 단위로 모든 run 의
 * text 를 합친 뒤 치환한 결과를 첫 run 의 <w:t> 들에 단일 텍스트로 넣고 나머지 run 들의
 * 텍스트는 비운다.
 *
 * <p>지원 syntax:
 * <ul>
 *   <li>{@code ${key}} — 텍스트 치환 (value 가 {@code null} 이면 빈 문자열)</li>
 *   <li>{@code ${key.image}} — 이미지 임베드 (inspect-report-d-v6 Phase E 신규).
 *       images map 에 해당 키가 있으면 POI {@code XWPFRun.addPicture} 로 PNG 삽입.
 *       paragraph 안의 placeholder 텍스트는 제거된다.</li>
 * </ul>
 */
public final class DocxTemplateProcessor {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)\\}");

    /** 메트릭 추이 차트 크기 — 17.4cm × 8.0cm (디자인팀 R6). */
    public static final int DEFAULT_IMAGE_WIDTH_CM  = 174;   // 0.1cm 단위
    public static final int DEFAULT_IMAGE_HEIGHT_CM = 80;

    private DocxTemplateProcessor() {}

    /** 텍스트만 처리 (이미지 placeholder 없는 v5 호환 경로). */
    public static void process(XWPFDocument doc, Map<String, String> vars) {
        process(doc, vars, java.util.Collections.emptyMap());
    }

    /**
     * 문서 전체 paragraph + table 안 paragraph 를 모두 처리.
     * @param vars   {@code ${key}} → 문자열 치환 맵
     * @param images {@code ${key.image}} → PNG byte[] 임베드 맵 (v6 메트릭 차트)
     */
    public static void process(XWPFDocument doc, Map<String, String> vars, Map<String, byte[]> images) {
        for (XWPFParagraph p : doc.getParagraphs()) {
            replaceInParagraph(p, vars, images);
        }
        for (XWPFTable t : doc.getTables()) {
            processTable(t, vars, images);
        }
    }

    private static void processTable(XWPFTable table, Map<String, String> vars, Map<String, byte[]> images) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph p : cell.getParagraphs()) {
                    replaceInParagraph(p, vars, images);
                }
                for (XWPFTable nt : cell.getTables()) {
                    processTable(nt, vars, images);
                }
            }
        }
    }

    /**
     * paragraph 의 모든 run text 를 합쳐서 ${key} 치환 후 첫 run 에 일괄 set.
     * 이미지 placeholder({@code .image} 로 끝나는 키) 가 있으면 텍스트 제거 + addPicture.
     */
    static void replaceInParagraph(XWPFParagraph p, Map<String, String> vars, Map<String, byte[]> images) {
        List<XWPFRun> runs = p.getRuns();
        if (runs.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        for (XWPFRun r : runs) {
            String text = r.text();
            if (text != null) sb.append(text);
        }
        String full = sb.toString();
        if (full.indexOf("${") < 0) return;

        // 1) 이미지 placeholder 우선 처리 — paragraph 에 ${...image} 가 있고 images map 에 키 있음
        String imageKey = findImagePlaceholder(full, images);
        if (imageKey != null) {
            byte[] png = images.get(imageKey);
            // run 0 의 텍스트 비우기 + 이미지 추가
            XWPFRun first = runs.get(0);
            CTR ctr = first.getCTR();
            for (int i = ctr.sizeOfTArray() - 1; i >= 0; i--) ctr.removeT(i);
            for (int i = 1; i < runs.size(); i++) {
                XWPFRun r = runs.get(i);
                CTR ct = r.getCTR();
                for (int j = ct.sizeOfTArray() - 1; j >= 0; j--) ct.removeT(j);
            }
            try (ByteArrayInputStream bin = new ByteArrayInputStream(png)) {
                int widthEmu  = Units.toEMU(DEFAULT_IMAGE_WIDTH_CM  / 10.0 * 28.3464567);  // cm → pt → EMU
                int heightEmu = Units.toEMU(DEFAULT_IMAGE_HEIGHT_CM / 10.0 * 28.3464567);
                first.addPicture(bin, XWPFDocument.PICTURE_TYPE_PNG, "chart.png", widthEmu, heightEmu);
            } catch (Exception e) {
                first.setText("[차트 임베드 실패: " + e.getMessage() + "]", 0);
            }
            return;
        }

        // 2) 텍스트 치환 (v5 동작과 동일)
        Matcher m = PLACEHOLDER.matcher(full);
        if (!m.find()) return;

        StringBuilder out = new StringBuilder();
        int pos = 0;
        m.reset();
        while (m.find()) {
            out.append(full, pos, m.start());
            String key = m.group(1);
            String value = vars.get(key);
            out.append(value != null ? value : "");
            pos = m.end();
        }
        out.append(full, pos, full.length());

        XWPFRun first = runs.get(0);
        CTR ctr = first.getCTR();
        for (int i = ctr.sizeOfTArray() - 1; i >= 0; i--) {
            ctr.removeT(i);
        }
        first.setText(out.toString(), 0);

        for (int i = 1; i < runs.size(); i++) {
            XWPFRun r = runs.get(i);
            CTR ct = r.getCTR();
            for (int j = ct.sizeOfTArray() - 1; j >= 0; j--) {
                ct.removeT(j);
            }
        }
    }

    /** paragraph 본문에서 ${...image} 패턴 + images map 키 매칭되는 첫 키 반환. */
    private static String findImagePlaceholder(String paragraphText, Map<String, byte[]> images) {
        if (images == null || images.isEmpty()) return null;
        Matcher m = PLACEHOLDER.matcher(paragraphText);
        while (m.find()) {
            String key = m.group(1);
            if (key.endsWith(".image") && images.containsKey(key)) {
                return key;
            }
        }
        return null;
    }
}
