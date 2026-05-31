package com.swmanager.system.service.inspection;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 유지보수유형(maint_type) → 점검 범위(프로파일) 매핑 — 단일 소스(SSOT).
 *
 * <p>리포트·작성UI·QR적재가 공용으로 사용한다. 기획서: {@code docs/product-specs/inspect-maint-profile.md}.
 *
 * <p><b>도출 규칙(§3-3)</b>
 * <pre>
 *   HW(AP/DB)·DBMS 점검  ⟺  maint_type ∈ {HS, DHS}
 *   표준시스템(APP) 점검 ⟺  maint_type ∈ {SU, HS, DHS}  AND  시스템 표준보유
 *   GIS엔진 점검         ⟺  항상
 *   maint_type 미지정(null) → 전체(보수적 폴백)
 * </pre>
 *
 * <p><b>코드 체계</b> — 기존 운영 코드 재사용(2026-05-31 확정). DB {@code maint_tp_mst} 에 이미 존재:
 * {@code SW}(GIS만) · {@code DS}(GIS만) · {@code HS}(전체) · {@code DHS}(전체).
 * 신규는 {@code SU}(SW/표준, GIS+표준) 하나뿐. 기획서 초안의 {@code S} 코드는 {@code SW} 로 흡수.
 *
 * <p><b>표준보유</b> 는 {@code inspect_template} 에 해당 {@code template_type} 의 {@code section='APP'}
 * 행이 존재하는지로 자동 판단한다(UPIS=보유, KRAS=미보유). 별도 마스터 플래그를 두지 않는다.
 */
public final class InspectMaintProfile {

    private InspectMaintProfile() {}

    /** 점검 섹션 코드 — inspect_check_result.section / inspect_template.section 과 동일. */
    public static final String AP = "AP", DB = "DB", DBMS = "DBMS", GIS = "GIS", APP = "APP";

    private static String norm(String maintType) {
        return maintType == null ? "" : maintType.trim().toUpperCase();
    }

    /** 전체(운영장비·DBMS 포함) 프로파일 여부. null(미지정)은 보수적으로 전체. */
    private static boolean isFull(String mt) {
        return mt.isEmpty() || mt.equals("HS") || mt.equals("DHS");
    }

    /** 표준시스템 점검 대상 유형 여부(보유 판단은 별도). null은 보수적으로 포함. */
    private static boolean wantsStandard(String mt) {
        return isFull(mt) || mt.equals("SU");
    }

    /**
     * maint_type + 표준보유 여부 → 기대 섹션 집합(렌더 순서 보존).
     *
     * @param maintType   사업 유지보수유형 (SW/SU/DS/HS/DHS/…; null 허용 → 전체 폴백)
     * @param hasStandard 해당 시스템(template_type)이 표준시스템(APP) 템플릿 보유 여부
     */
    public static Set<String> sections(String maintType, boolean hasStandard) {
        String mt = norm(maintType);
        Set<String> s = new LinkedHashSet<>();
        if (isFull(mt)) { s.add(AP); s.add(DB); s.add(DBMS); }
        s.add(GIS); // 항상
        if (wantsStandard(mt) && hasStandard) s.add(APP);
        return s;
    }

    /** 특정 섹션이 점검 범위 내인지. */
    public static boolean includes(String maintType, boolean hasStandard, String section) {
        return sections(maintType, hasStandard).contains(section);
    }

    /** 표지 유형 배지 라벨. */
    public static String badgeLabel(String maintType) {
        return switch (norm(maintType)) {
            case "SW"  -> "SW · GIS엔진";
            case "SU"  -> "SW / 표준";
            case "DS"  -> "DB / SW";
            case "HS"  -> "HW / SW";
            case "DHS" -> "DB / HW / SW";
            case ""    -> "전체";
            default     -> maintType;
        };
    }

    /** 배지 색 토큰(템플릿 CSS 클래스 접미사): teal=GIS만 · amber=GIS+표준 · navy=전체. */
    public static String badgeTone(String maintType) {
        return switch (norm(maintType)) {
            case "SW", "DS" -> "teal";
            case "SU"        -> "amber";
            default           -> "navy"; // HS·DHS·미지정
        };
    }

    /** 요약 대시보드 점검 범위 칩 문구. */
    public static String scopeChip(String maintType, boolean hasStandard) {
        Set<String> s = sections(maintType, hasStandard);
        List<String> parts = new ArrayList<>();
        if (s.contains(AP) || s.contains(DB)) parts.add("운영장비(AP/DB)");
        if (s.contains(DBMS)) parts.add("DBMS");
        if (s.contains(GIS))  parts.add("GIS엔진");
        if (s.contains(APP))  parts.add("표준시스템");
        return "점검 범위: " + String.join(" + ", parts);
    }
}
