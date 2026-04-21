package com.swmanager.system.constant.enums;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * S9 access-log-action-and-menu-sync — AccessActionType 단위 테스트.
 */
class AccessActionTypeTest {

    // T1: 정확히 13개 상수 (FR-1 수치 게이트)
    @Test
    void enum_has_exactly_13_values() {
        assertThat(AccessActionType.values()).hasSize(13);
    }

    // T3: fromKoLabel 정확 매칭
    @Test
    void fromKoLabel_exact_match() {
        assertThat(AccessActionType.fromKoLabel("조회")).isEqualTo(AccessActionType.VIEW);
        assertThat(AccessActionType.fromKoLabel("등록")).isEqualTo(AccessActionType.CREATE);
        assertThat(AccessActionType.fromKoLabel("민감정보조회")).isEqualTo(AccessActionType.SENSITIVE_VIEW);
    }

    // fromKoLabel 동의어 (샘플)
    @Test
    void fromKoLabel_synonym() {
        assertThat(AccessActionType.fromKoLabel("목록조회")).isEqualTo(AccessActionType.VIEW);
        assertThat(AccessActionType.fromKoLabel("발급")).isEqualTo(AccessActionType.CREATE);
    }

    // fromKoLabel trim
    @Test
    void fromKoLabel_trim() {
        assertThat(AccessActionType.fromKoLabel("  조회  ")).isEqualTo(AccessActionType.VIEW);
    }

    // fromKoLabel unknown → null
    @Test
    void fromKoLabel_unknown_returns_null() {
        assertThat(AccessActionType.fromKoLabel("없는값")).isNull();
        assertThat(AccessActionType.fromKoLabel(null)).isNull();
    }

    // T6: JsonCreator label 역직렬화
    @Test
    void fromJson_by_label() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AccessActionType v = mapper.readValue("\"조회\"", AccessActionType.class);
        assertThat(v).isEqualTo(AccessActionType.VIEW);
    }

    // JsonCreator enum name 역직렬화 + trim (v1.1 보강)
    @Test
    void fromJson_by_name_with_trim() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AccessActionType v = mapper.readValue("\" VIEW \"", AccessActionType.class);
        assertThat(v).isEqualTo(AccessActionType.VIEW);
    }

    // JsonValue 직렬화 = label
    @Test
    void jsonValue_is_label() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(AccessActionType.DOWNLOAD);
        assertThat(json).isEqualTo("\"다운로드\"");
    }

    // NFR-7: label 한글 Freeze
    @Test
    void getLabel_is_frozen_korean() {
        assertThat(AccessActionType.VIEW.getLabel()).isEqualTo("조회");
        assertThat(AccessActionType.CREATE.getLabel()).isEqualTo("등록");
        assertThat(AccessActionType.UPDATE.getLabel()).isEqualTo("수정");
        assertThat(AccessActionType.DELETE.getLabel()).isEqualTo("삭제");
        assertThat(AccessActionType.DOWNLOAD.getLabel()).isEqualTo("다운로드");
        assertThat(AccessActionType.UPLOAD.getLabel()).isEqualTo("업로드");
        assertThat(AccessActionType.APPROVE.getLabel()).isEqualTo("승인");
        assertThat(AccessActionType.SIGN.getLabel()).isEqualTo("서명");
        assertThat(AccessActionType.PREVIEW.getLabel()).isEqualTo("미리보기");
        assertThat(AccessActionType.BATCH.getLabel()).isEqualTo("일괄처리");
        assertThat(AccessActionType.PATTERN_CRUD.getLabel()).isEqualTo("패턴관리");
        assertThat(AccessActionType.WAGE_CRUD.getLabel()).isEqualTo("노임관리");
        assertThat(AccessActionType.SENSITIVE_VIEW.getLabel()).isEqualTo("민감정보조회");
    }

    /**
     * 동의어 전수 검증 (기획서 §4-1 표 전체).
     * 항목이 누락·오매핑되면 즉시 실패.
     */
    @ParameterizedTest(name = "[{index}] {0} → {1}")
    @CsvSource({
            // VIEW
            "목록조회, VIEW",
            "상세조회, VIEW",
            "접근, VIEW",
            "발급폼접근, VIEW",
            "수정폼접근, VIEW",
            // CREATE
            "생성, CREATE",
            "발급, CREATE",
            "신청, CREATE",
            // UPDATE
            "정보수정, UPDATE",
            "비번변경, UPDATE",
            "상태변경, UPDATE",
            // DOWNLOAD
            "CSV다운로드, DOWNLOAD",
            // APPROVE
            "승인요청, APPROVE",
            // BATCH
            "일괄생성, BATCH",
            "집계, BATCH",
            "금액재계산, BATCH",
            "패턴복사, BATCH",
            "패턴초기화, BATCH",
            // PATTERN_CRUD
            "패턴등록, PATTERN_CRUD",
            "패턴수정, PATTERN_CRUD",
            "패턴삭제, PATTERN_CRUD",
            "비고패턴등록, PATTERN_CRUD",
            "비고패턴수정, PATTERN_CRUD",
            "비고패턴삭제, PATTERN_CRUD",
            // WAGE_CRUD
            "노임단가등록, WAGE_CRUD",
            "노임단가수정, WAGE_CRUD",
            "노임단가삭제, WAGE_CRUD"
    })
    void fromKoLabel_all_synonyms_full_coverage_parameterized(String synonym, AccessActionType expected) {
        assertThat(AccessActionType.fromKoLabel(synonym)).isEqualTo(expected);
    }
}
