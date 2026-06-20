package com.swmanager.system.dto.orgunit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OrgUnitForm / StaffForm 요청 바디 바인딩 골든 테스트 (orgunit-request-dto §6-4).
 *
 * 기존 비타입 Map 수신을 타입 DTO 로 바꾸면서, 프론트(org-unit-management.html)가 보내던
 * JSON 키(snake_case·숫자형)를 무손실 바인딩하는지 고정한다.
 */
class OrgUnitFormBindingTest {

    private final ObjectMapper om = new ObjectMapper();

    @Test
    void orgUnitForm_create_bindsSnakeCaseAndNumbers() throws Exception {
        // org-unit-management.html:313 생성 본문
        String json = "{\"parent_id\":7,\"unit_type\":\"DEPT\",\"name\":\"개발부\",\"sort_order\":3}";

        OrgUnitForm f = om.readValue(json, OrgUnitForm.class);

        assertThat(f.parentId()).isEqualTo(7L);       // parent_id → Long
        assertThat(f.unitType()).isEqualTo("DEPT");
        assertThat(f.name()).isEqualTo("개발부");
        assertThat(f.sortOrder()).isEqualTo(3);        // sort_order → Integer
        assertThat(f.useYn()).isNull();
    }

    @Test
    void orgUnitForm_update_partialKeys() throws Exception {
        // org-unit-management.html:280 수정 본문 (parent_id/unit_type 생략)
        OrgUnitForm f = om.readValue("{\"name\":\"기획팀\",\"sort_order\":1,\"use_yn\":\"N\"}", OrgUnitForm.class);
        assertThat(f.name()).isEqualTo("기획팀");
        assertThat(f.sortOrder()).isEqualTo(1);
        assertThat(f.useYn()).isEqualTo("N");
        assertThat(f.parentId()).isNull();
    }

    @Test
    void staffForm_create_bindsOrgUnitIdAndActive() throws Exception {
        // org-unit-management.html:250 생성 본문
        String json = "{\"name\":\"홍길동\",\"position\":\"대리\",\"org_unit_id\":12,\"active\":true}";

        StaffForm f = om.readValue(json, StaffForm.class);

        assertThat(f.name()).isEqualTo("홍길동");
        assertThat(f.position()).isEqualTo("대리");
        assertThat(f.orgUnitId()).isEqualTo(12L);      // org_unit_id → Long
        assertThat(f.active()).isTrue();
        assertThat(f.tel()).isNull();
    }

    @Test
    void staffForm_update_omitsOrgUnitId_bindsNull() throws Exception {
        // org-unit-management.html:256 수정 본문 — org_unit_id/tel 미전송 → null
        // applyStaff 가 orgUnitId!=null 일 때만 set 하므로 기존 유닛 보존(containsKey 등가)
        StaffForm f = om.readValue("{\"name\":\"김철수\",\"position\":\"과장\",\"active\":false}", StaffForm.class);
        assertThat(f.name()).isEqualTo("김철수");
        assertThat(f.active()).isFalse();
        assertThat(f.orgUnitId()).isNull();
        assertThat(f.tel()).isNull();
    }

    @Test
    void unknownKeys_ignored() throws Exception {
        OrgUnitForm f = om.readValue("{\"name\":\"x\",\"bogus\":1}", OrgUnitForm.class);
        assertThat(f.name()).isEqualTo("x");
    }
}
