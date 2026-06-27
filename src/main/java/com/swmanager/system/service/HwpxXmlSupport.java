package com.swmanager.system.service;

/**
 * HWPX(section0.xml) 후처리 순수 String 변환 모음 — HwpxExportService 에서 분리 (S4 거대클래스 #2).
 *
 * <p>기획서/개발계획 docs/{product-specs,exec-plans}/refactor-hwpx-xml-support.md.
 * 표 행수 동기화·rowAddr 재배정·XML 이스케이프·멀티라인 문단 확장·셀 제거 등 무상태 순수함수.
 * 필드/DI 의존 없음 → static. 동작은 이동 전과 바이트 동일(generateHwpx 회귀 스모크로 보증).
 */
final class HwpxXmlSupport {

    private HwpxXmlSupport() {}

    /** XML 이스케이프 + 줄바꿈을 sentinel 토큰으로 변환. expandMultilineParagraphs() 가 후처리. */
    static final String NL_SENTINEL = String.valueOf((char) 1) + "NL" + (char) 1;  // SOH NL SOH (원본 "0001NL0001" 와 런타임 동일, 이스케이프 회피)

    /** 표(<hp:tbl>)별 최상위 <hp:tr> 수로 rowCnt 갱신 + rowAddr 재배정. */
    static String syncTableRowCounts(String xml) {
        StringBuilder out = new StringBuilder();
        int cursor = 0;
        java.util.regex.Pattern tblOpen = java.util.regex.Pattern.compile("<hp:tbl\\b");
        java.util.regex.Matcher m = tblOpen.matcher(xml);
        while (m.find(cursor)) {
            int s = m.start();
            out.append(xml, cursor, s);
            // find matching </hp:tbl> with depth tracking
            int depth = 0, i = s, e = -1;
            while (i < xml.length()) {
                if (xml.startsWith("<hp:tbl", i) && (i + 7 < xml.length()) && (xml.charAt(i + 7) == ' ' || xml.charAt(i + 7) == '>')) {
                    depth++; i += 7;
                } else if (xml.startsWith("</hp:tbl>", i)) {
                    depth--;
                    if (depth == 0) { e = i + 9; break; }
                    i += 9;
                } else { i++; }
            }
            if (e < 0) { out.append(xml.substring(s)); cursor = xml.length(); break; }
            String tbl = xml.substring(s, e);

            // count top-level <hp:tr> within this table
            int rowCount = countTopLevelTags(tbl, "<hp:tr>", "</hp:tr>", "<hp:tbl", "</hp:tbl>");
            // update rowCnt attribute on opening tag
            int tagEnd = tbl.indexOf('>');
            String openTag = tbl.substring(0, tagEnd + 1);
            String rest = tbl.substring(tagEnd + 1);
            java.util.regex.Matcher rm = java.util.regex.Pattern.compile("rowCnt=\"(\\d+)\"").matcher(openTag);
            if (rm.find()) {
                openTag = openTag.substring(0, rm.start()) + "rowCnt=\"" + rowCount + "\"" + openTag.substring(rm.end());
            }
            // reassign rowAddr per top-level row
            rest = reassignRowAddrs(rest);
            out.append(openTag).append(rest);
            cursor = e;
        }
        out.append(xml.substring(cursor));
        return out.toString();
    }

    static int countTopLevelTags(String s, String open, String close, String nestOpen, String nestClose) {
        int count = 0, i = 0, depth = 0, nest = 0;
        // skip the opening <hp:tbl ...> itself: caller passes whole tbl, so track outer tbl depth
        while (i < s.length()) {
            if (s.startsWith(nestOpen, i) && (i + nestOpen.length() < s.length()) && (s.charAt(i + nestOpen.length()) == ' ' || s.charAt(i + nestOpen.length()) == '>')) {
                nest++; i += nestOpen.length();
            } else if (s.startsWith(nestClose, i)) {
                nest--; i += nestClose.length();
            } else if (nest == 1 && s.startsWith(open, i)) {
                // depth==0 means inside outermost tbl content
                if (depth == 0) count++;
                depth++; i += open.length();
            } else if (s.startsWith(close, i)) {
                depth--; i += close.length();
            } else { i++; }
        }
        return count;
    }

