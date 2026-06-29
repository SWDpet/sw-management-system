package com.swmanager.system.license.controller;

import com.swmanager.system.constants.MenuName;
import com.swmanager.system.constant.enums.AccessActionType;
import com.swmanager.system.license.domain.LicenseSyncHistory;
import com.swmanager.system.license.sync.LicenseSyncService;
import com.swmanager.system.security.CustomUserDetails;
import com.swmanager.system.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * License4J 매월 연동 수동 트리거 (P7, FR-6). ADMIN 전용.
 */
@Slf4j
@Controller
@RequestMapping("/license/sync")
@RequiredArgsConstructor
public class LicenseSyncController {

    private final LicenseSyncService syncService;
    private final LogService logService;

    private boolean isAdmin(CustomUserDetails u) {
        return u != null && u.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    /** ADMIN 수동 연동 실행 → 업로드 페이지로 복귀 */
    @PostMapping("/run")
    public String runManual(@AuthenticationPrincipal CustomUserDetails userDetails,
                            RedirectAttributes ra) {
        if (!isAdmin(userDetails)) {
            log.warn("연동 수동실행 권한 없음 - 사용자: {}",
                    userDetails != null ? userDetails.getUsername() : "anonymous");
            ra.addFlashAttribute("error", true);
            ra.addFlashAttribute("uploadMessage", "수동 연동은 관리자(ADMIN)만 실행할 수 있습니다.");
            return "redirect:/license/registry/upload";
        }
        try {
            LicenseSyncHistory h = syncService.run("MANUAL", userDetails.getUsername());
            logService.log(MenuName.LICENSE_REGISTRY, AccessActionType.CREATE,
                String.format("License4J 수동 연동 - 상태:%s 총:%d 신규:%d 갱신:%d 중복:%d 실패:%d - 사용자:%s",
                    h.getStatus(), h.getTotalCount(), h.getNewCount(), h.getUpdatedCount(),
                    h.getDuplicateCount(), h.getFailCount(), userDetails.getUsername()));
            ra.addFlashAttribute("success", !"FAIL".equals(h.getStatus()));
            ra.addFlashAttribute("error", "FAIL".equals(h.getStatus()));
            ra.addFlashAttribute("uploadMessage", String.format(
                "🔄 연동 %s — 총 %d / 신규 %d / 갱신 %d / 중복 %d / 실패 %d",
                h.getStatus(), h.getTotalCount(), h.getNewCount(),
                h.getUpdatedCount(), h.getDuplicateCount(), h.getFailCount()));
        } catch (IllegalStateException e) {   // 동시 실행 가드 등
            ra.addFlashAttribute("error", true);
            ra.addFlashAttribute("uploadMessage", e.getMessage());
        } catch (Exception e) {
            log.error("수동 연동 실패", e);
            ra.addFlashAttribute("error", true);
            ra.addFlashAttribute("uploadMessage", "연동 실행 중 오류가 발생했습니다.");
        }
        return "redirect:/license/registry/upload";
    }
}
