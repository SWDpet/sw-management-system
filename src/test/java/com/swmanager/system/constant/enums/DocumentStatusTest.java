package com.swmanager.system.constant.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * DocumentStatus Enum 단위 테스트.
 *
 * 기획서/개발계획서 매핑:
 *  - NFR-3: DB 저장 문자열("COMPLETED") ↔ Enum 라운드트립
 *  - NFR-9 ⑤ (서비스): @JsonCreator fromString 정상/실패
 *  - FR-A1: label()/color() 메서드 계약
 *  - §5-7-2: ALIASES 맵은 빈 상태 (v6 기준)
 */
class DocumentStatusTest {

    @Test
    void valueOf_roundTrip_matchesDbStringValues() {
        // Pre-flight 결과 DB에 저장되는 값 (2026-04-20): DRAFT(3), COMPLETED(12)
        assertThat(DocumentStatus.valueOf("DRAFT")).isEqualTo(DocumentStatus.DRAFT);
        assertThat(DocumentStatus.valueOf("COMPLETED")).isEqualTo(DocumentStatus.COMPLETED);
        // 역방향: Enum.name() 은 항상 STRING 저장 포맷
        assertThat(DocumentStatus.DRAFT.name()).isEqualTo("DRAFT");
        assertThat(DocumentStatus.COMPLETED.name()).isEqualTo("COMPLETED");
    }

    @Test
    void label_returnsKoreanDisplayString() {
        assertThat(DocumentStatus.DRAFT.label()).isEqualTo("작성중");
        assertThat(DocumentStatus.COMPLETED.label()).isEqualTo("작성완료");
    }

    @Test
    void color_returnsBadgeHexColor() {
        assertThat(DocumentStatus.DRAFT.color()).isEqualTo("#858796");
        assertThat(DocumentStatus.COMPLETED.color()).isEqualTo("#1cc88a");
    }

    @Test
    void fromString_nullOrBlank_returnsNull() {
        assertThat(DocumentStatus.fromString(null)).isNull();
        assertThat(DocumentStatus.fromString("")).isNull();
        assertThat(DocumentStatus.fromString("   ")).isNull();
    }

    @Test
    void fromString_trimsAndUppercases() {
        assertThat(DocumentStatus.fromString("draft")).isEqualTo(DocumentStatus.DRAFT);
        assertThat(DocumentStatus.fromString(" Completed ")).isEqualTo(DocumentStatus.COMPLETED);
        assertThat(DocumentStatus.fromString("COMPLETED")).isEqualTo(DocumentStatus.COMPLETED);
    }

    @Test
    void fromString_invalidValue_throwsEnumValueNotAllowed() {
        // §5-7 실패 응답의 근거: IllegalArgumentException → GlobalExceptionHandler 400
        assertThatThrownBy(() -> DocumentStatus.fromString("???"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ENUM_VALUE_NOT_ALLOWED");
        assertThatThrownBy(() -> DocumentStatus.fromString("완료"))  // 레거시 한글값은 데이터 정제 대상 (허용 X)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ENUM_VALUE_NOT_ALLOWED");
    }
}
