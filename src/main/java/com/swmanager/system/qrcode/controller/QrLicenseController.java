package com.swmanager.system.qrcode.controller;

import com.swmanager.system.config.CustomUserDetails;
import com.swmanager.system.qrcode.domain.QrLicense;
import com.swmanager.system.qrcode.service.QrLicenseService;
import com.swmanager.system.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class QrLicenseController {

    private final QrLicenseService qrLicenseService;
    private final LogService logService;

    // ===== Helper =====

    private CustomUserDetails getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return (CustomUserDetails) auth.getPrincipal();
        }
        return null;
    }

    private String getCurrentUserName() {
        CustomUserDetails user = getCurrentUser();
        if (user != null && user.getUser() != null && user.getUser().getUsername() != null) {
            return user.getUser().getUsername();
        }
        return (user != null) ? user.getUsername() : "unknown";
    }

    private boolean hasLicenseAuth() {
        CustomUserDetails user = getCurrentUser();
        if (user == null) return false;
        if ("ROLE_ADMIN".equals(user.getUser().getUserRole())) return true;
        String auth = user.getUser().getAuthLicense();
        return auth != null && !"NONE".equals(auth);
    }

    private boolean hasLicenseEditAuth() {
        CustomUserDetails user = getCurrentUser();
        if (user == null) return false;
        if ("ROLE_ADMIN".equals(user.getUser().getUserRole())) return true;
        return "EDIT".equals(user.getUser().getAuthLicense());
    }

    private void checkViewAuth() {
        if (!hasLicenseAuth()) {
            throw new RuntimeException("QR 라이선스 조회 권한이 없습니다.");
        }
    }

    private void checkEditAuth() {
        if (!hasLicenseEditAuth()) {
            throw new RuntimeException("QR 라이선스 편집 권한이 없습니다.");
        }
    }

    // ===== Pages =====

    /** QR 발급 페이지 */
    @GetMapping("/qr-license/issue")
    public String issuePage(Model model) {
        checkEditAuth();
        model.addAttribute("currentUserName", getCurrentUserName());
        return "qrcode/qr-issue";
    }

    /** QR 발급 대장 */
    @GetMapping("/qr-license/ledger")
    public String ledgerPage(@RequestParam(required = false) String keyword, Model model) {
        checkViewAuth();
        model.addAttribute("list", qrLicenseService.search(keyword));
        model.addAttribute("keyword", keyword);
        return "qrcode/qr-ledger";
    }

    /** QR 상세 보기 */
    @GetMapping("/qr-license/{id}")
    public String detailPage(@PathVariable Long id, Model model) {
        checkViewAuth();
        QrLicense qr = qrLicenseService.getById(id)
                .orElseThrow(() -> new RuntimeException("QR 라이선스를 찾을 수 없습니다."));
        model.addAttribute("qr", qr);
        return "qrcode/qr-detail";
    }

    /** QR 수정 페이지 */
    @GetMapping("/qr-license/{id}/edit")
    public String editPage(@PathVariable Long id, Model model) {
        checkEditAuth();
        QrLicense qr = qrLicenseService.getById(id)
                .orElseThrow(() -> new RuntimeException("QR 라이선스를 찾을 수 없습니다."));
        model.addAttribute("qr", qr);
        model.addAttribute("currentUserName", getCurrentUserName());
        return "qrcode/qr-issue";
    }

    // ===== REST API =====

    /** QR 발급 (생성) */
    @PostMapping("/api/qr-license")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> issueQr(@RequestBody QrLicense qr) {
        checkEditAuth();
        try {
            qr.setIssuedBy(getCurrentUserName());
            QrLicense saved = qrLicenseService.issue(qr);
            logService.log("QR라이선스", "발급", "QR 발급: " + saved.getEndUserName() + " - " + saved.getProducts());
            return ResponseEntity.ok(Map.of("success", true, "qrId", saved.getQrId()));
        } catch (Exception e) {
            log.error("QR 발급 실패", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /** QR 수정 */
    @PutMapping("/api/qr-license/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateQr(@PathVariable Long id, @RequestBody QrLicense qr) {
        checkEditAuth();
        try {
            qr.setQrId(id);
            qr.setIssuedBy(getCurrentUserName());
            QrLicense saved = qrLicenseService.update(qr);
            logService.log("QR라이선스", "수정", "QR 수정: " + saved.getEndUserName());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /** QR 삭제 */
    @DeleteMapping("/api/qr-license/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteQr(@PathVariable Long id) {
        checkEditAuth();
        try {
            qrLicenseService.delete(id);
            logService.log("QR라이선스", "삭제", "QR 삭제: ID " + id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /** QR 이미지만 재생성 (미리보기) */
    @PostMapping("/api/qr-license/preview")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> previewQr(@RequestBody Map<String, String> body) {
        try {
            String content = body.getOrDefault("content", "");
            if (content.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "내용이 없습니다."));
            }
            String base64 = qrLicenseService.generateQrBase64(content, 300, 300);
            return ResponseEntity.ok(Map.of("success", true, "image", base64));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}
