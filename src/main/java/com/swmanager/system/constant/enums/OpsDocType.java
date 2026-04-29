package com.swmanager.system.constant.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collections;
import java.util.Map;

/**
 * 운영·유지보수 문서 유형 — tb_ops_doc.doc_type 매핑.
 *
 * 기획서: docs/product-specs/doc-split-ops.md (v3, 2026-04-29)
 *
 * 5 종 분류:
 *   - INSPECT  : 점검내역서 (코드 90% 완성, 본 데이터는 inspect_report 계열에 보존)
 *   - FAULT    : 장애처리
 *   - SUPPORT  : 업무지원
 *   - INSTALL  : 설치보고서
 *   - PATCH    : 패치내역서
 *
 * docNo prefix 컨벤션: INSP- / FLT- / SUP- / INS- / PAT-
 */
public enum OpsDocType {
    INSPECT("점검내역서", "INSP", "doc-inspect", "pdf-inspect"),
    FAULT("장애처리", "FLT", "doc-fault", "pdf-fault"),
    SUPPORT("업무지원", "SUP", "doc-support", "pdf-support"),
    INSTALL("설치보고서", "INS", "doc-install", "pdf-install"),
    PATCH("패치내역서", "PAT", "doc-patch", "pdf-patch");

    private final String label;
    private final String docNoPrefix;
    private final String templateName;
    private final String pdfTemplateName;

    private static final Map<String, OpsDocType> ALIASES = Collections.emptyMap();

    OpsDocType(String label, String docNoPrefix, String templateName, String pdfTemplateName) {
        this.label = label;
        this.docNoPrefix = docNoPrefix;
        this.templateName = templateName;
        this.pdfTemplateName = pdfTemplateName;
    }

    public String label() { return label; }
    public String docNoPrefix() { return docNoPrefix; }
    public String templateName() { return "ops-doc/" + templateName; }
    public String pdfTemplateName() { return "pdf/" + pdfTemplateName; }

    @JsonCreator
    public static OpsDocType fromString(String v) {
        if (v == null || v.isBlank()) return null;
        String norm = v.trim().toUpperCase();
        try {
            return OpsDocType.valueOf(norm);
        } catch (IllegalArgumentException ignore) {
            OpsDocType aliased = ALIASES.get(v.trim());
            if (aliased != null) return aliased;
            throw new IllegalArgumentException("ENUM_VALUE_NOT_ALLOWED: " + v);
        }
    }
}
