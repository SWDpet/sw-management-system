package com.swmanager.system.service.inspection;

import org.junit.jupiter.api.Test;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * maint_type 점검범위 분기 — 매핑 로직(§3-3) + 신규 Thymeleaf 표현식 렌더 검증.
 * DB/Spring 컨텍스트 없이 SpringTemplateEngine 으로 표현식만 평가한다.
 */
class InspectMaintProfileTest {

    // ── 1. 도출 규칙 (§3-3) ────────────────────────────────────────
    @Test
    void sections_upis_hasStandard() {
        assertEquals("[GIS]",                       InspectMaintProfile.sections("SW",  true).toString());
        assertEquals("[GIS, APP]",                  InspectMaintProfile.sections("SU",  true).toString());
        assertEquals("[GIS]",                       InspectMaintProfile.sections("DS",  true).toString());
        assertEquals("[AP, DB, DBMS, GIS, APP]",    InspectMaintProfile.sections("HS",  true).toString());
        assertEquals("[AP, DB, DBMS, GIS, APP]",    InspectMaintProfile.sections("DHS", true).toString());
        assertEquals("[AP, DB, DBMS, GIS, APP]",    InspectMaintProfile.sections(null,  true).toString(), "미지정→전체 폴백");
    }

    @Test
    void sections_kras_noStandard() {
        assertEquals("[GIS]",                  InspectMaintProfile.sections("SW",  false).toString());
        assertEquals("[GIS]",                  InspectMaintProfile.sections("SU",  false).toString(), "표준 미보유→APP 제외");
        assertEquals("[AP, DB, DBMS, GIS]",    InspectMaintProfile.sections("HS",  false).toString(), "표준 미보유→APP 제외");
        assertEquals("[AP, DB, DBMS, GIS]",    InspectMaintProfile.sections("DHS", false).toString());
    }

    @Test
    void badgeAndTone() {
        assertEquals("SW · GIS엔진", InspectMaintProfile.badgeLabel("SW"));
        assertEquals("SW / 표준",    InspectMaintProfile.badgeLabel("SU"));
        assertEquals("DB / HW / SW", InspectMaintProfile.badgeLabel("DHS"));
        assertEquals("전체",          InspectMaintProfile.badgeLabel(null));
        assertEquals("teal",  InspectMaintProfile.badgeTone("SW"));
        assertEquals("amber", InspectMaintProfile.badgeTone("SU"));
        assertEquals("navy",  InspectMaintProfile.badgeTone("HS"));
    }

    @Test
    void scopeChip() {
        assertEquals("점검 범위: GIS엔진", InspectMaintProfile.scopeChip("SW", true));
        assertEquals("점검 범위: GIS엔진 + 표준시스템", InspectMaintProfile.scopeChip("SU", true));
        assertEquals("점검 범위: 운영장비(AP/DB) + DBMS + GIS엔진 + 표준시스템",
                InspectMaintProfile.scopeChip("DHS", true));
    }

    // ── 2. 신규 Thymeleaf 표현식 렌더 (parse + 평가 오류 회귀 방지) ──
    private static SpringTemplateEngine engine() {
        StringTemplateResolver r = new StringTemplateResolver();
        r.setTemplateMode(TemplateMode.HTML);
        SpringTemplateEngine e = new SpringTemplateEngine();
        e.setTemplateResolver(r);
        return e;
    }

    /** pdf-inspect-report-v2.html 표지 배지 + 요약 칩. */
    @Test
    void renderPdfBadgeAndChip() {
        Context ctx = new Context();
        ctx.setVariable("maintTone", InspectMaintProfile.badgeTone("SU"));
        ctx.setVariable("maintBadge", InspectMaintProfile.badgeLabel("SU"));
        ctx.setVariable("scopeChip", InspectMaintProfile.scopeChip("SU", true));
        String html = engine().process(
                "<span class=\"type-badge\" th:classappend=\"${maintTone}\" th:text=\"${maintBadge}\">x</span>" +
                "<div th:if=\"${scopeChip != null}\" th:text=\"${scopeChip}\">y</div>", ctx);
        assertTrue(html.contains("class=\"type-badge amber\""), html);
        assertTrue(html.contains("SW / 표준"), html);
        assertTrue(html.contains("점검 범위: GIS엔진 + 표준시스템"), html);
    }

    /** ops-doc/list.html 의 유형 배지 — class="mtag" + th:classappend(tone). */
    @Test
    void renderOpsListBadgeClass() {
        String tmpl =
            "<span th:if=\"${maintLabelByDoc.get(d.docId) != null}\" class=\"mtag\" " +
            "th:classappend=\"${maintToneByDoc.get(d.docId)}\" th:text=\"${maintLabelByDoc.get(d.docId)}\">x</span>";

        assertTrue(renderOpsRow(tmpl, "teal").contains("class=\"mtag teal\""));
        assertTrue(renderOpsRow(tmpl, "amber").contains("class=\"mtag amber\""));
        assertTrue(renderOpsRow(tmpl, "navy").contains("class=\"mtag navy\""));
        assertTrue(renderOpsRow(tmpl, "teal").contains("SW · GIS엔진"));
    }

    private String renderOpsRow(String tmpl, String tone) {
        Context ctx = new Context();
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("docId", 7L);
        ctx.setVariable("d", d);
        Map<Long, String> labels = new LinkedHashMap<>();
        Map<Long, String> tones = new LinkedHashMap<>();
        labels.put(7L, InspectMaintProfile.badgeLabel("SW"));
        tones.put(7L, tone);
        ctx.setVariable("maintLabelByDoc", labels);
        ctx.setVariable("maintToneByDoc", tones);
        return engine().process(tmpl, ctx);
    }
}
