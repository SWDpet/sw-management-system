package com.swmanager.system.controller.ops;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmanager.system.domain.User;
import com.swmanager.system.security.CustomUserDetails;
import org.springframework.core.io.ClassPathResource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * InspectAgentController(점검 수집모듈 다운로드) 단위 테스트 (beyond-A 커버리지 스프린트 — 컨트롤러 통합영역 15탄).
 *
 * <p>@RequiredArgsConstructor(ObjectMapper) + 권한은 @AuthenticationPrincipal CustomUserDetails 파라미터
 * (authDocument) + isAdmin(SecurityContextHolder)에서 읽는다. 테스트 classpath 에 agent/release-manifest.json
 * 픽스처를 두어 meta() 파싱 경로까지 커버한다(zip 본체는 미배치 → download 는 404). 실 Postgres 불필요.
 */
class InspectAgentControllerTest {

    private InspectAgentController controller;

    @BeforeEach
    void setUp() {
        controller = new InspectAgentController(new ObjectMapper());
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() { SecurityContextHolder.clearContext(); }

    private static CustomUserDetails u(String authDocument) {
        User user = new User();
        user.setUserSeq(1L);
        user.setUserid("tester");
        user.setUserRole("ROLE_USER");
        user.setAuthDocument(authDocument);
        return new CustomUserDetails(user);
    }

    /** SecurityContext 에 ROLE_ADMIN 세팅(isAdmin()=true). */
    private void adminContext() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", "N/A",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }

    private static Model model() { return new ExtendedModelMap(); }

    // ───────────────────────── 페이지 ─────────────────────────

    @Test
    void page_none_forbidden() {
        // 메시지 문자열 대신 typed status 단언(Spring 버전 비의존)
        assertThatThrownBy(() -> controller.page(u("NONE"), model()))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void page_view_rendersWithMeta() {
        Model m = model();
        assertThat(controller.page(u("VIEW"), m)).isEqualTo("ops-doc/inspect-agent");
        // 테스트 classpath manifest → meta() 파싱 성공
        InspectAgentController.AgentMeta meta = (InspectAgentController.AgentMeta) m.getAttribute("agent");
        assertThat(meta).isNotNull();
        assertThat(meta.getVersion()).isEqualTo("1.0.0-test");
        assertThat(meta.getZipName()).isEqualTo("inspect-agent-1.0.0-test.zip");
    }

    @Test
    void page_admin_rendersEvenWhenAuthNone() {
        adminContext();
        // authDocument=NONE 이어도 isAdmin()→getAuth()="EDIT" 우회
        assertThat(controller.page(u("NONE"), model())).isEqualTo("ops-doc/inspect-agent");
    }

    // ───────────────────────── 다운로드 ─────────────────────────

    @Test
    void download_none_forbidden() {
        ResponseEntity<byte[]> res = controller.download(u("NONE"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void download_view_forbidden() { // [viewer-action-button-guard] 조회자 다운로드 차단(기존 VIEW 허용→EDIT)
        assertThat(controller.download(u("VIEW")).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void download_admin_forbiddenWhenNoZip_butGuardPasses() { // 관리자(getAuth admin→EDIT)는 가드 통과 → zip 없으니 404(403 아님)
        adminContext();
        assertThat(controller.download(u("NONE")).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void download_edit_metaPresentButZipMissing_notFound() {
        // 전제 명시: manifest 는 있으나 zip 본체는 classpath 미배치. zip 이 추가되면 이 단언이 전제 위반을 알림.
        assertThat(new ClassPathResource("agent/release-manifest.json").exists()).isTrue();
        assertThat(new ClassPathResource("agent/inspect-agent-1.0.0-test.zip").exists()).isFalse();
        ResponseEntity<byte[]> res = controller.download(u("EDIT"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
