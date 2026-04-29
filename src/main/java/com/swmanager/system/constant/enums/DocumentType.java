package com.swmanager.system.constant.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collections;
import java.util.Map;

/**
 * 문서 유형 Enum — tb_document.doc_type 매핑.
 *
 * 기획서: docs/product-specs/hardcoding-improvement.md (v6)
 * 개발계획서 §0-2에 따라 명명: `WorkPlanType` → `DocumentType`으로 변경
 *   (Document.docType 필드에 대응, WorkPlan.planType과 의미 충돌 방지).
 *
 * 매핑 정책 (§5-6-1): @Enumerated(EnumType.STRING) 기본 선택.
 *   Pre-flight 결과 공백·별칭 이슈 0건.
 *
 * 값 세트: DocumentDTO.getDocTypeLabel / DocumentController 라우팅 실측 기반.
 */
public enum DocumentType {
    // doc-split-ops: 사업문서 3 종만 tb_document.doc_type 으로 사용.
    // 운영문서 5 종 (INSPECT/FAULT/SUPPORT/INSTALL/PATCH) 은 OpsDocType + tb_ops_doc 로 이관.
    COMMENCE("착수계", "doc-commence", "pdf-commence"),
    INTERIM("기성계", "doc-interim", "pdf-interim"),
    COMPLETION("준공계", "doc-completion", "pdf-completion");

    private final String label;
    private final String templateName;
    private final String pdfTemplateName;

    /** 별칭 단일 소스 (JSON body 전용). v6 기준 별칭 없음. */
    private static final Map<String, DocumentType> ALIASES = Collections.emptyMap();

    DocumentType(String label, String templateName, String pdfTemplateName) {
        this.label = label;
        this.templateName = templateName;
        this.pdfTemplateName = pdfTemplateName;
    }

    public String label() { return label; }

    /** 웹 문서 작성 템플릿 (예: document/doc-inspect) */
    public String templateName() { return "document/" + templateName; }

    /** PDF 출력 템플릿 (예: pdf/pdf-inspect) */
    public String pdfTemplateName() { return "pdf/" + pdfTemplateName; }

    @JsonCreator
    public static DocumentType fromString(String v) {
        if (v == null || v.isBlank()) return null;
        String norm = v.trim().toUpperCase();
        try {
            return DocumentType.valueOf(norm);
        } catch (IllegalArgumentException ignore) {
            DocumentType aliased = ALIASES.get(v.trim());
            if (aliased != null) return aliased;
            throw new IllegalArgumentException("ENUM_VALUE_NOT_ALLOWED: " + v);
        }
    }
}
