package com.swmanager.system.controller;

import com.swmanager.system.domain.Infra;
import com.swmanager.system.domain.PersonInfo;
import com.swmanager.system.domain.SigunguCode;
import com.swmanager.system.domain.SwProject;
import com.swmanager.system.domain.SysMst;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.ProcessMaster;
import com.swmanager.system.domain.workplan.ServicePurpose;
import com.swmanager.system.dto.workplan.InfraFindResult;
import com.swmanager.system.dto.workplan.InfraNotFound;
import com.swmanager.system.dto.workplan.ProcessMasterRow;
import com.swmanager.system.dto.workplan.ProjectFilterRow;
import com.swmanager.system.dto.workplan.ServicePurposeRow;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
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
    private SigunguCodeRepository sigunguCodeRepository;
    private SysMstRepository sysMstRepository;
    private InfraRepository infraRepository;
    private ProcessMasterRepository processMasterRepository;
    private ServicePurposeRepository servicePurposeRepository;

    @BeforeEach
    void setUp() {
        swProjectRepository = mock(SwProjectRepository.class);
        userRepository = mock(UserRepository.class);
        personInfoRepository = mock(PersonInfoRepository.class);
        sigunguCodeRepository = mock(SigunguCodeRepository.class);
        sysMstRepository = mock(SysMstRepository.class);
        infraRepository = mock(InfraRepository.class);
        processMasterRepository = mock(ProcessMasterRepository.class);
        servicePurposeRepository = mock(ServicePurposeRepository.class);
        // @RequiredArgsConstructor 인자 순서: swProject·sigungu·sysMst·infra·processMaster·servicePurpose·user·personInfo·access
        controller = new DocumentLookupController(
                swProjectRepository,
                sigunguCodeRepository,
                sysMstRepository,
                infraRepository,
                processMasterRepository,
                servicePurposeRepository,
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

    @Test
    void getUserInfoSecure_edit_notFound() {
        loginEdit();
        when(userRepository.findById(9L)).thenReturn(Optional.empty());
        assertThat(controller.getUserInfoSecure(9L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ───────────────── 사업 검색 cascade (years/cities/districts/systems) ─────────────────

    @Test
    void projectCascade_delegatesToRepository() {
        when(swProjectRepository.findDistinctYears()).thenReturn(List.of(2026, 2025));
        when(swProjectRepository.findDistinctCityNmByYear(2026)).thenReturn(List.of("강원도"));
        when(swProjectRepository.findDistinctDistNmByYearAndCityNm(2026, "강원도")).thenReturn(List.of("춘천시"));
        when(swProjectRepository.findDistinctSysNmEnByYearAndCity(2026, "강원도", "춘천시")).thenReturn(List.of("UPIS"));

        assertThat(controller.getProjectYears().getBody()).containsExactly(2026, 2025);
        assertThat(controller.getProjectCities(2026).getBody()).containsExactly("강원도");
        assertThat(controller.getProjectDistricts(2026, "강원도").getBody()).containsExactly("춘천시");
        assertThat(controller.getProjectSystems(2026, "강원도", "춘천시").getBody()).containsExactly("UPIS");
    }

    @Test
    void getProjectsFiltered_mapsRows() {
        SwProject p = new SwProject();
        p.setProjId(5L); p.setYear(2026); p.setProjNm("사업X");
        p.setSysNm("유피스"); p.setSysNmEn("UPIS"); p.setCityNm("강원도"); p.setDistNm("춘천시");
        when(swProjectRepository.findByYearAndCityNmAndDistNmAndSysNmEnOrderByProjIdDesc(2026, "강원도", "춘천시", "UPIS"))
                .thenReturn(List.of(p));
        List<ProjectFilterRow> rows = controller.getProjectsFiltered(2026, "강원도", "춘천시", "UPIS").getBody();
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).projId()).isEqualTo(5L);
        assertThat(rows.get(0).projNm()).isEqualTo("사업X");
        assertThat(rows.get(0).sysNmEn()).isEqualTo("UPIS");
    }

    // ───────────────── 지역/시스템 마스터 (sigungu/sysMst) ─────────────────

    @Test
    void regionAndSystems_delegateAndMapRows() {
        SigunguCode sg = new SigunguCode();
        sg.setAdmSectC("51110"); sg.setSggNm("춘천시"); sg.setSidoNm("강원특별자치도");
        SysMst sm = new SysMst();
        sm.setCd("UPIS"); sm.setNm("유피스");
        when(sigunguCodeRepository.findDistinctSidoNm()).thenReturn(List.of("강원특별자치도"));
        when(sigunguCodeRepository.findBySidoNmOrderBySggNm("강원특별자치도")).thenReturn(List.of(sg));
        when(sysMstRepository.findAll()).thenReturn(List.of(sm));

        assertThat(controller.getRegionSidos().getBody()).containsExactly("강원특별자치도");
        var sigungus = controller.getRegionSigungus("강원특별자치도").getBody();
        assertThat(sigungus).hasSize(1);
        assertThat(sigungus.get(0).sggNm()).isEqualTo("춘천시");
        assertThat(sigungus.get(0).admSectC()).isEqualTo("51110");
        var systems = controller.getSystemsAll().getBody();
        assertThat(systems).hasSize(1);
        assertThat(systems.get(0).cd()).isEqualTo("UPIS");
        assertThat(systems.get(0).nm()).isEqualTo("유피스");
    }

    // ───────────────── 인프라 cascade + find ─────────────────

    @Test
    void infraCascade_delegate() {
        when(infraRepository.findDistinctCities()).thenReturn(List.of("부산"));
        when(infraRepository.findDistinctDistrictsByCity("부산")).thenReturn(List.of("해운대"));
        when(infraRepository.findDistinctSystemsByRegion("부산", "해운대")).thenReturn(List.of("KRAS"));

        assertThat(controller.getInfraCities().getBody()).containsExactly("부산");
        assertThat(controller.getInfraDistricts("부산").getBody()).containsExactly("해운대");
        assertThat(controller.getInfraSystems("부산", "해운대").getBody()).containsExactly("KRAS");
    }

    @Test
    void findInfraByRegion_notFound_returnsInfraNotFound() {
        when(infraRepository.findByCityDistSystem("부산", "해운대", "KRAS")).thenReturn(List.of());
        Object body = controller.findInfraByRegion("부산", "해운대", "KRAS").getBody();
        assertThat(body).isInstanceOf(InfraNotFound.class);
        assertThat(((InfraNotFound) body).found()).isFalse();
    }

    @Test
    void findInfraByRegion_found_returnsResult() {
        Infra inf = new Infra();
        inf.setInfraId(7L); inf.setCityNm("부산"); inf.setDistNm("해운대");
        inf.setSysNm("크라스"); inf.setSysNmEn("KRAS");
        when(infraRepository.findByCityDistSystem("부산", "해운대", "KRAS")).thenReturn(List.of(inf));
        Object body = controller.findInfraByRegion("부산", "해운대", "KRAS").getBody();
        assertThat(body).isInstanceOf(InfraFindResult.class);
        InfraFindResult r = (InfraFindResult) body;
        assertThat(r.found()).isTrue();
        assertThat(r.infraId()).isEqualTo(7L);
        assertThat(r.cityNm()).isEqualTo("부산");
        assertThat(r.distNm()).isEqualTo("해운대");
        assertThat(r.sysNmEn()).isEqualTo("KRAS");
    }

    // ───────────────── 공정명 / 용역목적 ─────────────────

    @Test
    void getProcessMasterList_mapsRows() {
        ProcessMaster pm = new ProcessMaster();
        pm.setProcessId(1); pm.setProcessName("설계");
        when(processMasterRepository.findBySysNmEnAndUseYnOrderBySortOrder("UPIS", "Y"))
                .thenReturn(List.of(pm));
        List<ProcessMasterRow> rows = controller.getProcessMasterList("UPIS");
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).processId()).isEqualTo(1);
        assertThat(rows.get(0).processName()).isEqualTo("설계");
    }

    @Test
    void getServicePurposeList_withType_usesTypedQuery() {
        ServicePurpose sp = new ServicePurpose();
        sp.setPurposeId(2); sp.setPurposeType("MAINT"); sp.setPurposeText("유지보수");
        when(servicePurposeRepository.findBySysNmEnAndPurposeTypeAndUseYnOrderBySortOrder(eq("UPIS"), eq("MAINT"), eq("Y")))
                .thenReturn(List.of(sp));
        List<ServicePurposeRow> rows = controller.getServicePurposeList("UPIS", "MAINT");
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).purposeId()).isEqualTo(2);
        assertThat(rows.get(0).purposeType()).isEqualTo("MAINT");
        assertThat(rows.get(0).purposeText()).isEqualTo("유지보수");
    }

    @Test
    void getServicePurposeList_noType_usesDefaultQuery() {
        when(servicePurposeRepository.findBySysNmEnAndUseYnOrderBySortOrder("UPIS", "Y"))
                .thenReturn(List.of());
        assertThat(controller.getServicePurposeList("UPIS", null)).isEmpty(); // purposeType null 분기
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
