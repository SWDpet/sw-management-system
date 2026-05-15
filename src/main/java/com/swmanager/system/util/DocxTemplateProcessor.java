package com.swmanager.system.util;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;

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
 * <p>스타일은 첫 run 의 스타일을 따른다 — 시안D 의 모든 placeholder paragraph 는 단일
 * 스타일이므로 안전.
 *
 * <p>지원 syntax: {@code ${key}}. value 가 {@code null} 이면 빈 문자열로 치환.
 */
public final class DocxTemplateProcessor {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)\\}");

    private DocxTemplateProcessor() {}

    /**
     * 문서 전체의 paragraph + table 안 paragraph 를 모두 처리.
     */
    public static void process(XWPFDocument doc, Map<String, String> vars) {
        for (XWPFParagraph p : doc.getParagraphs()) {
            replaceInParagraph(p, vars);
        }
        for (XWPFTable t : doc.getTables()) {
            processTable(t, vars);
        }
    }

    private static void processTable(XWPFTable table, Map<String, String> vars) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph p : cell.getParagraphs()) {
                    replaceInParagraph(p, vars);
                }
                // nested tables
                for (XWPFTable nt : cell.getTables()) {
                    processTable(nt, vars);
                }
            }
        }
    }

    /**
     * paragraph 의 모든 run text 를 합쳐서 ${key} 치환 후 첫 run 에 일괄 set.
     * 한 paragraph 안에 placeholder 가 여러 개여도 모두 치환.
     */
    static void replaceInParagraph(XWPFParagraph p, Map<String, String> vars) {
        List<XWPFRun> runs = p.getRuns();
        if (runs.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        for (XWPFRun r : runs) {
            String text = r.text();
            if (text != null) sb.append(text);
        }
        String full = sb.toString();
        if (full.indexOf("${") < 0) return;

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

        // 첫 run 의 모든 <w:t> 제거 후 단일 텍스트로 set
        XWPFRun first = runs.get(0);
        CTR ctr = first.getCTR();
        for (int i = ctr.sizeOfTArray() - 1; i >= 0; i--) {
            ctr.removeT(i);
        }
        first.setText(out.toString(), 0);

        // 나머지 run 들의 <w:t> 비우기 (구조/스타일 자체는 유지)
        for (int i = 1; i < runs.size(); i++) {
            XWPFRun r = runs.get(i);
            CTR ct = r.getCTR();
            for (int j = ct.sizeOfTArray() - 1; j >= 0; j--) {
                ct.removeT(j);
            }
            // tab/break/instrText 같은 다른 child 는 손대지 않음
        }
    }
}
