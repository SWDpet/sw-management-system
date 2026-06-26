package com.swmanager.system.controller;

import com.swmanager.system.domain.SwProject;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.ContractParticipant;
import com.swmanager.system.response.ApiResult;
import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.repository.workplan.ContractParticipantRepository;
import com.swmanager.system.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DocumentParticipantController(사업별 과업참여자 API) 단위 테스트
 * (beyond-A 커버리지 스프린트 — 컨트롤러 통합영역 12탄).
 *
 * <p>@RequiredArgsConstructor(3 의존성) + 권한은 {@link SecurityContextHolder} principal 의
 * authDocument(getAuth/isAdmin)에서 읽으므로, mock 3종 생성자주입 + SecurityContext 직접세팅 후
 * 메서드 직접호출. 민감정보 조회/저장은 EDIT 가드(403), 저장은 사업 미존재 404. 실 Postgres 불필요.
 */
class DocumentParticipantControllerTest {

    private DocumentParticipantController controller;
    private ContractParticipantRepository contractParticipantRepository;
    private SwProjectRepository swProjectRepository;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        contractParticipantRepository = mock(ContractParticipantRepository.class);
        swProjectRepository = mock(SwProjectRepository.class);
        userRepository = mock(UserRepository.class);
        controller = new DocumentParticipantController(
                contractParticipantRepository, swProjectRepository, userRepository);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() { SecurityContextHolder.clearContext(); }

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

    // ───────────────────────── 비민감 조회 (가드 없음) ─────────────────────────

    @Test
    void getParticipants_mapsRows() {
        ContractParticipant cp = new ContractParticipant();
        cp.setRoleType("LEAD"); // user 미설정 → from() 이 null-safe 처리
        when(contractParticipantRepository.findByProject_ProjIdOrderBySortOrder(1L)).thenReturn(List.of(cp));
        var rows = controller.getContractParticipants(1L);
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).roleType()).isEqualTo("LEAD");
    }

    // ───────────────────────── 민감 조회 (EDIT 가드) ─────────────────────────

    @Test
    void getSecure_nonEdit_forbidden() {
        loginView();
        ResponseEntity<?> res = controller.getContractParticipantsSecure(1L);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(contractParticipantRepository, never()).findByProject_ProjIdOrderBySortOrder(any());
    }

    @Test
    void getSecure_edit_ok() {
        loginEdit();
        when(contractParticipantRepository.findByProject_ProjIdOrderBySortOrder(1L)).thenReturn(List.of());
        ResponseEntity<?> res = controller.getContractParticipantsSecure(1L);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ───────────────────────── 저장 (EDIT 가드) ─────────────────────────

    @Test
    void save_nonEdit_forbidden() {
        loginView();
        ResponseEntity<?> res = controller.saveContractParticipants(1L, List.of());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(swProjectRepository, never()).findById(any());
    }

    @Test
    void save_projectNotFound_404() {
        loginEdit();
        when(swProjectRepository.findById(1L)).thenReturn(Optional.empty());
        ResponseEntity<?> res = controller.saveContractParticipants(1L, List.of());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void save_edit_replacesAndSaves() {
        loginEdit();
        when(swProjectRepository.findById(1L)).thenReturn(Optional.of(new SwProject()));
        when(contractParticipantRepository.findByProject_ProjIdOrderBySortOrder(1L)).thenReturn(List.of());
        when(userRepository.findById(5L)).thenReturn(Optional.empty());
        List<Map<String, Object>> body = List.of(
                Map.of("userId", "5", "roleType", "LEAD", "techGrade", "특급", "isSiteRep", true),
                Map.of("roleType", "PARTICIPANT")); // userId 누락(lenient 파싱)
        ResponseEntity<?> res = controller.saveContractParticipants(1L, body);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(contractParticipantRepository).deleteAll(any());
        ArgumentCaptor<ContractParticipant> cap = ArgumentCaptor.forClass(ContractParticipant.class);
        verify(contractParticipantRepository, times(2)).save(cap.capture());
        // 1행: userId=5 지만 findById 미존재 → user null(lenient), roleType 매핑 + sortOrder 순번
        ContractParticipant first = cap.getAllValues().get(0);
        assertThat(first.getUser()).isNull();
        assertThat(first.getRoleType()).isEqualTo("LEAD");
        assertThat(first.getSortOrder()).isEqualTo(0);
        // 2행: userId 누락 → roleType 기본 보존
        assertThat(cap.getAllValues().get(1).getRoleType()).isEqualTo("PARTICIPANT");
    }

    @Test
    void save_edit_emptyList_ok() {
        loginEdit();
        when(swProjectRepository.findById(1L)).thenReturn(Optional.of(new SwProject()));
        when(contractParticipantRepository.findByProject_ProjIdOrderBySortOrder(1L)).thenReturn(List.of());
        ResponseEntity<?> res = controller.saveContractParticipants(1L, List.of());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(contractParticipantRepository, never()).save(any());
    }

    @Test
    void save_serviceThrows_okWithFailBody() {
        loginEdit();
        when(swProjectRepository.findById(1L)).thenReturn(Optional.of(new SwProject()));
        when(contractParticipantRepository.findByProject_ProjIdOrderBySortOrder(1L))
                .thenThrow(new RuntimeException("boom"));
        // 현행 계약: 예외도 HTTP 200 + failMessage(success=false, error=msg)
        ResponseEntity<?> res = controller.saveContractParticipants(1L, List.of());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).isInstanceOf(ApiResult.class);
        ApiResult body = (ApiResult) res.getBody();
        assertThat(body.success()).isFalse();
        assertThat(String.valueOf(body.error())).contains("boom");
    }
}
