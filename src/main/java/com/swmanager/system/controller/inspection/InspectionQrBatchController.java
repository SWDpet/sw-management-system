package com.swmanager.system.controller.inspection;

import com.swmanager.system.config.CustomUserDetails;
import com.swmanager.system.dto.inspection.InspectionQrBatchRequest;
import com.swmanager.system.dto.inspection.InspectionQrBatchResponse;
import com.swmanager.system.service.inspection.InspectionQrBatchService;
import com.swmanager.system.service.inspection.SiteNotMappedException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 점검 QR batch 업로드 — PWA decoder.mjs 가 디코드한 payload 를 받아
 * inspect_report DRAFT + inspect_check_result rows + inspect_qr_batch 원본 저장.
 *
 * 기획서: docs/product-specs/inspection-qr-batch.md v1
 * 개발계획서: docs/exec-plans/inspection-qr-batch.md v1 (Step 6)
 *
 * 인증: 세션 쿠키 (SecurityConfig.filterChain 의 /api/** CSRF 면제 정책 자동 적용).
 */
@Slf4j
@RestController
@RequestMapping("/api/inspection")
@RequiredArgsConstructor
public class InspectionQrBatchController {

    private final InspectionQrBatchService service;

    @PostMapping("/qr-batch")
    public ResponseEntity<InspectionQrBatchResponse> upload(
            @Valid @RequestBody InspectionQrBatchRequest req,
            @AuthenticationPrincipal CustomUserDetails me) {

        Long userId = (me != null && me.getUser() != null) ? me.getUser().getUserSeq() : null;
        InspectionQrBatchResponse res = service.upload(req, userId);
        return ResponseEntity.ok(res);
    }

    // ── 예외 매핑 ────────────────────────────────────────────────────────────

    @ExceptionHandler(SiteNotMappedException.class)
    public ResponseEntity<Map<String, Object>> handleSiteNotMapped(SiteNotMappedException e) {
        log.warn("inspection-qr-batch fail reason=site_not_mapped site={}", e.getSiteCode());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "site_not_mapped");
        body.put("site", e.getSiteCode());
        body.put("hint", "사업관리 화면에서 해당 사이트의 site_code 를 설정하세요");
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }
}
