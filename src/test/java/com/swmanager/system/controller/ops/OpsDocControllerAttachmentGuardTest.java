package com.swmanager.system.controller.ops;

import com.swmanager.system.config.CustomUserDetails;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.ops.OpsDocumentAttachment;
import com.swmanager.system.service.ops.OpsDocAttachmentService;
import com.swmanager.system.service.ops.OpsDocService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * OpsDocController 첨부 API 권한 가드 회귀 테스트 (S1 보안가드).
 *
 * 대상 엔드포인트:
 *   - POST   /ops-doc/api/attachment/upload/{docId}  → requireDocEditOrAdmin
 *   - DELETE /ops-doc/api/attachment/{attachId}       → requireDocEditOrAdmin
 *   - GET    /ops-doc/api/attachments/{docId}         → requireDocView
 *
 * 이전 상태: 위 3종은 권한 가드 없이 서비스로 직행 → 조회권한만 가진 사용자도
 * 남의 운영문서 첨부를 업로드/삭제 가능. 본 테스트가 가드 누락을 회귀 차단한다.
 *
 * 컨벤션: 풀 MockMvc+Security 대신 실제 컨트롤러 메서드 직접 호출
 * (MyPageControllerGuardTest 패턴). @RequiredArgsConstructor 의존성은 가드 경로에서
 * 사용되지 않으므로 all-null 로 생성 후, 허용 경로용 attachmentService 만 mock 주입.
 */
class OpsDocControllerAttachmentGuardTest {

    private OpsDocController controller;
    private OpsDocAttachmentService attachmentService;
    private OpsDocService opsDocService;

    @BeforeEach
    void setUp() throws Exception {
        @SuppressWarnings("unchecked")
        Constructor<OpsDocController> ctor =
                (Constructor<OpsDocController>) OpsDocController.class.getDeclaredConstructors()[0];
        ctor.setAccessible(true);
        Object[] args = new Object[ctor.getParameterCount()]; // 전부 null
        controller = ctor.newInstance(args);

        attachmentService = mock(OpsDocAttachmentService.class);
        opsDocService = mock(OpsDocService.class);
        inject("attachmentService", attachmentService);
        inject("opsDocService", opsDocService);
    }

    private void inject(String field, Object value) throws Exception {
        Field f = OpsDocController.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(controller, value);
    }

    private static CustomUserDetails userWith(String authDocument, String role) {
        User u = new User();
        u.setUserSeq(1L);
        u.setUserid("tester");
        u.setAuthDocument(authDocument);
        u.setUserRole(role);
        return new CustomUserDetails(u);
    }

    private static MultipartFile pdf() {
        return new MockMultipartFile("file", "a.pdf", "application/pdf", new byte[]{1, 2, 3});
    }

    /** 응답 본문을 JSON 으로 직렬화 — Map/ApiResult 무관하게 형태 단언(net, dto-migration 안전). */
    private static String bodyJson(ResponseEntity<?> res) {
        try {
            return new ObjectMapper().writeValueAsString(res.getBody());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ───────────────────────── upload ─────────────────────────

    @Test
    void upload_viewOnlyUser_forbidden_serviceNotCalled() throws Exception {
        ResponseEntity<?> res =
                controller.uploadAttachment(10L, pdf(), userWith("VIEW", "ROLE_USER"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(bodyJson(res)).contains("\"success\":false").contains("\"code\":\"FORBIDDEN\"");
        verifyNoInteractions(attachmentService);
    }

    @Test
    void upload_nullUser_forbidden() throws Exception {
        ResponseEntity<?> res = controller.uploadAttachment(10L, pdf(), null);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(bodyJson(res)).contains("\"success\":false").contains("\"code\":\"FORBIDDEN\"");
        verifyNoInteractions(attachmentService);
    }

    @Test
    void upload_editUser_ok_serviceCalledOnce() throws Exception {
        OpsDocumentAttachment att = new OpsDocumentAttachment();
        att.setAttachId(99L);
        att.setFileName("a.pdf");
        when(attachmentService.saveAttachment(eq(10L), any())).thenReturn(att);

        ResponseEntity<?> res =
                controller.uploadAttachment(10L, pdf(), userWith("EDIT", "ROLE_USER"));

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(bodyJson(res)).contains("\"success\":true").contains("\"attachId\":99");
        verify(attachmentService).saveAttachment(eq(10L), any());
    }

    @Test
    void upload_admin_ok_evenWhenAuthNone() throws Exception {
        OpsDocumentAttachment att = new OpsDocumentAttachment();
        att.setAttachId(1L);
        att.setFileName("a.pdf");
        when(attachmentService.saveAttachment(anyLong(), any())).thenReturn(att);

        ResponseEntity<?> res =
                controller.uploadAttachment(10L, pdf(), userWith("NONE", "ROLE_ADMIN"));

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(attachmentService).saveAttachment(anyLong(), any());
    }

    // ───────────────────────── delete ─────────────────────────

    @Test
    void delete_viewOnlyUser_forbidden_serviceNotCalled() {
        ResponseEntity<?> res =
                controller.deleteAttachment(5L, userWith("VIEW", "ROLE_USER"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(bodyJson(res)).contains("\"success\":false").contains("\"code\":\"FORBIDDEN\"");
        verifyNoInteractions(attachmentService);
    }

    @Test
    void delete_editUser_ok_serviceCalledOnce() {
        ResponseEntity<?> res =
                controller.deleteAttachment(5L, userWith("EDIT", "ROLE_USER"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(attachmentService).deleteAttachment(5L);
    }

    // ─────────────────────── attachments (view) ───────────────────────

    @Test
    void attachments_noneUser_forbidden_serviceNotCalled() {
        ResponseEntity<?> res = controller.getAttachments(7L, userWith("NONE", "ROLE_USER"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(bodyJson(res)).contains("\"success\":false").contains("\"code\":\"FORBIDDEN\"");
        verifyNoInteractions(attachmentService);
    }

    @Test
    void attachments_viewUser_ok_serviceCalledOnce() {
        when(attachmentService.getAttachments(7L)).thenReturn(List.of());
        ResponseEntity<?> res = controller.getAttachments(7L, userWith("VIEW", "ROLE_USER"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(attachmentService).getAttachments(7L);
    }

    // ─────────────────── 문서 삭제 DELETE /api/{docId} (codex 검증서 발견) ───────────────────

    @Test
    void deleteDoc_viewOnlyUser_forbidden_serviceNotCalled() {
        ResponseEntity<?> res =
                controller.delete(3L, userWith("VIEW", "ROLE_USER"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(opsDocService);
    }

    @Test
    void deleteDoc_nullUser_forbidden() {
        ResponseEntity<?> res = controller.delete(3L, null);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(opsDocService);
    }

    @Test
    void deleteDoc_editUser_ok_serviceCalledOnce() {
        ResponseEntity<?> res =
                controller.delete(3L, userWith("EDIT", "ROLE_USER"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(opsDocService).delete(3L);
    }

    @Test
    void deleteDoc_admin_ok() {
        ResponseEntity<?> res =
                controller.delete(3L, userWith("NONE", "ROLE_ADMIN"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(opsDocService).delete(3L);
    }
}