    static String reassignRowAddrs(String tblBody) {
        // Walk top-level <hp:tr>...</hp:tr> blocks; in each, replace rowAddr="N" with current index
        StringBuilder out = new StringBuilder();
        int i = 0, idx = 0, nest = 0;
        while (i < tblBody.length()) {
            if (tblBody.startsWith("<hp:tbl", i) && (i + 7 < tblBody.length()) && (tblBody.charAt(i + 7) == ' ' || tblBody.charAt(i + 7) == '>')) {
                // nested table: copy as-is until matching close
                int depth = 1, j = i + 7;
                while (j < tblBody.length()) {
                    if (tblBody.startsWith("<hp:tbl", j) && (j + 7 < tblBody.length()) && (tblBody.charAt(j + 7) == ' ' || tblBody.charAt(j + 7) == '>')) { depth++; j += 7; }
                    else if (tblBody.startsWith("</hp:tbl>", j)) { depth--; j += 9; if (depth == 0) break; }
                    else j++;
                }
                out.append(tblBody, i, j);
                i = j;
            } else if (nest == 0 && tblBody.startsWith("<hp:tr>", i)) {
                int depth = 1, j = i + 7;
                while (j < tblBody.length()) {
                    if (tblBody.startsWith("<hp:tr>", j)) { depth++; j += 7; }
                    else if (tblBody.startsWith("</hp:tr>", j)) { depth--; j += 8; if (depth == 0) break; }
                    else j++;
                }
                String row = tblBody.substring(i, j);
                final int rowIdx = idx;
                row = row.replaceAll("rowAddr=\"\\d+\"", "rowAddr=\"" + rowIdx + "\"");
                out.append(row);
                idx++;
                i = j;
            } else {
                out.append(tblBody.charAt(i));
                i++;
            }
        }
        return out.toString();
    }

    static String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    static String escapeXmlMultiline(String s) {
        if (s == null) return "";
        String esc = escapeXml(s.replace("\r\n", "\n").replace("\r", "\n"));
        return esc.replace("\n", NL_SENTINEL);
    }

    /**
     * sentinel 이 들어 있는 <hp:p>...</hp:p> 블록을 찾아 줄 단위로 paragraph 를 복제한다.
     * 각 줄은 원본 paragraph 의 속성/스타일을 그대로 유지한다.
     */
    static String expandMultilineParagraphs(String xml) {
        while (true) {
            int idx = xml.indexOf(NL_SENTINEL);
            if (idx < 0) break;
            int ps = xml.lastIndexOf("<hp:p ", idx);
            if (ps < 0) { xml = xml.replace(NL_SENTINEL, " "); break; }
            int peEnd = xml.indexOf("</hp:p>", idx);
            if (peEnd < 0) { xml = xml.replace(NL_SENTINEL, " "); break; }
            peEnd += "</hp:p>".length();
            String para = xml.substring(ps, peEnd);
            // text 노드 추출
            int ts = para.indexOf("<hp:t>");
            int te = para.indexOf("</hp:t>", ts);
            if (ts < 0 || te < 0) { xml = xml.replace(NL_SENTINEL, " "); break; }
            String fullText = para.substring(ts + "<hp:t>".length(), te);
            String[] lines = fullText.split(java.util.regex.Pattern.quote(NL_SENTINEL), -1);
            StringBuilder rebuilt = new StringBuilder();
            for (String line : lines) {
                String clone = para.substring(0, ts + "<hp:t>".length()) + line + para.substring(te);
                rebuilt.append(clone);
            }
            xml = xml.substring(0, ps) + rebuilt.toString() + xml.substring(peEnd);
        }
        return xml;
    }

}
