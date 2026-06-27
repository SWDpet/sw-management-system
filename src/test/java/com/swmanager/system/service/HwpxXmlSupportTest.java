package com.swmanager.system.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HwpxXmlSupport 단위 테스트 (refactor-hwpx-xml-support). 순수 String 변환 7종 직접 검증
 * — 기존엔 generateHwpx golden 으로만 간접 커버되던 XML 후처리 로직.
 */
class HwpxXmlSupportTest {

    @Test
    void escapeXml_onlyAmpLtGt() {
        // & < > 만 치환, 쌍/홑따옴표는 미치환(현 구현 보존)
        assertThat(HwpxXmlSupport.escapeXml("a&b<c>d\"e'f"))
                .isEqualTo("a&amp;b&lt;c&gt;d\"e'f");
        assertThat(HwpxXmlSupport.escapeXml(null)).isEqualTo("");
    }

    @Test
    void nlSentinel_isControlDelimited_notPlainNL() {
        // 값 잠금: SOH(0x01) + "NL" + SOH, 길이 4 — 평문 "NL" 과 충돌 불가(드리프트 방지)
        assertThat(HwpxXmlSupport.NL_SENTINEL).hasSize(4);
        assertThat(HwpxXmlSupport.NL_SENTINEL.charAt(0)).isEqualTo((char) 1);
        assertThat(HwpxXmlSupport.NL_SENTINEL.charAt(3)).isEqualTo((char) 1);
        assertThat(HwpxXmlSupport.NL_SENTINEL).contains("NL");
        assertThat(HwpxXmlSupport.NL_SENTINEL).isNotEqualTo("NL");
    }

    @Test
    void escapeXmlMultiline_plainNL_notTreatedAsSentinel() {
        // 개행 없는 평문 "NL" 은 sentinel 로 바뀌지 않음(충돌 없음)
        assertThat(HwpxXmlSupport.escapeXmlMultiline("a10NLb")).isEqualTo("a10NLb");
    }

    @Test
    void escapeXmlMultiline_newlineToSentinel() {
        String r = HwpxXmlSupport.escapeXmlMultiline("line1\r\nline2\nline3");
        assertThat(r).isEqualTo("line1" + HwpxXmlSupport.NL_SENTINEL + "line2" + HwpxXmlSupport.NL_SENTINEL + "line3");
        assertThat(HwpxXmlSupport.escapeXmlMultiline(null)).isEqualTo("");
    }

    @Test
    void expandMultilineParagraphs_splitsLinesIntoParagraphs() {
        String para = "<hp:p id=\"1\"><hp:run><hp:t>line1" + HwpxXmlSupport.NL_SENTINEL + "line2</hp:t></hp:run></hp:p>";
        String r = HwpxXmlSupport.expandMultilineParagraphs(para);
        assertThat(r).doesNotContain(HwpxXmlSupport.NL_SENTINEL);
        assertThat(r).contains("<hp:t>line1</hp:t>").contains("<hp:t>line2</hp:t>");
        // 두 문단으로 복제(원본 <hp:p 속성 보존)
        assertThat(r.split("<hp:p ", -1).length - 1).isEqualTo(2);
    }

    @Test
    void expandMultilineParagraphs_noParagraphWrapper_fallbackSpace() {
        // <hp:p 없으면 sentinel 을 공백으로 대체(fallback)
        String r = HwpxXmlSupport.expandMultilineParagraphs("plain" + HwpxXmlSupport.NL_SENTINEL + "text");
        assertThat(r).isEqualTo("plain text");
    }

    @Test
    void syncTableRowCounts_updatesRowCntAndReassignsRowAddr() {
        // 실제 구조: <hp:tr> 는 bare(속성無), rowAddr 은 행 내부 셀에 위치
        String xml = "<hp:tbl rowCnt=\"0\">"
                + "<hp:tr><hp:tc rowAddr=\"7\"/></hp:tr>"
                + "<hp:tr><hp:tc rowAddr=\"9\"/></hp:tr>"
                + "</hp:tbl>";
        String r = HwpxXmlSupport.syncTableRowCounts(xml);
        assertThat(r).contains("rowCnt=\"2\"");
        assertThat(r).contains("rowAddr=\"0\"").contains("rowAddr=\"1\"");
        assertThat(r).doesNotContain("rowAddr=\"7\"").doesNotContain("rowAddr=\"9\"");
    }

    @Test
    void countTopLevelTags_countsTopLevelRows() {
        // 표 내부 최상위 <hp:tr> 카운트(bare <hp:tr>)
        String tbl = "<hp:tbl ><hp:tr></hp:tr><hp:tr></hp:tr><hp:tr></hp:tr></hp:tbl>";
        int n = HwpxXmlSupport.countTopLevelTags(tbl, "<hp:tr>", "</hp:tr>", "<hp:tbl", "</hp:tbl>");
        assertThat(n).isEqualTo(3);
    }

    @Test
    void reassignRowAddrs_sequentialFromZero() {
        // bare <hp:tr> 블록별로 내부 rowAddr 을 행 인덱스로 재배정
        String body = "<hp:tr><c rowAddr=\"5\"/></hp:tr><hp:tr><c rowAddr=\"8\"/></hp:tr><hp:tr><c rowAddr=\"2\"/></hp:tr>";
        String r = HwpxXmlSupport.reassignRowAddrs(body);
        assertThat(r).isEqualTo("<hp:tr><c rowAddr=\"0\"/></hp:tr><hp:tr><c rowAddr=\"1\"/></hp:tr><hp:tr><c rowAddr=\"2\"/></hp:tr>");
    }

}
