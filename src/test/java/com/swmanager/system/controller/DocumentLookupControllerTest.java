package com.swmanager.system.controller;

import com.swmanager.system.domain.PersonInfo;
import com.swmanager.system.domain.SwProject;
import com.swmanager.system.domain.User;
import com.swmanager.system.repository.InfraRepository;
import com.swmanager.system.repository.PersonInfoRepository;
import com.swmanager.system.repository.SigunguCodeRepository;
import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.repository.SysMstRepository;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.repository.workplan.ProcessMasterRepository;
import com.swmanager.system.repository.workplan.ServicePurposeRepository;
import com.swmanager.system.security.CustomUserDetails;
import com.swmanager.system.security.DocumentAccessSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * DocumentLookupController 단위 테스트 (S4 Phase 5 — 신규).
 *
 * <p>DocumentController 의 조회 API(getUserInfo/getUserInfoSecure/getProjectInfo)를 편입
 * (refactor-document-controller-split-phase5)하며 무테스트였던 lookup 컨트롤러에 첫 테스트 추가.
 * 권한은 실제 DocumentAccessSupport(admin→EDIT). 생성자는 @RequiredArgsConstructor 필드 선언 순서.
 */
class DocumentLookupControllerTest {

    private DocumentLookupController controller;
    private SwProjectRepository swProjectRepository;
    private UserRepository userRepository;
    private PersonInfoRepository personInfoRepository;

    @BeforeEach
    void setUp() {
        swProjectRepository = mock(SwProjectRepository.class);
        userRepository = mock(UserRepository.class);
        personInfoRepository = mock(PersonInfoRepository.class);
        // @RequiredArgsConstructor 인자 순서: swProject·sigungu·sysMst·infra·processMaster·servicePurpose·user·personInfo·access
        controller = new DocumentLookupController(
                swProjectRepository,
                mock(SigunguCodeRepository.class),
                mock(SysMstRepository.class),
                mock(InfraRepository.class),
                mock(ProcessMasterRepository.class),
                mock(ServicePurposeRepository.class),
                userRepository,
                personInfoRepository,
                new DocumentAccessSupport());
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
    private void loginNone() { login("NONE", "ROLE_USER"); }
    private void loginAdmin() { login("NONE", "ROLE_ADMIN"); }

    // ───────────────────────── 사용자 정보 ─────────────────────────

    @Test
    void getUserInfo_none_forbidden() { // [harden-read-api] 문서 권한 없는 인증 사용자 차단
        loginNone();
        assertThat(controller.getUserInfo(2L).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(userRepository);
    }

    @Test
    void getUserInfo_found() {
        loginView();
        User u = new User();
        u.setUserSeq(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(u));
        assertThat(controller.getUserInfo(2L).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getUserInfo_admin_ok() { // hasDocRead: admin→getAuth EDIT→통과(차단 아님)
        loginAdmin();
        User u = new User();
        u.setUserSeq(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(u));
        assertThat(controller.getUserInfo(2L).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getUserInfo_notFound() {
        loginView();
        when(userRepository.findById(9L)).thenReturn(Optional.empty());
        assertThat(controller.getUserInfo(9L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getUserInfoSecure_nonEdit_forbidden() {
        loginView();
        ResponseEntity<?> res = controller.getUserInfoSecure(2L);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(userRepository);
    }

    @Test
    void getUserInfoSecure_edit_found() {
        loginEdit();
        User u = new User();
        u.setUserSeq(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(u));
        assertThat(controller.getUserInfoSecure(2L).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ───────────────────────── 사업 정보 ─────────────────────────

    @Test
    void getProjectInfo_none_forbidden() { // [harden-getprojectinfo-pii] 무권한 차단
        loginNone();
        assertThat(controller.getProjectInfo(3L).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(swProjectRepository, personInfoRepository);
    }

    @Test
    void getProjectInfo_view_forbidden() { // [harden-getprojectinfo-pii] 담당자 PII → VIEW 도 차단(EDIT 전용)
        loginView();
        assertThat(controller.getProjectInfo(3L).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(swProjectRepository, personInfoRepository);
    }

    @Test
    void getProjectInfo_admin_ok() { // admin→getAuth EDIT→통과
        loginAdmin();
        SwProject p = new SwProject();
        p.setProjId(3L);
        when(swProjectRepository.findById(3L)).thenReturn(Optional.of(p));
        assertThat(controller.getProjectInfo(3L).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getProjectInfo_notFound() {
        loginEdit();
        when(swProjectRepository.findById(9L)).thenReturn(Optional.empty());
        assertThat(controller.getProjectInfo(9L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getProjectInfo_found() {
        loginEdit();
        SwProject p = new SwProject();
        p.setProjId(3L);
        p.setProjNm("사업A");
        when(swProjectRepository.findById(3L)).thenReturn(Optional.of(p));
        ResponseEntity<Map<String, Object>> res = controller.getProjectInfo(3L);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).containsEntry("projNm", "사업A");
    }

    @Test
    void getProjectInfo_personMapping() { // [codex] personId 존재 시 담당자 필드 매핑(personId 자체는 응답 미포함)
        loginEdit();
        SwProject p = new SwProject();
        p.setProjId(3L);
        p.setProjNm("사업A");
        p.setPersonId(50L);
        when(swProjectRepository.findById(3L)).thenReturn(Optional.of(p));
        PersonInfo pi = new PersonInfo();
        pi.setUserNm("박욱진");
        pi.setTel("010-1111-2222");
        pi.setEmail("ukjin@jungdouit.com");
        pi.setDeptNm("SW지원부");
        pi.setTeamNm("지원팀");
        pi.setPos("부장");
        pi.setOrgNm("정도UIT");
        when(personInfoRepository.findById(50L)).thenReturn(Optional.of(pi));

        Map<String, Object> body = controller.getProjectInfo(3L).getBody();
        assertThat(body)
                .containsEntry("personNm", "박욱진")
                .containsEntry("personTel", "010-1111-2222")
                .containsEntry("personEmail", "ukjin@jungdouit.com")
                .containsEntry("personDept", "SW지원부")
                .containsEntry("personTeam", "지원팀")
                .containsEntry("personPos", "부장")
                .containsEntry("personOrg", "정도UIT");
        assertThat(body).doesNotContainKey("personId"); // codex: 담당자 personId 는 응답 Map 에 put 하지 않음
    }
}
