package com.swmanager.system.controller.ops;

import com.swmanager.system.domain.User;
import com.swmanager.system.domain.ops.OpsKb;
import com.swmanager.system.dto.ops.OpsKbForm;
import com.swmanager.system.dto.ops.RejectForm;
import com.swmanager.system.repository.SysMstRepository;
import com.swmanager.system.repository.ops.OpsKbRepository;
import com.swmanager.system.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OpsKbController 단위 테스트 (beyond-A 커버리지 스프린트 — 컨트롤러 통합영역 6탄).
 *
 * <p>OpsKbController 는 {@code @RequiredArgsConstructor}(2 의존성)이고 권한은 혼합:
 * 조회/편집 권한은 {@code @AuthenticationPrincipal CustomUserDetails} 파라미터(authDocument)에서,
 * 관리자 여부는 {@link SecurityContextHolder}(ROLE_ADMIN)에서 읽는다. 따라서 mock 2종 생성자주입 +
 * 사용자 파라미터 직접전달, 관리자 시나리오만 SecurityContext 에 ROLE_ADMIN 을 세팅한다.
 *
 * <p>실 Postgres 불필요 → 기본 CI 에서 JaCoCo floor 반영. 쓰기/승인 경로는 가드(403/redirect)와
 * 서비스 미호출(부수효과 0)을 함께 단언하고, 통과 경로는 mock 으로 상태전이(PENDING/ACTIVE/REJECTED/
 * DELETED)·소유 스코프·승인흐름 분기를 커버한다.
 */
class OpsKbControllerTest {

    private OpsKbController controller;
    private OpsKbRepository opsKbRepository;
    private SysMstRepository sysMstRepository;

