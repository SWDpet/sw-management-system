package com.swmanager.system.constant.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * DocumentType Enum 단위 테스트.
 *
 * 기획서/개발계획서 매핑:
 *  - NFR-3: DB 저장 문자열 ↔ Enum 라운드트립 (Pre-flight 실측: INSPECT/COMMENCE/COMPLETION/INTERIM)
 *  - FR-A1: label()/templateName()/pdfTemplateName() 계약
 *  - 개발계획서 §0-2: WorkPlanType → DocumentType 명 변경
 */
class DocumentTypeTest {

    @Test
    void valueOf_roundTrip_matchesDbStringValues() {
        // Pre-flight 결과 (2026-04-20): INSPECT(8), COMMENCE(3), COMPLETION(2), INTERIM(2)
        assertThat(DocumentType.valueOf("INSPECT")).isEqualTo(DocumentType.INSPECT);
        assertThat(DocumentType.valueOf("COMMENCE")).isEqualTo(DocumentType.COMMENCE);
        assertThat(DocumentType.valueOf("COMPLETION")).isEqualTo(DocumentType.COMPLETION);
        assertThat(DocumentType.valueOf("INTERIM")).isEqualTo(DocumentType.INTERIM);
        // 코드상 추가 타입
        assertThat(DocumentType.valueOf("FAULT")).isEqualTo(DocumentType.FAULT);
        assertThat(DocumentType.valueOf("SUPPORT")).isEqualTo(DocumentType.SUPPORT);
        assertThat(DocumentType.valueOf("INSTALL")).isEqualTo(DocumentType.INSTALL);
        assertThat(DocumentType.valueOf("PATCH")).isEqualTo(DocumentType.PATCH);
    }

    @Test
    void label_matchesKoreanDisplay() {
        assertThat(DocumentType.COMMENCE.label()).isEqualTo("착수계");
        assertThat(DocumentType.INTERIM.label()).isEqualTo("기성계");
        assertThat(DocumentType.COMPLETION.label()).isEqualTo("준공계");
        assertThat(DocumentType.INSPECT.label()).isEqualTo("점검내역서");
        assertThat(DocumentType.FAULT.label()).isEqualTo("장애처리");
        assertThat(DocumentType.SUPPORT.label()).isEqualTo("업무지원");
        assertThat(DocumentType.INSTALL.label()).isEqualTo("설치보고서");
        assertThat(DocumentType.PATCH.label()).isEqualTo("패치내역서");
    }

    @Test
    void templateName_returnsDocumentPathPrefix() {
        assertThat(DocumentType.INSPECT.templateName()).isEqualTo("document/doc-inspect");
        assertThat(DocumentType.COMMENCE.templateName()).isEqualTo("document/doc-commence");
        assertThat(DocumentType.SUPPORT.templateName()).isEqualTo("document/doc-support");
    }

    @Test
    void pdfTemplateName_returnsPdfPathPrefix() {
        assertThat(DocumentType.INSPECT.pdfTemplateName()).isEqualTo("pdf/pdf-inspect");
        assertThat(DocumentType.COMMENCE.pdfTemplateName()).isEqualTo("pdf/pdf-commence");
    }

    @Test
    void fromString_trimsAndUppercases() {
        assertThat(DocumentType.fromString("inspect")).isEqualTo(DocumentType.INSPECT);
        assertThat(DocumentType.fromString(" Commence ")).isEqualTo(DocumentType.COMMENCE);
    }

    @Test
    void fromString_invalidValue_throwsEnumValueNotAllowed() {
        assertThatThrownBy(() -> DocumentType.fromString("UNKNOWN_TYPE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ENUM_VALUE_NOT_ALLOWED");
    }

    @Test
    void fromString_nullOrBlank_returnsNull() {
        assertThat(DocumentType.fromString(null)).isNull();
        assertThat(DocumentType.fromString("")).isNull();
    }
}
