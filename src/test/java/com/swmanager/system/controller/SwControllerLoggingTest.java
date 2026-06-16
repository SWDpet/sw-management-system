package com.swmanager.system.controller;

import com.swmanager.system.config.CustomUserDetails;
import com.swmanager.system.constant.enums.AccessActionType;
import com.swmanager.system.constants.MenuName;
import com.swmanager.system.domain.SwProject;
import com.swmanager.system.domain.User;
import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.service.LogService;
import com.swmanager.system.service.SwService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 사업관리 CRUD 로깅 검증 — 폼/API 5개 경로가 등록/수정/삭제 로그를 1회씩 적재.
 */
class SwControllerLoggingTest {

    private SwService swService;
    private LogService logService;
    private SwController controller;

    @BeforeEach
    void setup() {
        swService = mock(SwService.class);
        logService = mock(LogService.class);
        controller = new SwController();
        ReflectionTestUtils.setField(controller, "swService", swService);
        ReflectionTestUtils.setField(controller, "logService", logService);
        ReflectionTestUtils.setField(controller, "swProjectRepository", mock(SwProjectRepository.class));

        // EDIT 권한 인증 사용자 컨텍스트 설정
        User user = mock(User.class);
        when(user.getAuthProject()).thenReturn("EDIT");
        when(user.getUserRole()).thenReturn("ROLE_USER");
        when(user.getUsername()).thenReturn("홍길동");
        CustomUserDetails cud = mock(CustomUserDetails.class);
        when(cud.getUser()).thenReturn(user);
        when(cud.getUsername()).thenReturn("tester");
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(cud);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void formSave_new_logsCreate() {
        SwProject p = new SwProject();              // projId == null → 신규
        when(swService.save(any())).thenReturn(p);
        controller.saveProject(p);
        verify(logService).log(eq(MenuName.PROJECT), eq(AccessActionType.CREATE), anyString());
        verify(logService, never()).log(eq(MenuName.PROJECT), eq(AccessActionType.UPDATE), anyString());
    }

    @Test
    void formSave_existing_logsUpdate() {
        SwProject p = new SwProject();
        p.setProjId(5L);                            // 기존 → 수정
        when(swService.save(any())).thenReturn(p);
        controller.saveProject(p);
        verify(logService).log(eq(MenuName.PROJECT), eq(AccessActionType.UPDATE), anyString());
    }

    @Test
    void formDelete_logsDelete() {
        SwProject t = new SwProject();
        t.setProjId(7L);
        when(swService.getProject(7L)).thenReturn(t);
        controller.deleteProject(7L);
        verify(logService).log(eq(MenuName.PROJECT), eq(AccessActionType.DELETE), anyString());
    }

    @Test
    void apiCreate_logsCreate() {
        when(swService.save(any())).thenReturn(new SwProject());
        controller.createProjectApi(new SwProject());
        verify(logService).log(eq(MenuName.PROJECT), eq(AccessActionType.CREATE), anyString());
    }

    @Test
    void apiUpdate_logsUpdate() {
        SwProject p = new SwProject();
        p.setProjId(3L);
        when(swService.save(any())).thenReturn(p);
        controller.updateProjectApi(3L, new SwProject());
        verify(logService).log(eq(MenuName.PROJECT), eq(AccessActionType.UPDATE), anyString());
    }

    @Test
    void apiDelete_logsDelete() {
        SwProject t = new SwProject();
        t.setProjId(9L);
        when(swService.getProject(9L)).thenReturn(t);
        controller.deleteProjectApi(9L);
        verify(logService).log(eq(MenuName.PROJECT), eq(AccessActionType.DELETE), anyString());
    }
}