    @BeforeEach
    void setUp() {
        opsKbRepository = mock(OpsKbRepository.class);
        sysMstRepository = mock(SysMstRepository.class);
        controller = new OpsKbController(opsKbRepository, sysMstRepository);
        SecurityContextHolder.clearContext();
        when(sysMstRepository.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(List.of());
    }

    @AfterEach
    void tearDown() { SecurityContextHolder.clearContext(); }

    /** authDocument 권한을 가진 일반 사용자 principal(관리자 아님). */
    private static CustomUserDetails u(String authDocument, String username) {
        User user = new User();
        user.setUserSeq(1L); user.setUserid(username); user.setUsername(username);
        user.setUserRole("ROLE_USER"); user.setAuthDocument(authDocument);
        return new CustomUserDetails(user);
    }
    private static CustomUserDetails viewU() { return u("VIEW", "viewer"); }
    private static CustomUserDetails editU() { return u("EDIT", "editor"); }
    private static CustomUserDetails noneU() { return u("NONE", "none"); }

    /** SecurityContext 에 ROLE_ADMIN 을 세팅(isAdmin()=true). principal 도 admin 으로. */
    private CustomUserDetails admin() {
        User user = new User();
        user.setUserSeq(2L); user.setUserid("admin"); user.setUsername("admin");
        user.setUserRole("ROLE_ADMIN"); user.setAuthDocument("NONE");
        CustomUserDetails cud = new CustomUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(cud, "N/A",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        return cud;
    }

    private static Model model() { return new ExtendedModelMap(); }

    private static OpsKbForm validForm() {
        return new OpsKbForm("GIS", "장애", "증상있음", "원인있음", "조치함", null, null, null);
    }

    private static OpsKb kb(String status, String createdBy) {
        OpsKb k = new OpsKb();
        k.setKbId(1L); k.setStatus(status); k.setCreatedBy(createdBy);
        return k;
    }

    // ───────────────────────── 화면 ─────────────────────────

    @Test
    void list_none_redirectsHome() {
        assertThat(controller.list(model(), noneU())).isEqualTo("redirect:/");
    }

    @Test
    void list_view_renders() {
        Model m = model();
        assertThat(controller.list(m, viewU())).isEqualTo("ops-doc/kb-list");
        assertThat(m.getAttribute("canEdit")).isEqualTo(false);
        assertThat(m.getAttribute("pendingCount")).isEqualTo(0L); // VIEW 는 대기 카운트 0
    }

    @Test
    void list_edit_showsOwnPendingCount() {
        when(opsKbRepository.countByStatusAndCreatedBy("PENDING", "editor")).thenReturn(2L);
        Model m = model();
        controller.list(m, editU());
        assertThat(m.getAttribute("pendingCount")).isEqualTo(2L);
    }

    @Test
    void list_admin_showsAllPendingCount() {
        CustomUserDetails a = admin();
        when(opsKbRepository.countByStatus("PENDING")).thenReturn(5L);
        Model m = model();
        controller.list(m, a);
        assertThat(m.getAttribute("isAdmin")).isEqualTo(true);
        assertThat(m.getAttribute("pendingCount")).isEqualTo(5L);
    }

    @Test
    void createForm_nonEdit_redirects() {
        assertThat(controller.createForm(model(), viewU())).isEqualTo("redirect:/ops-kb");
    }

    @Test
    void createForm_edit_ok() {
        assertThat(controller.createForm(model(), editU())).isEqualTo("ops-doc/kb-form");
    }

    @Test
    void editForm_nonEdit_redirects() {
        assertThat(controller.editForm(1L, model(), viewU())).isEqualTo("redirect:/ops-kb");
    }

    @Test
    void editForm_notFound_redirects() {
        when(opsKbRepository.findById(1L)).thenReturn(Optional.empty());
        assertThat(controller.editForm(1L, model(), editU())).isEqualTo("redirect:/ops-kb");
    }

    @Test
    void editForm_active_ok() {
        when(opsKbRepository.findById(1L)).thenReturn(Optional.of(kb("ACTIVE", "x")));
        assertThat(controller.editForm(1L, model(), editU())).isEqualTo("ops-doc/kb-form");
    }

    @Test
    void editForm_pendingNotOwner_redirects() {
        when(opsKbRepository.findById(1L)).thenReturn(Optional.of(kb("PENDING", "other")));
        assertThat(controller.editForm(1L, model(), editU())).isEqualTo("redirect:/ops-kb");
    }

    // ───────────────────────── 조회 API ─────────────────────────

    @Test
    void search_noView_empty() {
        assertThat(controller.search(null, null, null, null, noneU())).isEmpty();
    }

    @Test
    void search_deletedStatus_empty() {
        assertThat(controller.search(null, null, null, "DELETED", viewU())).isEmpty();
    }

    @Test
    void search_activeDefault_view_ok() {
        when(opsKbRepository.search(eq("ACTIVE"), any(), any(), any(), any())).thenReturn(List.of());
        assertThat(controller.search(null, null, null, null, viewU())).isEmpty();
        verify(opsKbRepository).search(eq("ACTIVE"), any(), any(), any(), any());
    }

    @Test
    void search_pendingQueue_viewOnly_empty() {
        // VIEW 전용은 대기/반려 큐 접근 불가 → search 호출 안 함
        assertThat(controller.search(null, null, null, "PENDING", viewU())).isEmpty();
        verify(opsKbRepository, never()).search(any(), any(), any(), any(), any());
    }

    @Test
    void search_pendingQueue_edit_ownerScoped() {
        when(opsKbRepository.search(eq("PENDING"), any(), any(), any(), eq("editor")))
                .thenReturn(List.of(kb("PENDING", "editor")));
        List<?> rows = controller.search(null, null, null, "PENDING", editU());
        assertThat(rows).hasSize(1);
        // 비관리자 편집자는 본인 제출(ownerScope=username)만
        verify(opsKbRepository).search(eq("PENDING"), any(), any(), any(), eq("editor"));
    }

    @Test
    void detail_noView_forbidden() {
        assertThat(controller.detail(1L, noneU()).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void detail_notFound_404() {
        when(opsKbRepository.findById(1L)).thenReturn(Optional.empty());
        assertThat(controller.detail(1L, viewU()).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void detail_active_ok() {
        when(opsKbRepository.findById(1L)).thenReturn(Optional.of(kb("ACTIVE", "x")));
        assertThat(controller.detail(1L, viewU()).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void detail_pending_viewOnly_forbidden() {
        when(opsKbRepository.findById(1L)).thenReturn(Optional.of(kb("PENDING", "other")));
        assertThat(controller.detail(1L, viewU()).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ───────────────────────── CRUD ─────────────────────────

    @Test
    void create_nonEdit_forbidden() {
        assertThat(controller.create(validForm(), viewU()).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(opsKbRepository, never()).save(any());
    }

    @Test
    void create_invalidForm_badRequest() {
        OpsKbForm bad = new OpsKbForm(null, "장애", "증상", "원인", "조치", null, null, null); // sysType 없음
        assertThat(controller.create(bad, editU()).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(opsKbRepository, never()).save(any());
    }

    @Test
    void create_edit_savesPending() {
        when(opsKbRepository.nextManualSeq()).thenReturn(1L);
        when(opsKbRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        ResponseEntity<?> res = controller.create(validForm(), editU());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        ArgumentCaptor<OpsKb> cap = ArgumentCaptor.forClass(OpsKb.class);
        verify(opsKbRepository).save(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("PENDING"); // 편집권한자=승인대기
        assertThat(cap.getValue().getCreatedBy()).isEqualTo("editor");
    }

    @Test
    void create_admin_savesActive() {
        CustomUserDetails a = admin();
        when(opsKbRepository.nextManualSeq()).thenReturn(2L);
        when(opsKbRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        ResponseEntity<?> res = controller.create(validForm(), a);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        ArgumentCaptor<OpsKb> cap = ArgumentCaptor.forClass(OpsKb.class);
        verify(opsKbRepository).save(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("ACTIVE"); // 관리자=즉시 게시
    }

    @Test
    void update_nonEdit_forbidden() {
        assertThat(controller.update(1L, validForm(), viewU()).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(opsKbRepository, never()).save(any());
    }

    @Test
    void update_notFound_404() {
        when(opsKbRepository.findById(1L)).thenReturn(Optional.empty());
        assertThat(controller.update(1L, validForm(), editU()).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(opsKbRepository, never()).save(any());
    }

    @Test
    void update_active_edit_resubmitsPending() {
        when(opsKbRepository.findById(1L)).thenReturn(Optional.of(kb("ACTIVE", "x")));
        when(opsKbRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        ResponseEntity<?> res = controller.update(1L, validForm(), editU());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        ArgumentCaptor<OpsKb> cap = ArgumentCaptor.forClass(OpsKb.class);
        verify(opsKbRepository).save(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("PENDING"); // 편집권한자 수정=재승인
    }

    @Test
    void update_admin_keepsActive() {
        CustomUserDetails a = admin();
        when(opsKbRepository.findById(1L)).thenReturn(Optional.of(kb("ACTIVE", "x")));
        when(opsKbRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        ResponseEntity<?> res = controller.update(1L, validForm(), a);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        ArgumentCaptor<OpsKb> cap = ArgumentCaptor.forClass(OpsKb.class);
        verify(opsKbRepository).save(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("ACTIVE");
    }

    // ───────────────────────── 승인/반려 (ADMIN) ─────────────────────────

    @Test
    void approve_nonAdmin_forbidden() {
        assertThat(controller.approve(1L, editU()).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(opsKbRepository, never()).save(any());
    }

    @Test
    void approve_admin_activates() {
        CustomUserDetails a = admin();
        when(opsKbRepository.findById(1L)).thenReturn(Optional.of(kb("PENDING", "editor")));
        when(opsKbRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        ResponseEntity<?> res = controller.approve(1L, a);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        ArgumentCaptor<OpsKb> cap = ArgumentCaptor.forClass(OpsKb.class);
        verify(opsKbRepository).save(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void reject_nonAdmin_forbidden() {
        assertThat(controller.reject(1L, new RejectForm("사유"), editU()).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
        verify(opsKbRepository, never()).save(any());
    }

    @Test
    void reject_noReason_badRequest() {
        CustomUserDetails a = admin();
        when(opsKbRepository.findById(1L)).thenReturn(Optional.of(kb("PENDING", "editor")));
        assertThat(controller.reject(1L, new RejectForm("  "), a).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        verify(opsKbRepository, never()).save(any());
    }

    @Test
    void reject_admin_setsRejected() {
        CustomUserDetails a = admin();
        when(opsKbRepository.findById(1L)).thenReturn(Optional.of(kb("PENDING", "editor")));
        when(opsKbRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        ResponseEntity<?> res = controller.reject(1L, new RejectForm("내용 부족"), a);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        ArgumentCaptor<OpsKb> cap = ArgumentCaptor.forClass(OpsKb.class);
        verify(opsKbRepository).save(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("REJECTED");
        assertThat(cap.getValue().getRejectReason()).isEqualTo("내용 부족");
    }

    // ───────────────────────── 삭제 ─────────────────────────

    @Test
    void delete_nonEdit_forbidden() {
        assertThat(controller.delete(1L, viewU()).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(opsKbRepository, never()).save(any());
    }

    @Test
    void delete_notFound_idempotentOk() {
        when(opsKbRepository.findById(1L)).thenReturn(Optional.empty());
        assertThat(controller.delete(1L, editU()).getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(opsKbRepository, never()).save(any());
    }

    @Test
    void delete_notOwner_forbidden() {
        when(opsKbRepository.findById(1L)).thenReturn(Optional.of(kb("ACTIVE", "other")));
        assertThat(controller.delete(1L, editU()).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(opsKbRepository, never()).save(any());
    }

    @Test
    void delete_owner_softDeletes() {
        when(opsKbRepository.findById(1L)).thenReturn(Optional.of(kb("ACTIVE", "editor")));
        when(opsKbRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        ResponseEntity<?> res = controller.delete(1L, editU());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        ArgumentCaptor<OpsKb> cap = ArgumentCaptor.forClass(OpsKb.class);
        verify(opsKbRepository).save(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("DELETED");
    }

    @Test
    void delete_admin_anyOwner_softDeletes() {
        CustomUserDetails a = admin();
        when(opsKbRepository.findById(1L)).thenReturn(Optional.of(kb("ACTIVE", "someoneElse")));
        when(opsKbRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        assertThat(controller.delete(1L, a).getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(opsKbRepository).save(any());
    }
}
