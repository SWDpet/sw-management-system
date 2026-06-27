package com.swmanager.system.controller;

import com.swmanager.system.domain.SwProject;
import com.swmanager.system.domain.User;
import com.swmanager.system.repository.PjtManpowerPlanRepository;
import com.swmanager.system.repository.PjtScheduleRepository;
import com.swmanager.system.repository.PjtTargetRepository;
import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.response.ApiResult;
import com.swmanager.system.security.CustomUserDetails;
import com.swmanager.system.security.DocumentAccessSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * DocumentPlanController 단위 테스트 (S4 Phase 3 — DocumentControllerTest 에서 이관 + 성공경로 신규).
 *
 * <p>사업수행계획서 분리(refactor-document-controller-split-phase3). 권한은 실제 DocumentAccessSupport.
 * 성공 저장은 sw_pjt 갱신 + targets/manpowerPlans/schedules delete&insert verify.
 * ⚠savePlanData 의 rollback 예외경로는 @Transactional 직접호출 불가(TransactionAspectSupport) → 미검증(MockMvc 백로그).
 */
class DocumentPlanControllerTest {

    private DocumentPlanController controller;
    private SwProjectRepository swProjectRepository;
    private PjtTargetRepository pjtTargetRepository;
    private PjtManpowerPlanRepository pjtManpowerPlanRepository;
    private PjtScheduleRepository pjtScheduleRepository;

    @BeforeEach
    void setUp() throws Exception {
        controller = new DocumentPlanController();
        swProjectRepository = mock(SwProjectRepository.class);
        pjtTargetRepository = mock(PjtTargetRepository.class);
        pjtManpowerPlanRepository = mock(PjtManpowerPlanRepository.class);
        pjtScheduleRepository = mock(PjtScheduleRepository.class);

        inject("swProjectRepository", swProjectRepository);
        inject("pjtTargetRepository", pjtTargetRepository);
        inject("pjtManpowerPlanRepository", pjtManpowerPlanRepository);
        inject("pjtScheduleRepository", pjtScheduleRepository);
        inject("access", new DocumentAccessSupport());

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() { SecurityContextHolder.clearContext(); }

    private void inject(String field, Object value) throws Exception {
        Field f = DocumentPlanController.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(controller, value);
    }

    private void login(String authDocument, String role) {
        User u = new User();
        u.setUserSeq(1L);
        u.setUserid("tester");
        u.setUsername("테스터");
        u.setUserRole(role);
        u.setAuthDocument(authDocument);
        CustomUserDetails cud = new CustomUserDetails(u);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(cud, "N/A", cud.getAuthorities()));
    }

    private void loginEdit() { login("EDIT", "ROLE_USER"); }
    private void loginView() { login("VIEW", "ROLE_USER"); }
    private void loginNone() { login("NONE", "ROLE_USER"); }
    private void loginAdmin() { login("NONE", "ROLE_ADMIN"); }

    @Test
    void getPlanData_none_forbidden() { // [harden-read-api] 무권한 계획서 조회 차단
        loginNone();
        assertThat(controller.getPlanData(3L).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(swProjectRepository, pjtTargetRepository, pjtManpowerPlanRepository, pjtScheduleRepository);
    }

    @Test
    void getPlanData_admin_ok() { // hasDocRead: admin→getAuth EDIT→통과
        loginAdmin();
        SwProject p = new SwProject();
        p.setProjId(3L);
        when(swProjectRepository.findById(3L)).thenReturn(Optional.of(p));
        when(pjtTargetRepository.findByProjIdOrderBySortOrderAsc(any())).thenReturn(List.of());
        when(pjtManpowerPlanRepository.findByProjIdOrderBySortOrderAsc(any())).thenReturn(List.of());
        when(pjtScheduleRepository.findByProjIdOrderBySortOrderAsc(any())).thenReturn(List.of());
        assertThat(controller.getPlanData(3L).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getPlanData_notFound() {
        loginView();
        when(swProjectRepository.findById(9L)).thenReturn(Optional.empty());
        assertThat(controller.getPlanData(9L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getPlanData_found() {
        loginView();
        SwProject p = new SwProject();
        p.setProjId(3L);
        when(swProjectRepository.findById(3L)).thenReturn(Optional.of(p));
        when(pjtTargetRepository.findByProjIdOrderBySortOrderAsc(any())).thenReturn(List.of());
        when(pjtManpowerPlanRepository.findByProjIdOrderBySortOrderAsc(any())).thenReturn(List.of());
        when(pjtScheduleRepository.findByProjIdOrderBySortOrderAsc(any())).thenReturn(List.of());
        assertThat(controller.getPlanData(3L).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void savePlanData_nonEdit_forbidden() {
        loginView();
        ResponseEntity<?> res = controller.savePlanData(3L, Map.of());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(swProjectRepository);
    }

    @Test
    void savePlanData_edit_success_deletesAndInserts() { // [codex] 성공 저장 — delete&insert 검증
        loginEdit();
        SwProject p = new SwProject();
        p.setProjId(3L);
        when(swProjectRepository.findById(3L)).thenReturn(Optional.of(p));

        Map<String, Object> body = Map.of(
                "projPurpose", "유지보수 목적",
                "targets", List.of(Map.of("productName", "제품A", "qty", 2)),
                "manpowerPlans", List.of(Map.of("stepName", "분석", "gradeHigh", 1)),
                "schedules", List.of(Map.of("processName", "설계", "m01", true)));

        ResponseEntity<ApiResult> res = controller.savePlanData(3L, body);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).isNotNull();
        assertThat(res.getBody().success()).isTrue();
        // sw_pjt 4컬럼 저장 + 각 pjt* delete & insert
        verify(swProjectRepository).save(p);
        verify(pjtTargetRepository).deleteByProjId(3L);
        verify(pjtTargetRepository).save(any());
        verify(pjtManpowerPlanRepository).deleteByProjId(3L);
        verify(pjtManpowerPlanRepository).save(any());
        verify(pjtScheduleRepository).deleteByProjId(3L);
        verify(pjtScheduleRepository).save(any());
    }
}
