package com.swmanager.system.constant.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * DocumentType Enum 단위 테스트.
 *
 * doc-split-ops (2026-04-29) 이후: 사업문서 3 종 (COMMENCE/INTERIM/COMPLETION) 만 존재.
 * 운영문서 5 종 (INSPECT/FAULT/SUPPORT/INSTALL/PATCH) 은 OpsDocType 으로 이관 (OpsDocTypeTest).
 */
class DocumentTypeTest {

    @Test
    void valueOf_roundTrip_matchesDbStringValues() {
        assertThat(DocumentType.valueOf("COMMENCE")).isEqualTo(DocumentType.COMMENCE);
        assertThat(DocumentType.valueOf("COMPLETION")).isEqualTo(DocumentType.COMPLETION);
        assertThat(DocumentType.valueOf("INTERIM")).isEqualTo(DocumentType.INTERIM);
    }

    @Test
    void label_matchesKoreanDisplay() {
        assertThat(DocumentType.COMMENCE.label()).isEqualTo("착수계");
        assertThat(DocumentType.INTERIM.label()).isEqualTo("기성계");
        assertThat(DocumentType.COMPLETION.label()).isEqualTo("준공계");
    }

    @Test
    void templateName_returnsDocumentPathPrefix() {
        assertThat(DocumentType.COMMENCE.templateName()).isEqualTo("document/doc-commence");
        assertThat(DocumentType.INTERIM.templateName()).isEqualTo("document/doc-interim");
        assertThat(DocumentType.COMPLETION.templateName()).isEqualTo("document/doc-completion");
    }

    @Test
    void pdfTemplateName_returnsPdfPathPrefix() {
        assertThat(DocumentType.COMMENCE.pdfTemplateName()).isEqualTo("pdf/pdf-commence");
        assertThat(DocumentType.INTERIM.pdfTemplateName()).isEqualTo("pdf/pdf-interim");
    }

    @Test
    void fromString_trimsAndUppercases() {
        assertThat(DocumentType.fromString("commence")).isEqualTo(DocumentType.COMMENCE);
        assertThat(DocumentType.fromString(" Interim ")).isEqualTo(DocumentType.INTERIM);
    }

    @Test
    void fromString_invalidValue_throwsEnumValueNotAllowed() {
        // 운영문서 종류는 더 이상 DocumentType 에서 인식되지 않음
        assertThatThrownBy(() -> DocumentType.fromString("INSPECT"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ENUM_VALUE_NOT_ALLOWED");
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
