package com.swmanager.system.service;

import com.swmanager.system.constant.enums.DocumentStatus;
import com.swmanager.system.constant.enums.DocumentType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 서비스 계층 관점의 Enum 바인딩 보장 테스트.
 * (NFR-9 시나리오 ⑤ 서비스 계층 — 개발계획서 v2 추가)
 *
 * 실제 컨트롤러/Jackson 없이, Service가 Enum을 사용할 때의 보장:
 * - DTO에서 넘어온 값이 Enum으로 정상 변환 (정상 → 실제 Enum)
 * - 잘못된 입력(fromString)에서 ENUM_VALUE_NOT_ALLOWED 던지기
 * - Spring ConverterFactory가 trim+toUpperCase 수행하는 것과 별개로, 서비스 내부에서 입력값 정제 호출이 안전함을 검증
 */
class DocumentStatusBindingServiceTest {

    @Test
    void documentStatus_validValues_bindSuccessfully() {
        assertThat(DocumentStatus.fromString("DRAFT")).isEqualTo(DocumentStatus.DRAFT);
        assertThat(DocumentStatus.fromString("COMPLETED")).isEqualTo(DocumentStatus.COMPLETED);
        // 대소문자/공백 정제
        assertThat(DocumentStatus.fromString("  draft  ")).isEqualTo(DocumentStatus.DRAFT);
    }

    @Test
    void documentStatus_invalidValue_throwsForServiceLayerToPropagate() {
        // 서비스에서 이 예외가 그대로 전파되어 GlobalExceptionHandler 400 응답을 유발
        assertThatThrownBy(() -> DocumentStatus.fromString("???"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ENUM_VALUE_NOT_ALLOWED: ???");
    }

    @Test
    void documentType_validValues_bindSuccessfully() {
        // doc-split-ops: 사업문서 3 종만 DocumentType 으로 인식.
        assertThat(DocumentType.fromString("COMMENCE")).isEqualTo(DocumentType.COMMENCE);
        assertThat(DocumentType.fromString("INTERIM")).isEqualTo(DocumentType.INTERIM);
        assertThat(DocumentType.fromString("  completion  ")).isEqualTo(DocumentType.COMPLETION);
    }

    @Test
    void documentType_invalidValue_throws() {
        assertThatThrownBy(() -> DocumentType.fromString("WORKPLAN_PLAN_TYPE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ENUM_VALUE_NOT_ALLOWED");
        // 운영문서 5 종 모두 — DocumentType 에서는 거부 (OpsDocType 으로 이관됨).
        for (String opsType : new String[]{"INSPECT", "FAULT", "SUPPORT", "INSTALL", "PATCH"}) {
            assertThatThrownBy(() -> DocumentType.fromString(opsType))
                    .as("운영문서 종류 %s 은 DocumentType 에서 거부되어야 함", opsType)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ENUM_VALUE_NOT_ALLOWED");
        }
    }

    @Test
    void fromString_null_returnsNullForOptionalFields() {
        // DTO의 선택적 필드가 null로 넘어올 때 예외 없이 null 반환 (NPE 방지)
        assertThat(DocumentStatus.fromString(null)).isNull();
        assertThat(DocumentType.fromString(null)).isNull();
    }
}
