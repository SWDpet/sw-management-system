package com.swmanager.system.license.controller;

import com.swmanager.system.domain.User;
import com.swmanager.system.license.domain.LicenseSyncHistory;
import com.swmanager.system.license.sync.LicenseSyncService;
import com.swmanager.system.security.CustomUserDetails;
import com.swmanager.system.service.LogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * LicenseSyncController ADMIN 가드 단위테스트 (P10, T-6).
 * 비ADMIN은 연동 실행이 차단되고 syncService 가 호출되지 않음을, ADMIN은 호출됨을 검증.
 */
class LicenseSyncControllerGuardTest {

    private final LicenseSyncService syncService = mock(LicenseSyncService.class);
    private final LogService logService = mock(LogService.class);
    private final LicenseSyncController controller = new LicenseSyncController(syncService, logService);

    private CustomUserDetails user(String role) {
        User u = new User();
        u.setUserid("u1");
        u.setUserRole(role);
        return new CustomUserDetails(u);
    }

    @Test
    @DisplayName("비ADMIN → 차단(syncService 미호출)")
    void blocksNonAdmin() {
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String view = controller.runManual(user("ROLE_USER"), ra);

        assertThat(view).isEqualTo("redirect:/license/registry/upload");
        assertThat(ra.getFlashAttributes().get("error")).isEqualTo(true);
        verify(syncService, never()).run(any(), any());
    }

    @Test
    @DisplayName("ADMIN → 연동 실행(syncService 호출)")
    void allowsAdmin() {
        LicenseSyncHistory h = new LicenseSyncHistory();
        h.setStatus("SUCCESS");
        h.setTotalCount(10); h.setNewCount(3); h.setUpdatedCount(2);
        h.setDuplicateCount(5); h.setFailCount(0);
        when(syncService.run(eq("MANUAL"), eq("u1"))).thenReturn(h);
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String view = controller.runManual(user("ROLE_ADMIN"), ra);

        assertThat(view).isEqualTo("redirect:/license/registry/upload");
        verify(syncService, times(1)).run("MANUAL", "u1");
    }
}
