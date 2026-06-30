package com.swmanager.system.lsa.controller;

import com.swmanager.system.domain.User;
import com.swmanager.system.exception.GlobalExceptionHandler;
import com.swmanager.system.lsa.dto.LsaDTO;
import com.swmanager.system.lsa.dto.LsaForm;
import com.swmanager.system.lsa.service.LsaService;
import com.swmanager.system.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * [P2] LsaController HTTP 표면 net — standalone MockMvc (mock LsaService 주입).
 * 인증=auth_lsa(VIEW이상 또는 ROLE_ADMIN). [owner-edit-guard] 수정/삭제는 작성자 본인(createdBy=로그인ID) 또는 관리자.
 */
class LsaControllerMvcTest {

    private MockMvc mockMvc;
    private LsaService lsaService;

    @BeforeEach
    void setUp() {
        lsaService = mock(LsaService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new LsaController(lsaService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() { SecurityContextHolder.clearContext(); }

    /** 로그인 — userid 고정 "tester"(=createdBy 비교 소스). */
    private void login(String authLsa, String role) {
        loginAs("tester", authLsa, role);
    }

    private void loginAs(String userid, String authLsa, String role) {
        User u = new User();
        u.setUserSeq(1L); u.setUserid(userid); u.setUsername("표시명");
        u.setUserRole(role); u.setAuthLsa(authLsa);
        CustomUserDetails cud = new CustomUserDetails(u);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(cud, "N/A", cud.getAuthorities()));
    }

    /** createdBy 지정 LsaDTO (소유권 케이스용). */
    private LsaDTO dtoBy(String createdBy) {
        return new LsaDTO(5L, "전라남도", "강진군", "정보화", "GIS",
                "홍길동", "061-1", "h@x.kr", "Basic 3.0", "박욱진", null, createdBy, null, null);
    }

    // ── 미인증/권한 ─────────────────────────────────────────────────────────
    @Test
    void list_notAuthenticated_403() throws Exception {
        mockMvc.perform(get("/lsa/list")).andExpect(status().isForbidden());
        verify(lsaService, never()).list(any());
    }

    @Test
    void list_none_403() throws Exception {
        login("NONE", "ROLE_USER");
        mockMvc.perform(get("/lsa/list")).andExpect(status().isForbidden());
    }

    // ── 목록 모델: canEditBase / isAdmin / currentUserId ──────────────────────
    @Test
    void list_view_rendersWithRowOwnershipModel() throws Exception {
        login("VIEW", "ROLE_USER");
        when(lsaService.list("강진")).thenReturn(java.util.List.of());
        mockMvc.perform(get("/lsa/list").param("keyword", "강진"))
                .andExpect(status().isOk())
                .andExpect(view().name("lsa/lsa-list"))
                .andExpect(model().attributeExists("lsaList"))
                .andExpect(model().attribute("keyword", "강진"))
                .andExpect(model().attribute("canEditBase", false))   // VIEW → 작성 불가
                .andExpect(model().attribute("isAdmin", false))
                .andExpect(model().attribute("currentUserId", "tester"));
        verify(lsaService).list("강진");
    }

    @Test
    void list_edit_canEditBaseTrue() throws Exception {
        login("EDIT", "ROLE_USER");
        when(lsaService.list(any())).thenReturn(java.util.List.of());
        mockMvc.perform(get("/lsa/list"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("canEditBase", true))
                .andExpect(model().attribute("isAdmin", false));
    }

    @Test
    void list_adminBypassesNone_canEditBaseTrue() throws Exception {
        login("NONE", "ROLE_ADMIN");
        when(lsaService.list(any())).thenReturn(java.util.List.of());
        mockMvc.perform(get("/lsa/list"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("canEditBase", true))
                .andExpect(model().attribute("isAdmin", true));
    }

    // ── 작성 폼 ──────────────────────────────────────────────────────────────
    @Test
    void newForm_edit_rendersForm() throws Exception {
        login("EDIT", "ROLE_USER");
        when(lsaService.sidoList()).thenReturn(java.util.List.of("전라남도"));
        when(lsaService.issuerCandidates()).thenReturn(java.util.List.of("박욱진", "테스터"));
        mockMvc.perform(get("/lsa/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("lsa/lsa-form"))
                .andExpect(model().attributeExists("sidoList", "userList"))
                .andExpect(model().attribute("isAdmin", false));
    }

    @Test
    void newForm_viewOnly_403() throws Exception {
        login("VIEW", "ROLE_USER");
        mockMvc.perform(get("/lsa/new")).andExpect(status().isForbidden());
        verify(lsaService, never()).sidoList();
    }

    // ── 상세 canEdit 행단위 소유권 ───────────────────────────────────────────
    @Test
    void detail_viewUser_canEditFalse() throws Exception {
        login("VIEW", "ROLE_USER");
        when(lsaService.getById(5L)).thenReturn(dtoBy("tester"));   // 소유자여도 VIEW 권한이라 편집 불가
        mockMvc.perform(get("/lsa/5"))
                .andExpect(status().isOk())
                .andExpect(view().name("lsa/lsa-detail"))
                .andExpect(model().attribute("canEdit", false));
    }

    @Test
    void detail_editOwner_canEditTrue() throws Exception {
        login("EDIT", "ROLE_USER");
        when(lsaService.getById(5L)).thenReturn(dtoBy("tester"));   // 본인 작성건
        mockMvc.perform(get("/lsa/5"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("canEdit", true));
    }

    @Test
    void detail_editNonOwner_canEditFalse() throws Exception {
        login("EDIT", "ROLE_USER");
        when(lsaService.getById(5L)).thenReturn(dtoBy("other"));    // 타인 작성건
        mockMvc.perform(get("/lsa/5"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("canEdit", false));
    }

    @Test
    void detail_adminNonOwner_canEditTrue() throws Exception {
        login("NONE", "ROLE_ADMIN");
        when(lsaService.getById(5L)).thenReturn(dtoBy("other"));
        mockMvc.perform(get("/lsa/5"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("canEdit", true));
    }

    @Test
    void detail_none_403() throws Exception {
        login("NONE", "ROLE_USER");
        mockMvc.perform(get("/lsa/5")).andExpect(status().isForbidden());
        verify(lsaService, never()).getById(any());
    }

    // ── 수정 폼 진입: 소유권 가드 ────────────────────────────────────────────
    @Test
    void editForm_editOwner_renders() throws Exception {
        login("EDIT", "ROLE_USER");
        when(lsaService.getById(5L)).thenReturn(dtoBy("tester"));
        when(lsaService.sidoList()).thenReturn(java.util.List.of("전라남도"));
        when(lsaService.issuerCandidates()).thenReturn(java.util.List.of("박욱진"));
        mockMvc.perform(get("/lsa/5/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("lsa/lsa-form"))
                .andExpect(model().attributeExists("lsa", "sidoList", "userList"));
    }

    @Test
    void editForm_editNonOwner_403() throws Exception {
        login("EDIT", "ROLE_USER");
        when(lsaService.getById(5L)).thenReturn(dtoBy("other"));
        mockMvc.perform(get("/lsa/5/edit")).andExpect(status().isForbidden());
    }

    @Test
    void editForm_adminNonOwner_renders() throws Exception {
        login("NONE", "ROLE_ADMIN");
        when(lsaService.getById(5L)).thenReturn(dtoBy("other"));
        when(lsaService.sidoList()).thenReturn(java.util.List.of("전라남도"));
        when(lsaService.issuerCandidates()).thenReturn(java.util.List.of("박욱진"));
        mockMvc.perform(get("/lsa/5/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("lsa/lsa-form"));
    }

    @Test
    void editForm_viewOnly_403() throws Exception {
        login("VIEW", "ROLE_USER");
        mockMvc.perform(get("/lsa/5/edit")).andExpect(status().isForbidden());
        verify(lsaService, never()).getById(any());   // checkEditAuth 가 먼저 차단
    }

    // ── 저장: 신규=소유권 무제한, 수정=소유권 가드 ───────────────────────────
    @Test
    void save_create_nonAdmin_forcesIssuerToLoginName() throws Exception {
        login("EDIT", "ROLE_USER");
        when(lsaService.create(any(), any(), any())).thenReturn(1L);
        mockMvc.perform(post("/lsa/save")
                        .param("cityNm", "전라남도").param("distNm", "강진군")
                        .param("userNm", "홍길동").param("issuer", "위조된관리자"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/lsa/list"));
        // 비관리자 신규 → 폼 issuer 무시, 로그인 표시명 강제, createdBy=userid
        verify(lsaService).create(any(LsaForm.class), eq("표시명"), eq("tester"));
    }

    @Test
    void save_updateOwner_callsUpdate() throws Exception {
        login("EDIT", "ROLE_USER");
        when(lsaService.getById(5L)).thenReturn(dtoBy("tester"));   // 소유권 통과
        when(lsaService.update(eq(5L), any(), any(), any())).thenReturn(5L);
        mockMvc.perform(post("/lsa/save")
                        .param("id", "5").param("cityNm", "전라남도").param("distNm", "강진군")
                        .param("userNm", "홍길동").param("version", "Pro 3.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/lsa/list"));
        verify(lsaService).update(eq(5L), any(), isNull(), eq("tester"));
        verify(lsaService, never()).create(any(), any(), any());
    }

    @Test
    void save_updateNonOwner_403() throws Exception {
        login("EDIT", "ROLE_USER");
        when(lsaService.getById(5L)).thenReturn(dtoBy("other"));
        mockMvc.perform(post("/lsa/save")
                        .param("id", "5").param("cityNm", "전라남도").param("distNm", "강진군")
                        .param("userNm", "홍길동").param("version", "Pro 3.0"))
                .andExpect(status().isForbidden());
        verify(lsaService, never()).update(any(), any(), any(), any());
    }

    @Test
    void save_updateAdminNonOwner_callsUpdate() throws Exception {
        login("NONE", "ROLE_ADMIN");
        when(lsaService.getById(5L)).thenReturn(dtoBy("other"));
        when(lsaService.update(eq(5L), any(), any(), any())).thenReturn(5L);
        mockMvc.perform(post("/lsa/save")
                        .param("id", "5").param("cityNm", "전라남도").param("distNm", "강진군")
                        .param("userNm", "홍길동").param("version", "Pro 3.0").param("issuer", "박욱진"))
                .andExpect(status().is3xxRedirection());
        verify(lsaService).update(eq(5L), any(), eq("박욱진"), eq("tester"));
    }

    @Test
    void save_viewOnly_403() throws Exception {
        login("VIEW", "ROLE_USER");
        mockMvc.perform(post("/lsa/save").param("userNm", "x")).andExpect(status().isForbidden());
        verify(lsaService, never()).create(any(), any(), any());
    }

    // ── 삭제: 소유권 가드 ────────────────────────────────────────────────────
    @Test
    void delete_owner_deletes() throws Exception {
        login("EDIT", "ROLE_USER");
        when(lsaService.getById(5L)).thenReturn(dtoBy("tester"));
        mockMvc.perform(post("/lsa/5/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/lsa/list"));
        verify(lsaService).delete(5L);
    }

    @Test
    void delete_nonOwner_403() throws Exception {
        login("EDIT", "ROLE_USER");
        when(lsaService.getById(5L)).thenReturn(dtoBy("other"));
        mockMvc.perform(post("/lsa/5/delete")).andExpect(status().isForbidden());
        verify(lsaService, never()).delete(any());
    }

    @Test
    void delete_adminNonOwner_deletes() throws Exception {
        login("NONE", "ROLE_ADMIN");
        when(lsaService.getById(5L)).thenReturn(dtoBy("other"));
        mockMvc.perform(post("/lsa/5/delete")).andExpect(status().is3xxRedirection());
        verify(lsaService).delete(5L);
    }

    @Test
    void delete_viewOnly_403() throws Exception {
        login("VIEW", "ROLE_USER");
        mockMvc.perform(post("/lsa/5/delete")).andExpect(status().isForbidden());
        verify(lsaService, never()).delete(any());
    }

    // ── 캐스케이드 API (조회 권한) ───────────────────────────────────────────
    @Test
    void districts_api_returnsJson() throws Exception {
        login("VIEW", "ROLE_USER");
        when(lsaService.districtList("전라남도")).thenReturn(java.util.List.of("강진군"));
        mockMvc.perform(get("/lsa/api/districts").param("sido", "전라남도"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$[0]").value("강진군"));
    }

    @Test
    void persons_api_returnsJson() throws Exception {
        login("VIEW", "ROLE_USER");
        when(lsaService.findPersonCandidates(any(), any(), any(), any())).thenReturn(java.util.List.of());
        mockMvc.perform(get("/lsa/api/persons").param("city", "전라남도").param("dist", "강진군"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }
}
