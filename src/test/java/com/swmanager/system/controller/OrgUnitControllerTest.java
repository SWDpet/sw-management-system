package com.swmanager.system.controller;

import com.swmanager.system.domain.ops.Staff;
import com.swmanager.system.dto.orgunit.OrgUnitForm;
import com.swmanager.system.dto.orgunit.StaffForm;
import com.swmanager.system.repository.ops.StaffRepository;
import com.swmanager.system.service.OrgUnitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ExtendedModelMap;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OrgUnitController(조직도 조회 + 관리) 단위 테스트 (beyond-A 커버리지 스프린트 — 컨트롤러 통합영역 13탄).
 *
 * <p>OrgUnitController 는 필드 주입(@Autowired 2)이고 접근통제는 SecurityConfig 레벨(/admin/** ADMIN,
 * /api/org-units/** 인증)이라 컨트롤러 자체 가드는 없다. mock 2종을 reflection 으로 필드 주입하고 메서드를
 * 직접 호출한다. 실 Postgres 불필요 → 기본 CI 에서 JaCoCo floor 반영.
 */
class OrgUnitControllerTest {

    private OrgUnitController controller;
    private OrgUnitService orgUnitService;
    private StaffRepository staffRepository;

    @BeforeEach
    void setUp() throws Exception {
        controller = new OrgUnitController();
        orgUnitService = mock(OrgUnitService.class);
        staffRepository = mock(StaffRepository.class);
        inject("orgUnitService", orgUnitService);
        inject("staffRepository", staffRepository);
    }

    private void inject(String field, Object value) throws Exception {
        Field f = OrgUnitController.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(controller, value);
    }

    private static OrgUnitForm unitForm(String name, Integer sortOrder) {
        return new OrgUnitForm(1L, "TEAM", name, sortOrder, "Y");
    }
    private static StaffForm staffForm(String name) {
        return new StaffForm(name, "과장", 3L, true, "010-1111-2222");
    }

    // ───────────────────────── 조회 API ─────────────────────────

    @Test
    void getTree_delegates() {
        when(orgUnitService.getTree()).thenReturn(List.of());
        assertThat(controller.getTree()).isEmpty();
    }

    @Test
    void getRoots_delegates() {
        when(orgUnitService.getRoots()).thenReturn(List.of());
        assertThat(controller.getRoots()).isEmpty();
    }

    @Test
    void getChildren_delegates() {
        when(orgUnitService.getChildren(1L)).thenReturn(List.of());
        assertThat(controller.getChildren(1L)).isEmpty();
    }

    @Test
    void getMembers_delegates() {
        when(orgUnitService.getMembers(1L)).thenReturn(List.of());
        assertThat(controller.getMembers(1L)).isEmpty();
    }

    @Test
    void manage_renders() {
        assertThat(controller.manage(new ExtendedModelMap())).isEqualTo("admin/org-unit-management");
    }

    // ───────────────────────── 유닛 CRUD ─────────────────────────

    @Test
    void create_ok() {
        ResponseEntity<?> res = controller.create(unitForm("개발팀", 5));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(orgUnitService).create(1L, "TEAM", "개발팀", 5);
    }

    @Test
    void create_nullSortOrder_defaultsZero() {
        assertThat(controller.create(unitForm("개발팀", null)).getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(orgUnitService).create(any(), any(), any(), eq(0)); // 기본 0
    }

    @Test
    void create_invalid_badRequest() {
        when(orgUnitService.create(any(), any(), any(), anyInt()))
                .thenThrow(new IllegalArgumentException("부모 없음"));
        assertThat(controller.create(unitForm("X", 0)).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void update_ok() {
        assertThat(controller.update(1L, unitForm("수정팀", 1)).getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(orgUnitService).update(1L, "수정팀", 1, "Y");
    }

    @Test
    void update_invalid_badRequest() {
        when(orgUnitService.update(anyLong(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("없음"));
        assertThat(controller.update(1L, unitForm("X", 0)).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void delete_ok() {
        assertThat(controller.delete(1L).getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(orgUnitService).delete(1L);
    }

    @Test
    void delete_notFound_badRequest() {
        doThrow(new IllegalArgumentException("없음")).when(orgUnitService).delete(1L);
        assertThat(controller.delete(1L).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void delete_hasChildren_badRequest() {
        doThrow(new IllegalStateException("하위 있음")).when(orgUnitService).delete(1L);
        assertThat(controller.delete(1L).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ───────────────────────── 직원 CRUD ─────────────────────────

    @Test
    void createStaff_blankName_badRequest() {
        assertThat(controller.createStaff(staffForm("  ")).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(staffRepository, never()).save(any());
    }

    @Test
    void createStaff_ok() {
        Staff saved = new Staff();
        saved.setStaffId(9L);
        when(staffRepository.save(any())).thenReturn(saved);
        ResponseEntity<?> res = controller.createStaff(staffForm("박직원"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        // 저장된 staffId 가 응답에 전파됨
        var body = (com.swmanager.system.dto.orgunit.StaffCreateResult) res.getBody();
        assertThat(body.staffId()).isEqualTo(9L);
        verify(staffRepository).save(any());
    }

    @Test
    void updateStaff_notFound_404() {
        when(staffRepository.findById(1L)).thenReturn(Optional.empty());
        assertThat(controller.updateStaff(1L, staffForm("X")).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(staffRepository, never()).save(any());
    }

    @Test
    void updateStaff_ok() {
        when(staffRepository.findById(1L)).thenReturn(Optional.of(new Staff()));
        assertThat(controller.updateStaff(1L, staffForm("수정직원")).getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(staffRepository).save(any());
    }

    @Test
    void deleteStaff_ok() {
        assertThat(controller.deleteStaff(1L).getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(staffRepository).deleteById(1L);
    }

    @Test
    void deleteStaff_inUse_badRequest() {
        doThrow(new RuntimeException("FK")).when(staffRepository).deleteById(1L);
        assertThat(controller.deleteStaff(1L).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
